package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class PaymentPage extends JFrame {

    private String flightNo;
    private String basePrice;
    private String bookingId;

    private BigDecimal totalAmount;

    private JTextField cardNameField, cardNumberField, expiryField, cvvField;
    private JButton payButton, backButton;
    private JLabel errorLabel;

    public PaymentPage(
            String flightNo, String totalAmount, String seatNumbers,
            String classes, String basePrice, String bookingId) {

        this.flightNo = flightNo;
        this.basePrice = basePrice;
        this.bookingId = bookingId;

        try {
            this.totalAmount = new BigDecimal(totalAmount);
        } catch (Exception e) {
            this.totalAmount = BigDecimal.ZERO;
        }

        setTitle("ByteAir - Payment");
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // FULL SCREEN
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(230, 240, 255));
        setLayout(null);

        initComponents();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {

        JLabel title = new JLabel("Complete Your Payment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setBounds(500, 40, 500, 40);
        add(title);

        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(Color.WHITE);
        card.setBounds(300, 120, 800, 500);
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 255), 3, true));
        add(card);

        JLabel summaryLabel = new JLabel(
                "<html><b>Flight:</b> " + flightNo +
                        "<br><b>Seats:</b> " + String.join(", ", BookingPage.selectedSeats) +
                        "<br><b>Classes:</b> " + String.join(", ", BookingPage.selectedClasses) +
                        "<br><b>Total:</b> " + totalAmount.toPlainString() + " SAR" +
                        "</html>"
        );
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        summaryLabel.setBounds(40, 20, 700, 90);
        card.add(summaryLabel);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 17);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);

        int xLabel = 40;
        int xField = 260;
        int y = 140;
        int gap = 50;

        JLabel nameLabel = new JLabel("Name on Card:");
        nameLabel.setFont(labelFont);
        nameLabel.setBounds(xLabel, y, 200, 25);
        card.add(nameLabel);

        cardNameField = new JTextField();
        cardNameField.setFont(fieldFont);
        cardNameField.setBounds(xField, y, 380, 32);
        cardNameField.setToolTipText("Enter the card holder full name.");
        card.add(cardNameField);
        y += gap;

        JLabel numberLabel = new JLabel("Card Number:");
        numberLabel.setFont(labelFont);
        numberLabel.setBounds(xLabel, y, 200, 25);
        card.add(numberLabel);

        cardNumberField = new JTextField();
        cardNumberField.setFont(fieldFont);
        cardNumberField.setBounds(xField, y, 380, 32);
        cardNumberField.setToolTipText("Enter 16 digits card number (numbers only).");
        card.add(cardNumberField);
        y += gap;

        JLabel expiryLabel = new JLabel("Expiry Date (YYYY-MM-DD):");
        expiryLabel.setFont(labelFont);
        expiryLabel.setBounds(xLabel, y, 240, 25);
        card.add(expiryLabel);

        expiryField = new JTextField();
        expiryField.setFont(fieldFont);
        expiryField.setBounds(xField, y, 380, 32);
        expiryField.setToolTipText("Example: 2028-12-01");
        card.add(expiryField);
        y += gap;

        JLabel cvvLabel = new JLabel("CVV:");
        cvvLabel.setFont(labelFont);
        cvvLabel.setBounds(xLabel, y, 200, 25);
        card.add(cvvLabel);

        cvvField = new JTextField();
        cvvField.setFont(fieldFont);
        cvvField.setBounds(xField, y, 120, 32);
        cvvField.setToolTipText("Enter 3 digits CVV.");
        card.add(cvvField);
        y += gap;

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        errorLabel.setForeground(Color.RED);
        errorLabel.setBounds(xLabel, y, 700, 25);
        card.add(errorLabel);

        int buttonsY = y + 50;

        payButton = createButton("Confirm Payment", xLabel + 80, buttonsY);
        backButton = createButton("Back", xLabel + 300, buttonsY);

        card.add(payButton);
        card.add(backButton);

        payButton.addActionListener(e -> processPayment());
        backButton.addActionListener(e -> {
            new BookingPage().setVisible(true);
            dispose();
        });
    }

    private JButton createButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        button.setBounds(x, y, 200, 40);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2, true));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(210, 225, 250)); }
            public void mouseExited(MouseEvent e) { button.setBackground(Color.WHITE); }
        });

        return button;
    }

    // -------------------------------------------------------------------------
    // FIXED SEQUENTIAL PAYMENT ID → PAY001, PAY002, PAY003, ...
    // -------------------------------------------------------------------------
    private String generateNextPaymentId(Connection conn) throws SQLException {

        String sql =
                "SELECT CAST(SUBSTRING(payment_id, 4) AS UNSIGNED) AS num " +
                        "FROM Payment " +
                        "WHERE payment_id LIKE 'PAY___' " +   // exactly 3 digits
                        "ORDER BY num DESC LIMIT 1";

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

        return String.format("PAY%03d", next);
    }

    // -------------------------------------------------------------------------
    // MAIN PAYMENT PROCESS LOGIC
    // -------------------------------------------------------------------------
    private void processPayment() {

        errorLabel.setText(" ");

        // ✅ Required: logged in
        if (Session.currentUserId == null || Session.currentUserId.trim().isEmpty()) {
            errorLabel.setText("You must be logged in to pay.");
            JOptionPane.showMessageDialog(this,
                    "Please log in first.",
                    "Not Logged In",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ Required: booking id exists (so we can link feedback later)
        if (bookingId == null || bookingId.trim().isEmpty()) {
            errorLabel.setText("Booking ID is missing. Please go back and select a flight again.");
            JOptionPane.showMessageDialog(this,
                    "Booking ID is missing.\nGo back and try again.",
                    "Missing Booking ID",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cardName = cardNameField.getText().trim();
        String cardNumber = cardNumberField.getText().trim();
        String expiry = expiryField.getText().trim();
        String cvv = cvvField.getText().trim();

        if (cardName.isEmpty() || cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
            errorLabel.setText("Please fill in all payment fields.");
            return;
        }

        if (!cardNumber.matches("\\d{16}")) {
            errorLabel.setText("Card number must be exactly 16 digits.");
            return;
        }

        if (!cvv.matches("\\d{3}")) {
            errorLabel.setText("CVV must be exactly 3 digits.");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date expiryDate = sdf.parse(expiry);
            if (expiryDate.before(new Date())) {
                errorLabel.setText("Expiry date cannot be in the past.");
                return;
            }
        } catch (Exception ex) {
            errorLabel.setText("Invalid expiry date format.");
            return;
        }

        // ---------------------------- DATABASE ----------------------------
        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn == null) {
                errorLabel.setText("Database connection failed.");
                return;
            }

            // ✅ Make it atomic
            conn.setAutoCommit(false);

            String paymentId = generateNextPaymentId(conn);

            String insertPayment = "INSERT INTO Payment " +
                    "(payment_id, amount, payment_date, customer_id, booking_id, payment_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pst = conn.prepareStatement(insertPayment)) {
                pst.setString(1, paymentId);
                pst.setBigDecimal(2, totalAmount);
                pst.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                pst.setString(4, Session.currentUserId);
                pst.setString(5, bookingId);
                pst.setString(6, "CreditCard");
                pst.executeUpdate();
            }

            // ------------------ CREATE TICKETS ------------------
            for (int i = 0; i < BookingPage.selectedSeats.size(); i++) {
                String ticketId = generateNextTicketId(conn);
                String ticketCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                String sql = "INSERT INTO Ticket " +
                        "(ticket_id, booking_id, seat_no, class, issue_date, ticket_code, boarding_gate) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement t = conn.prepareStatement(sql)) {
                    t.setString(1, ticketId);
                    t.setString(2, bookingId);
                    t.setString(3, BookingPage.selectedSeats.get(i));
                    t.setString(4, convertClassName(BookingPage.selectedClasses.get(i)));
                    t.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                    t.setString(6, ticketCode);
                    t.setString(7, "G1");
                    t.executeUpdate();
                }
            }

            String cardSQL = "INSERT INTO CreditCard (card_number, card_holder, expiry_date, cvv, payment_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement cardStmt = conn.prepareStatement(cardSQL)) {
                cardStmt.setString(1, cardNumber);
                cardStmt.setString(2, cardName);
                cardStmt.setDate(3, java.sql.Date.valueOf(expiry));
                cardStmt.setString(4, cvv);
                cardStmt.setString(5, paymentId);
                cardStmt.executeUpdate();
            }

            String receiptSQL = "INSERT INTO Receipt (payment_id, total_amount) VALUES (?, ?)";

            try (PreparedStatement rec = conn.prepareStatement(receiptSQL)) {
                rec.setString(1, paymentId);
                rec.setBigDecimal(2, totalAmount);
                rec.executeUpdate();
            }

            conn.commit();

            // Show receipt
            JOptionPane.showMessageDialog(this,
                    "Payment successful!\nPayment ID: " + paymentId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // ✅ FIX: pass bookingId to FeedbackPage so booking_id is saved
            new FeedbackPage(bookingId).setVisible(true);
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Payment failed. Please try again.");

            JOptionPane.showMessageDialog(this,
                    "Payment failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- Ticket ID generator (TKT100, TKT101, ...) ----
    private String generateNextTicketId(Connection conn) throws SQLException {
        String sql =
                "SELECT CAST(SUBSTRING(ticket_id, 4) AS UNSIGNED) AS num " +
                        "FROM Ticket WHERE ticket_id LIKE 'TKT%' " +
                        "ORDER BY num DESC LIMIT 1";

        int next = 100;
        try (PreparedStatement p = conn.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {

            if (rs.next()) next = rs.getInt("num") + 1;
        }
        return "TKT" + next;
    }

    private String convertClassName(String name) {
        return switch (name) {
            case "Business" -> "Business";
            case "First Class" -> "First";
            default -> "Economy";
        };
    }
}
