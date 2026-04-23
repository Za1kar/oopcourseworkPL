import auth.AuthService;
import db.DatabaseService;
import service.FileService;
import service.InventoryService;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        InventoryService inventoryService = new InventoryService();
        FileService fileService           = new FileService();
        DatabaseService databaseService   = new DatabaseService();
        AuthService authService           = new AuthService();

        ConsoleUI ui = new ConsoleUI(inventoryService, fileService, databaseService, authService);
        ui.start();
    }
}