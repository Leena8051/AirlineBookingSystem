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

    //  Deals section fields 
    private JTextField dealTitleField;
    private JTextArea dealDescriptionArea;
    private JComboBox<String> dealTypeCombo;
    private JTextField dealValueField;
    private JTextField dealStartDateField;
    private JTextField dealEndDateField;
    private JButton createDealButton;

    public ManageFlights() {
        initComponents();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        loadFlights();
    }

    private void initComponents() {
        setTitle("Manage Flights");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            setContentPane(new JLabel(new ImageIcon(
                    getClass().getResource("/images/AirPlaneSky.jpg"))));
        } catch (Exception e) {
            // fallback background color
            getContentPane().setBackground(new Color(200, 220, 255));
        }
        getContentPane().setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Manage Flights", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        // table
        flightsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(flightsTable);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // form (bottom)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
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

        // buttons
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

        leftPanel.add(formPanel, BorderLayout.SOUTH);

        //  RIGHT SIDE: deals panel 
        JPanel dealsPanel = buildDealsPanel();

        //  SPLIT PANE: left & right 
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, dealsPanel);
        splitPane.setResizeWeight(0.75);         // 75% width for left side, 25% for right
        splitPane.setDividerSize(5);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        //  events 
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
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(label, gbc);

        gbc.gridx = 1;
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textField.setPreferredSize(new Dimension(260, 28));
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

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(220, 230, 255)); }
            public void mouseExited(MouseEvent e)  { button.setBackground(Color.WHITE); }
        });

        return button;
    }

    //  Deals panel builder 
    private JPanel buildDealsPanel() {
        JPanel dealsPanel = new JPanel(new GridBagLayout());
        dealsPanel.setOpaque(false);
        dealsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        dealsPanel.setPreferredSize(new Dimension(360, 0)); // width for right side

        GridBagConstraints dgc = new GridBagConstraints();
        dgc.insets = new Insets(8, 8, 8, 8);
        dgc.fill = GridBagConstraints.HORIZONTAL;
        dgc.weightx = 1.0;
        dgc.gridx = 0;
        dgc.gridy = 0;

        JLabel sectionTitle = new JLabel("Flight Deals");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(Color.WHITE);
        dgc.gridwidth = 2;
        dealsPanel.add(sectionTitle, dgc);

        dgc.gridwidth = 2;
        dgc.gridy++;
        JLabel info = new JLabel("<html>Select a flight in the table,<br>then create a deal for it.</html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setForeground(Color.WHITE);
        dealsPanel.add(info, dgc);

        dgc.gridwidth = 1;

        // Deal Title
        dgc.gridy++;
        dgc.gridx = 0;
        JLabel titleLabel = new JLabel("Deal Title:");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dealsPanel.add(titleLabel, dgc);

        dgc.gridx = 1;
        dealTitleField = new JTextField();
        dealTitleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dealsPanel.add(dealTitleField, dgc);

        // Description
        dgc.gridy++;
        dgc.gridx = 0;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setForeground(Color.WHITE);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dealsPanel.add(descLabel, dgc);

        dgc.gridx = 1;
        dealDescriptionArea = new JTextArea(3, 16);
        dealDescriptionArea.setLineWrap(true);
        dealDescriptionArea.setWrapStyleWord(true);
        dealDescriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane descScroll = new JScrollPane(dealDescriptionArea);
        dealsPanel.add(descScroll, dgc);

        // Discount Type
        dgc.gridy++;
        dgc.gridx = 0;
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setForeground(Color.WHITE);
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dealsPanel.add(typeLabel, dgc);

        dgc.gridx = 1;
        dealTypeCombo = new JComboBox<>(new String[]{"PERCENT", "FIXED"});
        dealTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dealsPanel.add(dealTypeCombo, dgc);

        // Discount Value
        dgc.gridy++;
        dgc.gridx = 0;
        JLabel valueLabel = new JLabel("Value:");
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dealsPanel.add(valueLabel, dgc);

        dgc.gridx = 1;
        dealValueField = new JTextField();
        dealValueField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dealsPanel.add(dealValueField, dgc);

        // Start Date
        dgc.gridy++;
        dgc.gridx = 0;
        JLabel startLabel = new JLabel("Start (YYYY-MM-DD):");
        startLabel.setForeground(Color.WHITE);
        startLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dealsPanel.add(startLabel, dgc);

        dgc.gridx = 1;
        dealStartDateField = new JTextField();
        dealStartDateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dealsPanel.add(dealStartDateField, dgc);

        // End Date
        dgc.gridy++;
        dgc.gridx = 0;
        JLabel endLabel = new JLabel("End (YYYY-MM-DD):");
        endLabel.setForeground(Color.WHITE);
        endLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dealsPanel.add(endLabel, dgc);

        dgc.gridx = 1;
        dealEndDateField = new JTextField();
        dealEndDateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dealsPanel.add(dealEndDateField, dgc);

        // Button
        dgc.gridy++;
        dgc.gridx = 0;
        dgc.gridwidth = 2;
        createDealButton = new JButton("Create Deal for Flight");
        createDealButton.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        createDealButton.setBackground(Color.WHITE);
        createDealButton.setForeground(Color.BLACK);
        createDealButton.setFocusPainted(false);
        createDealButton.setBorder(BorderFactory.createLineBorder(new Color(150, 180, 255), 2, true));
        createDealButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { createDealButton.setBackground(new Color(220, 230, 255)); }
            public void mouseExited(MouseEvent e)  { createDealButton.setBackground(Color.WHITE); }
        });
        dealsPanel.add(createDealButton, dgc);

        createDealButton.addActionListener(e -> createDealForSelectedFlight());

        return dealsPanel;
    }

    //  LOAD FLIGHTS 

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

    //  ADD FLIGHT 

    private void addFlight() {
        if (!validateFields()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String flightNo = generateNextFlightNo(conn); // FL107, FL108, ...

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
            pst.setString(9, departureTimeField.getText().substring(0, 10)); // flight_date

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

    private String generateNextFlightNo(Connection conn) throws SQLException {
        String sql =
                "SELECT MAX(CAST(SUBSTRING(flight_no, 3) AS UNSIGNED)) AS num " +
                "FROM Flight WHERE flight_no LIKE 'FL%'";
        int start = 100; // first flight if none

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

    //  UPDATE FLIGHT 

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

    //  DELETE FLIGHT 

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

    //  DEALS: DB HELPERS 

    private String generateNextDealId(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(deal_id, 6) AS UNSIGNED)) AS max_num FROM FlightDeal";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            int next = 1;
            if (rs.next()) {
                int max = rs.getInt("max_num");
                if (!rs.wasNull()) {
                    next = max + 1;
                }
            }
            return String.format("DEAL-%03d", next);
        }
    }

    private void createDealForSelectedFlight() {
        int selectedRow = flightsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a flight from the table first.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String flightNo = tableModel.getValueAt(selectedRow, 0).toString();
        String title = dealTitleField.getText().trim();
        String description = dealDescriptionArea.getText().trim();
        String type = (String) dealTypeCombo.getSelectedItem();
        String valueStr = dealValueField.getText().trim();
        String start = dealStartDateField.getText().trim();
        String end = dealEndDateField.getText().trim();

        if (title.isEmpty() || valueStr.isEmpty() || start.isEmpty() || end.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Title, Discount Value, Start Date, and End Date are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Discount value must be numeric.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String dealId = generateNextDealId(conn);

            String sql = "INSERT INTO FlightDeal " +
                    "(deal_id, flight_no, title, description, discount_type, " +
                    " discount_value, start_date, end_date, is_active) " +
                    "VALUES (?,?,?,?,?,?,?,?,1)";

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, dealId);
                pst.setString(2, flightNo);
                pst.setString(3, title);
                pst.setString(4, description);
                pst.setString(5, type);
                pst.setDouble(6, value);
                pst.setString(7, start);
                pst.setString(8, end);

                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Deal " + dealId + " created for flight " + flightNo + ".");
            clearDealFields();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error creating deal: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearDealFields() {
        dealTitleField.setText("");
        dealDescriptionArea.setText("");
        dealValueField.setText("");
        dealStartDateField.setText("");
        dealEndDateField.setText("");
        dealTypeCombo.setSelectedIndex(0);
    }

 

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
