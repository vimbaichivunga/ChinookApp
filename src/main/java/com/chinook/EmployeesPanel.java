package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EmployeesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public EmployeesPanel() {
        setLayout(new BorderLayout());

        // Search bar at the top
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search by name or city:"));
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by first name, last name or city");
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Clear");
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(clearBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"First Name", "Last Name", "Title", "City", "Country", "Phone", "Supervisor"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Load all employees on startup
        loadEmployees("");

        // Button actions
        searchBtn.addActionListener(e -> loadEmployees(searchField.getText().trim()));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadEmployees("");
        });
    }

    private void loadEmployees(String filter) {
        tableModel.setRowCount(0);
        String sql = "SELECT e.FirstName, e.LastName, e.Title, e.City, e.Country, e.Phone, " +
                     "CONCAT(IFNULL(m.FirstName, ''), ' ', IFNULL(m.LastName, '')) AS Supervisor " +
                     "FROM Employee e LEFT JOIN Employee m ON e.ReportsTo = m.EmployeeId " +
                     "WHERE e.FirstName LIKE ? OR e.LastName LIKE ? OR e.City LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String f = "%" + filter + "%";
            stmt.setString(1, f);
            stmt.setString(2, f);
            stmt.setString(3, f);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("Title"),
                    rs.getString("City"),
                    rs.getString("Country"),
                    rs.getString("Phone"),
                    rs.getString("Supervisor")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}