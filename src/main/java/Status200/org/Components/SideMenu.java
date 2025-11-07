package Status200.org.Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class SideMenu extends JPanel {
    private boolean isExpanded = true;
    private final int expandedWidth = 200;
    private final int collapsedWidth = 50;
    private final JPanel buttonPanel;
    private final JLabel toggleLabel;
    private final Consumer<String> pageChangeCallback;

    public SideMenu(Consumer<String> pageChangeCallback) {
        this.pageChangeCallback = pageChangeCallback;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(expandedWidth, getHeight()));
        setBackground(new Color(45, 45, 45));

        toggleLabel = new JLabel("\u2630", SwingConstants.CENTER); // ☰
        toggleLabel.setForeground(Color.WHITE);
        toggleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        toggleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        toggleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleMenu();
            }
        });
        add(toggleLabel, BorderLayout.NORTH);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(45, 45, 45));

        addMenuButton("Dashboard");
        addMenuButton("Passwords");
        addMenuButton("Settings");
        addMenuButton("Logout");

        add(buttonPanel, BorderLayout.CENTER);
    }

    private void addMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (pageChangeCallback != null)
                pageChangeCallback.accept(text);
        });

        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btn);
    }

    private void toggleMenu() {
        isExpanded = !isExpanded;
        int targetWidth = isExpanded ? expandedWidth : collapsedWidth;
        setPreferredSize(new Dimension(targetWidth, getHeight()));
        revalidate();
        repaint();
    }
}
