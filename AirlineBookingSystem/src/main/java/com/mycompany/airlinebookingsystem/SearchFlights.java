package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class SearchFlights extends JFrame {

    private JTable flightsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> fromComboBox, toComboBox, timeFilterComboBox;
    private JDateChooser dateChooser;
    private JTextField minPriceField, maxPriceField, seatsField;
    private JButton searchButton, clearButton, backButton;
    private JLabel noResultsLabel;

    public SearchFlights() {
        setTitle("ByteAir - Search Flights");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(230, 240, 255));
        setLayout(null);

        initComponents();
        populateCityComboBoxes();
        setVisible(true);
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Search Flights");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setBounds(450, 20, 400, 50);
        add(titleLabel);

        
        int labelX = 100, inputX = 250, y = 100, spacing = 50;

        JLabel fromLabel = new JLabel("From:");
        fromLabel.setBounds(labelX, y, 100, 30);
        fromLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(fromLabel);

        fromComboBox = new JComboBox<>();
        fromComboBox.setBounds(inputX, y, 200, 30);
        fromComboBox.setToolTipText("Select departure city");
        add(fromComboBox);

        y += spacing;

        JLabel toLabel = new JLabel("To:");
        toLabel.setBounds(labelX, y, 100, 30);
        toLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(toLabel);

        toComboBox = new JComboBox<>();
        toComboBox.setBounds(inputX, y, 200, 30);
        toComboBox.setToolTipText("Select destination city");
        add(toComboBox);

        y += spacing;

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setBounds(labelX, y, 100, 30);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(dateLabel);

        dateChooser = new JDateChooser();
        dateChooser.setBounds(inputX, y, 200, 30);
        dateChooser.setToolTipText("Select flight date");
        add(dateChooser);

        y += spacing;

        JLabel priceLabel = new JLabel("Price Range:");
        priceLabel.setBounds(labelX, y, 120, 30);
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(priceLabel);

        minPriceField = new JTextField();
        minPriceField.setBounds(inputX, y, 90, 30);
        minPriceField.setToolTipText("Minimum price");
        add(minPriceField);

        maxPriceField = new JTextField();
        maxPriceField.setBounds(inputX + 110, y, 90, 30);
        maxPriceField.setToolTipText("Maximum price");
        add(maxPriceField);

        y += spacing;

        JLabel seatsLabel = new JLabel("Min Seats:");
        seatsLabel.setBounds(labelX, y, 100, 30);
        seatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(seatsLabel);

        seatsField = new JTextField();
        seatsField.setBounds(inputX, y, 200, 30);
        seatsField.setToolTipText("Minimum available seats");
        add(seatsField);

        y += spacing;

        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setBounds(labelX, y, 100, 30);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(timeLabel);

        timeFilterComboBox = new JComboBox<>(new String[]{"Any", "Morning (6 AM - 12 PM)", "Evening (6 PM - 12 AM)"});
        timeFilterComboBox.setBounds(inputX, y, 200, 30);
        timeFilterComboBox.setToolTipText("Filter by time of flight");
        add(timeFilterComboBox);

        
        searchButton = createButton("Search", inputX, y + 60);
        searchButton.setToolTipText("Search flights based on selected filters");
        searchButton.addActionListener(e -> searchFlights());
        add(searchButton);

        clearButton = createButton("Clear Search", inputX + 160, y + 60);
        clearButton.setToolTipText("Clear all search fields");
        clearButton.addActionListener(e -> clearSearchFields());
        add(clearButton);

        backButton = createButton("Back to Menu", inputX + 320, y + 60);
        backButton.setToolTipText("Return to main menu");
        backButton.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });
        add(backButton);

        
        flightsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(flightsTable);
        scrollPane.setBounds(100, y + 120, 1000, 400);
        add(scrollPane);

        
        noResultsLabel = new JLabel("No flights found. Try changing your filters.");
        noResultsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        noResultsLabel.setForeground(Color.RED);
        noResultsLabel.setBounds(100, y + 480, 500, 30);
        noResultsLabel.setVisible(false);
        add(noResultsLabel);
    }

    private JButton createButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 140, 40);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230), 2));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(210, 225, 255));
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    private void populateCityComboBoxes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT from_location, destination FROM Flights");

            while (rs.next()) {
                String from = rs.getString("from_location");
                String to = rs.getString("destination");

                if (((DefaultComboBoxModel<String>) fromComboBox.getModel()).getIndexOf(from) == -1)
                    fromComboBox.addItem(from);

                if (((DefaultComboBoxModel<String>) toComboBox.getModel()).getIndexOf(to) == -1)
                    toComboBox.addItem(to);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading cities from database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchFlights() {
        noResultsLabel.setVisible(false);

        String from = (String) fromComboBox.getSelectedItem();
        String to = (String) toComboBox.getSelectedItem();
        java.util.Date date = dateChooser.getDate();
        String minPrice = minPriceField.getText().trim();
        String maxPrice = maxPriceField.getText().trim();
        String minSeats = seatsField.getText().trim();
        String timeFilter = (String) timeFilterComboBox.getSelectedItem();

        if (from == null || to == null || date == null || minPrice.isEmpty() || maxPrice.isEmpty() || minSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields before searching.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String timeCondition = "";
            if (timeFilter.contains("Morning")) {
                timeCondition = "AND TIME(departure_time) BETWEEN '06:00:00' AND '12:00:00'";
            } else if (timeFilter.contains("Evening")) {
                timeCondition = "AND TIME(departure_time) BETWEEN '18:00:00' AND '23:59:59'";
            }

            String sql = "SELECT * FROM Flights WHERE from_location=? AND destination=? AND flight_date=? " +
                    "AND price BETWEEN ? AND ? AND capacity >= ? " +
                    timeCondition + " AND flight_status='Scheduled'";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, from);
            pst.setString(2, to);
            pst.setDate(3, new java.sql.Date(date.getTime()));
            pst.setDouble(4, Double.parseDouble(minPrice));
            pst.setDouble(5, Double.parseDouble(maxPrice));
            pst.setInt(6, Integer.parseInt(minSeats));

            ResultSet rs = pst.executeQuery();
            tableModel = buildTableModel(rs);
            flightsTable.setModel(tableModel);

            if (tableModel.getRowCount() == 0) {
                noResultsLabel.setVisible(true);
            }

        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while searching for flights.", "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearSearchFields() {
        fromComboBox.setSelectedIndex(0);
        toComboBox.setSelectedIndex(0);
        dateChooser.setDate(null);
        minPriceField.setText("");
        maxPriceField.setText("");
        seatsField.setText("");
        timeFilterComboBox.setSelectedIndex(0);
        flightsTable.setModel(new DefaultTableModel());
        noResultsLabel.setVisible(false);
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        Vector<String> columnNames = new Vector<>();
        int columnCount = meta.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(meta.getColumnName(i));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            data.add(row);
        }

        return new DefaultTableModel(data, columnNames);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SearchFlights::new);
    }
}
