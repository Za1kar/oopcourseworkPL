package service;

import model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventoryService {

    private List<Item> items = new ArrayList<>();

    // CREATE
    public void addItem(Item item) {
        if (findById(item.getId()).isPresent()) {
            throw new IllegalArgumentException("Item with ID " + item.getId() + " already exists.");
        }
        items.add(item);
    }

    // READ ALL
    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }

    // READ BY ID
    public Optional<Item> findById(String id) {
        return items.stream()
                .filter(i -> i.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    // READ BY NAME (search)
    public List<Item> searchByName(String name) {
        return items.stream()
                .filter(i -> i.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    // READ BY CATEGORY
    public List<Item> filterByCategory(String category) {
        return items.stream()
                .filter(i -> i.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    // UPDATE
    public boolean updateItem(String id, String newName, int newQuantity, double newPrice) {
        Optional<Item> found = findById(id);
        if (found.isEmpty()) return false;

        Item item = found.get();
        if (!newName.isBlank()) item.setName(newName);
        if (newQuantity >= 0) item.setQuantity(newQuantity);
        if (newPrice >= 0) item.setPrice(newPrice);
        return true;
    }

    // DELETE
    public boolean deleteItem(String id) {
        return items.removeIf(i -> i.getId().equalsIgnoreCase(id));
    }

    // LOAD from file (replaces current list)
    public void loadItems(List<Item> loaded) {
        items.clear();
        items.addAll(loaded);
    }

    // Stats
    public int getTotalItems() {
        return items.size();
    }

    public double getTotalValue() {
        return items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
    }

    public List<Item> getLowStockItems(int threshold) {
        return items.stream()
                .filter(i -> i.getQuantity() <= threshold)
                .collect(Collectors.toList());
    }
}