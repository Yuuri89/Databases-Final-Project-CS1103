import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

// Name: Guilherme Machado da Silva
// ID: 3800746
//
// Compile: javac LibraryApp.java
// Run: java --enable-native-access=ALL-UNNAMED -classpath ".:sqlite-jdbc.jar:slf4j-simple.jar:slf4j-api.jar" LibraryApp

public class LibraryApp {
    
    public static void main(String[] args) {
        
        // Standard local variables
        Connection conn = null;
        Scanner scanner = new Scanner(System.in);

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:library.db");

            boolean running = true;

            // The Interactive Menu Loop
            while (running) {
                System.out.println("\n=== LIBRARY MANAGEMENT SYSTEM ===");
                System.out.println("1. View all Checked Out Books");
                System.out.println("2. Add a New Member");
                System.out.println("3. Update a Member's Email");
                System.out.println("4. Checkout a Book");
                System.out.println("5. Return a Book");
                System.out.println("6. Exit Program");
                System.out.print("Please select an option (1-5): ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline
                System.out.println(); 

                // Passing the local variables to our functions
                if (choice == 1) {
                    viewLoans(conn);
                } else if (choice == 2) {
                    addMember(conn, scanner);
                } else if (choice == 3) {
                    updateEmail(conn, scanner);
                } else if (choice == 4) {
                    checkoutBook(conn, scanner);
                } else if (choice == 5) {
                    returnBook(conn, scanner);
                } else if (choice == 6) {
                    System.out.println("Exiting the Library System. Goodbye!");
                    running = false;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Database Connection Error!");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
                scanner.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // select statement so we can see all of the current loans.
    private static void viewLoans(Connection conn) throws SQLException {
        System.out.println("--- CURRENT LOANS ---");
        Statement stmt = conn.createStatement();
        String query = "SELECT Member.FullName, Book.Title, Loan.CheckoutDate " +
                       "FROM Loan JOIN Member ON Loan.MemberID = Member.MemberID " +
                       "JOIN Book ON Loan.BookID = Book.BookID";
        
        ResultSet rs = stmt.executeQuery(query);
        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            System.out.println("> " + rs.getString("FullName") + " borrowed '" + 
                               rs.getString("Title") + "' on " + rs.getString("CheckoutDate"));
        }
        if (!hasData) System.out.println("No books are currently checked out.");
    }

    // Insert statement to add new members.
    private static void addMember(Connection conn, Scanner scanner) throws SQLException {
        Statement stmt = conn.createStatement();
        System.out.print("Enter new Member ID (e.g., 00004): ");
        String id = scanner.nextLine();
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine();

        String query = "INSERT INTO Member (MemberID, FullName, Email) VALUES ('" + 
                       id + "', '" + name + "', '" + email + "')";
        try {
            if (stmt.executeUpdate(query) > 0) System.out.println("Success! New member added.");
        } catch (SQLException e) {
            System.out.println("Error: Could not add member. ID or Email must be unique!");
        }
    }

    // update statement to update the email.
    private static void updateEmail(Connection conn, Scanner scanner) throws SQLException {
        Statement stmt = conn.createStatement();
        System.out.print("Enter Member ID to update (e.g., 00001): ");
        String id = scanner.nextLine();
        System.out.print("Enter the new email address: ");
        String email = scanner.nextLine();

        String query = "UPDATE Member SET Email = '" + email + "' WHERE MemberID = '" + id + "'";
        if (stmt.executeUpdate(query) > 0) {
            System.out.println("Success! Email updated.");
        } else {
            System.out.println("Error: Member ID not found.");
        }
    }

    // delete statement so they can return a book.
    private static void returnBook(Connection conn, Scanner scanner) throws SQLException {
        Statement stmt = conn.createStatement();
        System.out.print("Enter Loan ID to process return (e.g., 00001): ");
        String id = scanner.nextLine();

        String query = "DELETE FROM Loan WHERE LoanID = '" + id + "'";
        if (stmt.executeUpdate(query) > 0) {
            System.out.println("Success! Book returned and loan record deleted.");
        } else {
            System.out.println("Error: Loan ID not found.");
        }
    }

    // Transaction so 2 people cant take the same book.
    private static void checkoutBook(Connection conn, Scanner scanner) {
        System.out.print("Enter Member ID (e.g., 00001): ");
        String memberId = scanner.nextLine();
        
        System.out.print("Enter Book ID to checkout (e.g., 00001): ");
        String bookId = scanner.nextLine();
        
        System.out.print("Enter today's date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        // We create a random LoanID for this example (in a real app, this would auto-generate)
        String loanId = "L" + (int)(Math.random() * 10000); 

        try {
            // 1. START THE TRANSACTION (Turn off auto-save)
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            // 2. CHECK IF THE BOOK IS AVAILABLE
            String checkQuery = "SELECT * FROM Loan WHERE BookID = '" + bookId + "'";
            ResultSet rs = stmt.executeQuery(checkQuery);

            if (rs.next()) {
                // The book is already in the Loan table!
                System.out.println("Error: That book is currently checked out by someone else.");
                
                // Cancel the transaction
                conn.rollback(); 
            } else {
                // 3. THE BOOK IS FREE! Insert the loan record.
                String insertQuery = "INSERT INTO Loan (LoanID, BookID, MemberID, CheckoutDate) VALUES ('" + 
                                     loanId + "', '" + bookId + "', '" + memberId + "', '" + date + "')";
                stmt.executeUpdate(insertQuery);
                
                // Save the transaction permanently
                conn.commit(); 
                System.out.println("Success! Book checked out to Member " + memberId);
            }

        } catch (SQLException e) {
            System.out.println("A database error occurred during checkout.");
            try {
                // If anything crashes, undo all changes
                conn.rollback(); 
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                // 4. ALWAYS TURN AUTO-COMMIT BACK ON when finished!
                conn.setAutoCommit(true); 
            } catch (SQLException autoCommitEx) {
                autoCommitEx.printStackTrace();
            }
        }
    }
}