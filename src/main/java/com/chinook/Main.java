package com.chinook;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chinook Music Store - u25136608");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750);
            frame.setMinimumSize(new Dimension(800, 600));

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(new Font("Arial", Font.BOLD, 13));

            tabs.addTab("👥 Employees", new EmployeesPanel());
            tabs.addTab("🎵 Tracks", new TracksPanel());
            tabs.addTab("📊 Report", new ReportPanel());
            tabs.addTab("🔔 Notifications", new NotificationsPanel());
            tabs.addTab("⭐ Recommendations", new RecommendationsPanel());

            frame.add(tabs);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}