package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageBookings extends JFrame {

    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JTextField bookingIdField, customerIdField, flightIdField, bookingDateField, statusField;
    private JButton updateButton, deleteButton, refreshButton, backButton;

    public ManageBookings() {
        initComponents();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        loadBookings();
    }

    private void initComponents() {
        setTitle("Manage Bookings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            setContentPane(new JLabel(new ImageIcon(getClass().getResource("/images/AirPlaneSky.jpg"))));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Background image not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        getContentPane().setLayout(null);

        JLabel titleLabel = new JLabel("Manage Bookings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(450, 30, 600, 50);
        add(titleLabel);

        bookingsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBounds(50, 100, 1000, 300);
        add(scrollPane);

        int xLabel = 50, xField = 250, y = 420, gap = 60;

        JLabel bookingIdLabel = createLabel("Booking ID:", xLabel, y);
        bookingIdField = createTextField(xField, y);

        JLabel customerIdLabel = createLabel("Customer ID:", xLabel, y + gap);
        customerIdField = createTextField(xField, y + gap);

        JLabel flightIdLabel = createLabel("Flight ID:", xLabel, y + 2 * gap);
        flightIdField = createTextField(xField, y + 2 * gap);

        JLabel bookingDateLabel = createLabel("Booking Date:", xLabel, y + 3 * gap);
        bookingDateField = createTextField(xField, y + 3 * gap);

        JLabel statusLabel = createLabel("Status:", xLabel, y + 4 * gap);
        statusField = createTextField(xField, y + 4 * gap);

        add(bookingIdLabel); add(bookingIdField);
        add(customerIdLabel); add(customerIdField);
        add(flightIdLabel); add(flightIdField);
        add(bookingDateLabel); add(bookingDateField);
        add(statusLabel); add(statusField);

        updateButton = createButton("Update Booking", 500, y);
        deleteButton = createButton("Delete Booking", 500, y + gap);
        refreshButton = createButton("Refresh", 500, y + 2 * gap);
        backButton = createButton("Back to Dashboard", 500, y + 3 * gap);

        add(updateButton); add(deleteButton); add(refreshButton); add(backButton);

        updateButton.addActionListener(e -> updateBooking());
        deleteButton.addActionListener(e -> deleteBooking());
        refreshButton.addActionListener(e -> loadBookings());
        backButton.addActionListener(e -> {
            new AdminDashboard().setVisible(true);
            dispose();
        });

        bookingsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedRow = bookingsTable.getSelectedRow();
                if (selectedRow != -1) {
                    bookingIdField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    customerIdField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    flightIdField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    bookingDateField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    statusField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                }
            }
        });

        setVisible(true);
    }

    private JLabel createLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        label.setBounds(x, y, 180, 30);
        return label;
    }

    private JTextField createTextField(int x, int y) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        field.setBounds(x, y, 200, 30);
        return field;
    }

    private JButton createButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        button.setBounds(x, y, 250, 40);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(150, 180, 255), 2, true));
        return button;
    }

    private void loadBookings() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Booking";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            tableModel = buildTableModel(rs);
            bookingsTable.setModel(tableModel);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading bookings!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookingId = bookingIdField.getText().trim();
        String customerId = customerIdField.getText().trim();
        String flightId = flightIdField.getText().trim();
        String bookingDate = bookingDateField.getText().trim();
        String status = statusField.getText().trim();

        if (bookingId.isEmpty() || customerId.isEmpty() || flightId.isEmpty() || bookingDate.isEmpty() || status.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE Booking SET customer_id = ?, flight_no = ?, booking_date = ?, status = ? WHERE booking_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, customerId);
            pst.setString(2, flightId);
            pst.setString(3, bookingDate);
            pst.setString(4, status);
            pst.setString(5, bookingId);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Booking updated successfully!");
            loadBookings();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating booking!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookingId = tableModel.getValueAt(selectedRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete booking " + bookingId + " and all related data?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1) Feedback linked to this booking
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM Feedback WHERE booking_id = ?")) {
                pst.setString(1, bookingId);
                pst.executeUpdate();
            }

            // 2) Tickets
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM Ticket WHERE booking_id = ?")) {
                pst.setString(1, bookingId);
                pst.executeUpdate();
            }

            // 3) Passengers
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM Passenger WHERE booking_id = ?")) {
                pst.setString(1, bookingId);
                pst.executeUpdate();
            }

            // 4) Find payments for this booking
            String paymentId = null;
            try (PreparedStatement getPayment = conn.prepareStatement(
                    "SELECT payment_id FROM Payment WHERE booking_id = ?")) {
                getPayment.setString(1, bookingId);
                ResultSet rs = getPayment.executeQuery();
                if (rs.next()) {
                    paymentId = rs.getString("payment_id");
                }
            }

            if (paymentId != null) {
                // 5) Receipts
                try (PreparedStatement pst = conn.prepareStatement(
                        "DELETE FROM Receipt WHERE payment_id = ?")) {
                    pst.setString(1, paymentId);
                    pst.executeUpdate();
                }

                // 6) CreditCard
                try (PreparedStatement pst = conn.prepareStatement(
                        "DELETE FROM CreditCard WHERE payment_id = ?")) {
                    pst.setString(1, paymentId);
                    pst.executeUpdate();
                }

                // 7) Payment
                try (PreparedStatement pst = conn.prepareStatement(
                        "DELETE FROM Payment WHERE payment_id = ?")) {
                    pst.setString(1, paymentId);
                    pst.executeUpdate();
                }
            }

            // 8) Finally delete booking
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM Booking WHERE booking_id = ?")) {
                pst.setString(1, bookingId);
                pst.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Booking deleted successfully!");
            loadBookings();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting booking: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];

        for (int column = 1; column <= columnCount; column++) {
            columnNames[column - 1] = metaData.getColumnName(column);
        }

        java.util.Vector<String[]> data = new java.util.Vector<>();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getString(i);
            }
            data.add(row);
        }

        String[][] dataArray = new String[data.size()][];
        data.toArray(dataArray);

        return new DefaultTableModel(dataArray, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void main(String[] args) {
        new ManageBookings();
    }
}
