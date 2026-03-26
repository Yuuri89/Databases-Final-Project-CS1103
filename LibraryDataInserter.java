import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Name: Guilherme Machado da Silva
// ID: 3800746
// 
// First Compile in the Terminal then,
// Use this command to execute: java --enable-native-access=ALL-UNNAMED -classpath ".:sqlite-jdbc.jar:slf4j-simple.jar:slf4j-api.jar" LibraryDataInserter

public class LibraryDataInserter {
    public static void main(String[] args){
        Connection conn = null;

        try {
            // Load the SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the database
            conn = DriverManager.getConnection("jdbc:sqlite:library.db");

            // Creating a statement
            Statement stmt = conn.createStatement();

            System.out.println("Building database tables...");

            // Create  Entity Tables
            String createAuthor = "CREATE TABLE IF NOT EXISTS Author (" +
                "AuthorID VARCHAR(6) PRIMARY KEY, " +
                "FirstName VARCHAR(50) NOT NULL, " +
                "LastName VARCHAR(50) NOT NULL)";

            stmt.executeUpdate(createAuthor);

            String createMember = "CREATE TABLE IF NOT EXISTS Member (" +
                "MemberID VARCHAR(5) PRIMARY KEY, " +
                "FullName VARCHAR(100) NOT NULL, " +
                "Email VARCHAR(100) UNIQUE)";

            stmt.executeUpdate(createMember);

            String createBook = "CREATE TABLE IF NOT EXISTS Book (" +
                "BookID VARCHAR(5) PRIMARY KEY, " +
                "Title VARCHAR(200) NOT NULL, " +
                "AuthorID VARCHAR(6), " +
                "FOREIGN KEY (AuthorID) REFERENCES Author(AuthorID))";
            
            stmt.executeUpdate(createBook);

            // Create Relation Table
            String createLoan = "CREATE TABLE IF NOT EXISTS Loan (" +
                "LoanID VARCHAR(5) PRIMARY KEY, " +
                "BookID VARCHAR(5), " +
                "MemberID VARCHAR(5), " +
                "CheckoutDate DATE NOT NULL, " +
                "FOREIGN KEY (BookID) REFERENCES Book(BookID), " +
                "FOREIGN KEY (MemberID) REFERENCES Member(MemberID))";

            stmt.executeUpdate(createLoan);

            System.out.println("Tables were created successfully");
            System.out.println();

            // INSERTING DATA INTO THE DATABASE
            System.out.println("Inserting data into the database");

            // Inserting into the Author Table
            String insertAuthors = "INSERT INTO Author (AuthorID, FirstName, LastName) VALUES " +
                "('00001', 'Lewis', 'Loftus')," +
                "('00002', 'Liana', 'Cincotti')," +
                "('00003', 'Jeff', 'Kinney')";
            
            stmt.executeUpdate(insertAuthors);

            // Inserting into the Member Table
            String insertMembers = "INSERT INTO Member (MemberID, FullName, Email) VALUES " + 
                "('00001', 'Guilherme Machado da Silva', 'gui.silva.machado@unb.ca')," +
                "('00002', 'Meaghan Mahoney', 'meaghan.mahoney@unb.ca')," +
                "('00003', 'Lacey Allen', 'lacey.allen@icloud.com')";
            
            stmt.executeUpdate(insertMembers);

            // Inserting into the Book Table
            String insertBooks = "INSERT INTO Book (BookID, Title, AuthorID) VALUES " +
                "('00001', 'Java Software Solutions', '00001')," +
                "('00002', 'Picking Daisies on Sundays', '00002')," +
                "('00003', 'Diary of a Wimpy Kid', '00003')";

            stmt.executeUpdate(insertBooks);

            // Inserting into the Loan Table
            String insertLoans = "INSERT INTO Loan (LoanID, BookID, MemberID, CheckoutDate) VALUES " +
                "('00001', '00001', '00001', '2026-03-20')," + 
                "('00002', '00002', '00001', '2026-03-22')," + 
                "('00003', '00003', '00003', '2026-03-24')";

            stmt.executeUpdate(insertLoans);

            System.out.println("All data was inserted sucessfully");
            System.out.println();
        }

        catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            }

            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}