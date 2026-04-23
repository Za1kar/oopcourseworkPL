package auth;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthService {

    private static final String USERS_PATH = "data/users.txt";
    private List<User> users = new ArrayList<>();
    private User currentUser = null;

    public AuthService() {
        loadUsers();
        ensureDefaultAdmin();
    }

    // LOGIN
    public boolean login(String username, String password) {
        Optional<User> found = users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username)
                        && u.getPassword().equals(password))
                .findFirst();

        if (found.isPresent()) {
            currentUser = found.get();
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // REGISTER a regular user
    public boolean register(String username, String password) {
        if (username.isBlank() || password.isBlank()) return false;
        boolean exists = users.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) return false;

        User newUser = new User(username, password, "USER");
        users.add(newUser);
        saveUsers();
        return true;
    }

    // Only admins can create other admins
    public boolean registerAdmin(String username, String password) {
        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) return false;
        if (username.isBlank() || password.isBlank()) return false;

        boolean exists = users.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) return false;

        Admin admin = new Admin(username, password);
        users.add(admin);
        saveUsers();
        return true;
    }

    // ─── Persistence ─────────────────────────────────────────────────────────

    private void saveUsers() {
        new File("data").mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_PATH))) {
            for (User u : users) {
                writer.write(u.getRole() + "," + u.getUsername() + "," + u.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save users: " + e.getMessage());
        }
    }

    private void loadUsers() {
        File file = new File(USERS_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue;
                String role     = parts[0].trim();
                String username = parts[1].trim();
                String password = parts[2].trim();

                if ("ADMIN".equals(role)) {
                    users.add(new Admin(username, password));
                } else {
                    users.add(new User(username, password, "USER"));
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load users: " + e.getMessage());
        }
    }

    // Creates default admin if no users exist
    private void ensureDefaultAdmin() {
        if (users.isEmpty()) {
            users.add(new Admin("admin", "admin123"));
            saveUsers();
            System.out.println("Default admin created: admin / admin123");
        }
    }
}