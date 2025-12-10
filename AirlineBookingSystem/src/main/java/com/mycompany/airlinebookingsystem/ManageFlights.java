package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ManageFlights extends JFrame {

    private JTable flightsTable;
    private DefaultTableModel tableModel;
    private JTextField flightNameField, departureField, destinationField,
            departureTimeField, arrivalTimeField, priceField, capacityField;
    private JButton addButton, updateButton, deleteButton, backButton, refreshButton;

    public ManageFlights() {
        initComponents();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        loadFlights();
    }

    private void initComponents() {
        setTitle("Manage Flights");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            setContentPane(new JLabel(new ImageIcon(
                    getClass().getResource("/images/AirPlaneSky.jpg"))));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Background image not found.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        getContentPane().setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Manage Flights", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        // Table
        flightsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(flightsTable);
        scrollPane.setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addFormField(formPanel, gbc, row++, "Flight Name:",
                flightNameField = new JTextField());
        addFormField(formPanel, gbc, row++, "Departure:",
                departureField = new JTextField());
        addFormField(formPanel, gbc, row++, "Destination:",
                destinationField = new JTextField());
        addFormField(formPanel, gbc, row++,
                "Departure Time (YYYY-MM-DD HH:MM:SS):",
                departureTimeField = new JTextField());
        addFormField(formPanel, gbc, row++,
                "Arrival Time (YYYY-MM-DD HH:MM:SS):",
                arrivalTimeField = new JTextField());
        addFormField(formPanel, gbc, row++, "Price:",
                priceField = new JTextField());
        addFormField(formPanel, gbc, row++, "Capacity:",
                capacityField = new JTextField());

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setOpaque(false);

        addButton = createButton("Add Flight", buttonsPanel);
        updateButton = createButton("Update Flight", buttonsPanel);
        deleteButton = createButton("Delete Flight", buttonsPanel);
        refreshButton = createButton("Refresh", buttonsPanel);
        backButton = createButton("Back", buttonsPanel);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(buttonsPanel, gbc);

        add(formPanel, BorderLayout.SOUTH);

        // Event Listeners
        addButton.addActionListener(e -> addFlight());
        updateButton.addActionListener(e -> updateFlight());
        deleteButton.addActionListener(e -> deleteFlight());
        refreshButton.addActionListener(e -> loadFlights());
        backButton.addActionListener(e -> {
            new AdminDashboard().setVisible(true);
            dispose();
        });

        flightsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedRow = flightsTable.getSelectedRow();
                if (selectedRow == -1) return;
                flightNameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                departureField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                destinationField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                departureTimeField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                arrivalTimeField.setText(tableModel.getValueAt(selectedRow, 5).toString());
                priceField.setText(tableModel.getValueAt(selectedRow, 6).toString());
                capacityField.setText(tableModel.getValueAt(selectedRow, 7).toString());
            }
        });

        setVisible(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row,
                              String labelText, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        panel.add(label, gbc);
        gbc.gridx = 1;
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(textField, gbc);
    }

    private JButton createButton(String text, JPanel panel) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        panel.add(button);
        return button;
    }

    private void loadFlights() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT flight_no, flight_name, from_location, destination, " +
                           "departure_time, arrival_time, price, capacity, flight_date, flight_status " +
                           "FROM Flight";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            tableModel = buildTableModel(rs);
            flightsTable.setModel(tableModel);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading flights!", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateFields() {
        if (flightNameField.getText().isEmpty() ||
            departureField.getText().isEmpty() ||
            destinationField.getText().isEmpty() ||
            departureTimeField.getText().isEmpty() ||
            arrivalTimeField.getText().isEmpty() ||
            priceField.getText().isEmpty() ||
            capacityField.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "All fields must be filled.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Double.parseDouble(priceField.getText());
            Integer.parseInt(capacityField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Price must be a number, and Capacity must be an integer.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setLenient(false);
            sdf.parse(departureTimeField.getText());
            sdf.parse(arrivalTimeField.getText());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid datetime format. Use YYYY-MM-DD HH:MM:SS",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    // ======================= ADD FLIGHT =======================

    private void addFlight() {
        if (!validateFields()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String flightNo = generateNextFlightNo(conn); // ✅ FL107, FL108, ...

            String query = "INSERT INTO Flight " +
                    "(flight_no, flight_name, from_location, destination, " +
                    " departure_time, arrival_time, price, capacity, flight_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, flightNo);
            pst.setString(2, flightNameField.getText());
            pst.setString(3, departureField.getText());
            pst.setString(4, destinationField.getText());
            pst.setString(5, departureTimeField.getText());
            pst.setString(6, arrivalTimeField.getText());
            pst.setDouble(7, Double.parseDouble(priceField.getText()));
            pst.setInt(8, Integer.parseInt(capacityField.getText()));
            // flight_date = date part of departure_time
            pst.setString(9, departureTimeField.getText().substring(0, 10));

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Flight added successfully!\nFlight No: " + flightNo);
            clearFields();
            loadFlights();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error adding flight: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * يولّد رقم الرحلة التالي بالشكل FL100, FL101, ... إلخ
     */
    private String generateNextFlightNo(Connection conn) throws SQLException {
        String sql =
                "SELECT MAX(CAST(SUBSTRING(flight_no, 3) AS UNSIGNED)) AS num " +
                "FROM Flight WHERE flight_no LIKE 'FL%'";
        int start = 100; // أول رحلة لو ما فيه شيء

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                int last = rs.getInt("num");
                if (!rs.wasNull()) {
                    return "FL" + (last + 1);
                }
            }
        }
        return "FL" + start;
    }

    // ======================= UPDATE FLIGHT =======================

    private void updateFlight() {
        int selectedRow = flightsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a flight to update.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validateFields()) return;

        String flightNo = tableModel.getValueAt(selectedRow, 0).toString();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE Flight SET " +
                    "flight_name=?, from_location=?, destination=?, " +
                    "departure_time=?, arrival_time=?, price=?, capacity=? " +
                    "WHERE flight_no=?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, flightNameField.getText());
            pst.setString(2, departureField.getText());
            pst.setString(3, destinationField.getText());
            pst.setString(4, departureTimeField.getText());
            pst.setString(5, arrivalTimeField.getText());
            pst.setDouble(6, Double.parseDouble(priceField.getText()));
            pst.setInt(7, Integer.parseInt(capacityField.getText()));
            pst.setString(8, flightNo);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Flight updated successfully!");
            loadFlights();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error updating flight!", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======================= DELETE FLIGHT (WITH CHILD ROWS) =======================

    private void deleteFlight() {
        int selectedRow = flightsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a flight to delete.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String flightNo = tableModel.getValueAt(selectedRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete flight " + flightNo +
                        " and all related bookings / payments / seats ?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1) Seats for this flight
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM Seat WHERE flight_no = ?")) {
                pst.setString(1, flightNo);
                pst.executeUpdate();
            }

            // 2) Flight deals for this flight
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM FlightDeal WHERE flight_no = ?")) {
                pst.setString(1, flightNo);
                pst.executeUpdate();
            }

            // 3) Handle all bookings for this flight
            try (PreparedStatement getBookings = conn.prepareStatement(
                    "SELECT booking_id FROM Booking WHERE flight_no = ?")) {
                getBookings.setString(1, flightNo);
                ResultSet rsB = getBookings.executeQuery();
                while (rsB.next()) {
                    String bookingId = rsB.getString("booking_id");

                    // Feedback
                    try (PreparedStatement pst = conn.prepareStatement(
                            "DELETE FROM Feedback WHERE booking_id = ?")) {
                        pst.setString(1, bookingId);
                        pst.executeUpdate();
                    }

                    // Tickets
                    try (PreparedStatement pst = conn.prepareStatement(
                            "DELETE FROM Ticket WHERE booking_id = ?")) {
                        pst.setString(1, bookingId);
                        pst.executeUpdate();
                    }

                    // Passengers
                    try (PreparedStatement pst = conn.prepareStatement(
                            "DELETE FROM Passenger WHERE booking_id = ?")) {
                        pst.setString(1, bookingId);
                        pst.executeUpdate();
                    }

                    // Payments related to this booking
                    String paymentId = null;
                    try (PreparedStatement getPayment = conn.prepareStatement(
                            "SELECT payment_id FROM Payment WHERE booking_id = ?")) {
                        getPayment.setString(1, bookingId);
                        ResultSet rsP = getPayment.executeQuery();
                        if (rsP.next()) {
                            paymentId = rsP.getString("payment_id");
                        }
                    }

                    if (paymentId != null) {
                        // Receipts
                        try (PreparedStatement pst = conn.prepareStatement(
                                "DELETE FROM Receipt WHERE payment_id = ?")) {
                            pst.setString(1, paymentId);
                            pst.executeUpdate();
                        }

                        // Credit cards
                        try (PreparedStatement pst = conn.prepareStatement(
                                "DELETE FROM CreditCard WHERE payment_id = ?")) {
                            pst.setString(1, paymentId);
                            pst.executeUpdate();
                        }

                        // Payment
                        try (PreparedStatement pst = conn.prepareStatement(
                                "DELETE FROM Payment WHERE payment_id = ?")) {
                            pst.setString(1, paymentId);
                            pst.executeUpdate();
                        }
                    }

                    // Finally delete booking
                    try (PreparedStatement pst = conn.prepareStatement(
                            "DELETE FROM Booking WHERE booking_id = ?")) {
                        pst.setString(1, bookingId);
                        pst.executeUpdate();
                    }
                }
            }

            // 4) Delete the flight itself
            try (PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM Flight WHERE flight_no = ?")) {
                pst.setString(1, flightNo);
                pst.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this,
                    "Flight and all related data deleted successfully!");
            loadFlights();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting flight: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======================= HELPERS =======================

    private void clearFields() {
        flightNameField.setText("");
        departureField.setText("");
        destinationField.setText("");
        departureTimeField.setText("");
        arrivalTimeField.setText("");
        priceField.setText("");
        capacityField.setText("");
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];

        for (int column = 1; column <= columnCount; column++) {
            columnNames[column - 1] = metaData.getColumnName(column);
        }

        Vector<String[]> data = new Vector<>();
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
        new ManageFlights();
    }
}
