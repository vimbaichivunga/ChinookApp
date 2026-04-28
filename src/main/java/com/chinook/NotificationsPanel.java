package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class NotificationsPanel extends JPanel {

    private DefaultTableModel customerModel;
    private DefaultTableModel inactiveModel;
    private JTable customerTable;
    private JTextField searchField;

    public NotificationsPanel() {
        setLayout(new BorderLayout());

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.addTab("Customer CRUD", buildCrudPanel());
        subTabs.addTab("Inactive Customers", buildInactivePanel());
        add(subTabs, BorderLayout.CENTER);
    }

    // ─── CRUD Panel ───────────────────────────────────────────────
    private JPanel buildCrudPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn    = new JButton("+ Add Customer");
        JButton editBtn   = new JButton("✏ Edit Customer");
        JButton deleteBtn = new JButton("🗑 Delete Customer");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        panel.add(btnPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "First Name", "Last Name", "Email", "Phone", "Country"};
        customerModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        customerTable = new JTable(customerModel);
        customerTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(customerTable), BorderLayout.CENTER);

        loadCustomers();

        addBtn.addActionListener(e -> showCustomerDialog(null));
        editBtn.addActionListener(e -> {
            int row = customerTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            showCustomerDialog(row);
        });
        deleteBtn.addActionListener(e -> deleteCustomer());

        return panel;
    }

    private void loadCustomers() {
        customerModel.setRowCount(0);
        String sql = "SELECT CustomerId, FirstName, LastName, Email, Phone, Country FROM Customer ORDER BY LastName";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customerModel.addRow(new Object[]{
                    rs.getInt("CustomerId"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("Email"),
                    rs.getString("Phone"),
                    rs.getString("Country")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showCustomerDialog(Integer row) {
        boolean isEdit = row != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Customer" : "Add Customer", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        JTextField firstField   = new JTextField();
        JTextField lastField    = new JTextField();
        JTextField emailField   = new JTextField();
        JTextField phoneField   = new JTextField();
        JTextField countryField = new JTextField();

        if (isEdit) {
            firstField.setText(customerModel.getValueAt(row, 1).toString());
            lastField.setText(customerModel.getValueAt(row, 2).toString());
            emailField.setText(customerModel.getValueAt(row, 3).toString());
            phoneField.setText(customerModel.getValueAt(row, 4) != null ? customerModel.getValueAt(row, 4).toString() : "");
            countryField.setText(customerModel.getValueAt(row, 5) != null ? customerModel.getValueAt(row, 5).toString() : "");
        }

        dialog.add(new JLabel("First Name:")); dialog.add(firstField);
        dialog.add(new JLabel("Last Name:"));  dialog.add(lastField);
        dialog.add(new JLabel("Email:"));      dialog.add(emailField);
        dialog.add(new JLabel("Phone:"));      dialog.add(phoneField);
        dialog.add(new JLabel("Country:"));    dialog.add(countryField);

        JButton saveBtn   = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        dialog.add(saveBtn);
        dialog.add(cancelBtn);

        saveBtn.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                if (isEdit) {
                    int id = (int) customerModel.getValueAt(row, 0);
                    String sql = "UPDATE Customer SET FirstName=?, LastName=?, Email=?, Phone=?, Country=? WHERE CustomerId=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, firstField.getText());
                    stmt.setString(2, lastField.getText());
                    stmt.setString(3, emailField.getText());
                    stmt.setString(4, phoneField.getText());
                    stmt.setString(5, countryField.getText());
                    stmt.setInt(6, id);
                    stmt.executeUpdate();
                } else {
                    String sql = "INSERT INTO Customer (FirstName, LastName, Email, Phone, Country) VALUES (?,?,?,?,?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, firstField.getText());
                    stmt.setString(2, lastField.getText());
                    stmt.setString(3, emailField.getText());
                    stmt.setString(4, phoneField.getText());
                    stmt.setString(5, countryField.getText());
                    stmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(dialog, "Saved successfully!");
                dialog.dispose();
                loadCustomers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void deleteCustomer() {
        int row = customerTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
        int id = (int) customerModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this customer?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Customer WHERE CustomerId=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            loadCustomers();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ─── Inactive Customers Panel ─────────────────────────────────
    private JPanel buildInactivePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton clearBtn  = new JButton("Clear");
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(clearBtn);
        panel.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "First Name", "Last Name", "Email", "Country", "Last Invoice"};
        inactiveModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable inactiveTable = new JTable(inactiveModel);
        inactiveTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(inactiveTable), BorderLayout.CENTER);

        loadInactiveCustomers("");

        searchBtn.addActionListener(e -> loadInactiveCustomers(searchField.getText().trim()));
        clearBtn.addActionListener(e -> { searchField.setText(""); loadInactiveCustomers(""); });

        return panel;
    }

    private void loadInactiveCustomers(String filter) {
        inactiveModel.setRowCount(0);
        String sql = "SELECT c.CustomerId, c.FirstName, c.LastName, c.Email, c.Country, " +
                     "MAX(i.InvoiceDate) AS LastInvoice " +
                     "FROM Customer c LEFT JOIN Invoice i ON c.CustomerId = i.CustomerId " +
                     "GROUP BY c.CustomerId, c.FirstName, c.LastName, c.Email, c.Country " +
                     "HAVING LastInvoice IS NULL OR LastInvoice < DATE_SUB(NOW(), INTERVAL 2 YEAR) " +
                     "AND (c.FirstName LIKE ? OR c.LastName LIKE ? OR c.Email LIKE ?) " +
                     "ORDER BY LastInvoice ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String f = "%" + filter + "%";
            stmt.setString(1, f);
            stmt.setString(2, f);
            stmt.setString(3, f);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                inactiveModel.addRow(new Object[]{
                    rs.getInt("CustomerId"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("Email"),
                    rs.getString("Country"),
                    rs.getString("LastInvoice")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}