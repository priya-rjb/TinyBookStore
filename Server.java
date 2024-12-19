import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Server { 
    /* Connection to database */
    private static Connection c = null;
    
    /* Log of purchases */
    private static HashMap<Integer, Integer> log = null;

    /* Declare a semaphore with 1 permit (only 1 user can buy at a time) */
    private static final Semaphore SP = new Semaphore(1);

    /* Restock information */
    private static int restockAmount = 5;          // Default set to 5
    private static int restockInterval = 60000;    // Default set to 1 minute
    private static Thread restockThread;

    /* Start the server */
    private static void startServer() {
        try {
           PropertyHandlerMapping phm = new PropertyHandlerMapping();
           XmlRpcServer xmlRpcServer;
           WebServer server = new WebServer(8200);
           xmlRpcServer = server.getXmlRpcServer();
           phm.addHandler("sample", Server.class);
           xmlRpcServer.setHandlerMapping(phm);
           System.out.println("Server starting on port 8200");
           server.start();
       } catch (Exception exception) {
           System.err.println("Server: " + exception);
       }
    }

    /* Connect to the database */
    private static void connectDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:books.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /* Create the restock thread */
    private static void setRestock() {
        // Create a thread for restocking books at intervals (passed via command-line args)
        final int finalRestockAmount = restockAmount;       // Assign to a final variable for use in the lambda
        final int finalRestockInterval = restockInterval;   // Assign to a final variable for use in the lambda
        restockThread = new Thread(() -> {
            while (true) {
                try {
                    // Sleep for interval before restocking
                    Thread.sleep(finalRestockInterval);

                    // Restock all books
                    restock(53477, finalRestockAmount);
                    restock(53573, finalRestockAmount);
                    restock(12365, finalRestockAmount);
                    restock(12498, finalRestockAmount);
                    System.out.println("Restocked all books successfully");
                } catch (InterruptedException e) {
                    System.err.println("Restock thread interrupted: " + e.getMessage());
                }
            }
        });
    }

    /* Execute command line requests */
    private static void execute(String command) {
        String[] commandParts = command.split(" ");

        // Get the command name (log, restock, or update)
        String commandName = commandParts[0];
        switch (commandName.toLowerCase()) {
            case "log":
                // Update command takes an ID (int) and price (double)
                if (commandParts.length == 1) {
                    log();
                } else {
                    System.out.println("Usage: log");
                }
                break;
            case "restock":
                // Restock command takes an ID and an amount (both integers)
                if (commandParts.length == 3) {
                    try {
                        int id = Integer.parseInt(commandParts[1]);
                        int amount = Integer.parseInt(commandParts[2]);
                        restock(id, amount);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format. Please provide valid integers for ID and amount.");
                    }
                } else {
                    System.out.println("Usage: restock <id> <amount>");
                }
                break;
            case "update":
                // Update command takes an ID (int) and price (double)
                if (commandParts.length == 3) {
                    try {
                        int id = Integer.parseInt(commandParts[1]);
                        double price = Double.parseDouble(commandParts[2]);
                        update(id, price);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format. Please provide valid integers for ID and price.");
                    }
                } else {
                    System.out.println("Usage: update <id> <price>");
                }
                break;
            default:
                System.out.println("Unknown command: " + command);
                break;
        }
    }

    /* Insert books into table (used initially to insert the 4 books) */
    private static void insertBooks() {
        Statement stmt = null;

        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO BOOKS (ID,TITLE,TOPIC,STOCK,PRICE) " +
                        "VALUES (53477, 'Achieve Less Bugs and More Hugs', 'distributed systems', 10, 100.00 );"; 
            stmt.executeUpdate(sql);

            sql = "INSERT INTO BOOKS (ID,TITLE,TOPIC,STOCK,PRICE) " +
                "VALUES (53573, 'Distributed Systems for Dummies', 'distributed systems', 10, 100.00 );"; 
            stmt.executeUpdate(sql);

            sql = "INSERT INTO BOOKS (ID,TITLE,TOPIC,STOCK,PRICE) " +
                "VALUES (12365, 'Surviving College', 'college life', 10, 100.00 );"; 
            stmt.executeUpdate(sql);

            sql = "INSERT INTO BOOKS (ID,TITLE,TOPIC,STOCK,PRICE) " +
                "VALUES (12498, 'Cooking for the Impatient Undergraduate', 'college life', 10, 100.00 );"; 
            stmt.executeUpdate(sql);

            stmt.close();

            c.commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Records created successfully");
    }

    /* Helper function for updating book stock (used by the buy() and restock() functions) */ 
    private static Boolean updateStock(int id, int quantity, boolean buy, boolean restock) {
        Statement stmt = null;
        ResultSet rs = null;
        int currentStock = 0;

        try {
            // Use the passed-in connection object to create a Statement
            stmt = c.createStatement();

            String query = "SELECT STOCK FROM BOOKS WHERE ID = " + id + ";";
            rs = stmt.executeQuery(query);

            // if (rs.next()) {
            //     currentStock = rs.getInt("STOCK");
            // }
            // Check if the ID exists in the database
            if (!rs.next()) {
                System.out.println("Book with ID " + id + " not found in the database.");
                return false;  // ID does not exist
            }

            // If the ID exists, retrieve the current stock
            currentStock = rs.getInt("STOCK");

            // Determine the new stock value based on the flags
            int newStock = currentStock;
            if (buy) {
                if (currentStock < 1) {
                    System.out.println("Insufficient stock to complete the purchase.");
                    return false;
                }
                newStock = currentStock - 1; // Decrease stock by 1 for buying
            } else if (restock) {
                newStock = currentStock + quantity; // Increase stock by the specified quantity for restocking
            }
        
            String sql = "UPDATE BOOKS SET STOCK = " + newStock + " WHERE ID = " + id + ";";
            stmt.executeUpdate(sql);
            c.commit();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return true;
    }

    /**************** INTERNAL SERVER-FACING COMMANDS ****************/

    /* Print log of purchases */
    private static void log() {
        System.out.println(log.toString());
    }

    /* Update stocks for books */
    private static void restock(int id, int amount) {
        try {
            // Acquire the semaphore before proceeding
            SP.acquire();

            // Perform the restock
            updateStock(id, amount, false, true);
            System.out.println("Item " + id + " restocked successfully");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Release the semaphore after the operation
            SP.release();
        }
    }

    /* Update price for a book */
    private static void update(int id, double price){
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Use the passed-in connection object to create a Statement
            stmt = c.createStatement();
            // Update price
            String sql = "UPDATE BOOKS SET PRICE = " + price + " WHERE ID = " + id + ";";
            stmt.executeUpdate(sql);
            c.commit();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        System.out.println("Item " + id + " price updated to " + price);
    }

    /**************** EXTERNAL CLIENT-FACING COMMANDS ****************/

    /* Find all books in a given topic */
    public String[] search(String topic){
        Statement stmt = null;
        ArrayList<String> books = new ArrayList<>(); 
        try {
            stmt = c.createStatement();
            // Query the books table to search for books based on the topic
            ResultSet rs = stmt.executeQuery("SELECT * FROM BOOKS WHERE topic = '" + topic.toLowerCase() + "';");
            
            while ( rs.next() ) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                
                String result = "Item Number: " + id + ", Title: " + title;
                books.add(result); 
            }
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            //System.exit(0);
        }
        //System.out.println("Table read successfully");
        return books.toArray(new String[0]);
    }

    /* Look up book details with item number */
    public String lookup(int id){
        Statement stmt = null;
        String result = "";
        try {
            stmt = c.createStatement();
            // Query the books table to search for books based on the topic
            ResultSet rs = stmt.executeQuery("SELECT * FROM BOOKS WHERE ID = '" + id+ "';");
            
            String title = rs.getString("title");
            float cost = rs.getFloat("price");
            String topic = rs.getString("topic");
            int stock = rs.getInt("stock");
            String inStock = "No";
            if (stock > 0) {
                inStock = "Yes";
            }
            result = "Title: " + title + ", Cost: " + cost + ", Topic: " + topic + ", In Stock: " + inStock;
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            //System.exit(0);
        }
        //System.out.println("Table read successfully");
        return result;
    }

    /* Purchases a book (decrements stock by 1) with item number */
    public String buy(int id){
        try {
            // Acquire the semaphore before proceeding
            SP.acquire();

            // Perform the stock update and log the purchase
            if (updateStock(id, 1, true, false)) {
                int count = log.getOrDefault(id, 0);
                log.put(id, count + 1);
                System.out.println("Item " + id + " purchased successfully");
                return "Item " + id + " purchased successfully";
            } else {
                return "Item " + id + " cannot be purchased.";
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "Error processing purchase.";
        } finally {
            // Release the semaphore after the operation
            SP.release();
        }
    }

    /* Set up server and database connection */
    public static void main (String [] args) {
        // Starting up the server
        startServer();

        // Connecting to the database
        connectDB();

        // Create log dictionary
        log = new HashMap<>();
    
        // Set restock interval and amount
        if (args.length == 2) {
            restockAmount = Integer.parseInt(args[0]);
            restockInterval = Integer.parseInt(args[1]);
        }
        setRestock();
        restockThread.start();

        // Scanner for taking command line requests
        Scanner scanner = new Scanner(System.in);
        String command;

        // Loop to take input commands
        while (true) {
            System.out.print("\n> "); 
            command = scanner.nextLine();
            execute(command);
        }
    }
}