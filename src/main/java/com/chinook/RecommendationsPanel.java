package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RecommendationsPanel extends JPanel {

    private JComboBox<String[]> customerBox;
    private JLabel totalSpentLabel;
    private JLabel totalPurchasesLabel;
    private JLabel lastPurchaseLabel;
    private JLabel favouriteGenreLabel;
    private DefaultTableModel recommendModel;

    public RecommendationsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top - customer selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Customer:"));
        customerBox = new JComboBox<>();
        customerBox.setPreferredSize(new Dimension(250, 25));
        topPanel.add(customerBox);
        add(topPanel, BorderLayout.NORTH);

        // Middle - spending summary
        JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Spending Summary"));
        totalSpentLabel     = new JLabel("-");
        totalPurchasesLabel = new JLabel("-");
        lastPurchaseLabel   = new JLabel("-");
        favouriteGenreLabel = new JLabel("-");
        summaryPanel.add(new JLabel("Total Spent:")); summaryPanel.add(totalSpentLabel);
        summaryPanel.add(new JLabel("Total Purchases:")); summaryPanel.add(totalPurchasesLabel);
        summaryPanel.add(new JLabel("Last Purchase:")); summaryPanel.add(lastPurchaseLabel);
        summaryPanel.add(new JLabel("Favourite Genre:")); summaryPanel.add(favouriteGenreLabel);
        add(summaryPanel, BorderLayout.CENTER);

        // Bottom - recommendations table
        JPanel recPanel = new JPanel(new BorderLayout());
        recPanel.setBorder(BorderFactory.createTitledBorder("Recommended Tracks"));
        String[] cols = {"Track", "Album", "Genre", "Price"};
        recommendModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable recTable = new JTable(recommendModel);
        recTable.setFillsViewportHeight(true);
        recPanel.add(new JScrollPane(recTable), BorderLayout.CENTER);
        add(recPanel, BorderLayout.SOUTH);

        // Load customers into dropdown
        populateCustomers();

        // When customer changes, update everything
        customerBox.addActionListener(e -> {
            String[] selected = (String[]) customerBox.getSelectedItem();
            if (selected != null) {
                int customerId = Integer.parseInt(selected[0]);
                loadSummary(customerId);
                loadRecommendations(customerId);
            }
        });
    }

    private void populateCustomers() {
        String sql = "SELECT CustomerId, CONCAT(FirstName, ' ', LastName) AS Name FROM Customer ORDER BY LastName";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customerBox.addItem(new String[]{
                    rs.getString("CustomerId"),
                    rs.getString("Name")
                });
            }
            customerBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                JLabel label = new JLabel(value != null ? value[1] : "");
                if (isSelected) label.setBackground(list.getSelectionBackground());
                label.setOpaque(true);
                return label;
            });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
        // Trigger load for first customer
        if (customerBox.getItemCount() > 0) {
            String[] first = (String[]) customerBox.getItemAt(0);
            loadSummary(Integer.parseInt(first[0]));
            loadRecommendations(Integer.parseInt(first[0]));
        }
    }

    private void loadSummary(int customerId) {
        String sql = "SELECT ROUND(SUM(Total), 2) AS TotalSpent, COUNT(*) AS TotalPurchases, " +
                     "MAX(InvoiceDate) AS LastPurchase FROM Invoice WHERE CustomerId = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalSpentLabel.setText("$" + rs.getString("TotalSpent"));
                totalPurchasesLabel.setText(rs.getString("TotalPurchases"));
                lastPurchaseLabel.setText(rs.getString("LastPurchase"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        String genreSql = "SELECT g.Name, COUNT(*) AS cnt " +
                          "FROM InvoiceLine il " +
                          "JOIN Invoice i ON il.InvoiceId = i.InvoiceId " +
                          "JOIN Track t ON il.TrackId = t.TrackId " +
                          "JOIN Genre g ON t.GenreId = g.GenreId " +
                          "WHERE i.CustomerId = ? " +
                          "GROUP BY g.GenreId, g.Name " +
                          "ORDER BY cnt DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(genreSql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                favouriteGenreLabel.setText(rs.getString("Name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadRecommendations(int customerId) {
        recommendModel.setRowCount(0);
        String sql = "SELECT t.Name, al.Title AS Album, g.Name AS Genre, t.UnitPrice " +
                     "FROM Track t " +
                     "JOIN Album al ON t.AlbumId = al.AlbumId " +
                     "JOIN Genre g ON t.GenreId = g.GenreId " +
                     "JOIN ( " +
                     "    SELECT t2.GenreId, COUNT(*) AS cnt " +
                     "    FROM InvoiceLine il " +
                     "    JOIN Invoice i ON il.InvoiceId = i.InvoiceId " +
                     "    JOIN Track t2 ON il.TrackId = t2.TrackId " +
                     "    WHERE i.CustomerId = ? " +
                     "    GROUP BY t2.GenreId " +
                     "    ORDER BY cnt DESC " +
                     ") AS topGenres ON t.GenreId = topGenres.GenreId " +
                     "WHERE t.TrackId NOT IN ( " +
                     "    SELECT il2.TrackId FROM InvoiceLine il2 " +
                     "    JOIN Invoice i2 ON il2.InvoiceId = i2.InvoiceId " +
                     "    WHERE i2.CustomerId = ? " +
                     ") " +
                     "ORDER BY topGenres.cnt DESC, RAND() LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recommendModel.addRow(new Object[]{
                    rs.getString("Name"),
                    rs.getString("Album"),
                    rs.getString("Genre"),
                    "$" + rs.getDouble("UnitPrice")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}