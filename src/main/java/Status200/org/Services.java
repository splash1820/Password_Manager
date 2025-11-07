package Status200.org;

// Services.java
public class Services {
    // Very simple heuristic for demo; you can replace later
    public static PasswordEntry.Strength estimateStrength(String password) {
        if (password == null) return PasswordEntry.Strength.WEAK;
        int score = 0;
        if (password.length() >= 12) score += 2;
        else if (password.length() >= 8) score += 1;
        if (password.matches(".*[0-9].*")) score += 1;
        if (password.matches(".*[a-z].*")) score += 1;
        if (password.matches(".*[A-Z].*")) score += 1;
        if (password.matches(".*[^a-zA-Z0-9].*")) score += 1;

        if (score >= 5) return PasswordEntry.Strength.STRONG;
        if (score >= 3) return PasswordEntry.Strength.MEDIUM;
        return PasswordEntry.Strength.WEAK;
    }
}
