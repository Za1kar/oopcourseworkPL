package auth;

public class User {

    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole()     { return role; }

    public void setPassword(String password) { this.password = password; }

    public boolean canWrite() {
        return true;
    }

    public boolean canDelete() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("User[%s, role=%s]", username, role);
    }
}