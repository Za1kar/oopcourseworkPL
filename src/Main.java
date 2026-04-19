import auth.AuthService;
import db.DatabaseService;
import gui.MainGUI;
import service.FileService;
import service.InventoryService;
import ui.ConsoleUI;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        InventoryService inventoryService = new InventoryService();
        FileService fileService           = new FileService();
        DatabaseService databaseService   = new DatabaseService();
        AuthService authService           = new AuthService();

        Scanner scanner = new Scanner(System.in);
        System.out.println("===========================================");
        System.out.println("   Inventory Tracker — Choose Mode        ");
        System.out.println("===========================================");
        System.out.println("1. Console Mode");
        System.out.println("2. GUI Mode (Swing)");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        if ("2".equals(choice)) {
            javax.swing.SwingUtilities.invokeLater(() ->
                    new MainGUI(inventoryService, fileService, databaseService, authService)
            );
        } else {
            ConsoleUI ui = new ConsoleUI(inventoryService, fileService, databaseService, authService);
            ui.start();
        }
    }
}