package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ManageTourGuidesPage extends JFrame {

    private JTable guideTable;
    private DefaultTableModel tableModel;

    private JTextField fullNameField, cityField, priceField;
    private JCheckBox activeCheck;
    private JButton addButton, updateButton, deleteButton, refreshButton, backButton;

    public ManageTourGuidesPage() {
        setTitle("Manage Tour Guides - ByteAir");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        try {
            setContentPane(new JLabel(new ImageIcon(
                    getClass().getResource("/images/AirPlaneSky.jpg"))));
        } catch (Exception e) {
            getContentPane().setBackground(new Color(230, 240, 255));
        }
        getContentPane().setLayout(null);

        initComponents();
        loadGuides();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        JLabel title = new JLabel("Manage Tour Guides");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setBounds(60, 20, 500, 40);
        add(title);

        guideTable = new JTable();
        guideTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        guideTable.setRowHeight(28);
        guideTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(guideTable);
        scroll.setBounds(60, 80, 900, 280);
        add(scroll);

        // ==== Form ====
        int xLabel = 60, xField = 220, y = 380, gap = 45, h = 30, w = 260;

        JLabel nameLbl = createLabel("Full Name:", xLabel, y);
        fullNameField = createField(xField, y, w, h);
        add(nameLbl); add(fullNameField);
        y += gap;

        JLabel cityLbl = createLabel("City:", xLabel, y);
        cityField = createField(xField, y, w, h);
        add(cityLbl); add(cityField);
        y += gap;

        JLabel priceLbl = createLabel("Price per day (SAR):", xLabel, y);
        priceField = createField(xField, y, w, h);
        add(priceLbl); add(priceField);
        y += gap;

        JLabel activeLbl = createLabel("Active:", xLabel, y);
        activeCheck = new JCheckBox("Is Active");
        activeCheck.setOpaque(false);
        activeCheck.setForeground(Color.WHITE);
        activeCheck.setBounds(xField, y, 120, h);
        add(activeLbl); add(activeCheck);

        // ==== Buttons ====
        addButton = createButton("Add Guide", 550, 380);
        updateButton = createButton("Update Guide", 550, 380 + gap);
        deleteButton = createButton("Delete Guide", 550, 380 + 2 * gap);
        refreshButton = createButton("Refresh", 550, 380 + 3 * gap);
        backButton = createButton("Back to Dashboard", 550, 380 + 4 * gap);

        add(addButton); add(updateButton); add(deleteButton);
        add(refreshButton); add(backButton);

        addButton.addActionListener(e -> addGuide());
        updateButton.addActionListener(e -> updateGuide());
        deleteButton.addActionListener(e -> deleteGuide());
        refreshButton.addActionListener(e -> loadGuides());
        backButton.addActionListener(e -> {
            new AdminDashboard().setVisible(true);
            dispose();
        });

        guideTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = guideTable.getSelectedRow();
                if (row == -1) return;
                fullNameField.setText(tableModel.getValueAt(row, 1).toString());
                cityField.setText(tableModel.getValueAt(row, 2).toString());
                priceField.setText(tableModel.getValueAt(row, 3).toString());
                String active = tableModel.getValueAt(row, 4).toString();
                activeCheck.setSelected("1".equals(active) || "true".equalsIgnoreCase(active));
            }
        });
    }

    private JLabel createLabel(String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(Color.WHITE);
        l.setBounds(x, y, 160, 30);
        return l;
    }

    private JTextField createField(int x, int y, int w, int h) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setBounds(x, y, w, h);
        return f;
    }

    private JButton createButton(String text, int x, int y) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        b.setBounds(x, y, 230, 36);
        b.setBackground(Color.WHITE);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 255), 2, true));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(210, 225, 250)); }
            public void mouseExited(MouseEvent e) { b.setBackground(Color.WHITE); }
        });
        return b;
    }

    // ---------- DB ops ----------

    private void loadGuides() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT guide_id, full_name, city, price_per_day, is_active FROM TourGuide";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            tableModel = buildTableModel(rs);
            guideTable.setModel(tableModel);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load tour guides.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateGuideFields() {
        if (fullNameField.getText().trim().isEmpty() ||
            cityField.getText().trim().isEmpty() ||
            priceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Full name, city and price must be filled.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        return false;
        }
        try {
            Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Price must be a number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private String generateNextGuideId(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(guide_id,4) AS UNSIGNED)) AS num " +
                     "FROM TourGuide WHERE guide_id LIKE 'TG-%'";
        int start = 1;
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                int last = rs.getInt("num");
                if (!rs.wasNull()) {
                    return "TG-" + (last + 1);
                }
            }
        }
        return "TG-" + start;
    }

    private void addGuide() {
        if (!validateGuideFields()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String guideId = generateNextGuideId(conn);

            String sql = "INSERT INTO TourGuide " +
                         "(guide_id, full_name, city, price_per_day, is_active) " +
                         "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, guideId);
            pst.setString(2, fullNameField.getText().trim());
            pst.setString(3, cityField.getText().trim());
            pst.setDouble(4, Double.parseDouble(priceField.getText().trim()));
            pst.setInt(5, activeCheck.isSelected() ? 1 : 0);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Tour guide added successfully!\nID: " + guideId);
            loadGuides();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error adding guide: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGuide() {
        int row = guideTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a guide to update.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateGuideFields()) return;

        String guideId = tableModel.getValueAt(row, 0).toString();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE TourGuide " +
                         "SET full_name=?, city=?, price_per_day=?, is_active=? " +
                         "WHERE guide_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, fullNameField.getText().trim());
            pst.setString(2, cityField.getText().trim());
            pst.setDouble(3, Double.parseDouble(priceField.getText().trim()));
            pst.setInt(4, activeCheck.isSelected() ? 1 : 0);
            pst.setString(5, guideId);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Guide updated successfully!");
            loadGuides();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error updating guide: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteGuide() {
        int row = guideTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a guide to delete.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String guideId = tableModel.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete guide " + guideId + " and all their tour bookings?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Delete tour bookings for this guide
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM TourBooking WHERE guide_id=?")) {
                pst.setString(1, guideId);
                pst.executeUpdate();
            }

            // Delete guide
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM TourGuide WHERE guide_id=?")) {
                pst.setString(1, guideId);
                pst.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this,
                    "Guide and related bookings deleted successfully.");
            loadGuides();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting guide: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        String[] colNames = new String[colCount];
        for (int i = 1; i <= colCount; i++) colNames[i - 1] = meta.getColumnName(i);

        Vector<String[]> data = new Vector<>();
        while (rs.next()) {
            String[] row = new String[colCount];
            for (int i = 1; i <= colCount; i++) row[i - 1] = rs.getString(i);
            data.add(row);
        }
        String[][] arr = new String[data.size()][];
        data.toArray(arr);

        return new DefaultTableModel(arr, colNames) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static void main(String[] args) {
        new ManageTourGuidesPage();
    }
}
