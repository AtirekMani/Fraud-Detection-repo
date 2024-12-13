import javax.xml.crypto.Data;
import java.sql.*;
import java.lang.*;

public class DatabaseConnection{

    private static final String URL = "jdbc:mysql://localhost:3306/BANK";
    private static final String USER = "root";
    private static final String PASSWORD = "atirekATIREK";
    private Connection conn;

    // Constructor to establish the connection
    public DatabaseConnection() throws SQLException {
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Method to close the connection
    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to create a new account
    public void createAccount(String name, String password, String email, String phone, String accountType, double initialBalance) {
        try {
            // Insert user into the 'users' table
            String insertUserQuery = "INSERT INTO users (name, password, email, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmtUser = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, name);
            pstmtUser.setString(2, password);
            pstmtUser.setString(3, email);
            pstmtUser.setString(4, phone);
            pstmtUser.executeUpdate();

            // Get the user_id of the newly created user
            ResultSet rs = pstmtUser.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            // Insert account into the 'accounts' table
            String insertAccountQuery = "INSERT INTO accounts (user_id, account_type, balance) VALUES (?, ?, ?)";
            PreparedStatement pstmtAccount = conn.prepareStatement(insertAccountQuery);
            pstmtAccount.setInt(1, userId);
            pstmtAccount.setString(2, accountType);
            pstmtAccount.setDouble(3, initialBalance);
            pstmtAccount.executeUpdate();
            System.out.println("Account created successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to deposit money into an account
    public void deposit(int accountId, double amount) {
        try {
            String query = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();

            // Record the transaction
            recordTransaction(accountId, "Deposit", amount);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to withdraw money from an account
    public void withdraw(int accountId, double amount) {
        try {
            String query = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();

            // Record the transaction
            recordTransaction(accountId, "Withdraw", amount);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to transfer money between accounts
    public void transferFunds(int fromAccountId, int toAccountId, double amount) {
        try {
            // Withdraw from the sender's account
            String withdrawQuery = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            PreparedStatement pstmtWithdraw = conn.prepareStatement(withdrawQuery);
            pstmtWithdraw.setDouble(1, amount);
            pstmtWithdraw.setInt(2, fromAccountId);
            pstmtWithdraw.executeUpdate();

            // Deposit to the recipient's account
            String depositQuery = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            PreparedStatement pstmtDeposit = conn.prepareStatement(depositQuery);
            pstmtDeposit.setDouble(1, amount);
            pstmtDeposit.setInt(2, toAccountId);
            pstmtDeposit.executeUpdate();

            // Record the transactions
            recordTransaction(fromAccountId, "Transfer Out", amount);
            recordTransaction(toAccountId, "Transfer In", amount);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to record a transaction
    private void recordTransaction(int accountId, String type, double amount) {
        try {
            String transactionQuery = "INSERT INTO transactions (account_id, type, amount) VALUES (?, ?, ?)";
            PreparedStatement pstmtTransaction = conn.prepareStatement(transactionQuery);
            pstmtTransaction.setInt(1, accountId);
            pstmtTransaction.setString(2, type);
            pstmtTransaction.setDouble(3, amount);
            pstmtTransaction.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            DatabaseConnection bankingSystem = new DatabaseConnection();

            // Example usage
            bankingSystem.createAccount("John Doe", "password123", "john.doe@example.com", "1234567890", "Saving", 5000.00);
            bankingSystem.deposit(1, 1500.00); // Deposit into account with ID 1
            bankingSystem.withdraw(1, 500.00); // Withdraw from account with ID 1
            bankingSystem.transferFunds(1, 2, 200.00); // Transfer from account 1 to account 2

            bankingSystem.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
