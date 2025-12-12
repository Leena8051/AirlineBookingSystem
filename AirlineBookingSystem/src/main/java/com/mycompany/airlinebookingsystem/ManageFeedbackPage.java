package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ManageFeedbackPage extends JFrame {

    private JTable feedbackTable;
    private DefaultTableModel tableModel;
    private JTextField feedbackIdField, customerIdField, bookingIdField, ratingField, createdAtField;
    private JTextArea commentsArea;
    private JButton updateButton, deleteButton, refreshButton, backButton;

    public ManageFeedbackPage() {
        setTitle("Manage Feedback - ByteAir");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);

        try {
            setContentPane(new JLabel(new ImageIcon(
                    getClass().getResource("/images/AirPlaneSky.jpg"))));
        } catch (Exception e) {
            getContentPane().setBackground(new Color(200, 220, 255));
        }
        getContentPane().setLayout(null);

        initComponents();
        loadFeedback();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        JLabel title = new JLabel("Manage Customer Feedback");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setBounds(60, 20, 700, 40);
        add(title);

        feedbackTable = new JTable();
        feedbackTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        feedbackTable.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(feedbackTable);
        scroll.setBounds(60, 80, 1000, 260);
        add(scroll);

        int xLabel = 60, xField = 250, y = 360, h = 26, gap = 40;

        addLabel("Feedback ID:", xLabel, y);
        feedbackIdField = addTextField(xField, y); feedbackIdField.setEditable(false); y += gap;

        addLabel("Customer ID:", xLabel, y);
        customerIdField = addTextField(xField, y); customerIdField.setEditable(false); y += gap;

        addLabel("Booking ID:", xLabel, y);
        bookingIdField = addTextField(xField, y); bookingIdField.setEditable(false); y += gap;

        addLabel("Rating (1–5):", xLabel, y);
        ratingField = addTextField(xField, y); y += gap;

        addLabel("Created At:", xLabel, y);
        createdAtField = addTextField(xField, y); createdAtField.setEditable(false); y += gap;

        addLabel("Comments:", xLabel, y);
        commentsArea = new JTextArea();
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        JScrollPane commScroll = new JScrollPane(commentsArea);
        commScroll.setBounds(xField, y, 400, 110);
        add(commScroll);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        btnPanel.setOpaque(false);
        btnPanel.setBounds(700, 360, 220, 180);

        updateButton = createButton("Update Feedback");
        deleteButton = createButton("Delete Feedback");
        refreshButton = createButton("Refresh");
        backButton = createButton("Back to Dashboard");

        btnPanel.add(updateButton);
        btnPanel.add(deleteButton);
        btnPanel.add(refreshButton);
        btnPanel.add(backButton);
        add(btnPanel);

        feedbackTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = feedbackTable.getSelectedRow();
                if (row != -1) {
                    feedbackIdField.setText(tableModel.getValueAt(row, 0).toString());
                    customerIdField.setText(tableModel.getValueAt(row, 1).toString());
                    bookingIdField.setText(tableModel.getValueAt(row, 2).toString());
                    ratingField.setText(tableModel.getValueAt(row, 3).toString());
                    commentsArea.setText(tableModel.getValueAt(row, 4) == null ?
                            "" : tableModel.getValueAt(row, 4).toString());
                    createdAtField.setText(tableModel.getValueAt(row, 5).toString());
                }
            }
        });

        updateButton.addActionListener(e -> updateFeedback());
        deleteButton.addActionListener(e -> deleteFeedback());
        refreshButton.addActionListener(e -> loadFeedback());
        backButton.addActionListener(e -> {
            new AdminDashboard().setVisible(true);
            dispose();
        });
    }

    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setBounds(x, y, 180, 26);
        add(label);
    }

    private JTextField addTextField(int x, int y) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setBounds(x, y, 250, 26);
        add(tf);
        return tf;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 255), 2, true));
        btn.setFocusPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(210, 225, 250)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    //  LOAD 
    private void loadFeedback() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT feedback_id, customer_id, booking_id, rating, comments, created_at " +
                         "FROM Feedback ORDER BY created_at DESC";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            tableModel = buildTableModel(rs);
            feedbackTable.setModel(tableModel);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading feedback.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        String[] cols = new String[colCount];
        for (int i = 1; i <= colCount; i++) cols[i - 1] = meta.getColumnName(i);

        Vector<String[]> data = new Vector<>();
        while (rs.next()) {
            String[] row = new String[colCount];
            for (int i = 1; i <= colCount; i++) row[i - 1] = rs.getString(i);
            data.add(row);
        }

        return new DefaultTableModel(data.toArray(new String[0][]), cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    //  UPDATE 
    private void updateFeedback() {
        int row = feedbackTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a feedback entry to update.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String feedbackId = feedbackIdField.getText().trim();
        String ratingStr  = ratingField.getText().trim();
        String comments   = commentsArea.getText().trim();

        int rating;
        try {
            rating = Integer.parseInt(ratingStr);
            if (rating < 1 || rating > 5) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Rating must be an integer between 1 and 5.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Feedback SET rating = ?, comments = ? WHERE feedback_id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, rating);
            pst.setString(2, comments);
            pst.setInt(3, Integer.parseInt(feedbackId));
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Feedback updated.");
            loadFeedback();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error updating feedback.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //  DELETE 
    private void deleteFeedback() {
        int row = feedbackTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a feedback entry to delete.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String feedbackId = tableModel.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete feedback ID " + feedbackId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM Feedback WHERE feedback_id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, Integer.parseInt(feedbackId));
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Feedback deleted.");
            loadFeedback();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error deleting feedback.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new ManageFeedbackPage();
    }
}
