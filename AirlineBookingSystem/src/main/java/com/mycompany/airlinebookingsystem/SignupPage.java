package com.mycompany.airlinebookingsystem;

import com.toedter.calendar.JDateChooser;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.*;
import java.util.Date;

public class SignupPage extends JFrame {
    private JTextField firstNameField, middleNameField, lastNameField, emailField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JRadioButton maleButton, femaleButton;
    private ButtonGroup genderGroup;
    private JDateChooser birthDateChooser;
    private JButton signupButton, backButton;
    private JLabel errorLabel;

    public SignupPage() {
        setTitle("ByteAir - Sign Up");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Set background image
        try {
            JLabel bg = new JLabel(new ImageIcon(ImageIO.read(
                    getClass().getResource("/images/Map.jpg"))));
            setContentPane(bg);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load background image.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        getContentPane().setLayout(null);

        JLabel titleLabel = new JLabel("Create Your ByteAir Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(530, 30, 600, 50);
        add(titleLabel);

        // ---------- First Name ----------
        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameLabel.setBounds(500, 100, 120, 20);
        styleLabel(firstNameLabel);
        add(firstNameLabel);

        firstNameField = new JTextField();
        firstNameField.setToolTipText("Enter your first name.");
        firstNameField.setBounds(500, 130, 200, 30);
        add(firstNameField);

        //  Middle Name 
        JLabel middleNameLabel = new JLabel("Middle Name:");
        middleNameLabel.setBounds(750, 100, 120, 20);
        styleLabel(middleNameLabel);
        add(middleNameLabel);

        middleNameField = new JTextField();
        middleNameField.setToolTipText("Enter your middle name.");
        middleNameField.setBounds(750, 130, 200, 30);
        add(middleNameField);

        //  Last Name 
        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setBounds(500, 170, 120, 20);
        styleLabel(lastNameLabel);
        add(lastNameLabel);

        lastNameField = new JTextField();
        lastNameField.setToolTipText("Enter your last name.");
        lastNameField.setBounds(500, 200, 200, 30);
        add(lastNameField);

        //  Email 
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(750, 170, 120, 20);
        styleLabel(emailLabel);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setToolTipText("Enter a valid email (e.g. name@gmail.com).");
        emailField.setBounds(750, 200, 200, 30);
        add(emailField);

        //  Phone 
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(500, 240, 120, 20);
        styleLabel(phoneLabel);
        add(phoneLabel);

        phoneField = new JTextField();
        phoneField.setToolTipText("Enter 10-digit phone number.");
        phoneField.setBounds(500, 270, 200, 30);
        add(phoneField);

        //  Gender 
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setBounds(750, 240, 120, 20);
        styleLabel(genderLabel);
        add(genderLabel);

        maleButton = new JRadioButton("Male");
        femaleButton = new JRadioButton("Female");
        maleButton.setBounds(750, 270, 70, 20);
        femaleButton.setBounds(830, 270, 80, 20);

        maleButton.setOpaque(false);
        femaleButton.setOpaque(false);
        maleButton.setForeground(Color.WHITE);
        femaleButton.setForeground(Color.WHITE);

        genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);
        add(maleButton);
        add(femaleButton);

        //  Birth Date 
        JLabel birthDateLabel = new JLabel("Birth Date:");
        birthDateLabel.setBounds(500, 310, 120, 20);
        styleLabel(birthDateLabel);
        add(birthDateLabel);

        birthDateChooser = new JDateChooser();
        birthDateChooser.setToolTipText("Select your birth date.");
        birthDateChooser.setBounds(500, 340, 200, 30);
        birthDateChooser.setDateFormatString("yyyy-MM-dd");
        add(birthDateChooser);

        //  Password 
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(750, 310, 120, 20);
        styleLabel(passwordLabel);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setToolTipText("Min 6 chars, 1 uppercase, 1 symbol.");
        passwordField.setBounds(750, 340, 200, 30);
        add(passwordField);

        //  Confirm Password 
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setBounds(500, 380, 200, 20);
        styleLabel(confirmPasswordLabel);
        add(confirmPasswordLabel);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setToolTipText("Re-enter your password.");
        confirmPasswordField.setBounds(500, 410, 450, 30);
        add(confirmPasswordField);

        //  Show Password 
        JCheckBox showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setBounds(500, 450, 200, 20);
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.setForeground(Color.WHITE);
        showPasswordCheckBox.setFocusPainted(false);
        showPasswordCheckBox.addActionListener(e -> {
            char echo = showPasswordCheckBox.isSelected() ? 0 : '\u2022';
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });
        add(showPasswordCheckBox);

        //  Error Label 
        errorLabel = new JLabel("");
        errorLabel.setBounds(500, 480, 600, 20);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(errorLabel);

        //  Buttons 
        signupButton = new JButton("Sign Up");
        signupButton.setBounds(500, 510, 140, 40);
        signupButton.setBackground(Color.decode("#ADD8E6"));
        signupButton.setForeground(Color.WHITE);
        signupButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        signupButton.setFocusPainted(false);
        signupButton.addActionListener(e -> signupAction());
        add(signupButton);

        backButton = new JButton("Back");
        backButton.setBounds(660, 510, 140, 40);
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(Color.decode("#ADD8E6"));
        backButton.setBorder(BorderFactory.createLineBorder(Color.decode("#ADD8E6")));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            dispose();
            new LoginSignupPage();
        });
        add(backButton);

        setVisible(true);
    }

    private void signupAction() {
        String first = firstNameField.getText().trim();
        String middle = middleNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String gender = maleButton.isSelected() ? "Male" : (femaleButton.isSelected() ? "Female" : "");
        Date birthDate = birthDateChooser.getDate();

        //  Validation 
        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() ||
                gender.isEmpty() || birthDate == null) {
            errorLabel.setText("Please fill in all fields and select gender.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            errorLabel.setText("Invalid email format. Example: name@example.com");
            return;
        }

        if (!phone.matches("\\d{10}")) {
            errorLabel.setText("Phone number must be 10 digits.");
            return;
        }

        if (password.length() < 6 || !password.matches(".*[A-Z].*") ||
                !password.matches(".*[!@#$%^&*()].*")) {
            errorLabel.setText("Weak password: min 6 chars, 1 uppercase, 1 symbol.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        Date today = new Date();
        if (birthDate.after(today)) {
            errorLabel.setText("Birth date cannot be in the future.");
            return;
        }

        long ageInMillis = today.getTime() - birthDate.getTime();
        long ageInYears = ageInMillis / (1000L * 60 * 60 * 24 * 365);
        if (ageInYears < 18) {
            errorLabel.setText("You must be at least 18 years old.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            String checkSql = "SELECT 1 FROM Login WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        errorLabel.setText("Email already exists.");
                        return;
                    }
                }
            }

            int count = 0;
            String countSql = "SELECT COUNT(*) FROM Customer";
            try (PreparedStatement idStmt = conn.prepareStatement(countSql);
                 ResultSet rsId = idStmt.executeQuery()) {
                if (rsId.next()) {
                    count = rsId.getInt(1) + 1; // next number
                }
            }
            String customerId = "C-" + count + "A";

            String customerSql = "INSERT INTO Customer " +
                    "(customer_id, first_name, middle_name, last_name, phone_number, gender, birth_date, email) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pst = conn.prepareStatement(customerSql)) {
                pst.setString(1, customerId);
                pst.setString(2, first);
                pst.setString(3, middle);
                pst.setString(4, last);
                pst.setString(5, phone);
                pst.setString(6, gender);
                pst.setDate(7, new java.sql.Date(birthDate.getTime()));
                pst.setString(8, email);

                int rowsCustomer = pst.executeUpdate();
                if (rowsCustomer <= 0) {
                    errorLabel.setText("Failed to create account. Try again.");
                    return;
                }
            }

            String loginSql = "INSERT INTO Login (email, password, role, customer_id, admin_id) " +
                    "VALUES (?, ?, 'Customer', ?, NULL)";
            try (PreparedStatement pstLogin = conn.prepareStatement(loginSql)) {
                pstLogin.setString(1, email);
                pstLogin.setString(2, password); // (you can hash this later)
                pstLogin.setString(3, customerId);

                int rowsLogin = pstLogin.executeUpdate();
                if (rowsLogin <= 0) {
                    errorLabel.setText("Failed to create login account. Try again.");
                    return;
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Account created successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginSignupPage();

        } catch (SQLException ex) {
            ex.printStackTrace();
            errorLabel.setText("Database error: " + ex.getMessage());
        }
    }

    private void styleLabel(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    }

    public static void main(String[] args) {
        new SignupPage();
    }
}
