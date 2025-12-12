package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.IOException;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        initComponents();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true); 
    }

    private void initComponents() {
        setTitle("Airline Admin Dashboard");
        setLayout(null);

        try {
            setContentPane(new JLabel(new ImageIcon(
                ImageIO.read(getClass().getResource("/images/AirPlaneSky.jpg"))
            )));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Background image not found.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        getContentPane().setLayout(null);

        ImageIcon logoIcon = new ImageIcon("logo.png");
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setBounds(50, 20, 100, 100);
        add(logoLabel);

        JLabel companyNameLabel = new JLabel("ByteAir Airlines Admin Panel");
        companyNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        companyNameLabel.setForeground(Color.BLACK);
        companyNameLabel.setBounds(180, 40, 600, 50);
        add(companyNameLabel);

        String adminName = Session.currentName != null ? 
                           Session.currentName : 
                           Session.currentUserId;

        JLabel welcomeLabel = new JLabel("Welcome, " + adminName + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        welcomeLabel.setForeground(Color.BLACK);
        welcomeLabel.setBounds(180, 90, 400, 30);
        add(welcomeLabel);

        //  BUTTONS 
        JButton flightsButton   = createButton("️  Manage Flights",     250, "View, add, or edit flight details.");
        JButton bookingsButton  = createButton(" Manage Bookings",     330, "Review or manage customer bookings.");
        JButton addAdminButton  = createButton("  Add New Admin",      410, "Register a new administrator.");

        //  NEW BUTTON ADDED HERE 
        JButton feedbackButton  = createButton("  Manage Feedback",     490, "Review and manage customer feedback.");

        JButton logoutButton    = createButton(" Logout",              570, "Sign out and return to login screen.");

        add(flightsButton);
        add(bookingsButton);
        add(addAdminButton);
        add(feedbackButton);
        add(logoutButton);

        //  ACTION LISTENERS 
        flightsButton.addActionListener(e -> new ManageFlights());
        bookingsButton.addActionListener(e -> new ManageBookings());
        addAdminButton.addActionListener(e -> new AddAdminPage());

        feedbackButton.addActionListener(e -> new ManageFeedbackPage()); 
        logoutButton.addActionListener(e -> {
            new LoginSignupPage().setVisible(true);
            dispose();
        });
    }

    private JButton createButton(String text, int yPosition, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        button.setBounds(500, yPosition, 400, 50);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 255), 2, true));
        button.setToolTipText(tooltip);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    public static void main(String[] args) {
        new AdminDashboard();
    }
}
