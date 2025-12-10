package com.mycompany.airlinebookingsystem;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TourBookingPage extends JFrame {

    private String guideId, guideName, city, pricePerDay;

    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;

    private JButton confirmButton, backButton;
    private JLabel errorLabel;

    public TourBookingPage(String guideId, String guideName, String city, String pricePerDay) {
        this.guideId = guideId;
        this.guideName = guideName;
        this.city = city;
        this.pricePerDay = pricePerDay;

        setTitle("ByteAir - Book Tour");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(230, 240, 255));
        setLayout(null);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Book Tour with " + guideName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setBounds(40, 20, 600, 35);
        add(titleLabel);

        JLabel cityLabel = new JLabel("City: " + city);
        cityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cityLabel.setBounds(40, 60, 300, 25);
        add(cityLabel);

        JLabel priceLabel = new JLabel("Price per day: " + pricePerDay + " SAR");
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        priceLabel.setBounds(40, 90, 300, 25);
        add(priceLabel);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);

        // ---------------------------
        // START DATE (JDateChooser)
        // ---------------------------
        JLabel startLabel = new JLabel("Start Date:");
        startLabel.setFont(labelFont);
        startLabel.setBounds(40, 150, 120, 25);
        add(startLabel);

        startDateChooser = new JDateChooser();
        startDateChooser.setBounds(160, 150, 220, 30);
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        startDateChooser.setToolTipText("Select the start date (today or later)");
        add(startDateChooser);

        // ---------------------------
        // END DATE (JDateChooser)
        // ---------------------------
        JLabel endLabel = new JLabel("End Date:");
        endLabel.setFont(labelFont);
        endLabel.setBounds(40, 200, 120, 25);
        add(endLabel);

        endDateChooser = new JDateChooser();
        endDateChooser.setBounds(160, 200, 220, 30);
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        endDateChooser.setToolTipText("Select the end date (same or after start date)");
        add(endDateChooser);

        // Prevent selecting past dates
        Date today = new Date();
        startDateChooser.setMinSelectableDate(today);
        endDateChooser.setMinSelectableDate(today);

        // If startDate changes, update min for endDate
        startDateChooser.addPropertyChangeListener("date", evt -> {
            Date start = startDateChooser.getDate();
            if (start != null) {
                endDateChooser.setMinSelectableDate(start.after(today) ? start : today);
            } else {
                endDateChooser.setMinSelectableDate(today);
            }
        });

        // Error label
        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorLabel.setForeground(Color.RED);
        errorLabel.setBounds(40, 250, 600, 25);
        add(errorLabel);

        // Buttons
        confirmButton = new JButton("Confirm Booking");
        confirmButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        confirmButton.setBounds(40, 300, 200, 40);
        styleMainButton(confirmButton);
        add(confirmButton);

        backButton = new JButton("Back");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        backButton.setBounds(260, 300, 120, 40);
        styleMainButton(backButton);
        add(backButton);

        confirmButton.setToolTipText("Confirm and save your tour booking.");
        backButton.setToolTipText("Return to the Tour Guides page.");

        confirmButton.addActionListener(e -> handleBooking());
        backButton.addActionListener(e -> {
            new TourGuidePage().setVisible(true);
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
            public void mouseExited(MouseEvent e)  { btn.setBackground(Color.WHITE); }
        });
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // ======================================================
    //  AUTO ID: TB-001, TB-002, TB-003, ...
    // ======================================================
    private String generateNextTourBookingId(Connection conn) throws SQLException {
        String sql =
            "SELECT MAX(CAST(SUBSTRING(tour_booking_id, 4) AS UNSIGNED)) AS num " +
            "FROM TourBooking WHERE tour_booking_id LIKE 'TB-%'";

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

        // TB-001, TB-002, TB-010, ...
        return String.format("TB-%03d", next);
    }

    // ======================================================
    //  HANDLE BOOKING + INSERT INTO DB
    // ======================================================
    private void handleBooking() {
        errorLabel.setText("");

        Date startRaw = startDateChooser.getDate();
        Date endRaw   = endDateChooser.getDate();

        if (startRaw == null || endRaw == null) {
            errorLabel.setText("Please select both start and end dates.");
            return;
        }

        LocalDate start = toLocalDate(startRaw);
        LocalDate end   = toLocalDate(endRaw);
        LocalDate today = LocalDate.now();

        // 1) No past start date
        if (start.isBefore(today)) {
            errorLabel.setText("Start date cannot be in the past.");
            return;
        }

        // 2) end ≥ start
        if (end.isBefore(start)) {
            errorLabel.setText("End date cannot be before start date.");
            return;
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        if (days <= 0) {
            errorLabel.setText("Booking length must be at least 1 day.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(pricePerDay);
        } catch (NumberFormatException ex) {
            errorLabel.setText("Internal error: invalid guide price.");
            return;
        }

        double totalPrice = days * price;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Book for " + days + " day(s) with total: " + totalPrice + " SAR?",
                "Confirm Booking",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // 3) Check that user is logged in (no role check)
        String customerId = Session.currentUserId;
        if (customerId == null) {
            JOptionPane.showMessageDialog(this,
                    "You must be logged in to book a tour.",
                    "Not Logged In",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4) Save to DB
        try (Connection conn = DatabaseConnection.getConnection()) {

            String tourBookingId = generateNextTourBookingId(conn);

            String sql = "INSERT INTO TourBooking " +
                    "(tour_booking_id, customer_id, guide_id, start_date, end_date, total_price, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, tourBookingId);
            pst.setString(2, customerId);
            pst.setString(3, guideId);
            pst.setDate(4, java.sql.Date.valueOf(start));
            pst.setDate(5, java.sql.Date.valueOf(end));
            pst.setDouble(6, totalPrice);
            pst.setString(7, "Pending");

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Tour booked successfully!\nBooking ID: " + tourBookingId,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            new MainMenu().setVisible(true);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            errorLabel.setText("Database error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // For standalone testing
        SwingUtilities.invokeLater(() ->
                new TourBookingPage("TG-1", "Test Guide", "Riyadh", "350.00"));
    }
}
