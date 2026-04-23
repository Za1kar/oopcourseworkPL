package gui;

import auth.AuthService;
import db.DatabaseService;
import model.ElectronicsItem;
import model.FoodItem;
import model.Item;
import service.FileService;
import service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class MainGUI extends JFrame {

    private final InventoryService inventoryService;
    private final FileService fileService;
    private final DatabaseService databaseService;
    private final AuthService authService;

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;

    public MainGUI(InventoryService inventoryService, FileService fileService,
                   DatabaseService databaseService, AuthService authService) {
        this.inventoryService = inventoryService;
        this.fileService = fileService;
        this.databaseService = databaseService;
        this.authService = authService;

        showLoginDialog();
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    private void showLoginDialog() {
        JDialog dialog = new JDialog(this, "Login", true);
        dialog.setSize(320, 200);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");
        JLabel errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; dialog.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; dialog.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; dialog.add(loginBtn, gbc);
        gbc.gridy = 3; dialog.add(errorLabel, gbc);

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (authService.login(username, password)) {
                List<Item> items = databaseService.getAllItems();
                inventoryService.loadItems(items);
                dialog.dispose();
                buildMainWindow();
            } else {
                errorLabel.setText("Invalid credentials!");
            }
        });

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    // ─── Main Window ──────────────────────────────────────────────────────────

    private void buildMainWindow() {
        setTitle("Inventory Tracker — " + authService.getCurrentUser().getUsername()
                + " [" + authService.getCurrentUser().getRole() + "]");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Table
        String[] columns = {"ID", "Type", "Name", "Quantity", "Price", "Category", "Extra Info"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(table);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton addBtn    = new JButton("Add Item");
        JButton editBtn   = new JButton("Edit Item");
        JButton deleteBtn = new JButton("Delete Item");
        JButton refreshBtn = new JButton("Refresh");
        JButton exportJsonBtn = new JButton("Export JSON");
        JButton exportCsvBtn  = new JButton("Export CSV");
        JButton importJsonBtn = new JButton("Import JSON");
        JButton importCsvBtn  = new JButton("Import CSV");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        if (authService.getCurrentUser().canDelete()) buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(exportJsonBtn);
        buttonPanel.add(exportCsvBtn);
        buttonPanel.add(importJsonBtn);
        buttonPanel.add(importCsvBtn);

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        searchPanel.add(new JLabel("Search by name:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(showAllBtn);

        // Status bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(buttonPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Actions
        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> refreshTable(inventoryService.getAllItems()));
        exportJsonBtn.addActionListener(e -> exportJson());
        exportCsvBtn.addActionListener(e -> exportCsv());
        importJsonBtn.addActionListener(e -> importJson());
        importCsvBtn.addActionListener(e -> importCsv());
        searchBtn.addActionListener(e -> refreshTable(inventoryService.searchByName(searchField.getText().trim())));
        showAllBtn.addActionListener(e -> refreshTable(inventoryService.getAllItems()));

        refreshTable(inventoryService.getAllItems());
        setVisible(true);
    }

    // ─── Table ────────────────────────────────────────────────────────────────

    private void refreshTable(List<Item> items) {
        tableModel.setRowCount(0);
        for (Item item : items) {
            tableModel.addRow(new Object[]{
                    item.getId(), item.getItemType(), item.getName(),
                    item.getQuantity(), String.format("$%.2f", item.getPrice()),
                    item.getCategory(), item.getExtraInfo()
            });
        }
        statusLabel.setText("Total items: " + items.size()
                + " | Total value: $" + String.format("%.2f", inventoryService.getTotalValue()));
    }

    // ─── Add Dialog ───────────────────────────────────────────────────────────

    private void showAddDialog() {
        JDialog dialog = new JDialog(this, "Add Item", true);
        dialog.setSize(400, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] types = {"Electronics", "Food"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        JTextField idField       = new JTextField(15);
        JTextField nameField     = new JTextField(15);
        JTextField qtyField      = new JTextField(15);
        JTextField priceField    = new JTextField(15);
        JTextField categoryField = new JTextField(15);
        JTextField extra1Field   = new JTextField(15);
        JTextField extra2Field   = new JTextField(15);
        JLabel extra1Label = new JLabel("Brand:");
        JLabel extra2Label = new JLabel("Warranty (months):");

        typeBox.addActionListener(e -> {
            boolean isElec = typeBox.getSelectedItem().equals("Electronics");
            extra1Label.setText(isElec ? "Brand:" : "Expiry (YYYY-MM-DD):");
            extra2Label.setText(isElec ? "Warranty (months):" : "Is Organic (true/false):");
        });

        int row = 0;
        addRow(dialog, gbc, row++, "Type:", typeBox);
        addRow(dialog, gbc, row++, "ID:", idField);
        addRow(dialog, gbc, row++, "Name:", nameField);
        addRow(dialog, gbc, row++, "Quantity:", qtyField);
        addRow(dialog, gbc, row++, "Price:", priceField);
        addRow(dialog, gbc, row++, "Category:", categoryField);
        addRow(dialog, gbc, row++, extra1Label, extra1Field);
        addRow(dialog, gbc, row++, extra2Label, extra2Field);

        JButton saveBtn = new JButton("Save");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String type     = (String) typeBox.getSelectedItem();
                String id       = idField.getText().trim();
                String name     = nameField.getText().trim();
                int qty         = Integer.parseInt(qtyField.getText().trim());
                double price    = Double.parseDouble(priceField.getText().trim());
                String category = categoryField.getText().trim();

                if (id.isBlank() || name.isBlank() || category.isBlank()) {
                    JOptionPane.showMessageDialog(dialog, "Fields cannot be empty.");
                    return;
                }
                if (inventoryService.findById(id).isPresent()) {
                    JOptionPane.showMessageDialog(dialog, "ID already exists.");
                    return;
                }

                Item item;
                if ("Electronics".equals(type)) {
                    int warranty = Integer.parseInt(extra2Field.getText().trim());
                    item = new ElectronicsItem(id, name, qty, price, category,
                            extra1Field.getText().trim(), warranty);
                } else {
                    LocalDate expiry = LocalDate.parse(extra1Field.getText().trim());
                    boolean organic  = Boolean.parseBoolean(extra2Field.getText().trim());
                    item = new FoodItem(id, name, qty, price, category, expiry, organic);
                }

                inventoryService.addItem(item);
                databaseService.insertItem(item);
                fileService.saveToJson(inventoryService.getAllItems());
                refreshTable(inventoryService.getAllItems());
                dialog.dispose();
                statusLabel.setText("Item added: " + name);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    // ─── Edit Dialog ──────────────────────────────────────────────────────────

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        String id = (String) tableModel.getValueAt(selectedRow, 0);
        Item item = inventoryService.findById(id).orElse(null);
        if (item == null) return;

        JDialog dialog = new JDialog(this, "Edit Item", true);
        dialog.setSize(350, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField  = new JTextField(item.getName(), 15);
        JTextField qtyField   = new JTextField(String.valueOf(item.getQuantity()), 15);
        JTextField priceField = new JTextField(String.valueOf(item.getPrice()), 15);

        addRow(dialog, gbc, 0, "Name:", nameField);
        addRow(dialog, gbc, 1, "Quantity:", qtyField);
        addRow(dialog, gbc, 2, "Price:", priceField);

        JButton saveBtn = new JButton("Save");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String name  = nameField.getText().trim();
                int qty      = Integer.parseInt(qtyField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());

                inventoryService.updateItem(id, name, qty, price);
                inventoryService.findById(id).ifPresent(databaseService::updateItem);
                fileService.saveToJson(inventoryService.getAllItems());
                refreshTable(inventoryService.getAllItems());
                dialog.dispose();
                statusLabel.setText("Item updated: " + id);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        String id   = (String) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete item: " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            inventoryService.deleteItem(id);
            databaseService.deleteItem(id);
            try { fileService.saveToJson(inventoryService.getAllItems()); } catch (Exception ex) { /* ignore */ }
            refreshTable(inventoryService.getAllItems());
            statusLabel.setText("Deleted: " + name);
        }
    }

    // ─── Import / Export ──────────────────────────────────────────────────────

    private void exportJson() {
        try {
            fileService.saveToJson(inventoryService.getAllItems());
            statusLabel.setText("Exported to data/inventory.json");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export error: " + e.getMessage());
        }
    }

    private void exportCsv() {
        try {
            fileService.exportToCsv(inventoryService.getAllItems());
            statusLabel.setText("Exported to data/inventory.csv");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export error: " + e.getMessage());
        }
    }

    private void importJson() {
        try {
            List<Item> loaded = fileService.loadFromJson();
            inventoryService.loadItems(loaded);
            databaseService.clearAll();
            loaded.forEach(databaseService::insertItem);
            refreshTable(inventoryService.getAllItems());
            statusLabel.setText("Imported " + loaded.size() + " items from JSON.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Import error: " + e.getMessage());
        }
    }

    private void importCsv() {
        try {
            List<Item> loaded = fileService.importFromCsv();
            inventoryService.loadItems(loaded);
            databaseService.clearAll();
            loaded.forEach(databaseService::insertItem);
            refreshTable(inventoryService.getAllItems());
            statusLabel.setText("Imported " + loaded.size() + " items from CSV.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Import error: " + e.getMessage());
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private void addRow(JDialog dialog, GridBagConstraints gbc, int row, Object label, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        if (label instanceof String s) dialog.add(new JLabel(s), gbc);
        else dialog.add((JComponent) label, gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
    }
}