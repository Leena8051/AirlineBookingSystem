package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.sql.*;

public class LoginSignupPage extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, signupButton;
    private JCheckBox showPasswordCheckbox;
    private JLabel errorLabel;

    public LoginSignupPage() {
        initComponents();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void initComponents() {
        setTitle("ByteAir - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        try {
            setContentPane(new JLabel(new ImageIcon(
                    ImageIO.read(getClass().getResource("/images/Map.jpg")))));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load background image.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        try {
            BufferedImage original = ImageIO.read(new File("C:/Users/leena/Desktop/iii.jpg"));
            int size = 50;
            Image scaledImage = original.getScaledInstance(size, size, Image.SCALE_SMOOTH);

            BufferedImage rounded = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = rounded.createGraphics();
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
            g2.drawImage(scaledImage, 0, 0, null);
            g2.dispose();

            JLabel iconLabel = new JLabel(new ImageIcon(rounded));
            iconLabel.setBounds(530, 55, size, size);
            add(iconLabel);

            JLabel welcomeLabel = new JLabel("Welcome to ByteAir!");
            welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
            welcomeLabel.setForeground(Color.WHITE);
            welcomeLabel.setBounds(590, 50, 600, 50);
            add(welcomeLabel);
        } catch (Exception ex) {
            System.out.println("Icon failed to load: " + ex.getMessage());
        }

        // Email label + field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setBounds(650, 230, 300, 20);
        emailLabel.setForeground(Color.WHITE);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(650, 260, 300, 30);
        emailField.setToolTipText("Enter your email (example: user@gmail.com)");
        add(emailField);

        // Password label + field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(650, 310, 300, 20);
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(650, 340, 300, 30);
        passwordField.setToolTipText("Enter your password.");
        add(passwordField);

        // Show password checkbox
        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.setBounds(650, 375, 150, 20);
        showPasswordCheckbox.setBackground(new Color(255, 255, 255, 200));
        showPasswordCheckbox.setForeground(Color.BLACK);
        showPasswordCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPasswordCheckbox.setFocusPainted(false);
        showPasswordCheckbox.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? (char) 0 : '•');
        });
        add(showPasswordCheckbox);

        // Error label
        errorLabel = new JLabel("");
        errorLabel.setBounds(650, 400, 450, 20);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(errorLabel);

        // Login button
        loginButton = new JButton("Log in");
        loginButton.setBounds(650, 440, 140, 40);
        loginButton.setBackground(Color.decode("#ADD8E6"));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> loginAction());
        add(loginButton);

        // Signup button
        signupButton = new JButton("Sign up");
        signupButton.setBounds(810, 440, 140, 40);
        signupButton.setBackground(Color.WHITE);
        signupButton.setForeground(Color.decode("#ADD8E6"));
        signupButton.setBorder(BorderFactory.createLineBorder(Color.decode("#ADD8E6")));
        signupButton.setFocusPainted(false);
        signupButton.addActionListener(e -> {
            dispose();
            new SignupPage();
        });
        add(signupButton);

        setVisible(true);
    }

    
    private void loginAction() {
        errorLabel.setText("");

        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        //  Basic validation 
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in both email and password.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Invalid email format.");
            return;
        }

        //  Database check 
        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn == null) {
                errorLabel.setText("Cannot connect to database.");
                return;
            }

            // Query Login + join with Customer & Admin
            String sql =
                "SELECT l.role, " +
                "       c.customer_id, c.first_name AS customer_name, " +
                "       a.admin_id,    a.first_name AS admin_name " +
                "FROM Login l " +
                "LEFT JOIN Customer c ON l.customer_id = c.customer_id " +
                "LEFT JOIN Admin    a ON l.admin_id    = a.admin_id " +
                "WHERE l.email = ? AND l.password = ?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                // No match at all
                errorLabel.setText("Incorrect email or password.");
                return;
            }

            String role = rs.getString("role");

            if ("Customer".equalsIgnoreCase(role)) {
                String customerId = rs.getString("customer_id");
                String customerName = rs.getString("customer_name");

                if (customerId == null) {
                    errorLabel.setText("Login record not linked to any customer.");
                    return;
                }

                Session.currentUserId = customerId;
                Session.currentName = customerName;
                Session.currentRole = "customer";

                JOptionPane.showMessageDialog(this,
                        "Welcome, " + Session.currentName + "!");
                dispose();
                new MainMenu();

            } else if ("Admin".equalsIgnoreCase(role)) {
                String adminId = rs.getString("admin_id");
                String adminName = rs.getString("admin_name");

                if (adminId == null) {
                    errorLabel.setText("Login record not linked to any admin.");
                    return;
                }

                Session.currentUserId = adminId;
                Session.currentName = adminName;
                Session.currentRole = "admin";

                JOptionPane.showMessageDialog(this,
                        "Welcome Admin, " + Session.currentName + "!");
                dispose();
                new AdminDashboard();

            } else {
                errorLabel.setText("Unknown user role. Please contact support.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new LoginSignupPage();
    }
}
