package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReportPanel extends JPanel {

    private DefaultTableModel tableModel;

    public ReportPanel() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Genre Revenue Report", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        title.setForeground(new java.awt.Color(0, 102, 204));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Genre", "Total Revenue (USD)"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadReport();
    }

    private void loadReport() {
        tableModel.setRowCount(0);
        String sql = "SELECT g.Name AS Genre, ROUND(SUM(il.UnitPrice * il.Quantity), 2) AS Revenue " +
                     "FROM InvoiceLine il " +
                     "JOIN Track t ON il.TrackId = t.TrackId " +
                     "JOIN Genre g ON t.GenreId = g.GenreId " +
                     "GROUP BY g.GenreId, g.Name " +
                     "ORDER BY Revenue DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("Genre"),
                    rs.getDouble("Revenue")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}