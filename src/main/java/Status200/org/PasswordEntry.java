package Status200.org;


import java.util.UUID;

public class PasswordEntry {
    public enum Strength { STRONG, MEDIUM, WEAK }

    private final String id;
    private String description;
    private String username;
    private String password;
    private Strength strength;
    private boolean visible; // whether password is shown

    public PasswordEntry(String description, String username, String password, Strength strength) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.username = username;
        this.password = password;
        this.strength = strength;
        this.visible = false;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Strength getStrength() { return strength; }
    public void setStrength(Strength strength) { this.strength = strength; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}

