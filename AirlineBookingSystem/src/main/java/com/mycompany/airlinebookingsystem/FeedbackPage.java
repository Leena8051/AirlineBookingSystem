package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FeedbackPage extends JFrame {

    private JSpinner ratingSpinner;
    private JTextArea commentsArea;
    private JLabel errorLabel;
    private JLabel charCountLabel;

    //  NEW: booking id for this feedback
    private final String bookingId;

    private static final int MAX_COMMENT_CHARS = 300;
    private static final String PLACEHOLDER_TEXT =
            "Write your feedback here…\nTell us what you loved or what we can improve.";

    //  NEW: constructor requires bookingId
    public FeedbackPage(String bookingId) {
        this.bookingId = bookingId;

        setTitle("ByteAir - Feedback");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        try {
            setContentPane(new JLabel(new ImageIcon(
                    getClass().getResource("/images/AirPlaneSky.jpg")
            )));
        } catch (Exception e) {
            getContentPane().setBackground(new Color(200, 220, 255));
        }
        getContentPane().setLayout(null);

        // Make tooltips appear faster and stay a bit longer
        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(8000);

        initComponents();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {

        // -----------------------------------------
        // TITLE BANNER
        // -----------------------------------------
        JPanel titleBanner = new JPanel(null);
        titleBanner.setOpaque(true);
        titleBanner.setBackground(new Color(255, 255, 255, 230));
        titleBanner.setBorder(new LineBorder(new Color(180, 210, 255), 2, true));
        titleBanner.setBounds(60, 25, 520, 80);
        titleBanner.setToolTipText("Share your honest experience with ByteAir ");
        add(titleBanner);

        JLabel title = new JLabel("Share Your Feedback");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(40, 70, 120));
        title.setBounds(20, 5, 400, 35);
        titleBanner.add(title);

        JLabel tagline = new JLabel("Your experience helps us improve your journey ️");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(new Color(70, 100, 150));
        tagline.setBounds(20, 40, 460, 25);
        titleBanner.add(tagline);

        // -----------------------------------------
        // MAIN INPUT CARD
        // -----------------------------------------
        JPanel inputCard = new JPanel(null);
        inputCard.setBackground(new Color(250, 252, 255, 235));
        inputCard.setBounds(60, 130, 1000, 360);
        inputCard.setBorder(new LineBorder(new Color(150, 180, 255), 2, true));
        inputCard.setToolTipText("Please rate your experience and leave a comment.");
        add(inputCard);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 18);
        int x1 = 40, x2 = 260, y = 40, gap = 60;

        //  show booking id (read-only) to avoid confusion
        JLabel bookingLabel = new JLabel("Booking ID:");
        bookingLabel.setBounds(x1, y, 160, 30);
        bookingLabel.setFont(labelFont);
        inputCard.add(bookingLabel);

        JTextField bookingField = new JTextField(bookingId == null ? "" : bookingId);
        bookingField.setBounds(x2, y, 200, 32);
        bookingField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        bookingField.setEditable(false);
        bookingField.setBackground(new Color(245, 245, 245));
        bookingField.setBorder(new LineBorder(new Color(210, 220, 240), 1, true));
        bookingField.setToolTipText("This feedback will be linked to this booking.");
        inputCard.add(bookingField);

        y += gap;

        // -----------------------------------------
        // RATING SPINNER (1–5)
        // -----------------------------------------
        JLabel ratingLabel = new JLabel("Rating (1–5):");
        ratingLabel.setBounds(x1, y, 160, 30);
        ratingLabel.setFont(labelFont);
        inputCard.add(ratingLabel);

        ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        ratingSpinner.setBounds(x2, y, 70, 32);
        ratingSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        ratingSpinner.setToolTipText("Choose a value between 1 (poor) and 5 (excellent).");
        inputCard.add(ratingSpinner);

        y += gap;

        JSeparator sep = new JSeparator();
        sep.setBounds(30, y - 20, 940, 1);
        sep.setForeground(new Color(210, 220, 240));
        inputCard.add(sep);

        // -----------------------------------------
        // COMMENTS FIELD
        // -----------------------------------------
        JLabel commentsLabel = new JLabel("Comments:");
        commentsLabel.setBounds(x1, y, 160, 30);
        commentsLabel.setFont(labelFont);
        inputCard.add(commentsLabel);

        commentsArea = new JTextArea();
        commentsArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setForeground(Color.GRAY);
        commentsArea.setText(PLACEHOLDER_TEXT);
        commentsArea.setToolTipText(
                "<html>Tell us what you liked, what went wrong,<br>" +
                        "or how we can make your next trip better.</html>"
        );

        JScrollPane commentScroll = new JScrollPane(commentsArea);
        commentScroll.setBounds(x2, y, 420, 140);
        commentScroll.setToolTipText("Type your feedback here. Maximum " + MAX_COMMENT_CHARS + " characters.");
        inputCard.add(commentScroll);

        // Character counter
        charCountLabel = new JLabel("0 / " + MAX_COMMENT_CHARS);
        charCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        charCountLabel.setForeground(new Color(90, 110, 140));
        charCountLabel.setBounds(x2, y + 145, 200, 20);
        charCountLabel.setToolTipText("Shows how many characters you have used out of " + MAX_COMMENT_CHARS + ".");
        inputCard.add(charCountLabel);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorLabel.setBounds(x1, 290, 900, 30);
        errorLabel.setToolTipText("Any problems with your input will appear here.");
        inputCard.add(errorLabel);

        // Placeholder handling
        commentsArea.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (commentsArea.getText().equals(PLACEHOLDER_TEXT)) {
                    commentsArea.setForeground(Color.BLACK);
                    commentsArea.setText("");
                }
            }

            public void focusLost(FocusEvent e) {
                if (commentsArea.getText().trim().isEmpty()) {
                    commentsArea.setForeground(Color.GRAY);
                    commentsArea.setText(PLACEHOLDER_TEXT);
                }
            }
        });

        // Character counter updates
        commentsArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCharCount(); }
            public void removeUpdate(DocumentEvent e) { updateCharCount(); }
            public void changedUpdate(DocumentEvent e) { updateCharCount(); }
        });

        // -----------------------------------------
        // BUTTONS
        // -----------------------------------------
        JButton submitBtn = createButton("Submit Feedback");
        submitBtn.setBounds(720, 80, 220, 48);
        submitBtn.setToolTipText("Save your rating and comment. Thank you for helping us improve!");
        submitBtn.addActionListener(e -> submitFeedback());
        inputCard.add(submitBtn);

        JButton backBtn = createButton("Back to Main Menu");
        backBtn.setBounds(720, 150, 220, 48);
        backBtn.setToolTipText("Go back to the main menu without submitting new feedback.");
        backBtn.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });
        inputCard.add(backBtn);

        // Make Enter key press submit (nice UX)
        getRootPane().setDefaultButton(submitBtn);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(40, 70, 120));
        btn.setBorder(new LineBorder(new Color(130, 170, 240), 2, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(210, 225, 255));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    // ---------------- CHARACTER COUNTER -------------------
    private void updateCharCount() {
        String text = commentsArea.getText();

        if (text.equals(PLACEHOLDER_TEXT)) {
            charCountLabel.setText("0 / " + MAX_COMMENT_CHARS);
            charCountLabel.setForeground(new Color(90, 110, 140));
            return;
        }

        int len = text.length();
        charCountLabel.setText(len + " / " + MAX_COMMENT_CHARS);

        if (len > MAX_COMMENT_CHARS) {
            charCountLabel.setForeground(Color.RED);
            errorLabel.setText("Maximum " + MAX_COMMENT_CHARS + " characters allowed.");
        } else {
            charCountLabel.setForeground(new Color(90, 110, 140));
            if (errorLabel.getText().startsWith("Maximum")) {
                errorLabel.setText(" ");
            }
        }
    }

    // ---------------- SUBMIT FEEDBACK -------------------
    private void submitFeedback() {

        int rating = (int) ratingSpinner.getValue();

        String rawText = commentsArea.getText();
        String comments = rawText.equals(PLACEHOLDER_TEXT) ? "" : rawText.trim();

        // 0) booking id required
        if (bookingId == null || bookingId.trim().isEmpty()) {
            errorLabel.setText("No booking selected. Open feedback from your booking page.");
            JOptionPane.showMessageDialog(this,
                    "No booking ID was provided.\nOpen Feedback from your booking or tickets page.",
                    "Missing Booking ID",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1) Check user is logged in
        if (Session.currentUserId == null || Session.currentUserId.trim().isEmpty()) {
            errorLabel.setText("You must be logged in to submit feedback.");
            JOptionPane.showMessageDialog(this,
                    "Please log in before submitting feedback.",
                    "Not logged in",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2) Comment not empty
        if (comments.isEmpty()) {
            errorLabel.setText("Please write your feedback.");
            return;
        }

        // 3) Minimum length
        if (comments.length() < 5) {
            errorLabel.setText("Please add a bit more detail (at least 5 characters).");
            return;
        }

        // 4) Maximum length
        if (comments.length() > MAX_COMMENT_CHARS) {
            errorLabel.setText("Your feedback is too long. Maximum " + MAX_COMMENT_CHARS + " characters allowed.");
            return;
        }

        // 5) Try saving to database
        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn == null) {
                errorLabel.setText("Unable to connect to the database. Please try again later.");
                JOptionPane.showMessageDialog(this,
                        "We couldn't connect to the database.\nPlease try again later.",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            //  Optional safety: ensure booking belongs to this customer
            String checkSql = "SELECT 1 FROM Booking WHERE booking_id = ? AND customer_id = ?";
            try (PreparedStatement chk = conn.prepareStatement(checkSql)) {
                chk.setString(1, bookingId);
                chk.setString(2, Session.currentUserId);
                ResultSet rs = chk.executeQuery();
                if (!rs.next()) {
                    errorLabel.setText("Invalid booking. Please choose your own booking.");
                    JOptionPane.showMessageDialog(this,
                            "This booking does not belong to your account.\nPlease choose your own booking.",
                            "Invalid Booking",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // insert booking_id too
            String insert =
                    "INSERT INTO Feedback (customer_id, booking_id, rating, comments) VALUES (?, ?, ?, ?)";

            try (PreparedStatement pst = conn.prepareStatement(insert)) {
                pst.setString(1, Session.currentUserId);
                pst.setString(2, bookingId);
                pst.setInt(3, rating);
                pst.setString(4, comments);
                pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Thank you! Your feedback has been submitted.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            new MainMenu().setVisible(true);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            errorLabel.setText("There was a problem saving your feedback. Please try again.");
            JOptionPane.showMessageDialog(this,
                    "There was a problem saving your feedback.\nPlease try again later.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //  Example test run
    public static void main(String[] args) {
        // simulate logged-in user (remove this in real project)
        // Session.currentUserId = "C-1A";

        new FeedbackPage("B-K1");
    }
}
