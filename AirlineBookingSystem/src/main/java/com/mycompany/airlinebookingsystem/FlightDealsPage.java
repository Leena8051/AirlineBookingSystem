package com.mycompany.airlinebookingsystem;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class FlightDealsPage extends JFrame {

    // UI
    private JTextField searchField;
    private JComboBox<String> destinationFilter;
    private JComboBox<String> sortCombo;
    private JCheckBox activeOnlyCheck;
    private JButton backButton;

    private JPanel dealsContainer;     // holds all deal cards
    private JPanel dealOfDayPanel;     // special top card

    // Data
    private java.util.List<Deal> allDeals = new ArrayList<>();
    private java.util.Set<String> destinations = new TreeSet<>();

    public FlightDealsPage() {
        setTitle("ByteAir - Flight Deals");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);

        // Background image
        try {
            setContentPane(new JLabel(new ImageIcon(
                    ImageIO.read(getClass().getResource("/images/TicketImage.jpg")))));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Background image not found.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            getContentPane().setBackground(new Color(230, 240, 255));
        }
        getContentPane().setLayout(null);

        initHeaderBanner();
        initFilterBar();
        initDealsArea();

        loadDealsFromDatabase();
        populateFilters();
        renderDeals();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // =========================================================
    // UI SECTIONS
    // =========================================================
    private void initHeaderBanner() {
        JPanel banner = new JPanel();
        banner.setLayout(null);
        banner.setBounds(60, 30, 1150, 100);
        banner.setBackground(new Color(0, 0, 0, 140));
        banner.setBorder(new LineBorder(new Color(200, 220, 255), 2, true));

        JLabel title = new JLabel("ByteAir Flight Deals & Offers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBounds(25, 10, 600, 35);
        banner.add(title);

        JLabel subtitle = new JLabel("Discover limited-time discounts on your favorite routes.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(Color.WHITE);
        subtitle.setBounds(25, 50, 700, 30);
        banner.add(subtitle);

        backButton = new JButton("Back to Main Menu");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        backButton.setBounds(900, 35, 200, 35);
        styleMainButton(backButton);
        banner.add(backButton);

        backButton.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });

        add(banner);
    }

    private void initFilterBar() {
        JPanel filterBar = new JPanel();
        filterBar.setLayout(null);
        filterBar.setOpaque(false);
        filterBar.setBounds(60, 140, 1150, 60);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchLabel.setBounds(0, 5, 70, 25);
        filterBar.add(searchLabel);

        searchField = new JTextField();
        searchField.setBounds(70, 5, 200, 28);
        searchField.setToolTipText("Search by destination or deal title");
        filterBar.add(searchField);

        JLabel destLabel = new JLabel("Destination:");
        destLabel.setForeground(Color.WHITE);
        destLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        destLabel.setBounds(290, 5, 90, 25);
        filterBar.add(destLabel);

        destinationFilter = new JComboBox<>();
        destinationFilter.setBounds(380, 5, 180, 28);
        destinationFilter.setToolTipText("Filter by destination city");
        filterBar.add(destinationFilter);

        JLabel sortLabel = new JLabel("Sort by:");
        sortLabel.setForeground(Color.WHITE);
        sortLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sortLabel.setBounds(580, 5, 70, 25);
        filterBar.add(sortLabel);

        sortCombo = new JComboBox<>(new String[]{
                "Highest Discount",
                "Soon to Expire",
                "Destination (A-Z)",
                "Price (Low to High)"
        });
        sortCombo.setBounds(650, 5, 200, 28);
        sortCombo.setToolTipText("Change how deals are ordered");
        filterBar.add(sortCombo);

        activeOnlyCheck = new JCheckBox("Only currently active deals");
        activeOnlyCheck.setBounds(870, 5, 230, 28);
        activeOnlyCheck.setForeground(Color.WHITE);
        activeOnlyCheck.setOpaque(false);
        activeOnlyCheck.setSelected(true);
        filterBar.add(activeOnlyCheck);

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { renderDeals(); }
        });
        destinationFilter.addActionListener(e -> renderDeals());
        sortCombo.addActionListener(e -> renderDeals());
        activeOnlyCheck.addActionListener(e -> renderDeals());

        add(filterBar);
    }

    private void initDealsArea() {
        dealOfDayPanel = new JPanel();
        dealOfDayPanel.setLayout(null);
        dealOfDayPanel.setBounds(60, 200, 1150, 120);
        dealOfDayPanel.setBackground(new Color(255, 255, 255, 230));
        dealOfDayPanel.setBorder(new LineBorder(new Color(255, 215, 0), 3, true));
        dealOfDayPanel.setVisible(false);
        add(dealOfDayPanel);

        dealsContainer = new JPanel();
        dealsContainer.setLayout(new BoxLayout(dealsContainer, BoxLayout.Y_AXIS));
        dealsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(dealsContainer);
        scrollPane.setBounds(60, 325, 1150, 360);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane);
    }

    // =========================================================
    // DATA MODEL
    // =========================================================
    private static class Deal {
        String dealId;
        String flightNo;
        String from;
        String to;
        double basePrice;
        String title;
        String description;
        String discountType; // PERCENT or FIXED
        double discountValue;
        LocalDate startDate;
        LocalDate endDate;
        boolean isActive;

        double getDiscountPercent() {
            if ("PERCENT".equalsIgnoreCase(discountType)) {
                return discountValue;
            }
            if (basePrice > 0) {
                return (discountValue / basePrice) * 100.0;
            }
            return 0;
        }

        boolean isCurrentlyActive() {
            LocalDate today = LocalDate.now();
            return isActive &&
                    (startDate == null || !today.isBefore(startDate)) &&
                    (endDate == null || !today.isAfter(endDate));
        }

        long daysUntilEnd() {
            if (endDate == null) return Long.MAX_VALUE;
            LocalDate today = LocalDate.now();
            return ChronoUnit.DAYS.between(today, endDate);
        }

        long daysUntilStart() {
            if (startDate == null) return Long.MAX_VALUE;
            LocalDate today = LocalDate.now();
            return ChronoUnit.DAYS.between(today, startDate);
        }
    }

    // =========================================================
    // DB LOADING
    // =========================================================
    private void loadDealsFromDatabase() {
        allDeals.clear();
        destinations.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql =
                    "SELECT d.deal_id, d.flight_no, f.from_location, f.destination, f.price, " +
                            "d.title, d.description, d.discount_type, d.discount_value, " +
                            "d.start_date, d.end_date, d.is_active " +
                            "FROM FlightDeal d " +
                            "JOIN Flight f ON d.flight_no = f.flight_no";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Deal d = new Deal();
                d.dealId = rs.getString("deal_id");
                d.flightNo = rs.getString("flight_no");
                d.from = rs.getString("from_location");
                d.to = rs.getString("destination");
                d.basePrice = rs.getDouble("price");
                d.title = rs.getString("title");
                d.description = rs.getString("description");
                d.discountType = rs.getString("discount_type");
                d.discountValue = rs.getDouble("discount_value");

                java.sql.Date s = rs.getDate("start_date");
                java.sql.Date e = rs.getDate("end_date");
                if (s != null) d.startDate = s.toLocalDate();
                if (e != null) d.endDate = e.toLocalDate();

                d.isActive = rs.getInt("is_active") == 1;

                allDeals.add(d);
                if (d.to != null && !d.to.isEmpty()) {
                    destinations.add(d.to);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load flight deals from database.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFilters() {
        destinationFilter.removeAllItems();
        destinationFilter.addItem("All Destinations");
        for (String dest : destinations) {
            destinationFilter.addItem(dest);
        }
        destinationFilter.setSelectedIndex(0);
    }

    // =========================================================
    // RENDERING
    // =========================================================
    private void renderDeals() {
        dealsContainer.removeAll();
        dealOfDayPanel.setVisible(false);

        java.util.List<Deal> filtered = applyFilters();

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("No deals match your filters.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            empty.setForeground(Color.WHITE);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            dealsContainer.add(empty);
            dealsContainer.revalidate();
            dealsContainer.repaint();
            return;
        }

        Deal best = null;
        for (Deal d : filtered) {
            if (!d.isCurrentlyActive()) continue;
            if (best == null || d.getDiscountPercent() > best.getDiscountPercent()) {
                best = d;
            }
        }
        if (best != null) {
            buildDealOfDay(best);
        }

        sortDeals(filtered);

        for (Deal d : filtered) {
            JPanel card = buildDealCard(d, (best != null && d.dealId.equals(best.dealId)));
            dealsContainer.add(card);
            dealsContainer.add(Box.createVerticalStrut(10));
        }

        dealsContainer.revalidate();
        dealsContainer.repaint();
    }

    private java.util.List<Deal> applyFilters() {
        String search = searchField.getText().trim().toLowerCase();
        String destFilter = (String) destinationFilter.getSelectedItem();
        if (destFilter == null) destFilter = "All Destinations";
        boolean activeOnly = activeOnlyCheck.isSelected();

        java.util.List<Deal> result = new ArrayList<>();
        for (Deal d : allDeals) {
            if (activeOnly && !d.isCurrentlyActive()) continue;

            if (!"All Destinations".equals(destFilter)) {
                if (d.to == null || !d.to.equalsIgnoreCase(destFilter)) continue;
            }

            if (!search.isEmpty()) {
                String hay = ((d.title != null ? d.title : "") + " " +
                        (d.to != null ? d.to : "")).toLowerCase();
                if (!hay.contains(search)) continue;
            }

            result.add(d);
        }
        return result;
    }

    private void sortDeals(java.util.List<Deal> list) {
        String sort = (String) sortCombo.getSelectedItem();
        if (sort == null) return;

        switch (sort) {
            case "Highest Discount":
                list.sort(Comparator.comparingDouble(Deal::getDiscountPercent).reversed());
                break;
            case "Soon to Expire":
                list.sort(Comparator.comparingLong(Deal::daysUntilEnd));
                break;
            case "Destination (A-Z)":
                list.sort(Comparator.comparing(d -> d.to == null ? "" : d.to));
                break;
            case "Price (Low to High)":
                list.sort(Comparator.comparingDouble(d -> d.basePrice));
                break;
        }
    }

    // ===================== DEAL OF THE DAY ===================
    private void buildDealOfDay(Deal d) {
        dealOfDayPanel.removeAll();

        JLabel titleLbl = new JLabel("Deal of the Day");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(new Color(255, 140, 0));
        titleLbl.setBounds(20, 10, 300, 25);
        dealOfDayPanel.add(titleLbl);

        JLabel routeLbl = new JLabel(d.flightNo + "  " + d.from + " → " + d.to);
        routeLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        routeLbl.setForeground(new Color(30, 30, 30));
        routeLbl.setBounds(20, 40, 450, 25);
        dealOfDayPanel.add(routeLbl);

        JLabel discountLbl = new JLabel(
                String.format("Save up to %.0f%% · Base price: %.2f SAR",
                        d.getDiscountPercent(), d.basePrice));
        discountLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        discountLbl.setForeground(new Color(50, 50, 50));
        discountLbl.setBounds(20, 70, 450, 25);
        dealOfDayPanel.add(discountLbl);

        long daysLeft = d.daysUntilEnd();
        String expiresText = (daysLeft < 0) ? "Offer expired"
                : (daysLeft == 0 ? " Ends today!" : " Ends in " + daysLeft + " day(s)");
        JLabel expireLbl = new JLabel(expiresText);
        expireLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        expireLbl.setForeground(new Color(178, 34, 34));
        expireLbl.setBounds(520, 40, 250, 25);
        dealOfDayPanel.add(expireLbl);

        String dateRangeText = "Valid: ";
        if (d.startDate != null && d.endDate != null) {
            dateRangeText += d.startDate + " → " + d.endDate;
        } else if (d.startDate != null) {
            dateRangeText += "from " + d.startDate;
        } else if (d.endDate != null) {
            dateRangeText += "until " + d.endDate;
        } else {
            dateRangeText += "No date range specified";
        }
        JLabel dateLbl = new JLabel(dateRangeText);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLbl.setForeground(new Color(80, 80, 80));
        dateLbl.setBounds(520, 70, 350, 25);
        dealOfDayPanel.add(dateLbl);

        JButton bookBtn = new JButton("Book This Deal");
        bookBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        bookBtn.setBounds(860, 45, 180, 35);
        styleMainButton(bookBtn);
        dealOfDayPanel.add(bookBtn);

        bookBtn.addActionListener(e -> openBookingForDeal(d));

        dealOfDayPanel.setVisible(true);
    }

    // ===================== NORMAL DEAL CARDS =================
    private JPanel buildDealCard(Deal d, boolean isDealOfDay) {
        JPanel card = new JPanel();
        card.setLayout(null);

        // increased height so info is not cut
        card.setPreferredSize(new Dimension(1100, 170));
        card.setMaximumSize(new Dimension(1100, 170));
        card.setMinimumSize(new Dimension(1100, 170));

        card.setOpaque(true);

        Color baseColor = new Color(255, 255, 255, 235);
        Color hoverColor = new Color(230, 240, 255, 245);

        card.setBackground(baseColor);
        card.setBorder(new LineBorder(new Color(200, 220, 255), 2, true));

        // Route + title
        JLabel routeLbl = new JLabel(d.flightNo + "  " + d.from + " → " + d.to);
        routeLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        routeLbl.setBounds(20, 10, 600, 25);
        card.add(routeLbl);

        JLabel titleLbl = new JLabel(d.title != null ? d.title : "");
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLbl.setBounds(20, 35, 700, 22);
        card.add(titleLbl);

        // White strip
        JPanel infoStrip = new JPanel();
        infoStrip.setLayout(null);
        infoStrip.setBackground(Color.WHITE);
        infoStrip.setBorder(new LineBorder(new Color(220, 230, 240), 1, true));
        infoStrip.setBounds(15, 70, 820, 55);
        card.add(infoStrip);

        String discountText;
        if ("PERCENT".equalsIgnoreCase(d.discountType)) {
            discountText = String.format("Discount: %.0f%%", d.discountValue);
        } else {
            discountText = String.format("Discount: %.2f SAR OFF", d.discountValue);
        }
        JLabel discountLbl = new JLabel(discountText);
        discountLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        discountLbl.setBounds(10, 17, 220, 20);
        infoStrip.add(discountLbl);

        JLabel priceLbl = new JLabel(String.format("Base price: %.2f SAR", d.basePrice));
        priceLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priceLbl.setBounds(240, 17, 200, 20);
        infoStrip.add(priceLbl);

        String dateRangeText = "Valid: ";
        if (d.startDate != null && d.endDate != null) {
            dateRangeText += d.startDate + " → " + d.endDate;
        } else if (d.startDate != null) {
            dateRangeText += "from " + d.startDate;
        } else if (d.endDate != null) {
            dateRangeText += "until " + d.endDate;
        } else {
            dateRangeText += "No date range specified";
        }
        JLabel dateLbl = new JLabel(dateRangeText);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLbl.setForeground(new Color(70, 70, 70));
        dateLbl.setBounds(450, 17, 360, 20);
        infoStrip.add(dateLbl);

        // Expiry / status
        long daysLeft = d.daysUntilEnd();
        String expiresText;
        if (d.isCurrentlyActive()) {
            if (daysLeft < 0) {
                expiresText = "Offer expired";
            } else if (daysLeft == 0) {
                expiresText = " Ends today!";
            } else if (daysLeft <= 3) {
                expiresText = " Ends in " + daysLeft + " day(s)";
            } else {
                expiresText = "Ends in " + daysLeft + " day(s)";
            }
        } else {
            long daysToStart = d.daysUntilStart();
            if (daysToStart > 0 && d.startDate != null) {
                expiresText = "Starts on " + d.startDate + " (in " + daysToStart + " day(s))";
            } else {
                expiresText = "Offer not currently active";
            }
        }

        JLabel expireLbl = new JLabel(expiresText);
        expireLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        expireLbl.setForeground(new Color(178, 34, 34));
        expireLbl.setBounds(850, 25, 260, 20);
        card.add(expireLbl);

        JButton bookBtn = new JButton("Book Now");
        bookBtn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        bookBtn.setBounds(860, 90, 150, 35);
        styleMainButton(bookBtn);
        card.add(bookBtn);

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverColor);
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(baseColor);
                card.setCursor(Cursor.getDefaultCursor());
            }
        });

        bookBtn.addActionListener(e -> openBookingForDeal(d));

        return card;
    }

    // =========================================================
    // NAVIGATION
    // =========================================================
    private void openBookingForDeal(Deal d) {
        // store selected flight so BookingPage can pre-select it
        try {
            Session.selectedFlightFromDeals = d.flightNo;
        } catch (Exception ignored) {
            // if you didn't add this field in Session yet, add:
            // public static String selectedFlightFromDeals;
        }
        new BookingPage().setVisible(true);
        dispose();
    }

    private void styleMainButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 255), 2, true));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(210, 225, 250)); }
            public void mouseExited(MouseEvent e) { button.setBackground(Color.WHITE); }
        });
    }

    public static void main(String[] args) {
        new FlightDealsPage();
    }
}
