package com.mycompany.airlinebookingsystem;

import com.toedter.calendar.JDateChooser;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import java.util.Date;

public class EditProfile extends JFrame {
    private JTextField firstNameField, middleNameField, lastNameField, phoneField, emailField;
    private JPasswordField newPasswordField, confirmPasswordField;
    private JCheckBox showPasswordCheckBox;
    private JRadioButton maleButton, femaleButton;
    private ButtonGroup genderGroup;
    private JDateChooser birthDateChooser;
    private JButton saveButton, backButton;
    private JLabel errorLabel;

    public EditProfile() {
        setTitle("ByteAir - Edit Profile");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);

        try {
            setContentPane(new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/images/Map.jpg")))));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load background image.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        JLabel titleLabel = new JLabel("Edit Your Skyverra Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(530, 30, 600, 50);
        add(titleLabel);

        createFormUI();
        loadProfile();
        setVisible(true);
    }

    private void createFormUI() {
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);

        addLabel("First Name:", 500, 100, labelFont);
        firstNameField = createTextField(500, 130, "Enter your first name.");
        add(firstNameField);

        addLabel("Middle Name:", 750, 100, labelFont);
        middleNameField = createTextField(750, 130, "Enter your middle name.");
        add(middleNameField);

        addLabel("Last Name:", 500, 170, labelFont);
        lastNameField = createTextField(500, 200, "Enter your last name.");
        add(lastNameField);

        addLabel("Email:", 750, 170, labelFont);
        emailField = createTextField(750, 200, "Enter your email.");
        add(emailField);

        addLabel("Phone Number:", 500, 240, labelFont);
        phoneField = createTextField(500, 270, "10-digit phone number.");
        add(phoneField);

        addLabel("Gender:", 750, 240, labelFont);
        maleButton = new JRadioButton("Male");
        femaleButton = new JRadioButton("Female");
        maleButton.setBounds(750, 270, 70, 20);
        femaleButton.setBounds(830, 270, 80, 20);
        maleButton.setBackground(Color.WHITE);
        femaleButton.setBackground(Color.WHITE);
        maleButton.setForeground(Color.decode("#ADD8E6"));
        femaleButton.setForeground(Color.decode("#ADD8E6"));
        genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);
        add(maleButton);
        add(femaleButton);

        addLabel("Birth Date:", 500, 310, labelFont);
        birthDateChooser = new JDateChooser();
        birthDateChooser.setBounds(500, 340, 200, 30);
        birthDateChooser.setDateFormatString("yyyy-MM-dd");
        birthDateChooser.setToolTipText("Select your birth date.");
        add(birthDateChooser);

        addLabel("New Password:", 750, 310, labelFont);
        newPasswordField = new JPasswordField();
        newPasswordField.setBounds(750, 340, 200, 30);
        newPasswordField.setToolTipText("Leave blank if unchanged.");
        add(newPasswordField);

        addLabel("Confirm Password:", 750, 380, labelFont);
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(750, 410, 200, 30);
        confirmPasswordField.setToolTipText("Re-enter password.");
        add(confirmPasswordField);

        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setBounds(750, 445, 200, 20);
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.setForeground(Color.WHITE);
        showPasswordCheckBox.addActionListener(e -> {
            char echo = showPasswordCheckBox.isSelected() ? (char) 0 : '\u2022';
            newPasswordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });
        add(showPasswordCheckBox);

        errorLabel = new JLabel("");
        errorLabel.setBounds(500, 480, 600, 20);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(errorLabel);

        saveButton = new JButton("Save Changes");
        saveButton.setBounds(500, 510, 140, 40);
        styleButton(saveButton, "#ADD8E6", Color.WHITE);
        saveButton.addActionListener(e -> saveProfile());
        add(saveButton);

        backButton = new JButton("Back");
        backButton.setBounds(660, 510, 140, 40);
        styleButton(backButton, Color.WHITE, Color.decode("#ADD8E6"));
        backButton.addActionListener(e -> {
            dispose();
            new MainMenu();
        });
        add(backButton);
    }

    private void loadProfile() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM Customer WHERE customer_id = ?");
            pst.setString(1, Session.currentUserId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                firstNameField.setText(rs.getString("first_name"));
                middleNameField.setText(rs.getString("middle_name"));
                lastNameField.setText(rs.getString("last_name"));
                phoneField.setText(rs.getString("phone_number"));
                emailField.setText(rs.getString("email"));
                birthDateChooser.setDate(rs.getDate("birth_date"));
                String gender = rs.getString("gender");
                if ("Male".equalsIgnoreCase(gender)) maleButton.setSelected(true);
                else if ("Female".equalsIgnoreCase(gender)) femaleButton.setSelected(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load user data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveProfile() {
        String email = emailField.getText().trim();
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() || phoneField.getText().isEmpty() || email.isEmpty()) {
            errorLabel.setText("Required fields cannot be empty.");
            return;
        }
        if (!phoneField.getText().matches("\\d{10}")) {
            errorLabel.setText("Phone must be 10 digits.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Invalid email format.");
            return;
        }

        Date birthDate = birthDateChooser.getDate();
        Date today = new Date();
        if (birthDate != null && birthDate.after(today)) {
            errorLabel.setText("Birth date cannot be in the future.");
            return;
        }
        if (birthDate != null) {
            long age = (today.getTime() - birthDate.getTime()) / (1000L * 60 * 60 * 24 * 365);
            if (age < 18) {
                errorLabel.setText("You must be at least 18 years old.");
                return;
            }
        }

        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        boolean changePassword = !newPassword.isEmpty();

        if (changePassword) {
            if (newPassword.length() < 6 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[!@#$%^&*()].*")) {
                errorLabel.setText("Weak password: min 6 chars, 1 uppercase, 1 symbol.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                errorLabel.setText("Passwords do not match.");
                return;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE Customer SET first_name=?, middle_name=?, last_name=?, phone_number=?, gender=?, birth_date=?, email=?"
                         + (changePassword ? ", password=?" : "")
                         + " WHERE customer_id=?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, firstNameField.getText());
            pst.setString(2, middleNameField.getText());
            pst.setString(3, lastNameField.getText());
            pst.setString(4, phoneField.getText());
            String gender = maleButton.isSelected() ? "Male" : (femaleButton.isSelected() ? "Female" : null);
            pst.setString(5, gender);
            if (birthDate != null) {
                pst.setDate(6, new java.sql.Date(birthDate.getTime()));
            } else {
                pst.setNull(6, Types.DATE);
            }
            pst.setString(7, email);
            if (changePassword) {
                pst.setString(8, newPassword);
                pst.setString(9, Session.currentUserId);
            } else {
                pst.setString(8, Session.currentUserId);
            }

            int rows = pst.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                dispose();
                new MainMenu();
            } else {
                errorLabel.setText("Update failed. Try again.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            errorLabel.setText("Database error: " + ex.getMessage());
        }
    }

    private JTextField createTextField(int x, int y, String tooltip) {
        JTextField field = new JTextField();
        field.setBounds(x, y, 200, 30);
        field.setToolTipText(tooltip);
        return field;
    }

    private void addLabel(String text, int x, int y, Font font) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, 200, 20);
        label.setForeground(Color.WHITE);
        label.setFont(font);
        add(label);
    }

    private void styleButton(JButton btn, Object bg, Color fg) {
        if (bg instanceof String) {
            btn.setBackground(Color.decode((String) bg));
        } else if (bg instanceof Color) {
            btn.setBackground((Color) bg);
        }
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(fg));
    }

    public static void main(String[] args) {
        new EditProfile();
    }
}

