package db;

import model.ElectronicsItem;
import model.FoodItem;
import model.Item;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private static final String URL = "jdbc:sqlite:data/inventory.db";

    public DatabaseService() {
        initTable();
    }

    private Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver not found");
        }
        return DriverManager.getConnection(URL);
    }

    // Creates table if it doesn't exist
    private void initTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS items (
                    id             TEXT PRIMARY KEY,
                    type           TEXT NOT NULL,
                    name           TEXT NOT NULL,
                    quantity       INTEGER NOT NULL,
                    price          REAL NOT NULL,
                    category       TEXT NOT NULL,
                    extra1         TEXT,
                    extra2         TEXT
                );
                """;
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("DB init error: " + e.getMessage());
        }
    }

    // CREATE
    public boolean insertItem(Item item) {
        String sql = "INSERT INTO items(id, type, name, quantity, price, category, extra1, extra2) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, item);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Insert error: " + e.getMessage());
            return false;
        }
    }

    // READ ALL
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Item item = mapRow(rs);
                if (item != null) items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Read error: " + e.getMessage());
        }
        return items;
    }

    // UPDATE
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET name=?, quantity=?, price=?, category=?, extra1=?, extra2=? WHERE id=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getQuantity());
            ps.setDouble(3, item.getPrice());
            ps.setString(4, item.getCategory());

            if (item instanceof ElectronicsItem e) {
                ps.setString(5, e.getBrand());
                ps.setString(6, String.valueOf(e.getWarrantyMonths()));
            } else if (item instanceof FoodItem f) {
                ps.setString(5, f.getExpiryDate().toString());
                ps.setString(6, String.valueOf(f.isOrganic()));
            } else {
                ps.setString(5, null);
                ps.setString(6, null);
            }

            ps.setString(7, item.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean deleteItem(String id) {
        String sql = "DELETE FROM items WHERE id=?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete error: " + e.getMessage());
            return false;
        }
    }

    // DELETE ALL
    public void clearAll() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM items");
        } catch (SQLException e) {
            System.err.println("Clear error: " + e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void fillStatement(PreparedStatement ps, Item item) throws SQLException {
        ps.setString(1, item.getId());
        ps.setString(2, item.getItemType());
        ps.setString(3, item.getName());
        ps.setInt(4, item.getQuantity());
        ps.setDouble(5, item.getPrice());
        ps.setString(6, item.getCategory());

        if (item instanceof ElectronicsItem e) {
            ps.setString(7, e.getBrand());
            ps.setString(8, String.valueOf(e.getWarrantyMonths()));
        } else if (item instanceof FoodItem f) {
            ps.setString(7, f.getExpiryDate().toString());
            ps.setString(8, String.valueOf(f.isOrganic()));
        } else {
            ps.setString(7, null);
            ps.setString(8, null);
        }
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        String type     = rs.getString("type");
        String id       = rs.getString("id");
        String name     = rs.getString("name");
        int quantity    = rs.getInt("quantity");
        double price    = rs.getDouble("price");
        String category = rs.getString("category");
        String extra1   = rs.getString("extra1");
        String extra2   = rs.getString("extra2");

        return switch (type) {
            case "Electronics" -> new ElectronicsItem(id, name, quantity, price, category,
                    extra1, Integer.parseInt(extra2));
            case "Food" -> new FoodItem(id, name, quantity, price, category,
                    LocalDate.parse(extra1), Boolean.parseBoolean(extra2));
            default -> null;
        };
    }
}