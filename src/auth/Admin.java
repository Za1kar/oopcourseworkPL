package auth;

public class Admin extends User {

    public Admin(String username, String password) {
        super(username, password, "ADMIN");
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("Admin[%s, role=%s]", getUsername(), getRole());
    }
}