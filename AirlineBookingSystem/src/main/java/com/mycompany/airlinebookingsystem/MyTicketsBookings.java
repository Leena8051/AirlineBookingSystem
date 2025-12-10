package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.imageio.ImageIO;

public class MyTicketsBookings extends JFrame {

    private JTable ticketsTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel, reminderLabel, errorLabel;
    private JTextField searchField, dateField;
    private JButton searchButton, filterDateButton, refreshButton, backButton, cancelBookingButton, viewDetailsButton;

    public MyTicketsBookings() {
        setTitle("My Tickets & Bookings - ByteAir");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);

        try {
            setContentPane(new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("/images/TicketImage.jpg")))));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Background image not found.");
        }

        JLabel titleLabel = new JLabel("My Bookings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(120, 30, 600, 40);
        add(titleLabel);

        summaryLabel = new JLabel("Total Bookings: ");
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        summaryLabel.setForeground(Color.WHITE);
        summaryLabel.setBounds(120, 75, 400, 30);
        add(summaryLabel);

        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorLabel.setBounds(120, 110, 1000, 25);
        add(errorLabel);

        reminderLabel = new JLabel();
        reminderLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reminderLabel.setForeground(Color.BLACK);
        reminderLabel.setBounds(120, 140, 1000, 30);
        reminderLabel.setVisible(false);
        add(reminderLabel);

        searchField = new JTextField();
        searchField.setBounds(120, 180, 300, 30);
        searchField.setToolTipText("Search by flight number, origin, or destination");
        add(searchField);

        searchButton = createStyledButton("Search", 430, 180);
        searchButton.setToolTipText("Search your bookings");
        searchButton.addActionListener(e -> {
            errorLabel.setText("");
            searchBookings(searchField.getText());
        });
        add(searchButton);

        JLabel dateLabel = new JLabel("Filter by Date:");
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setBounds(550, 180, 120, 30);
        add(dateLabel);

        dateField = new JTextField("YYYY-MM-DD");
        dateField.setBounds(670, 180, 150, 30);
        dateField.setToolTipText("Example: 2025-05-03");
        add(dateField);

        filterDateButton = createStyledButton("Filter", 830, 180);
        filterDateButton.setToolTipText("Filter by date");
        filterDateButton.addActionListener(e -> {
            errorLabel.setText("");
            filterByDate(dateField.getText());
        });
        add(filterDateButton);

        ticketsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(ticketsTable);
        scrollPane.setBounds(120, 230, 1100, 350);
        add(scrollPane);

        viewDetailsButton = createStyledButton("View Details", 120, 600);
        viewDetailsButton.setToolTipText("View full details of selected ticket");
        viewDetailsButton.addActionListener(e -> {
            errorLabel.setText("");
            showBookingDetails();
        });
        add(viewDetailsButton);

        cancelBookingButton = createStyledButton("Cancel Booking", 360, 600);
        cancelBookingButton.setToolTipText("Cancel the selected booking");
        cancelBookingButton.addActionListener(e -> {
            errorLabel.setText("");
            cancelBooking();
        });
        add(cancelBookingButton);

        refreshButton = createStyledButton("Refresh", 600, 600);
        refreshButton.setToolTipText("Reload all bookings");
        refreshButton.addActionListener(e -> {
            errorLabel.setText("");
            loadBookings();
        });
        add(refreshButton);

        backButton = createStyledButton("Back to Menu", 840, 600);
        backButton.setToolTipText("Return to main menu");
        backButton.addActionListener(e -> {
            new MainMenu();
            dispose();
        });
        add(backButton);

        setVisible(true);
        loadBookings();
    }

    private JButton createStyledButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 200, 40);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(245, 245, 245));
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    private void loadBookings() {
    try (Connection conn = DatabaseConnection.getConnection()) {
        String query = "SELECT t.ticket_id, t.ticket_code, t.seat_no, t.class, t.issue_date, t.boarding_gate, " +
                "b.flight_no, b.booking_id, f.from_location, f.destination, f.flight_date, f.departure_time, f.arrival_time " +
                "FROM Ticket t " +
                "JOIN Booking b ON t.booking_id = b.booking_id " +
                "JOIN Flight f ON b.flight_no = f.flight_no " +
                "WHERE b.customer_id = ? AND b.status != 'Cancelled'";

        PreparedStatement pst = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setString(1, String.valueOf(Session.currentUserId));
        ResultSet rs = pst.executeQuery();

        tableModel = buildTableModel(rs);
        ticketsTable.setModel(tableModel);

        rs.beforeFirst();
        int count = 0;
        while (rs.next()) count++;
        summaryLabel.setText("Total Bookings: " + count);

        rs.beforeFirst();
        checkUpcomingFlights(rs);

    } catch (SQLException e) {
        errorLabel.setText("Error loading bookings: " + e.getMessage());
    }
}


    private void cancelBooking() {
        int row = ticketsTable.getSelectedRow();
        if (row == -1) {
            errorLabel.setText("Please select a booking to cancel.");
            return;
        }

        String bookingId = tableModel.getValueAt(row, 7).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this booking?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Booking SET Status = 'Cancelled' WHERE booking_id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, bookingId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Booking cancelled.");
            loadBookings();
        } catch (SQLException e) {
            errorLabel.setText("Error cancelling booking: " + e.getMessage());
        }
    }

    private void searchBookings(String keyword) {
    try (Connection conn = DatabaseConnection.getConnection()) {
        String query = "SELECT t.ticket_id, t.ticket_code, t.seat_no, t.class, t.issue_date, t.boarding_gate, " +
                "b.flight_no, b.booking_id, f.from_location, f.destination, f.flight_date, f.departure_time, f.arrival_time " +
                "FROM Ticket t " +
                "JOIN Booking b ON t.booking_id = b.booking_id " +
                "JOIN Flight f ON b.flight_no = f.flight_no " +
                "WHERE b.customer_id = ? AND b.status != 'Cancelled' AND " +
                "(f.flight_no LIKE ? OR f.from_location LIKE ? OR f.destination LIKE ?)";

        PreparedStatement pst = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setString(1, String.valueOf(Session.currentUserId));
        String pattern = "%" + keyword + "%";
        pst.setString(2, pattern);
        pst.setString(3, pattern);
        pst.setString(4, pattern);
        ResultSet rs = pst.executeQuery();

        tableModel = buildTableModel(rs);
        ticketsTable.setModel(tableModel);

    } catch (SQLException e) {
        errorLabel.setText("Search error: " + e.getMessage());
    }
}


    private void filterByDate(String date) {
    try (Connection conn = DatabaseConnection.getConnection()) {
        String query = "SELECT t.ticket_id, t.ticket_code, t.seat_no, t.class, t.issue_date, t.boarding_gate, " +
                "b.flight_no, b.booking_id, f.from_location, f.destination, f.flight_date, f.departure_time, f.arrival_time " +
                "FROM Ticket t " +
                "JOIN Booking b ON t.booking_id = b.booking_id " +
                "JOIN Flight f ON b.flight_no = f.flight_no " +
                "WHERE b.customer_id = ? AND b.status != 'Cancelled' AND f.flight_date = ?";

        PreparedStatement pst = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pst.setString(1, String.valueOf(Session.currentUserId));
        pst.setString(2, date);
        ResultSet rs = pst.executeQuery();

        tableModel = buildTableModel(rs);
        ticketsTable.setModel(tableModel);

    } catch (SQLException e) {
        errorLabel.setText("Date filter error: " + e.getMessage());
    }
}

    private void checkUpcomingFlights(ResultSet rs) throws SQLException {
        Date now = new Date();
        boolean hasUpcoming = false;

        while (rs.next()) {
            String time = rs.getString("departure_time");
            if (time != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    Date depTime = sdf.parse(time);
                    if (depTime.getTime() - now.getTime() <= 86400000 && depTime.getTime() - now.getTime() >= 0) {
                        hasUpcoming = true;
                        break;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        if (hasUpcoming) {
            reminderLabel.setText("Reminder: You have a flight within 24 hours!");
            reminderLabel.setOpaque(true);
            reminderLabel.setBackground(Color.YELLOW);
            reminderLabel.setVisible(true);
        }
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        String[] columns = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columns[i - 1] = meta.getColumnName(i);
        }

        Vector<String[]> data = new Vector<>();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getString(i);
            }
            data.add(row);
        }

        String[][] array = new String[data.size()][];
        data.toArray(array);

        return new DefaultTableModel(array, columns);
    }

    private void showBookingDetails() {
        int row = ticketsTable.getSelectedRow();
        if (row == -1) {
            errorLabel.setText("Select a booking to view details.");
            return;
        }

        StringBuilder details = new StringBuilder();
        for (int i = 0; i < ticketsTable.getColumnCount(); i++) {
            details.append(ticketsTable.getColumnName(i)).append(": ")
                    .append(ticketsTable.getValueAt(row, i)).append("\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Booking Details", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new MyTicketsBookings();
    }
}
