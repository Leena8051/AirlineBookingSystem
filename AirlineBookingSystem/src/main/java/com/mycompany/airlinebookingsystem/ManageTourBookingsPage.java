package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ManageTourBookingsPage extends JFrame {

    private JTable bookingTable;
    private DefaultTableModel tableModel;

    private JTextField bookingIdField, customerIdField, guideIdField,
            startDateField, endDateField, totalPriceField, statusField;

    private JButton updateButton, deleteButton, refreshButton, backButton;

    public ManageTourBookingsPage() {
        setTitle("Manage Tour Bookings - ByteAir");
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
        loadBookings();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        JLabel title = new JLabel("Manage Tour Bookings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setBounds(60, 20, 500, 40);
        add(title);

        bookingTable = new JTable();
        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bookingTable.setRowHeight(28);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(bookingTable);
        scroll.setBounds(60, 80, 1050, 280);
        add(scroll);

        int xLabel = 60, xField = 240, y = 380, gap = 40, h = 28, w = 240;

        bookingIdField = createFieldRow("Booking ID:", xLabel, xField, y, w, h); y += gap;
        customerIdField = createFieldRow("Customer ID:", xLabel, xField, y, w, h); y += gap;
        guideIdField    = createFieldRow("Guide ID:", xLabel, xField, y, w, h); y += gap;
        startDateField  = createFieldRow("Start Date (YYYY-MM-DD):", xLabel, xField, y, w, h); y += gap;
        endDateField    = createFieldRow("End Date (YYYY-MM-DD):", xLabel, xField, y, w, h); y += gap;
        totalPriceField = createFieldRow("Total Price:", xLabel, xField, y, w, h); y += gap;
        statusField     = createFieldRow("Status:", xLabel, xField, y, w, h);

        bookingIdField.setEditable(false); // primary key

        updateButton = createButton("Update Booking", 580, 380);
        deleteButton = createButton("Delete Booking", 580, 380 + gap);
        refreshButton = createButton("Refresh", 580, 380 + 2 * gap);
        backButton = createButton("Back to Dashboard", 580, 380 + 3 * gap);

        add(updateButton); add(deleteButton); add(refreshButton); add(backButton);

        updateButton.addActionListener(e -> updateBooking());
        deleteButton.addActionListener(e -> deleteBooking());
        refreshButton.addActionListener(e -> loadBookings());
        backButton.addActionListener(e -> {
            new AdminDashboard().setVisible(true);
            dispose();
        });

        bookingTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = bookingTable.getSelectedRow();
                if (row == -1) return;
                bookingIdField.setText(tableModel.getValueAt(row, 0).toString());
                customerIdField.setText(tableModel.getValueAt(row, 1).toString());
                guideIdField.setText(tableModel.getValueAt(row, 2).toString());
                startDateField.setText(tableModel.getValueAt(row, 3).toString());
                endDateField.setText(tableModel.getValueAt(row, 4).toString());
                totalPriceField.setText(tableModel.getValueAt(row, 5).toString());
                statusField.setText(tableModel.getValueAt(row, 6).toString());
            }
        });
    }

    private JTextField createFieldRow(String label, int xLabel, int xField,
                                      int y, int w, int h) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(Color.WHITE);
        l.setBounds(xLabel, y, 200, 30);
        add(l);

        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setBounds(xField, y, w, h);
        add(f);
        return f;
    }

    private JButton createButton(String text, int x, int y) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        b.setBounds(x, y, 240, 36);
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

    private void loadBookings() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT tour_booking_id, customer_id, guide_id, " +
                         "start_date, end_date, total_price, status " +
                         "FROM TourBooking";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            tableModel = buildTableModel(rs);
            bookingTable.setModel(tableModel);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load tour bookings.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBooking() {
        int row = bookingTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a booking to update.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = bookingIdField.getText().trim();
        String customer = customerIdField.getText().trim();
        String guide = guideIdField.getText().trim();
        String start = startDateField.getText().trim();
        String end = endDateField.getText().trim();
        String total = totalPriceField.getText().trim();
        String status = statusField.getText().trim();

        if (id.isEmpty() || customer.isEmpty() || guide.isEmpty() ||
                start.isEmpty() || end.isEmpty() || total.isEmpty() ||
                status.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields must be filled.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Double.parseDouble(total);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Total price must be numeric.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE TourBooking SET " +
                         "customer_id=?, guide_id=?, start_date=?, " +
                         "end_date=?, total_price=?, status=? " +
                         "WHERE tour_booking_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, customer);
            pst.setString(2, guide);
            pst.setString(3, start);
            pst.setString(4, end);
            pst.setDouble(5, Double.parseDouble(total));
            pst.setString(6, status);
            pst.setString(7, id);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Tour booking updated successfully!");
            loadBookings();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error updating booking: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBooking() {
        int row = bookingTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a booking to delete.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete tour booking " + id + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM TourBooking WHERE tour_booking_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Tour booking deleted successfully.");
            loadBookings();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting booking: " + ex.getMessage(),
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
        new ManageTourBookingsPage();
    }
}
