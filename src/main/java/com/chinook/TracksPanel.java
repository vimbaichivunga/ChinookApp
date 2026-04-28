package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TracksPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;

    public TracksPanel() {
        setLayout(new BorderLayout());

        // Add Track button at the top
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("+ Add New Track");
        topPanel.add(addBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Album", "Genre", "Media Type", "Composer", "Milliseconds", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadTracks();

        addBtn.addActionListener(e -> showAddTrackDialog());
    }

    private void loadTracks() {
        tableModel.setRowCount(0);
        String sql = "SELECT t.TrackId, t.Name, al.Title AS Album, g.Name AS Genre, " +
                     "m.Name AS MediaType, t.Composer, t.Milliseconds, t.UnitPrice " +
                     "FROM Track t " +
                     "JOIN Album al ON t.AlbumId = al.AlbumId " +
                     "JOIN Genre g ON t.GenreId = g.GenreId " +
                     "JOIN MediaType m ON t.MediaTypeId = m.MediaTypeId " +
                     "ORDER BY t.TrackId DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("TrackId"),
                    rs.getString("Name"),
                    rs.getString("Album"),
                    rs.getString("Genre"),
                    rs.getString("MediaType"),
                    rs.getString("Composer"),
                    rs.getInt("Milliseconds"),
                    rs.getDouble("UnitPrice")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showAddTrackDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Track", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(0, 2, 10, 10));

        // Fields
        JTextField nameField = new JTextField();
        JTextField composerField = new JTextField();
        JTextField msField = new JTextField("0");
        JTextField priceField = new JTextField("0.99");

        // Dropdowns
        JComboBox<String[]> albumBox = new JComboBox<>();
        JComboBox<String[]> genreBox = new JComboBox<>();
        JComboBox<String[]> mediaBox = new JComboBox<>();

        // Populate dropdowns
        populateCombo(albumBox, "SELECT AlbumId, Title FROM Album ORDER BY Title");
        populateCombo(genreBox, "SELECT GenreId, Name FROM Genre ORDER BY Name");
        populateCombo(mediaBox, "SELECT MediaTypeId, Name FROM MediaType ORDER BY Name");

        dialog.add(new JLabel("Track Name:")); dialog.add(nameField);
        dialog.add(new JLabel("Album:")); dialog.add(albumBox);
        dialog.add(new JLabel("Genre:")); dialog.add(genreBox);
        dialog.add(new JLabel("Media Type:")); dialog.add(mediaBox);
        dialog.add(new JLabel("Composer:")); dialog.add(composerField);
        dialog.add(new JLabel("Milliseconds:")); dialog.add(msField);
        dialog.add(new JLabel("Price:")); dialog.add(priceField);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        dialog.add(saveBtn);
        dialog.add(cancelBtn);

        saveBtn.addActionListener(e -> {
            String[] album = (String[]) albumBox.getSelectedItem();
            String[] genre = (String[]) genreBox.getSelectedItem();
            String[] media = (String[]) mediaBox.getSelectedItem();
            String sql = "INSERT INTO Track (Name, AlbumId, MediaTypeId, GenreId, Composer, Milliseconds, Bytes, UnitPrice) " +
                         "VALUES (?, ?, ?, ?, ?, ?, 0, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nameField.getText());
                stmt.setInt(2, Integer.parseInt(album[0]));
                stmt.setInt(3, Integer.parseInt(media[0]));
                stmt.setInt(4, Integer.parseInt(genre[0]));
                stmt.setString(5, composerField.getText());
                stmt.setInt(6, Integer.parseInt(msField.getText()));
                stmt.setDouble(7, Double.parseDouble(priceField.getText()));
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Track added successfully!");
                dialog.dispose();
                loadTracks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void populateCombo(JComboBox<String[]> box, String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                box.addItem(new String[]{rs.getString(1), rs.getString(2)});
            }
            box.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                JLabel label = new JLabel(value != null ? value[1] : "");
                if (isSelected) label.setBackground(list.getSelectionBackground());
                label.setOpaque(true);
                return label;
            });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}