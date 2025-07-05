import java.sql.*;
import java.util.*;

public class M {
    static final String DB_URL = "jdbc:mysql://localhost:3306/books_barter";
    static final String USER = "root";
    static final String PASS = "onkpatil!23";  // Replace with your actual password

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
        while (true) {
            System.out.println("\n📚 BOOK BARTER SYSTEM");
            System.out.println("1. Register Student");
            System.out.println("2. Donate Book");
            System.out.println("3. View All Books");
            System.out.println("4. Request Book");
            System.out.println("5. View Book Requests");
            System.out.println("6. Approve Request");
            System.out.println("7. Exit");
            System.out.print("👉 Your choice: ");
            int ch = sc.nextInt(); sc.nextLine();

            switch (ch) {
                case 1: registerStudent(con); break;
                case 2: donateBook(con); break;
                case 3: viewBooks(con); break;
                case 4: requestBook(con); break;
                case 5: viewRequests(con); break;
                case 6: approveRequest(con); break;
                case 7: con.close(); System.out.println("👋 Thanks for using our service, Bye!"); return;
                default: System.out.println("❌ Invalid choice!");
            }
        }
    }

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

    static void donateBook(Connection con) throws Exception {
        System.out.println("📦 Donate Book");
        System.out.print("Student ID (Donor): "); int sid = sc.nextInt(); sc.nextLine();
        System.out.print("Title: "); String title = sc.nextLine();
        System.out.print("Author: "); String author = sc.nextLine();
        System.out.print("Subject: "); String subject = sc.nextLine();
        System.out.print("Branch: "); String branch = sc.nextLine();
        System.out.print("Semester: "); int sem = sc.nextInt();

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

    static void requestBook(Connection con) throws Exception {
        System.out.println("📝 Request a Book");
        System.out.print("Your Student ID: "); int sid = sc.nextInt();
        System.out.print("Book ID to Request: "); int bid = sc.nextInt();

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO requests (book_id, requested_by) VALUES (?, ?)"
        );
        ps.setInt(1, bid);
        ps.setInt(2, sid);
        int r = ps.executeUpdate();
        System.out.println(r > 0 ? "✅ Request submitted!" : "❌ Failed.");
        ps.close();
    }

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

    static void approveRequest(Connection con) throws Exception {
        System.out.print("🔐 Enter Request ID to Approve: ");
        int reqid = sc.nextInt();

        PreparedStatement ps = con.prepareStatement(
            "UPDATE requests SET status = 'approved' WHERE request_id = ?"
        );
        ps.setInt(1, reqid);
        int r = ps.executeUpdate();
        System.out.println(r > 0 ? "✅ Request Approved!" : "❌ Invalid Request ID.");
        ps.close();
    }
}
