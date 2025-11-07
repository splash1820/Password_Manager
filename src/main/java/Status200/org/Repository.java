package Status200.org;
import Status200.org.PasswordEntry;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Repository {
    private static Repository instance;
    private Connection conn;

    private Repository() {
        connect();
        initTable();
    }

    public static synchronized Repository getInstance() {
        if (instance == null) instance = new Repository();
        return instance;
    }

    private void connect() {
        try {
            String url = "jdbc:REMOVEDql://localhost:5432/password_manager";
            String user = "REMOVED";   // change to your username
            String pass = "REMOVED";      // change to your password
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not connect to Postgres");
        }
    }

    private void initTable() {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS passwords(
                    id UUID PRIMARY KEY,
                    description TEXT NOT NULL,
                    username TEXT,
                    password TEXT NOT NULL,
                    strength VARCHAR(10) NOT NULL
                )
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<PasswordEntry> getAll() {
        List<PasswordEntry> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM passwords ORDER BY description")) {
            while (rs.next()) {
                String stored = rs.getString("password");
                String plaintext;
                try {
                    plaintext = CryptoUtil.decrypt(stored);
                } catch (RuntimeException ex) {
                    // If decryption fails (e.g., pre-migration plaintext rows), fall back to raw stored value
                    // but log so you can detect rows needing migration.
                    System.err.println("Warning: decryption failed for id=" + rs.getString("id") + " - using raw value.");
                    plaintext = stored;
                }
                PasswordEntry e = new PasswordEntry(
                        rs.getString("description"),
                        rs.getString("username"),
                        plaintext,
                        PasswordEntry.Strength.valueOf(rs.getString("strength"))
                );
                // override auto-generated UUID with DB one
                e.setVisible(false);
                // manually set id field via reflection (or change PasswordEntry to accept id)
                try {
                    var idField = PasswordEntry.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(e, rs.getString("id"));
                } catch (Exception ignored) {}
                list.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void add(PasswordEntry entry) {
        String sql = "INSERT INTO passwords(id, description, username, password, strength) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(entry.getId()));
            ps.setString(2, entry.getDescription());
            ps.setString(3, entry.getUsername());
            ps.setString(4, CryptoUtil.encrypt(entry.getPassword()));
            ps.setString(5, entry.getStrength().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void update(PasswordEntry entry) {
        String sql = "UPDATE passwords SET description=?, username=?, password=?, strength=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getDescription());
            ps.setString(2, entry.getUsername());
            ps.setString(3, CryptoUtil.encrypt(entry.getPassword()));
            ps.setString(4, entry.getStrength().name());
            ps.setObject(5, UUID.fromString(entry.getId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void delete(String id) {
        String sql = "DELETE FROM passwords WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.fromString(id));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Map<PasswordEntry.Strength, Long> strengthCounts() {
        Map<PasswordEntry.Strength, Long> counts = new EnumMap<>(PasswordEntry.Strength.class);
        String sql = "SELECT strength, COUNT(*) FROM passwords GROUP BY strength";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PasswordEntry.Strength s = PasswordEntry.Strength.valueOf(rs.getString(1));
                counts.put(s, rs.getLong(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public synchronized int totalCount() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM passwords")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
