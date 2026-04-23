package ui;

import auth.AuthService;
import auth.User;
import db.DatabaseService;
import model.ElectronicsItem;
import model.FoodItem;
import model.Item;
import service.FileService;
import service.InventoryService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);
    private final InventoryService inventoryService;
    private final FileService fileService;
    private final DatabaseService databaseService;
    private final AuthService authService;

    public ConsoleUI(InventoryService inventoryService, FileService fileService,
                     DatabaseService databaseService, AuthService authService) {
        this.inventoryService = inventoryService;
        this.fileService = fileService;
        this.databaseService = databaseService;
        this.authService = authService;
    }

    public void start() {
        System.out.println("===========================================");
        System.out.println("   Welcome to Inventory Tracker System    ");
        System.out.println("===========================================");
        authMenu();
    }

    // ─── Auth Menu ────────────────────────────────────────────────────────────

    private void authMenu() {
        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> { if (handleLogin()) mainMenu(); }
                case "2" -> handleRegister();
                case "0" -> { System.out.println("Goodbye!"); return; }
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    private boolean handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (authService.login(username, password)) {
            System.out.println("Welcome, " + authService.getCurrentUser().getUsername()
                    + "! [" + authService.getCurrentUser().getRole() + "]");
            loadDataFromDb();
            return true;
        }
        System.out.println("Invalid credentials. Try again.");
        return false;
    }

    private void handleRegister() {
        System.out.print("New username: ");
        String username = scanner.nextLine().trim();
        System.out.print("New password: ");
        String password = scanner.nextLine().trim();

        if (authService.register(username, password)) {
            System.out.println("Registered successfully! You can now login.");
        } else {
            System.out.println("Username already exists or invalid input.");
        }
    }

    // ─── Main Menu ────────────────────────────────────────────────────────────

    private void mainMenu() {
        while (true) {
            User user = authService.getCurrentUser();
            System.out.println("\n===========================================");
            System.out.println(" Logged in as: " + user.getUsername() + " [" + user.getRole() + "]");
            System.out.println("===========================================");
            System.out.println("1. View all items");
            System.out.println("2. Search item by name");
            System.out.println("3. Add item");
            System.out.println("4. Update item");
            if (user.canDelete()) System.out.println("5. Delete item");
            System.out.println("6. Filter by category");
            System.out.println("7. View statistics");
            System.out.println("8. Import / Export");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllItems();
                case "2" -> searchItem();
                case "3" -> addItem();
                case "4" -> updateItem();
                case "5" -> { if (user.canDelete()) deleteItem();
                else System.out.println("Access denied."); }
                case "6" -> filterByCategory();
                case "7" -> viewStats();
                case "8" -> importExportMenu();
                case "0" -> { authService.logout(); return; }
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    private void viewAllItems() {
        List<Item> items = inventoryService.getAllItems();
        if (items.isEmpty()) { System.out.println("No items found."); return; }
        System.out.println("\n--- All Items (" + items.size() + ") ---");
        items.forEach(System.out::println);
    }

    private void searchItem() {
        System.out.print("Enter name to search: ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) { System.out.println("Name cannot be empty."); return; }

        List<Item> results = inventoryService.searchByName(name);
        if (results.isEmpty()) { System.out.println("No items found."); return; }
        results.forEach(System.out::println);
    }

    private void addItem() {
        System.out.println("\n-- Add Item --");
        System.out.println("1. Electronics");
        System.out.println("2. Food");
        System.out.print("Type: ");
        String type = scanner.nextLine().trim();

        String id = promptNonEmpty("ID");
        if (inventoryService.findById(id).isPresent()) {
            System.out.println("Item with this ID already exists.");
            return;
        }
        String name     = promptNonEmpty("Name");
        int quantity    = promptInt("Quantity", 0, Integer.MAX_VALUE);
        double price    = promptDouble("Price", 0);
        String category = promptNonEmpty("Category");

        try {
            if ("1".equals(type)) {
                String brand   = promptNonEmpty("Brand");
                int warranty   = promptInt("Warranty (months)", 0, 120);
                ElectronicsItem item = new ElectronicsItem(id, name, quantity, price, category, brand, warranty);
                inventoryService.addItem(item);
                databaseService.insertItem(item);
            } else if ("2".equals(type)) {
                LocalDate expiry = promptDate("Expiry date (YYYY-MM-DD)");
                System.out.print("Is organic? (yes/no): ");
                boolean organic = scanner.nextLine().trim().equalsIgnoreCase("yes");
                FoodItem item = new FoodItem(id, name, quantity, price, category, expiry, organic);
                inventoryService.addItem(item);
                databaseService.insertItem(item);
            } else {
                System.out.println("Invalid type."); return;
            }
            fileService.saveToJson(inventoryService.getAllItems());
            System.out.println("Item added successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateItem() {
        System.out.print("Enter ID of item to update: ");
        String id = scanner.nextLine().trim();
        Optional<Item> found = inventoryService.findById(id);
        if (found.isEmpty()) { System.out.println("Item not found."); return; }

        System.out.println("Current: " + found.get());
        System.out.println("(Press Enter to keep current value)");

        System.out.print("New name: ");
        String name = scanner.nextLine().trim();
        System.out.print("New quantity (-1 to skip): ");
        String qtyStr = scanner.nextLine().trim();
        int qty = qtyStr.isBlank() || qtyStr.equals("-1") ? -1 : Integer.parseInt(qtyStr);
        System.out.print("New price (-1 to skip): ");
        String priceStr = scanner.nextLine().trim();
        double price = priceStr.isBlank() || priceStr.equals("-1") ? -1 : Double.parseDouble(priceStr);

        inventoryService.updateItem(id, name, qty, price);
        Optional<Item> updated = inventoryService.findById(id);
        updated.ifPresent(databaseService::updateItem);

        try { fileService.saveToJson(inventoryService.getAllItems()); } catch (Exception e) { /* ignore */ }
        System.out.println("Item updated successfully!");
    }

    private void deleteItem() {
        System.out.print("Enter ID of item to delete: ");
        String id = scanner.nextLine().trim();
        if (inventoryService.deleteItem(id)) {
            databaseService.deleteItem(id);
            try { fileService.saveToJson(inventoryService.getAllItems()); } catch (Exception e) { /* ignore */ }
            System.out.println("Item deleted.");
        } else {
            System.out.println("Item not found.");
        }
    }

    private void filterByCategory() {
        System.out.print("Enter category: ");
        String category = scanner.nextLine().trim();
        List<Item> results = inventoryService.filterByCategory(category);
        if (results.isEmpty()) { System.out.println("No items in this category."); return; }
        results.forEach(System.out::println);
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    private void viewStats() {
        System.out.println("\n--- Statistics ---");
        System.out.println("Total items:      " + inventoryService.getTotalItems());
        System.out.printf("Total value:      $%.2f%n", inventoryService.getTotalValue());

        List<Item> lowStock = inventoryService.getLowStockItems(5);
        if (!lowStock.isEmpty()) {
            System.out.println("Low stock items (<=5):");
            lowStock.forEach(i -> System.out.println("  " + i.getName() + " - qty: " + i.getQuantity()));
        }
    }

    // ─── Import / Export ──────────────────────────────────────────────────────

    private void importExportMenu() {
        System.out.println("\n1. Export to JSON");
        System.out.println("2. Export to CSV");
        System.out.println("3. Import from JSON");
        System.out.println("4. Import from CSV");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1" -> {
                    fileService.saveToJson(inventoryService.getAllItems());
                    System.out.println("Exported to data/inventory.json");
                }
                case "2" -> {
                    fileService.exportToCsv(inventoryService.getAllItems());
                    System.out.println("Exported to data/inventory.csv");
                }
                case "3" -> {
                    List<Item> loaded = fileService.loadFromJson();
                    inventoryService.loadItems(loaded);
                    databaseService.clearAll();
                    loaded.forEach(databaseService::insertItem);
                    System.out.println("Imported " + loaded.size() + " items from JSON.");
                }
                case "4" -> {
                    List<Item> loaded = fileService.importFromCsv();
                    inventoryService.loadItems(loaded);
                    databaseService.clearAll();
                    loaded.forEach(databaseService::insertItem);
                    System.out.println("Imported " + loaded.size() + " items from CSV.");
                }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void loadDataFromDb() {
        List<Item> items = databaseService.getAllItems();
        inventoryService.loadItems(items);
        System.out.println("Loaded " + items.size() + " items from database.");
    }

    private String promptNonEmpty(String field) {
        while (true) {
            System.out.print(field + ": ");
            String value = scanner.nextLine().trim();
            if (!value.isBlank()) return value;
            System.out.println(field + " cannot be empty.");
        }
    }

    private int promptInt(String field, int min, int max) {
        while (true) {
            try {
                System.out.print(field + ": ");
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.println("Enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
    }

    private double promptDouble(String field, double min) {
        while (true) {
            try {
                System.out.print(field + ": ");
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min) return value;
                System.out.println("Must be >= " + min);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
    }

    private LocalDate promptDate(String field) {
        while (true) {
            try {
                System.out.print(field + ": ");
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }
}