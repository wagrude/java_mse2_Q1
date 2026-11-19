
package org.example;

import java.sql.*;
import java.util.Scanner;

public class OrderManagement {

    static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";
    static final String PASSWORD = "1Shiv@nsh01";

    static Scanner sc = new Scanner(System.in);

    // Database Connection
    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // A) Insert at least three orders using PreparedStatement
    static void insertOrders() {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter Order " + i + " Details");

                System.out.print("Order ID: ");
                int id = Integer.parseInt(sc.nextLine());

                System.out.print("Customer Name: ");
                String customer = sc.nextLine();

                System.out.print("Product Name: ");
                String product = sc.nextLine();

                System.out.print("Quantity: ");
                int quantity = Integer.parseInt(sc.nextLine());

                System.out.print("Unit Price: ");
                double price = Double.parseDouble(sc.nextLine());

                System.out.print("Status: ");
                String status = sc.nextLine();

                ps.setInt(1, id);
                ps.setString(2, customer);
                ps.setString(3, product);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);
                ps.setString(6, status);

                try {
                    ps.executeUpdate();
                    System.out.println("Order inserted successfully!");

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID: " + id);

                } catch (SQLException e) {
                    System.out.println("Insert Error: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());

        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric input!");
        }
    }

    // B) Search orders by status and quantity > 2
    // Sort by unit price in descending order
    static void searchOrders() {

        System.out.print("Enter order status: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders WHERE status = ? " +
                "AND quantity > 2 ORDER BY unit_price DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                while (rs.next()) {

                    found = true;

                    System.out.println("\nOrder ID: " +
                            rs.getInt("order_id"));

                    System.out.println("Customer: " +
                            rs.getString("customer_name"));

                    System.out.println("Product: " +
                            rs.getString("product_name"));

                    System.out.println("Quantity: " +
                            rs.getInt("quantity"));

                    System.out.println("Unit Price: " +
                            rs.getDouble("unit_price"));

                    System.out.println("Status: " +
                            rs.getString("status"));
                }

                if (!found) {
                    System.out.println("No matching orders found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // C) Increase quantity of an existing order
    static void increaseQuantity() {

        System.out.print("Enter Order ID: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Additional Quantity: ");
        int qty = Integer.parseInt(sc.nextLine());

        String sql = "UPDATE Orders SET quantity = quantity + ? " +
                "WHERE order_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, qty);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Quantity updated successfully!");
            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // D) Retrieve order and update status after validation
    static void updateStatus() {

        System.out.print("Enter Order ID: ");
        int id = Integer.parseInt(sc.nextLine());

        String selectSQL =
                "SELECT * FROM Orders WHERE order_id = ?";

        String updateSQL =
                "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (Connection con = getConnection();
             PreparedStatement select = con.prepareStatement(selectSQL)) {

            select.setInt(1, id);

            String customer;
            String product;
            int quantity;
            double price;

            try (ResultSet rs = select.executeQuery()) {

                if (!rs.next()) {
                    System.out.println("Order does not exist.");
                    return;
                }

                customer = rs.getString("customer_name");
                product = rs.getString("product_name");
                quantity = rs.getInt("quantity");
                price = rs.getDouble("unit_price");

                System.out.println("\nExisting Order Details:");

                System.out.println("Order ID: " + id);
                System.out.println("Customer: " + customer);
                System.out.println("Product: " + product);
                System.out.println("Quantity: " + quantity);
                System.out.println("Unit Price: " + price);
                System.out.println("Current Status: " +
                        rs.getString("status"));
            }

            System.out.print("\nEnter New Status: ");
            String status = sc.nextLine();

            if (!status.equalsIgnoreCase("Pending") &&
                    !status.equalsIgnoreCase("Shipped") &&
                    !status.equalsIgnoreCase("Delivered")) {

                System.out.println("Invalid Status!");
                return;
            }

            try (PreparedStatement update =
                         con.prepareStatement(updateSQL)) {

                update.setString(1, status);
                update.setInt(2, id);

                int rows = update.executeUpdate();

                if (rows > 0) {

                    System.out.println("\nStatus Updated Successfully!");

                    System.out.println("\nUpdated Order Details:");

                    System.out.println("Order ID: " + id);
                    System.out.println("Customer: " + customer);
                    System.out.println("Product: " + product);
                    System.out.println("Quantity: " + quantity);
                    System.out.println("Unit Price: " + price);
                    System.out.println("New Status: " + status);

                } else {
                    System.out.println("Order could not be updated.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // E) Display total order value and delete Cancelled order
    static void totalAndDelete() {

        try (Connection con = getConnection()) {

            // Calculate total order value
            String sumSQL =
                    "SELECT SUM(quantity * unit_price) AS total FROM Orders";

            try (PreparedStatement ps = con.prepareStatement(sumSQL);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    System.out.println("\nTotal Order Value: Rs. " +
                            rs.getDouble("total"));
                }
            }

            // Delete order only if status is Cancelled
            System.out.print("\nEnter Order ID to delete: ");
            int id = Integer.parseInt(sc.nextLine());

            String selectSQL =
                    "SELECT status FROM Orders WHERE order_id = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(selectSQL)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        String status = rs.getString("status");

                        if (status.equalsIgnoreCase("Cancelled")) {

                            String deleteSQL =
                                    "DELETE FROM Orders WHERE order_id = ?";

                            try (PreparedStatement delete =
                                         con.prepareStatement(deleteSQL)) {

                                delete.setInt(1, id);
                                delete.executeUpdate();

                                System.out.println(
                                        "Cancelled Order Deleted Successfully!");
                            }

                        } else {

                            System.out.println(
                                    "Cannot Delete! Order is not Cancelled.");
                        }

                    } else {

                        System.out.println("Order does not exist.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Main Menu
    public static void main(String[] args) {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con = getConnection()) {
                System.out.println("Connected to MySQL successfully!");
            }

        } catch (Exception e) {

            System.out.println("Database Connection Failed: " +
                    e.getMessage());
            return;
        }

        while (true) {

            System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
            System.out.println("1. Insert Three Orders");
            System.out.println("2. Search Orders by Status");
            System.out.println("3. Increase Order Quantity");
            System.out.println("4. Update Order Status");
            System.out.println("5. Display Total Value and Delete Order");
            System.out.println("6. Exit");

            System.out.print("\nEnter your choice: ");

            try {

                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {

                    case 1:
                        insertOrders();
                        break;

                    case 2:
                        searchOrders();
                        break;

                    case 3:
                        increaseQuantity();
                        break;

                    case 4:
                        updateStatus();
                        break;

                    case 5:
                        totalAndDelete();
                        break;

                    case 6:
                        System.out.println("Exiting Program...");
                        sc.close();
                        return;

                    default:
                        System.out.println("Invalid Choice!");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }
}
