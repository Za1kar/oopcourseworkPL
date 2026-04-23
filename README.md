# Inventory Item Tracker
### tracks inventory items in a warehouse or store, allowing addition, ramoval, and updating of item details.

#### Student: *Zhaliev Zalkar 250142001*

----

### Description
Inventory Item Tracker is a Java-based application that helps manage items in a warehouse or store. Users can add, view, update, and delete items through either a console interface or a graphical user interface (GUI). The system supports two types of items — Electronics and Food — each with their own unique attributes. The application also includes an authentication system where users can register or log in as either an Admin or a regular User.

----
## Objectives

- Build a working inventory system where you can manage items
- Practice OOP concepts like inheritance, polymorphism, and encapsulation
- Store data in files and a database so nothing is lost after closing the app
- Make two interfaces — console and GUI — so the user can choose how to use the app
- Add a login system with different permissions for Admin and User
- Support import and export of data in JSON and CSV formats
---

## Project Requirements

Here are the 13 requirements I implemented:

1. **CRUD Operations** — you can Create, Read, Update, and Delete any item
2. **Console Interface** — a simple text menu with numbered options and clear prompts
3. **Input Validation** — the program won't accept empty fields, wrong number formats, or bad dates
4. **File Persistence (JSON)** — all items are saved to `data/inventory.json` automatically
5. **Database Persistence (SQLite)** — items are also stored in a real database using JDBC
6. **Modular Design** — code is split into packages: `model`, `service`, `auth`, `db`, `ui`, `gui`
7. **Error Handling** — try-catch blocks handle file errors, database errors, and wrong input
8. **Encapsulation** — all class fields are `private`, accessed only through getters and setters
9. **Inheritance** — `ElectronicsItem` and `FoodItem` extend `Item`; `Admin` extends `User`
10. **Polymorphism** — `getItemType()` and `getExtraInfo()` are defined in `Item` and overridden differently in each subclass
11. **Authentication & Roles** — users can register/login; only Admins can delete items
12. **GUI with Swing** — graphical window with a table, buttons, search bar, and dialogs
13. **Import / Export** — data can be exported and imported as JSON or CSV files
---

## Project Structure

```
oopcoursework/
├── src/
│   ├── model/
│   │   ├── Item.java                 # parent class (abstract)
│   │   ├── ElectronicsItem.java      # child class — has brand and warranty
│   │   └── FoodItem.java             # child class — has expiry date and organic flag
│   ├── service/
│   │   ├── InventoryService.java     # all CRUD logic, search, filter, stats
│   │   └── FileService.java          # save/load JSON, export/import CSV
│   ├── auth/
│   │   ├── User.java                 # base user class
│   │   ├── Admin.java                # admin — can also delete items
│   │   └── AuthService.java          # handles login, register, saving users
│   ├── db/
│   │   └── DatabaseService.java      # SQLite database operations
│   ├── ui/
│   │   └── ConsoleUI.java            # console menu
│   ├── gui/
│   │   └── MainGUI.java              # Swing GUI window
│   └── Main.java                     # starting point of the program
├── data/
│   ├── inventory.json                # items saved as JSON
│   ├── inventory.csv                 # CSV export file
│   ├── inventory.db                  # SQLite database file
│   └── users.txt                     # registered users
└── lib/
    └── sqlite-jdbc-3.41.2.2.jar      # SQLite driver
```
 
---

## OOP Concepts Explained

### Encapsulation
All the fields inside classes are `private` — nobody can access them directly from outside. You have to use getters and setters.

```java
private String name;
 
public String getName() { return name; }
public void setName(String name) { this.name = name; }
```

### Inheritance
`ElectronicsItem` and `FoodItem` are child classes of `Item`. They share common fields like id, name, price, and quantity — but each adds its own extra fields.

```java
public class ElectronicsItem extends Item {
    private String brand;
    private int warrantyMonths;
}
```

`Admin` extends `User` — same as a regular user but with the ability to delete items.

```java
public class Admin extends User {
    @Override
    public boolean canDelete() { return true; }
}
```

### Polymorphism
The `Item` class has two abstract methods. Each subclass implements them differently:

```java
// ElectronicsItem:
public String getItemType()  { return "Electronics"; }
public String getExtraInfo() { return "Brand: " + brand + " | Warranty: " + warrantyMonths + " months"; }
 
// FoodItem:
public String getItemType()  { return "Food"; }
public String getExtraInfo() { return "Expiry: " + expiryDate + " | Organic: " + isOrganic; }
```
 
---

## How to Run

**You need:**
- Java 17 or higher
- IntelliJ IDEA
- `sqlite-jdbc-3.41.2.2.jar` added to Libraries
  **Steps:**
1. Open the project in IntelliJ
2. Go to `File → Project Structure → Libraries → +` and add the SQLite JAR from the `lib/` folder
3. Run `Main.java`
4. Pick a mode:
    - `1` for Console
    - `2` for GUI window
      **Default admin login:**
```
Username: admin
Password: admin123
```
 
---

## Test Cases

### Console Mode

| What I tested | Input | Result |
|---|---|---|
| Login as admin | admin / admin123 | Welcome, admin! [ADMIN] |
| Login with wrong password | admin / wrongpass | Invalid credentials. |
| Add Electronics item | ID: e01, Name: iPhone, Qty: 5, Price: 999, Brand: Apple, Warranty: 12 | Item added successfully! |
| Add Food item | ID: f01, Name: Apple, Qty: 50, Price: 0.5, Expiry: 2026-12-01, Organic: yes | Item added successfully! |
| View all items | Option 1 | All items listed with details |
| Search by name | "iphone" | Returns matching items |
| Update item | ID: e01, new qty: 10 | Item updated successfully! |
| Delete as admin | ID: e01 | Item deleted. |
| Delete as user | Option 5 | Access denied. |
| Export to JSON | Option 8 → 1 | Exported to data/inventory.json |
| Export to CSV | Option 8 → 2 | Exported to data/inventory.csv |
| Import from CSV | Option 8 → 4 | Imported N items from CSV. |
| Low stock warning | Item with qty ≤ 5 | Shown in statistics |

### GUI Mode

| What I tested | Result |
|---|---|
| Wrong login | Red error message shows |
| Add item via form | Item appears in table right away |
| Edit item | Table updates with new values |
| Delete (admin only) | Confirmation popup, then row removed |
| Search by name | Table filters results |
| Export buttons | Files created in data/ folder |
 
---

## Challenges I faced

**SQLite driver issue** — The newer version of the SQLite JAR needed an extra library that I didn't have. Fixed it by using version 3.41.2.2 which works on its own.

**Parsing JSON without libraries** — I wrote my own simple JSON parser using string methods because I didn't want to add extra dependencies. It was tricky but worked out.

**Role-based GUI** — The Delete button only shows up for Admins. I had to check the user's role when building the window and conditionally add the button.
 
---

## Presentation

> Link to slides: *(add your Google Slides link here)*
 
---

## Author

Zhaliev Zalkar COMFCI-25 250142001
