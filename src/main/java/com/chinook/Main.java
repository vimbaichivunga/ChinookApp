package com.chinook;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chinook Music Store - u25136608");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750);

            JTabbedPane tabs = new JTabbedPane();

            tabs.addTab("Employees", new EmployeesPanel());
            tabs.addTab("Tracks", new TracksPanel());
            tabs.addTab("Report", new ReportPanel());
            tabs.addTab("Notifications", new NotificationsPanel());
            tabs.addTab("Recommendations", new RecommendationsPanel());

            frame.add(tabs);
            frame.setLocationRelativeTo(null);
            frame.setMinimumSize(new Dimension(800, 600));
            frame.setVisible(true);
        });
    }
}