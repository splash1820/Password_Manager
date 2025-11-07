package Status200.org;
import javax.swing.*;

import Status200.org.Components.LoginUI;
import Status200.org.Components.SideMenu;

import java.awt.*;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.stream.Collectors;

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
        cardLayout.show(contentPanel, "Dashboard");
    }

    // === Pages ===
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Dashboard", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        statsPanel.add(title);
        statsPanel.add(Box.createVerticalStrut(15));

        // Fetch counts
        Repository repo = Repository.getInstance();
        int total = repo.totalCount();
        long strong = repo.strengthCounts().getOrDefault(PasswordEntry.Strength.STRONG, 0L);
        long medium = repo.strengthCounts().getOrDefault(PasswordEntry.Strength.MEDIUM, 0L);
        long weak = repo.strengthCounts().getOrDefault(PasswordEntry.Strength.WEAK, 0L);

        statsPanel.add(makeStatRow("Total passwords", total));
        statsPanel.add(makeStatRow("Strong", (int) strong));
        statsPanel.add(makeStatRow("Medium", (int) medium));
        statsPanel.add(makeStatRow("Weak", (int) weak));

        panel.add(statsPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createPasswordsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top controls: Add new, Sort options
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add New");
        JButton sortAsc = new JButton("Sort: Description ↑");
        JButton sortDesc = new JButton("Sort: Description ↓");
        JButton sortStrength = new JButton("Sort: Strength");

        top.add(addBtn);
        top.add(sortAsc);
        top.add(sortDesc);
        top.add(sortStrength);
        panel.add(top, BorderLayout.NORTH);

        // Table
        Repository repo = Repository.getInstance();
        PasswordTableModel model = new PasswordTableModel(repo.getAll());
        JTable table = new JTable(model);

        // Render password column masked by default
        table.getColumnModel().getColumn(2).setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            PasswordEntry e = model.getAt(row);
            String pw = e.isVisible() ? e.getPassword() : mask(e.getPassword());
            JLabel lbl = new JLabel(pw);
            if (e.getStrength() == PasswordEntry.Strength.STRONG) lbl.setIcon(new ImageIcon()); // placeholder
            return lbl;
        });

        // Strength column custom renderer showing text and small colored indicator
        table.getColumnModel().getColumn(3).setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            PasswordEntry e = model.getAt(row);
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            p.setOpaque(true);
            p.setBackground(isSelected ? tbl.getSelectionBackground() : tbl.getBackground());
            JLabel dot = new JLabel("\u25CF");
            switch (e.getStrength()) {
                case STRONG: dot.setForeground(Color.GREEN.darker()); break;
                case MEDIUM: dot.setForeground(Color.ORANGE.darker()); break;
                default: dot.setForeground(Color.RED.darker()); break;
            }
            JLabel txt = new JLabel(" " + e.getStrength().name());
            p.add(dot);
            p.add(txt);
            return p;
        });

        // Actions column: Edit / Delete / Eye toggle
//        table.getColumnModel().getColumn(4).setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
//            PasswordEntry e = model.getAt(row);
//            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
//            p.setOpaque(true);
//            p.setBackground(isSelected ? tbl.getSelectionBackground() : tbl.getBackground());
//
////            JButton eye = new JButton(e.isVisible() ? "\uD83D\uDC41" : "\uD83D\uDC41"); // reuse icon text; you can replace with icons
////            eye.setToolTipText("Toggle visibility");
////            eye.addActionListener(a -> {
////                e.setVisible(!e.isVisible());
////                Repository.getInstance().update(e);
////                model.fireTableRowsUpdated(row, row);
////            });
//
//            JButton edit = new JButton("Edit");
//            edit.addActionListener(a -> openEditDialog(model.getAt(row), model, row));
//
//            JButton del = new JButton("Delete");
//            del.addActionListener(a -> {
//                int res = JOptionPane.showConfirmDialog(this,
//                        "Delete entry \"" + e.getDescription() + "\"?", "Confirm delete",
//                        JOptionPane.YES_NO_OPTION);
//                if (res == JOptionPane.YES_OPTION) {
//                    Repository.getInstance().delete(e.getId());
//                    model.setData(Repository.getInstance().getAll());
//                    // refresh dashboard counts
//                    contentPanel.add(createDashboardPanel(), "Dashboard"); // refresh on next show
//                }
//            });
//
//            //p.add(eye);
//            p.add(edit);
//            p.add(del);
//            return p;
//        });

        table.setRowHeight(28);
        JScrollPane sc = new JScrollPane(table);
        panel.add(sc, BorderLayout.CENTER);

        // Add new action
        addBtn.addActionListener(a -> {
            PasswordEntry newE = new PasswordEntry("New description", "", "", PasswordEntry.Strength.WEAK);
            // open edit dialog immediately
            openEditDialog(newE, model, -1);
        });

        sortAsc.addActionListener(a -> {
            java.util.List<PasswordEntry> sorted = Repository.getInstance().getAll();
            sorted.sort((x,y)-> x.getDescription().compareToIgnoreCase(y.getDescription()));
            model.setData(sorted);
        });
        sortDesc.addActionListener(a -> {
            java.util.List<PasswordEntry> sorted = Repository.getInstance().getAll();
            sorted.sort((x,y)-> y.getDescription().compareToIgnoreCase(x.getDescription()));
            model.setData(sorted);
        });
        sortStrength.addActionListener(a -> {
            java.util.List<PasswordEntry> sorted = Repository.getInstance().getAll();
            sorted.sort((x,y)-> y.getStrength().compareTo(x.getStrength()));
            model.setData(sorted);
        });

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        JButton switchUser = new JButton("Switch User");
        switchUser.addActionListener(a -> {
            // placeholder: we will implement authentication later
            JOptionPane.showMessageDialog(this, "Switch user feature will be implemented with authentication.");
        });

        JButton forgot = new JButton("Forgot Password");
        forgot.addActionListener(a -> {
            JOptionPane.showMessageDialog(this, "Forgot password flow will be implemented during authentication step.");
        });

        panel.add(switchUser);
        panel.add(Box.createVerticalStrut(8));
        panel.add(forgot);

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


    //utils:
    private JPanel makeStatRow(String label, int value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JLabel left = new JLabel(label);
        left.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel right = new JLabel(String.valueOf(value), SwingConstants.RIGHT);
        right.setFont(new Font("Segoe UI", Font.BOLD, 14));

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        row.add(Box.createVerticalStrut(6), BorderLayout.SOUTH);

        return row;
    }


    private String mask(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(12, s.length()); i++) sb.append("\u2022"); // bullet
        return sb.toString();
    }

    // Dialog to edit/create a password entry
    private void openEditDialog(PasswordEntry e, PasswordTableModel model, int rowIndexIfExisting) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JTextField desc = new JTextField(e.getDescription());
        JTextField user = new JTextField(e.getUsername());
        JPasswordField pass = new JPasswordField(e.getPassword());
        JButton generateBtn = new JButton("Generate Strong Password");

        form.add(new JLabel("Description:"));
        form.add(desc);
        form.add(new JLabel("Username (optional):"));
        form.add(user);
        form.add(new JLabel("Password:"));
        form.add(pass);
        form.add(Box.createVerticalStrut(5));
        form.add(generateBtn);

        // Action for generating strong password
        generateBtn.addActionListener(ev -> {
            String strongPass = generateStrongPassword(14); // you can adjust length
            pass.setText(strongPass);
            JOptionPane.showMessageDialog(form,
                    "Strong password generated and filled in.",
                    "Password Generated",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        int res = JOptionPane.showConfirmDialog(this, form,
                rowIndexIfExisting >= 0 ? "Edit Entry" : "New Entry",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            e.setDescription(desc.getText());
            e.setUsername(user.getText());
            e.setPassword(new String(pass.getPassword()));
            e.setStrength(Services.estimateStrength(e.getPassword()));

            if (rowIndexIfExisting >= 0) {
                Repository.getInstance().update(e);
            } else {
                Repository.getInstance().add(e);
            }
            model.setData(Repository.getInstance().getAll());
            contentPanel.add(createDashboardPanel(), "Dashboard");
        }
    }

    // Utility to generate a strong password
    private String generateStrongPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "!@#$%^&*()-_=+[]{};:,.<>?";
        String all = upper + lower + digits + symbols;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // ensure at least one from each category
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(symbols.charAt(random.nextInt(symbols.length())));

        // fill remaining length
        for (int i = 4; i < length; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // shuffle the characters to avoid predictable pattern
        java.util.List<Character> chars = password.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(chars, random);
        return chars.stream().map(String::valueOf).collect(Collectors.joining());
    }

}
