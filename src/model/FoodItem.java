package model;

import java.time.LocalDate;

public class FoodItem extends Item {

    private LocalDate expiryDate;
    private boolean isOrganic;

    public FoodItem(String id, String name, int quantity, double price,
                    String category, LocalDate expiryDate, boolean isOrganic) {
        super(id, name, quantity, price, category);
        this.expiryDate = expiryDate;
        this.isOrganic = isOrganic;
    }

    public LocalDate getExpiryDate() { return expiryDate; }
    public boolean isOrganic() { return isOrganic; }

    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setOrganic(boolean organic) { isOrganic = organic; }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    @Override
    public String getItemType() {
        return "Food";
    }

    @Override
    public String getExtraInfo() {
        return String.format("Expiry: %s | Organic: %s | Expired: %s",
                expiryDate, isOrganic ? "Yes" : "No", isExpired() ? "YES ⚠" : "No");
    }

    @Override
    public String toString() {
        return super.toString() + " | " + getExtraInfo();
    }
}