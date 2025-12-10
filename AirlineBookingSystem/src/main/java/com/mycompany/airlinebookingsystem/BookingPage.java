package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;

public class BookingPage extends JFrame {
    private JTable flightsTable;
    private DefaultTableModel tableModel;
    private JButton paymentButton, backButton;
    private JSpinner adultSpinner, childSpinner;
    private JPanel seatPanel, rightPanel;
    private JLabel totalPriceLabel;
    private JLabel discountLabel;          // shows discount info
    private List<JComboBox<String>> seatBoxes = new ArrayList<>();
    private List<JComboBox<String>> classBoxes = new ArrayList<>();
    private List<String> availableSeats = new ArrayList<>();
    private final String[] allSeats = generateSeatList();

    public static List<String> selectedSeats = new ArrayList<>();
    public static List<String> selectedClasses = new ArrayList<>();

    // effective base price (after discount if exists)
    private double effectiveBasePrice = 0.0;

    public BookingPage() {
        setTitle("ByteAir - Book Flight");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);

        try {
            setContentPane(new JLabel(new ImageIcon(
                    ImageIO.read(getClass().getResource("/images/TicketImage.jpg")))));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load background image.");
        }

        initComponents();
        loadFlights();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Book Your Flight");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setBounds(60, 30, 400, 40);
        add(titleLabel);

        flightsTable = new JTable();
        flightsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        flightsTable.setRowHeight(30);
        flightsTable.setToolTipText("Select a flight from the list below");
        JScrollPane scrollPane = new JScrollPane(flightsTable);
        scrollPane.setBounds(60, 90, 1100, 200);
        add(scrollPane);

        JLabel seatMessage = new JLabel("Choose your seat here — may you have a wonderful flight!");
        seatMessage.setFont(new Font("Segoe UI", Font.BOLD, 16));
        seatMessage.setBounds(60, 300, 700, 30);
        add(seatMessage);

        seatPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        seatPanel.setOpaque(false);
        seatPanel.setBounds(60, 340, 600, 250);
        add(seatPanel);

        rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setOpaque(false);
        rightPanel.setBounds(800, 340, 320, 280);
        add(rightPanel);

        JLabel adultLabel = createLabel("Adult Tickets:", 10, 10);
        adultSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 5, 1));
        adultSpinner.setBounds(160, 10, 60, 30);
        adultSpinner.setToolTipText("Select number of adult tickets (age 15+)");
        rightPanel.add(adultLabel);
        rightPanel.add(adultSpinner);

        JLabel childLabel = createLabel("Child Tickets (Age 1–14):", 10, 50);
        childSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        childSpinner.setBounds(240, 50, 60, 30);
        childSpinner.setToolTipText("Select number of child tickets (age 1–14)");
        rightPanel.add(childLabel);
        rightPanel.add(childSpinner);

        // === Total price on white strip ===
        totalPriceLabel = new JLabel("Total: 0.00 SAR");
        totalPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalPriceLabel.setBounds(10, 100, 280, 30);
        totalPriceLabel.setToolTipText("Total price including class add-ons and child discounts");
        totalPriceLabel.setOpaque(true);
        totalPriceLabel.setBackground(Color.WHITE);
        totalPriceLabel.setBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 255), 1, true)
        );
        rightPanel.add(totalPriceLabel);

        // === Discount info on white strip ===
        discountLabel = new JLabel("No active discount for this flight.");
        discountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        discountLabel.setBounds(10, 135, 280, 40);
        discountLabel.setVerticalAlignment(SwingConstants.TOP);
        discountLabel.setOpaque(true);
        discountLabel.setBackground(Color.WHITE);
        discountLabel.setBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 255), 1, true)
        );
        rightPanel.add(discountLabel);

        paymentButton = createButton("Proceed to Payment", 10, 190);
        paymentButton.setToolTipText("Click to proceed to payment");
        backButton = createButton("Back to Menu", 10, 235);
        backButton.setToolTipText("Return to the main menu");
        rightPanel.add(paymentButton);
        rightPanel.add(backButton);

        // Events
        adultSpinner.addChangeListener(e -> loadAvailableSeatsAndGenerateBoxes());
        childSpinner.addChangeListener(e -> loadAvailableSeatsAndGenerateBoxes());

        flightsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                loadAvailableSeatsAndGenerateBoxes();
            }
        });

        paymentButton.addActionListener(e -> proceedToPayment());
        backButton.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });

        setVisible(true);
    }

    private void loadFlights() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT flight_no, flight_name, from_location, destination, " +
                           "departure_time, arrival_time, price FROM Flight";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            tableModel = buildReadOnlyTableModel(rs);
            flightsTable.setModel(tableModel);

            // If coming from FlightDealsPage, select that flight
            if (Session.selectedFlightFromDeals != null) {
                String targetFlight = Session.selectedFlightFromDeals;
                boolean found = false;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Object value = tableModel.getValueAt(i, 0);
                    if (value != null && targetFlight.equals(value.toString())) {
                        flightsTable.setRowSelectionInterval(i, i);
                        flightsTable.scrollRectToVisible(
                                flightsTable.getCellRect(i, 0, true));
                        found = true;
                        break;
                    }
                }
                Session.selectedFlightFromDeals = null;
                if (found) {
                    loadAvailableSeatsAndGenerateBoxes();
                } else if (tableModel.getRowCount() > 0) {
                    flightsTable.setRowSelectionInterval(0, 0);
                    loadAvailableSeatsAndGenerateBoxes();
                }
            } else {
                if (tableModel.getRowCount() > 0) {
                    flightsTable.setRowSelectionInterval(0, 0);
                    loadAvailableSeatsAndGenerateBoxes();
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading flights.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAvailableSeatsAndGenerateBoxes() {
        seatPanel.removeAll();
        seatBoxes.clear();
        classBoxes.clear();
        availableSeats.clear();

        int selectedRow = flightsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a flight from the table above to see available seats.",
                    "No Flight Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String flightNo = tableModel.getValueAt(selectedRow, 0).toString();
        double originalBasePrice = Double.parseDouble(
                tableModel.getValueAt(selectedRow, 6).toString()
        );

        // 1) Load the active discount for this flight
        applyDiscountForFlight(flightNo, originalBasePrice);

        Set<String> bookedSeats = new HashSet<>();

        try (Connection conn = DatabaseConnection.getConnection()) {

            String query = "SELECT t.seat_no FROM Ticket t " +
                           "JOIN Booking b ON t.booking_id = b.booking_id " +
                           "WHERE b.flight_no = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, flightNo);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                bookedSeats.add(rs.getString("seat_no"));
            }

            for (String seat : allSeats) {
                if (!bookedSeats.contains(seat)) {
                    availableSeats.add(seat);
                }
            }

            int totalTickets = (int) adultSpinner.getValue() +
                               (int) childSpinner.getValue();

            if (totalTickets == 0) {
                JOptionPane.showMessageDialog(this,
                        "Please select at least one ticket (adult or child).",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (availableSeats.size() < totalTickets) {
                JOptionPane.showMessageDialog(this,
                        "Not enough seats available for this flight.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Dimension cardSize = new Dimension(320, 80);

            for (int i = 0; i < totalTickets; i++) {
                JComboBox<String> seatBox =
                        new JComboBox<>(availableSeats.toArray(new String[0]));
                JComboBox<String> classBox =
                        new JComboBox<>(new String[]{"Economy", "Business", "First Class"});

                seatBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                seatBox.setToolTipText("Select an available seat");

                classBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                classBox.setToolTipText("Select seat class (Economy, Business, First)");

                classBox.addActionListener(e -> updateTotalPrice());

                seatBoxes.add(seatBox);
                classBoxes.add(classBox);

                JLabel seatLabel = new JLabel("Seat " + (i + 1) + ":");
                seatLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                JPanel miniCard = new JPanel(new FlowLayout(FlowLayout.LEFT));
                miniCard.setPreferredSize(cardSize);
                miniCard.setMaximumSize(cardSize);
                miniCard.setMinimumSize(cardSize);
                miniCard.setOpaque(false);

                miniCard.add(seatLabel);
                miniCard.add(seatBox);
                miniCard.add(new JLabel("  Class: "));
                miniCard.add(classBox);

                seatPanel.add(miniCard);
            }

            seatPanel.revalidate();
            seatPanel.repaint();
            updateTotalPrice();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load seats.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reads FlightDeal for this flight and sets:
     *   - effectiveBasePrice
     *   - discountLabel text
     */
    private void applyDiscountForFlight(String flightNo, double originalBasePrice) {
        effectiveBasePrice = originalBasePrice; // default no discount
        discountLabel.setText("No active discount for this flight.");

        try (Connection conn = DatabaseConnection.getConnection()) {

            String sql =
                "SELECT title, discount_type, discount_value " +
                "FROM FlightDeal " +
                "WHERE flight_no = ? AND is_active = 1 " +
                "AND CURDATE() BETWEEN start_date AND end_date " +
                "ORDER BY discount_value DESC LIMIT 1";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, flightNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String title = rs.getString("title");
                String type = rs.getString("discount_type");
                double value = rs.getDouble("discount_value");

                double newPrice;
                String discountText;

                if ("PERCENT".equalsIgnoreCase(type)) {
                    newPrice = originalBasePrice * (1.0 - (value / 100.0));
                    discountText = String.format("🎉 %.0f%% OFF", value);
                } else { // FIXED
                    newPrice = Math.max(0, originalBasePrice - value);
                    discountText = String.format("🎉 %.2f SAR OFF", value);
                }

                effectiveBasePrice = newPrice;

                String titlePart = (title != null && !title.isEmpty())
                        ? " - " + title : "";

                discountLabel.setText(
                        String.format(
                            "<html>%s%s<br/>Base: %.2f → <b>%.2f</b> SAR</html>",
                            discountText, titlePart,
                            originalBasePrice, newPrice
                        )
                );
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            discountLabel.setText("Discount info unavailable (using normal price).");
            effectiveBasePrice = originalBasePrice;
        }
    }

    private void updateTotalPrice() {
        if (flightsTable.getSelectedRow() == -1) return;

        // use discounted base price if set
        double basePrice = effectiveBasePrice;
        if (basePrice <= 0) {
            basePrice = Double.parseDouble(
                    tableModel.getValueAt(flightsTable.getSelectedRow(), 6).toString()
            );
        }

        int adultCount = (int) adultSpinner.getValue();
        double total = 0.0;

        for (int i = 0; i < seatBoxes.size(); i++) {
            boolean isChild = i >= adultCount;
            String seatClass = (String) classBoxes.get(i).getSelectedItem();

            double addon = 0.0;
            if ("Business".equals(seatClass)) {
                addon = 100.0;
            } else if ("First Class".equals(seatClass)) {
                addon = 250.0;
            }

            double price = basePrice + addon;
            if (isChild) {
                price *= 0.5;
            }
            total += price;
        }

        totalPriceLabel.setText("Total: " + String.format("%.2f", total) + " SAR");
    }

    private void proceedToPayment() {
        if (flightsTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a flight.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedSeats.clear();
        selectedClasses.clear();
        Set<String> uniqueCheck = new HashSet<>();

        for (int i = 0; i < seatBoxes.size(); i++) {
            String seat = (String) seatBoxes.get(i).getSelectedItem();
            String seatClass = (String) classBoxes.get(i).getSelectedItem();

            if (seat == null || seat.trim().isEmpty() || seatClass == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select all seats and their classes.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String seatCombo = seat + "-" + seatClass;
            if (uniqueCheck.contains(seatCombo)) {
                JOptionPane.showMessageDialog(this,
                        "Duplicate seat and class selected: " + seat + " (" + seatClass + ")",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            uniqueCheck.add(seatCombo);
            selectedSeats.add(seat);
            selectedClasses.add(seatClass);
        }

        int row = flightsTable.getSelectedRow();
        String flightNo = tableModel.getValueAt(row, 0).toString();
        String basePrice = tableModel.getValueAt(row, 6).toString(); // original base (for info)

        String bookingId = null;

        try (Connection conn = DatabaseConnection.getConnection()) {

            // ✅ generate sequential booking ID: B-K1, B-K2, ... B-K8, B-K9, ...
            bookingId = generateNextBookingId(conn);

            String insertBooking = "INSERT INTO Booking " +
                    "(booking_id, customer_id, flight_no, booking_date, status) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(insertBooking);
            stmt.setString(1, bookingId);
            stmt.setString(2, Session.currentUserId);
            stmt.setString(3, flightNo);
            stmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            stmt.setString(5, "Confirmed");
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Booking created successfully! Redirecting to payment...\nBooking ID: " + bookingId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to create booking: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String totalAmount = totalPriceLabel.getText()
                .replace("Total: ", "")
                .replace(" SAR", "");

        new PaymentPage(
                flightNo,
                totalAmount,
                String.join(", ", selectedSeats),
                String.join(", ", selectedClasses),
                basePrice,
                bookingId
        ).setVisible(true);

        dispose();
    }

    /**
     * Generate the next booking_id in sequence: B-K1, B-K2, ..., B-K8, B-K9, ...
     * It reads the max numeric part from existing Booking rows.
     */
    private String generateNextBookingId(Connection conn) throws SQLException {
        String sql =
                "SELECT CAST(SUBSTRING(booking_id, 4) AS UNSIGNED) AS num " +
                "FROM Booking " +
                "WHERE booking_id LIKE 'B-K%' " +
                "ORDER BY num DESC " +
                "LIMIT 1";

        int nextNumber = 1;  // default if there is no B-K* yet

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                int lastNum = rs.getInt("num");   // highest existing number
                nextNumber = lastNum + 1;
            }
        }

        return "B-K" + nextNumber;
    }

    private DefaultTableModel buildReadOnlyTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }

        Vector<String[]> data = new Vector<>();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getString(i);
            }
            data.add(row);
        }

        return new DefaultTableModel(data.toArray(new String[0][]), columnNames) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JLabel createLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setBounds(x, y, 240, 30);
        return label;
    }

    private JButton createButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBounds(x, y, 200, 40);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(210, 225, 250));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    private static String[] generateSeatList() {
        String[] rows = {"A", "B", "C", "D"};
        java.util.List<String> seats = new ArrayList<>();
        for (String row : rows) {
            for (int i = 1; i <= 10; i++) {
                seats.add(row + i);
            }
        }
        return seats.toArray(new String[0]);
    }

    public static void main(String[] args) {
        new BookingPage();
    }
}
