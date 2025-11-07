package Status200.org;
import javax.swing.*;
import java.awt.*;

import Status200.org.Components.LoginUI;
import Status200.org.Components.SideMenu;

public class UI extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SideMenu sideMenu;

    public UI() {
        setTitle("Password Manager");
        setSize(900, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize layout manager for content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add pages
        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(createPasswordsPanel(), "Passwords");
        contentPanel.add(createSettingsPanel(), "Settings");
        contentPanel.add(createLogoutPanel(), "Logout");

        // Side menu
        sideMenu = new SideMenu(this::showPage); // pass callback for navigation
        add(sideMenu, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    // === Pages ===
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Dashboard Overview", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPasswordsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Your Saved Passwords", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Settings", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLogoutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Logout Page (Under Construction)", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    // === Navigation ===
    public void showPage(String pageName) {
        cardLayout.show(contentPanel, pageName);
    }

    // === Static Method to show Login page first ===
    public static void launchApp() {
        SwingUtilities.invokeLater(() -> {
            LoginUI login = new LoginUI();
            login.setVisible(true);
        });
    }
}
