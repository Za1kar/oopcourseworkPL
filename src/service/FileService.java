package service;

import model.ElectronicsItem;
import model.FoodItem;
import model.Item;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileService {

    private static final String JSON_PATH = "data/inventory.json";
    private static final String CSV_PATH  = "data/inventory.csv";

    // ─── JSON ────────────────────────────────────────────────────────────────

    public void saveToJson(List<Item> items) throws IOException {
        new File("data").mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(JSON_PATH))) {
            writer.write("[\n");
            for (int i = 0; i < items.size(); i++) {
                writer.write(itemToJson(items.get(i)));
                if (i < items.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("]");
        }
    }

    public List<Item> loadFromJson() throws IOException {
        List<Item> items = new ArrayList<>();
        File file = new File(JSON_PATH);
        if (!file.exists()) return items;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line.trim());
        }

        String content = sb.toString().trim();
        if (content.equals("[]") || content.isEmpty()) return items;

        // Remove outer brackets
        content = content.substring(1, content.length() - 1).trim();

        // Split by "},{"
        List<String> blocks = splitJsonObjects(content);
        for (String block : blocks) {
            Item item = parseJsonBlock(block);
            if (item != null) items.add(item);
        }
        return items;
    }

    private String itemToJson(Item item) {
        StringBuilder sb = new StringBuilder();
        sb.append("  {\n");
        sb.append(jsonField("type", item.getItemType())).append(",\n");
        sb.append(jsonField("id", item.getId())).append(",\n");
        sb.append(jsonField("name", item.getName())).append(",\n");
        sb.append(jsonNumField("quantity", item.getQuantity())).append(",\n");
        sb.append(jsonNumField("price", item.getPrice())).append(",\n");
        sb.append(jsonField("category", item.getCategory()));

        if (item instanceof ElectronicsItem e) {
            sb.append(",\n").append(jsonField("brand", e.getBrand()));
            sb.append(",\n").append(jsonNumField("warrantyMonths", e.getWarrantyMonths()));
        } else if (item instanceof FoodItem f) {
            sb.append(",\n").append(jsonField("expiryDate", f.getExpiryDate().toString()));
            sb.append(",\n").append(jsonBoolField("isOrganic", f.isOrganic()));
        }
        sb.append("\n  }");
        return sb.toString();
    }

    private List<String> splitJsonObjects(String content) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') { if (depth == 0) start = i; depth++; }
            else if (c == '}') { depth--; if (depth == 0) result.add(content.substring(start, i + 1)); }
        }
        return result;
    }

    private Item parseJsonBlock(String block) {
        try {
            String type         = extractString(block, "type");
            String id           = extractString(block, "id");
            String name         = extractString(block, "name");
            int    quantity     = Integer.parseInt(extractValue(block, "quantity"));
            double price        = Double.parseDouble(extractValue(block, "price"));
            String category     = extractString(block, "category");

            if ("Electronics".equals(type)) {
                String brand   = extractString(block, "brand");
                int warranty   = Integer.parseInt(extractValue(block, "warrantyMonths"));
                return new ElectronicsItem(id, name, quantity, price, category, brand, warranty);
            } else if ("Food".equals(type)) {
                LocalDate expiry = LocalDate.parse(extractString(block, "expiryDate"));
                boolean organic  = Boolean.parseBoolean(extractValue(block, "isOrganic"));
                return new FoodItem(id, name, quantity, price, category, expiry, organic);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse item: " + e.getMessage());
        }
        return null;
    }

    // ─── CSV ─────────────────────────────────────────────────────────────────

    public void exportToCsv(List<Item> items) throws IOException {
        new File("data").mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_PATH))) {
            writer.write("type,id,name,quantity,price,category,extra1,extra2\n");
            for (Item item : items) {
                String extra1 = "", extra2 = "";
                if (item instanceof ElectronicsItem e) {
                    extra1 = e.getBrand();
                    extra2 = String.valueOf(e.getWarrantyMonths());
                } else if (item instanceof FoodItem f) {
                    extra1 = f.getExpiryDate().toString();
                    extra2 = String.valueOf(f.isOrganic());
                }
                writer.write(String.format("%s,%s,%s,%d,%.2f,%s,%s,%s\n",
                        item.getItemType(), item.getId(), item.getName(),
                        item.getQuantity(), item.getPrice(), item.getCategory(),
                        extra1, extra2));
            }
        }
    }

    public List<Item> importFromCsv() throws IOException {
        List<Item> items = new ArrayList<>();
        File file = new File(CSV_PATH);
        if (!file.exists()) return items;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;
                String type     = p[0].trim();
                String id       = p[1].trim();
                String name     = p[2].trim();
                int quantity    = Integer.parseInt(p[3].trim());
                double price    = Double.parseDouble(p[4].trim());
                String category = p[5].trim();

                if ("Electronics".equals(type) && p.length >= 8) {
                    items.add(new ElectronicsItem(id, name, quantity, price, category,
                            p[6].trim(), Integer.parseInt(p[7].trim())));
                } else if ("Food".equals(type) && p.length >= 8) {
                    items.add(new FoodItem(id, name, quantity, price, category,
                            LocalDate.parse(p[6].trim()), Boolean.parseBoolean(p[7].trim())));
                }
            }
        }
        return items;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int colon = json.indexOf(":", idx) + 1;
        int q1    = json.indexOf("\"", colon) + 1;
        int q2    = json.indexOf("\"", q1);
        return json.substring(q1, q2);
    }

    private String extractValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx   = json.indexOf(search);
        if (idx == -1) return "0";
        int colon = json.indexOf(":", idx) + 1;
        int end   = json.indexOf(",", colon);
        if (end == -1) end = json.indexOf("}", colon);
        return json.substring(colon, end).trim().replace("\"", "");
    }

    private String jsonField(String key, String value) {
        return "    \"" + key + "\": \"" + value + "\"";
    }

    private String jsonNumField(String key, Number value) {
        return "    \"" + key + "\": " + value;
    }

    private String jsonBoolField(String key, boolean value) {
        return "    \"" + key + "\": " + value;
    }
}