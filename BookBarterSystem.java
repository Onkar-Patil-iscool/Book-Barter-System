import java.sql.*;
import java.util.*;

public class BookBarterSystem {

    // ✅ Database Configuration
    static final String DB_URL = "jdbc:mysql://localhost:3306/books_barter";
    static final String USER = "root";
    static final String PASS = "onkpatil!23";  // ⚠ Use your own password

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
        while (true) {
            // Main Menu
            System.out.println("\n📚 BOOK BARTER SYSTEM");
            System.out.println("1. Register Student");
            System.out.println("2. Donate Book");
            System.out.println("3. View All Books");
            System.out.println("4. Request Book");
            System.out.println("5. View Book Requests");
            System.out.println("6. Approve Request (Admin)");
            System.out.println("7. Exit");
            System.out.println("8. Admin Panel (Delete Student)");

            System.out.print("👉 Your choice: ");
            while (!sc.hasNextInt()) {
                System.out.print("❌ Enter valid choice: ");
                sc.next();
            }

            int ch = sc.nextInt(); sc.nextLine();

            //  Menu 
            switch (ch) {
                case 1: registerStudent(con); break;       
                case 2: donateBook(con); break;            
                case 3: viewBooks(con); break;             
                case 4: requestBook(con); break;           
                case 5: viewRequests(con); break;          
                case 6: approveRequest(con); break;        
                case 7: con.close(); 
                        System.out.println("👋 Thanks for using our service, Bye!"); 
                        return;
                case 8: adminPanel(con); break;            // Admin Only
                default: System.out.println("❌ Invalid choice!");
            }
        }
    }

    //  1. REGISTER STUDENT
    static void registerStudent(Connection con) throws Exception {
        System.out.println("👤 Register Student");
        System.out.print("Name: "); String name = sc.nextLine();
        System.out.print("Email: "); String email = sc.nextLine();
        System.out.print("Year (FE/SE/TE/BE): "); String year = sc.nextLine().toUpperCase();
        System.out.print("Branch: "); String branch = sc.nextLine();

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO students (name, email, year, branch) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, year);
        ps.setString(4, branch);
        int r = ps.executeUpdate();

        if (r > 0) {
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int studentId = keys.getInt(1);
                System.out.println("✅ Student registered! Your Student ID is: " + studentId);
            }
        } else {
            System.out.println("❌ Failed.");
        }
        ps.close();
    }

    //  2. DONATE / CONTRIBUTE BOOK
    static void donateBook(Connection con) throws Exception {
        System.out.println("📦 Donate Book");
        viewStudents(con); // View current students
        System.out.print("Student ID (Donor): ");
        while (!sc.hasNextInt()) {
            System.out.print("❌ Enter valid ID: ");
            sc.next();
        }
        int sid = sc.nextInt(); sc.nextLine();

        System.out.print("Title: "); String title = sc.nextLine();
        System.out.print("Author: "); String author = sc.nextLine();
        System.out.print("Subject: "); String subject = sc.nextLine();
        System.out.print("Branch: "); String branch = sc.nextLine();
        System.out.print("Semester: ");
        while (!sc.hasNextInt()) {
            System.out.print("❌ Enter valid semester: ");
            sc.next();
        }
        int sem = sc.nextInt(); sc.nextLine();

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO books (title, author, subject, branch, semester, donated_by) VALUES (?, ?, ?, ?, ?, ?)"
        );
        ps.setString(1, title);
        ps.setString(2, author);
        ps.setString(3, subject);
        ps.setString(4, branch);
        ps.setInt(5, sem);
        ps.setInt(6, sid);
        int r = ps.executeUpdate();
        System.out.println(r > 0 ? "✅ Book donated!" : "❌ Failed.");
        ps.close();
    }

    //  3. VIEW BOOKS
    static void viewBooks(Connection con) throws Exception {
        System.out.println("📚 All Available Books:");
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT b.book_id, b.title, b.subject, b.semester, b.donated_by, s.name AS donor " +
            "FROM books b JOIN students s ON b.donated_by = s.id"
        );

        while (rs.next()) {
            System.out.println("Book ID: " + rs.getInt("book_id"));
            System.out.println("  Title: " + rs.getString("title"));
            System.out.println("  Subject: " + rs.getString("subject"));
            System.out.println("  Semester: " + rs.getInt("semester"));
            System.out.println("  Donated By: " + rs.getString("donor") + " (ID: " + rs.getInt("donated_by") + ")");
            System.out.println("------------");
        }

        rs.close(); st.close();
    }

    //  4. REQUEST BOOK
    static void requestBook(Connection con) throws Exception {
        System.out.println("📝 Request a Book");
        viewStudents(con);
        System.out.print("Your Student ID: ");
        while (!sc.hasNextInt()) {
            System.out.print("❌ Enter valid ID: ");
            sc.next();
        }
        int sid = sc.nextInt();

        viewBooks(con);
        System.out.print("Book ID to Request: ");
        while (!sc.hasNextInt()) {
            System.out.print("❌ Enter valid Book ID: ");
            sc.next();
        }
        int bid = sc.nextInt();

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO requests (book_id, requested_by) VALUES (?, ?)"
        );
        ps.setInt(1, bid);
        ps.setInt(2, sid);
        int r = ps.executeUpdate();
        System.out.println(r > 0 ? "✅ Request submitted!" : "❌ Failed.");
        ps.close();
    }

    //  5. VIEW ALL REQUESTS
    static void viewRequests(Connection con) throws Exception {
        System.out.println("📋 All Book Requests:");
        String sql = "SELECT r.request_id, r.book_id, b.title, r.requested_by, s.name AS requester, r.status " +
                     "FROM requests r " +
                     "JOIN books b ON r.book_id = b.book_id " +
                     "JOIN students s ON r.requested_by = s.id";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            System.out.println("Request ID: " + rs.getInt("request_id"));
            System.out.println("  Book ID: " + rs.getInt("book_id") + " - " + rs.getString("title"));
            System.out.println("  Requested By: " + rs.getString("requester") + " (ID: " + rs.getInt("requested_by") + ")");
            System.out.println("  Status: " + rs.getString("status"));
            System.out.println("------------");
        }

        rs.close(); st.close();
    }

    //  6. APPROVE REQUEST (Admin)
    static void approveRequest(Connection con) throws Exception {
        System.out.print("🔐 Enter Request ID to Approve: ");
        while (!sc.hasNextInt()) {
            System.out.print("❌ Enter valid Request ID: ");
            sc.next();
        }
        int reqid = sc.nextInt();

        PreparedStatement ps = con.prepareStatement(
            "UPDATE requests SET status = 'approved' WHERE request_id = ?"
        );
        ps.setInt(1, reqid);
        int r = ps.executeUpdate();
        System.out.println(r > 0 ? "✅ Request Approved!" : "❌ Invalid Request ID.");
        ps.close();
    }

    //  View Students (Used internally)
    static void viewStudents(Connection con) throws Exception {
        System.out.println("📘 Registered Students:");
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, name, year, branch FROM students");
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") +
                               ", Year: " + rs.getString("year") + ", Branch: " + rs.getString("branch"));
        }
        rs.close(); st.close();
    }

    //  8. ADMIN PANEL – Delete Student and Related Data
    static void adminPanel(Connection con) throws Exception {
        System.out.print("🔐 Enter Admin Password: ");
        String pass = sc.nextLine();

        if (!pass.equals("Admin@01")) {
            System.out.println("❌ Access Denied!");
            return;
        }

        System.out.println("✅ Access Granted!");
        viewStudents(con);

        System.out.print("🗑 Enter Student ID to Delete: ");
        while (!sc.hasNextInt()) {
            System.out.print("❌ Enter valid ID: ");
            sc.next();
        }
        int sid = sc.nextInt();
        // Steps for Deleting ->
        //  Delete requests where requested_by is the student
        PreparedStatement ps1 = con.prepareStatement(
            "DELETE FROM requests WHERE requested_by = ?"
        );
        ps1.setInt(1, sid);
        ps1.executeUpdate();

        //  Get all book_ids donated by this student
        PreparedStatement psBooks = con.prepareStatement(
            "SELECT book_id FROM books WHERE donated_by = ?"
        );
        psBooks.setInt(1, sid);
        ResultSet rs = psBooks.executeQuery();

        //  For each book, delete all requests for that book
        while (rs.next()) {
            int bookId = rs.getInt("book_id");
            PreparedStatement ps2 = con.prepareStatement(
                "DELETE FROM requests WHERE book_id = ?"
            );
            ps2.setInt(1, bookId);
            ps2.executeUpdate();
            ps2.close();
        }
        rs.close();
        psBooks.close();

        // Delete books donated by the student
        PreparedStatement ps3 = con.prepareStatement(
            "DELETE FROM books WHERE donated_by = ?"
        );
        ps3.setInt(1, sid);
        ps3.executeUpdate();

        // Finally, delete the student
        PreparedStatement ps4 = con.prepareStatement(
            "DELETE FROM students WHERE id = ?"
        );
        ps4.setInt(1, sid);
        int result = ps4.executeUpdate();

        if (result > 0) {
            System.out.println("✅ Student and all related data deleted successfully.");
        } else {
            System.out.println("❌ No student found with that ID.");
        }

        // Close all
        ps1.close();
        ps3.close();
        ps4.close();
    }
}


// ISSUE : -> Always Showing all Registered Students -> So  -> When we want extension of this Project then we 
// can use One more option called Profile Where user can check his Id and As well as Book Donating ID 
// one more thing to do it LOGIN panne to more protection