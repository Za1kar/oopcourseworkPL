package model;

public class ElectronicsItem extends Item {

    private String brand;
    private int warrantyMonths;

    public ElectronicsItem(String id, String name, int quantity, double price,
                           String category, String brand, int warrantyMonths) {
        super(id, name, quantity, price, category);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    public String getBrand() { return brand; }
    public int getWarrantyMonths() { return warrantyMonths; }

    public void setBrand(String brand) { this.brand = brand; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    @Override
    public String getItemType() {
        return "Electronics";
    }

    @Override
    public String getExtraInfo() {
        return String.format("Brand: %s | Warranty: %d months", brand, warrantyMonths);
    }

    @Override
    public String toString() {
        return super.toString() + " | " + getExtraInfo();
    }
}