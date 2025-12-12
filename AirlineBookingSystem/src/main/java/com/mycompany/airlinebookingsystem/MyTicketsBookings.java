package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.imageio.ImageIO;

public class MyTicketsBookings extends JFrame {

    private JTable ticketsTable;
    private DefaultTableModel tableModel;

    private JLabel summaryLabel, reminderLabel, errorLabel;
    private JTextField searchField, dateField;

    private JButton searchButton, filterDateButton, refreshButton, backButton;
    private JButton cancelTicketButton, restoreTicketButton, viewDetailsButton;

    // column indexes (based on SELECT order)
    private static final int COL_TICKET_ID   = 0;
    private static final int COL_STATUS      = 6;   // Ticket.status
    private static final int COL_FLIGHT_STAT = 13;  // Flight.flight_status

    public MyTicketsBookings() {
        setTitle("My Tickets & Bookings - ByteAir");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        //  Background 
        setContentPane(createBackgroundLayer("/images/TicketImage.jpg"));
        setLayout(new BorderLayout());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(30, 60, 35, 60)); // padding around the whole UI
        add(wrapper, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("My Tickets");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);

        summaryLabel = new JLabel("Total Tickets: 0");
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        summaryLabel.setForeground(Color.WHITE);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorLabel.setForeground(new Color(255, 80, 80));

        reminderLabel = new JLabel(" ");
        reminderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reminderLabel.setForeground(Color.WHITE);
        reminderLabel.setVisible(false);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(summaryLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(errorLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(reminderLabel);

        wrapper.add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        wrapper.add(centerPanel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = 0;

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(350, 36));

        searchButton = createButton("Search");
        searchButton.addActionListener(e -> searchTickets(searchField.getText()));

        dateField = new JTextField("YYYY-MM-DD");
        dateField.setPreferredSize(new Dimension(180, 36));

        filterDateButton = createButton("Filter");
        filterDateButton.addActionListener(e -> filterByDate(dateField.getText()));

        gc.gridx = 0;
        gc.weightx = 1.0;
        controls.add(searchField, gc);

        gc.gridx = 1;
        gc.weightx = 0;
        controls.add(searchButton, gc);

        gc.gridx = 2;
        gc.weightx = 0;
        controls.add(dateField, gc);

        gc.gridx = 3;
        gc.insets = new Insets(0, 0, 0, 0);
        controls.add(filterDateButton, gc);

        centerPanel.add(controls, BorderLayout.NORTH);

        // Table
        ticketsTable = new JTable();
        ticketsTable.setRowHeight(28);
        ticketsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ticketsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(ticketsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        //  Bottom buttons row 
        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(18, 0, 0, 0));
        wrapper.add(bottom, BorderLayout.SOUTH);

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = 0;
        bc.insets = new Insets(0, 0, 0, 18);
        bc.fill = GridBagConstraints.NONE;

        viewDetailsButton = createButton("View Details");
        viewDetailsButton.addActionListener(e -> showBookingDetails());

        cancelTicketButton = createButton("Cancel Ticket");
        cancelTicketButton.addActionListener(e -> cancelTicket());

        restoreTicketButton = createButton("Restore Ticket");
        restoreTicketButton.addActionListener(e -> restoreTicket());

        refreshButton = createButton("Refresh");
        refreshButton.addActionListener(e -> loadTickets());

        backButton = createButton("Back");
        backButton.addActionListener(e -> {
            new MainMenu();
            dispose();
        });

        bc.gridx = 0; bottom.add(viewDetailsButton, bc);
        bc.gridx = 1; bottom.add(cancelTicketButton, bc);
        bc.gridx = 2; bottom.add(restoreTicketButton, bc);
        bc.gridx = 3; bottom.add(refreshButton, bc);

        // push Back button to the far right
        bc.gridx = 4;
        bc.insets = new Insets(0, 0, 0, 0);
        bc.weightx = 1;
        bc.anchor = GridBagConstraints.EAST;
        bottom.add(backButton, bc);

        setVisible(true);
        loadTickets();
    }

    private JComponent createBackgroundLayer(String resourcePath) {
        try {
            Image img = ImageIO.read(getClass().getResource(resourcePath));
            return new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (img != null) {
                        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    }
                }
            };
        } catch (Exception e) {
            JPanel p = new JPanel();
            p.setBackground(new Color(230, 240, 255));
            return p;
        }
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(180, 42));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        return b;
    }

    // LOAD TICKETS (WITH STATUS)
    private void loadTickets() {
        errorLabel.setText(" ");
        try (Connection conn = DatabaseConnection.getConnection()) {

            String sql =
                    "SELECT t.ticket_id, t.ticket_code, t.seat_no, t.class, " +
                    "t.issue_date, t.boarding_gate, t.status, " +
                    "b.flight_no, f.from_location, f.destination, " +
                    "f.flight_date, f.departure_time, f.arrival_time, f.flight_status " +
                    "FROM Ticket t " +
                    "JOIN Booking b ON t.booking_id = b.booking_id " +
                    "JOIN Flight f ON b.flight_no = f.flight_no " +
                    "WHERE b.customer_id = ?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, Session.currentUserId);
            ResultSet rs = pst.executeQuery();

            tableModel = buildTableModel(rs);
            ticketsTable.setModel(tableModel);
            summaryLabel.setText("Total Tickets: " + tableModel.getRowCount());

        } catch (SQLException e) {
            errorLabel.setText("Load error: " + e.getMessage());
        }
    }

    // CANCEL SINGLE TICKET
    private void cancelTicket() {
        errorLabel.setText(" ");
        int row = ticketsTable.getSelectedRow();
        if (row == -1) {
            errorLabel.setText("Select a ticket first.");
            return;
        }

        String ticketId = tableModel.getValueAt(row, COL_TICKET_ID).toString();
        String status   = tableModel.getValueAt(row, COL_STATUS).toString();
        String flightStatus = tableModel.getValueAt(row, COL_FLIGHT_STAT).toString();

        if ("Cancelled".equalsIgnoreCase(status)) {
            errorLabel.setText("This ticket is already cancelled.");
            return;
        }

        if ("Completed".equalsIgnoreCase(flightStatus)) {
            errorLabel.setText("Cannot cancel a ticket for a completed flight.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel this ticket?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pst =
                    conn.prepareStatement("UPDATE Ticket SET status='Cancelled' WHERE ticket_id=?");
            pst.setString(1, ticketId);
            pst.executeUpdate();
            loadTickets();
        } catch (SQLException e) {
            errorLabel.setText("Cancel error: " + e.getMessage());
        }
    }

    // RESTORE TICKET
    private void restoreTicket() {
        errorLabel.setText(" ");
        int row = ticketsTable.getSelectedRow();
        if (row == -1) {
            errorLabel.setText("Select a ticket first.");
            return;
        }

        String ticketId = tableModel.getValueAt(row, COL_TICKET_ID).toString();
        String status   = tableModel.getValueAt(row, COL_STATUS).toString();

        if (!"Cancelled".equalsIgnoreCase(status)) {
            errorLabel.setText("Only cancelled tickets can be restored.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pst =
                    conn.prepareStatement("UPDATE Ticket SET status='Active' WHERE ticket_id=?");
            pst.setString(1, ticketId);
            pst.executeUpdate();
            loadTickets();
        } catch (SQLException e) {
            errorLabel.setText("Restore error: " + e.getMessage());
        }
    }

    private void searchTickets(String keyword) {
        errorLabel.setText(" ");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql =
                    "SELECT t.ticket_id, t.ticket_code, t.seat_no, t.class, " +
                    "t.issue_date, t.boarding_gate, t.status, " +
                    "b.flight_no, f.from_location, f.destination, " +
                    "f.flight_date, f.departure_time, f.arrival_time, f.flight_status " +
                    "FROM Ticket t " +
                    "JOIN Booking b ON t.booking_id = b.booking_id " +
                    "JOIN Flight f ON b.flight_no = f.flight_no " +
                    "WHERE b.customer_id=? AND " +
                    "(b.flight_no LIKE ? OR f.from_location LIKE ? OR f.destination LIKE ?)";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, Session.currentUserId);
            String p = "%" + keyword + "%";
            pst.setString(2, p);
            pst.setString(3, p);
            pst.setString(4, p);
            ResultSet rs = pst.executeQuery();

            tableModel = buildTableModel(rs);
            ticketsTable.setModel(tableModel);
            summaryLabel.setText("Total Tickets: " + tableModel.getRowCount());

        } catch (SQLException e) {
            errorLabel.setText("Search error: " + e.getMessage());
        }
    }

    private void filterByDate(String date) {
        errorLabel.setText(" ");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql =
                    "SELECT t.ticket_id, t.ticket_code, t.seat_no, t.class, " +
                    "t.issue_date, t.boarding_gate, t.status, " +
                    "b.flight_no, f.from_location, f.destination, " +
                    "f.flight_date, f.departure_time, f.arrival_time, f.flight_status " +
                    "FROM Ticket t " +
                    "JOIN Booking b ON t.booking_id = b.booking_id " +
                    "JOIN Flight f ON b.flight_no = f.flight_no " +
                    "WHERE b.customer_id=? AND f.flight_date=?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, Session.currentUserId);
            pst.setString(2, date);
            ResultSet rs = pst.executeQuery();

            tableModel = buildTableModel(rs);
            ticketsTable.setModel(tableModel);
            summaryLabel.setText("Total Tickets: " + tableModel.getRowCount());

        } catch (SQLException e) {
            errorLabel.setText("Date filter error: " + e.getMessage());
        }
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        String[] headers = new String[cols];
        for (int i = 1; i <= cols; i++) headers[i - 1] = meta.getColumnName(i);

        Vector<String[]> data = new Vector<>();
        while (rs.next()) {
            String[] row = new String[cols];
            for (int i = 1; i <= cols; i++) row[i - 1] = rs.getString(i);
            data.add(row);
        }

        String[][] arr = new String[data.size()][];
        data.toArray(arr);

        return new DefaultTableModel(arr, headers) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private void showBookingDetails() {
        int row = ticketsTable.getSelectedRow();
        if (row == -1) {
            errorLabel.setText("Select a ticket first.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ticketsTable.getColumnCount(); i++) {
            sb.append(ticketsTable.getColumnName(i)).append(": ")
              .append(ticketsTable.getValueAt(row, i)).append("\n");
        }

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Ticket Details", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyTicketsBookings::new);
    }
}
