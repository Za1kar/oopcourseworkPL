package model;

public abstract class Item {

    private String id;
    private String name;
    private int quantity;
    private double price;
    private String category;

    public Item(String id, String name, int quantity, double price, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }

    // Polymorphism — each subclass describes itself differently
    public abstract String getItemType();

    public abstract String getExtraInfo();

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | Name: %s | Qty: %d | Price: $%.2f | Category: %s",
                getItemType(), id, name, quantity, price, category);
    }
}