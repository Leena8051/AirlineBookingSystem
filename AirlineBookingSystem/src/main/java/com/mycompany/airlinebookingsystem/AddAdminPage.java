package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.imageio.ImageIO;
import java.io.IOException;

public class AddAdminPage extends JFrame {

    private JTextField firstNameField, middleNameField, lastNameField, emailField, phoneField;
    private JRadioButton maleRadio, femaleRadio;
    private ButtonGroup genderGroup;
    private JLabel errorLabel;

    private String currentAdminId;

    public AddAdminPage() {
        this(null); // calls the main constructor with null
    }

    public AddAdminPage(String currentAdminId) {
        this.currentAdminId = currentAdminId;
        initUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AddAdminPage().setVisible(true));
    }

    private void initUI() {
        setTitle("Add New Admin - ByteAir");
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // full screen
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Background image
        try {
            JLabel bg = new JLabel(new ImageIcon(ImageIO.read(
                    getClass().getResource("/images/AirPlaneSky.jpg"))));
            setContentPane(bg);
        } catch (IOException ex) {
            getContentPane().setBackground(new Color(180, 210, 255));
        }

        getContentPane().setLayout(null);

        // Card panel
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(new Color(255, 255, 255, 230));
        card.setBounds(380, 100, 550, 520);
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 255), 2, true));
        add(card);

        Font titleFont = new Font("Segoe UI", Font.BOLD, 24);
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel title = new JLabel("Add New Admin");
        title.setFont(titleFont);
        title.setForeground(new Color(30, 60, 120));
        title.setBounds(30, 20, 300, 30);
        card.add(title);

        int xLabel = 40, xField = 180, y = 80, h = 28, w = 300, gap = 45;

        // First Name
        addLabel(card, "First Name:", xLabel, y, labelFont);
        firstNameField = createTextField(xField, y, w, h, inputFont);
        firstNameField.setToolTipText("Enter first name.");
        card.add(firstNameField);
        y += gap;

        // Middle Name
        addLabel(card, "Middle Name:", xLabel, y, labelFont);
        middleNameField = createTextField(xField, y, w, h, inputFont);
        middleNameField.setToolTipText("Enter middle name.");
        card.add(middleNameField);
        y += gap;

        // Last Name
        addLabel(card, "Last Name:", xLabel, y, labelFont);
        lastNameField = createTextField(xField, y, w, h, inputFont);
        lastNameField.setToolTipText("Enter last name.");
        card.add(lastNameField);
        y += gap;

        // Email
        addLabel(card, "Email:", xLabel, y, labelFont);
        emailField = createTextField(xField, y, w, h, inputFont);
        emailField.setToolTipText("Enter a valid email.");
        card.add(emailField);
        y += gap;

        // Phone
        addLabel(card, "Phone:", xLabel, y, labelFont);
        phoneField = createTextField(xField, y, w, h, inputFont);
        phoneField.setToolTipText("Enter 10-digit Saudi phone number.");
        card.add(phoneField);
        y += gap;

        // Gender
        addLabel(card, "Gender:", xLabel, y, labelFont);
        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");

        maleRadio.setOpaque(false);
        femaleRadio.setOpaque(false);

        maleRadio.setFont(inputFont);
        femaleRadio.setFont(inputFont);

        maleRadio.setToolTipText("Select if admin is male.");
        femaleRadio.setToolTipText("Select if admin is female.");

        maleRadio.setBounds(xField, y, 80, h);
        femaleRadio.setBounds(xField + 100, y, 100, h);

        genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);

        card.add(maleRadio);
        card.add(femaleRadio);
        y += gap;

        // Error Label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        errorLabel.setForeground(Color.RED);
        errorLabel.setBounds(xLabel, y, 460, 30);
        card.add(errorLabel);
        y += 40;

        // Buttons
        JButton backBtn = createStyledButton("Back");
        backBtn.setBounds(xLabel, y, 120, 36);
        backBtn.setToolTipText("Return to admin dashboard.");
        backBtn.addActionListener(e -> dispose());
        card.add(backBtn);

        JButton saveBtn = createStyledButton("Save");
        saveBtn.setBounds(xField + 140, y, 140, 36);
        saveBtn.setToolTipText("Save the new admin.");
        saveBtn.addActionListener(e -> saveAdmin());
        card.add(saveBtn);

        setVisible(true);
    }

    private void addLabel(JPanel parent, String text, int x, int y, Font font) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, 130, 30);
        label.setFont(font);
        label.setForeground(new Color(40, 40, 40));
        parent.add(label);
    }

    private JTextField createTextField(int x, int y, int w, int h, Font font) {
        JTextField field = new JTextField();
        field.setBounds(x, y, w, h);
        field.setFont(font);
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(new Color(180, 210, 255));
        btn.setForeground(Color.BLACK);
        btn.setBorder(BorderFactory.createLineBorder(new Color(150, 180, 255), 2, true));
        btn.setFocusPainted(false);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(160, 200, 255));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(180, 210, 255));
            }
        });

        return btn;
    }

    // ======================================================
    //  SEQUENTIAL ADMIN ID: AD-1, AD-2, AD-3, ...
    //  Ignores bad IDs like AD-81CCF
    // ======================================================
    private String generateNextAdminId(Connection conn) throws SQLException {
        String sql =
            "SELECT MAX(CAST(SUBSTRING(admin_id, 4) AS UNSIGNED)) AS num " +
            "FROM Admin " +
            "WHERE admin_id REGEXP '^AD-[0-9]+$'";

        int next = 1;

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                int last = rs.getInt("num");
                if (!rs.wasNull()) {
                    next = last + 1;
                }
            }
        }

        // AD-1, AD-2, AD-10, ...
        return "AD-" + next;
    }

    // ======================================================
    // SAVE ADMIN
    // ======================================================
    private void saveAdmin() {

        errorLabel.setText(" ");

        String first = firstNameField.getText().trim();
        String mid = middleNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = maleRadio.isSelected() ? "Male" :
                (femaleRadio.isSelected() ? "Female" : "");

        // Validation
        if (first.isEmpty() || mid.isEmpty() || last.isEmpty() ||
                email.isEmpty() || phone.isEmpty() || gender.isEmpty()) {

            errorLabel.setText("All fields must be filled and gender must be selected.");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            errorLabel.setText("Invalid email format.");
            return;
        }

        if (!phone.matches("\\d{10}")) {
            errorLabel.setText("Phone must be 10 digits.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn == null) {
                errorLabel.setText("Cannot connect to database.");
                return;
            }

            // Check email in Admin
            PreparedStatement chk = conn.prepareStatement("SELECT 1 FROM Admin WHERE email=?");
            chk.setString(1, email);
            ResultSet rs = chk.executeQuery();
            if (rs.next()) {
                errorLabel.setText("Email already exists in Admin.");
                return;
            }

            // Check email in Login
            chk = conn.prepareStatement("SELECT 1 FROM Login WHERE email=?");
            chk.setString(1, email);
            rs = chk.executeQuery();
            if (rs.next()) {
                errorLabel.setText("Email already exists in Login.");
                return;
            }

            // 🔹 Generate sequential admin ID: AD-8, AD-9, AD-10, ...
            String adminId = generateNextAdminId(conn);

            // Insert Admin
            PreparedStatement adminStmt = conn.prepareStatement(
                    "INSERT INTO Admin (admin_id, first_name, middle_name, last_name, email, phone_number, gender) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)");

            adminStmt.setString(1, adminId);
            adminStmt.setString(2, first);
            adminStmt.setString(3, mid);
            adminStmt.setString(4, last);
            adminStmt.setString(5, email);
            adminStmt.setString(6, phone);
            adminStmt.setString(7, gender);
            adminStmt.executeUpdate();

            // Insert Login
            PreparedStatement loginStmt = conn.prepareStatement(
                    "INSERT INTO Login (email, password, role, admin_id) VALUES (?, ?, ?, ?)");

            loginStmt.setString(1, email);
            loginStmt.setString(2, "default123"); // default password
            loginStmt.setString(3, "Admin");
            loginStmt.setString(4, adminId);
            loginStmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Admin Added Successfully!\n\nID: " + adminId +
                            "\nPassword: default123",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            errorLabel.setText("Database error occurred.");
        }
    }
}
