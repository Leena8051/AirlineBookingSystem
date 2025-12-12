package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class TourGuidePage extends JFrame {

    private JTable guideTable;
    private DefaultTableModel tableModel;
    private JButton bookTourButton, backButton;

    public TourGuidePage() {
        setTitle("ByteAir - Tour Guides");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);


        try {
            JLabel bg = new JLabel(new ImageIcon(
                    getClass().getResource("/images/AirPlaneSky.jpg")));
            setContentPane(bg);
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

        JLabel titleLabel = new JLabel("Available Tour Guides");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(60, 30, 600, 40);
        add(titleLabel);

        guideTable = new JTable();
        guideTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        guideTable.setRowHeight(28);
        guideTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        guideTable.setToolTipText("Select a tour guide from the list");

        JScrollPane scrollPane = new JScrollPane(guideTable);
        scrollPane.setBounds(60, 100, 1000, 350);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane);

        bookTourButton = new JButton("Book Tour with Selected Guide");
        bookTourButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        bookTourButton.setBounds(60, 480, 320, 45);
        styleMainButton(bookTourButton);
        add(bookTourButton);

        backButton = new JButton("Back to Main Menu");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        backButton.setBounds(400, 480, 250, 45);
        styleMainButton(backButton);
        add(backButton);

        bookTourButton.addActionListener(e -> openTourBooking());
        backButton.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });
    }

    private void styleMainButton(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 255), 2, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(210, 225, 250)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
    }

    private void loadGuides() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT guide_id, full_name, city, price_per_day " +
                    "FROM TourGuide WHERE is_active = 1";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            tableModel = buildReadOnlyTableModel(rs);
            guideTable.setModel(tableModel);

            if (tableModel.getRowCount() > 0) {
                guideTable.setRowSelectionInterval(0, 0);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load tour guides.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DefaultTableModel buildReadOnlyTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        String[] colNames = new String[colCount];
        for (int i = 1; i <= colCount; i++) {
            colNames[i - 1] = meta.getColumnName(i);
        }

        Vector<String[]> data = new Vector<>();
        while (rs.next()) {
            String[] row = new String[colCount];
            for (int i = 1; i <= colCount; i++) {
                row[i - 1] = rs.getString(i);
            }
            data.add(row);
        }

        return new DefaultTableModel(data.toArray(new String[0][]), colNames) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private void openTourBooking() {
        int row = guideTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a tour guide first.",
                    "No Guide Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String guideId = tableModel.getValueAt(row, 0).toString();
        String fullName = tableModel.getValueAt(row, 1).toString();
        String city = tableModel.getValueAt(row, 2).toString();
        String pricePerDay = tableModel.getValueAt(row, 3).toString();

        new TourBookingPage(guideId, fullName, city, pricePerDay).setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        new TourGuidePage();
    }
}
