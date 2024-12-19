import java.util.*;
import java.net.URL;    
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;


public class Client {
    /*Search for books by topic*/
    private static void searchBooksfromServer(XmlRpcClient client, String topic) {
        try {
            // Maps to the function in our server "sample":
            Vector<Object> params = new Vector<Object>();
            params.addElement(topic);
            Object[] result = (Object[]) client.execute("sample.search", params);

            // Check if there are books in the database:
            if (result.length > 0) {
                System.out.println("Books found:");
                for (int i = 0; i < result.length; i++) {
                    System.out.println(result[i]);
                }
            } else {
                System.out.println("No books found under this topic");
            }
        } catch (Exception e) {
            System.err.println("Client error: " + e);
        }
   }


    /* Lookup a book by ID */
    private static void lookupBookfromServer(XmlRpcClient client, int id){
        try {
            // Maps to the function lookup in our server "sample":
            Vector<Object> params = new Vector<Object>();
            params.addElement(id);
            String result = (String) client.execute("sample.lookup", params);
            if (!result.isEmpty()){
                System.out.println("Book Found: "+ result);
            } else {
                System.out.println("Book not found");
            } 
        } catch (Exception e) {
            System.err.println("Client error: " + e);
        }
    }


    /* Buy a book by ID */
    private static void buyBookfromServer(XmlRpcClient client, int id){
        try {
            // Maps to the function buy in our server "sample":
            Vector<Object> params = new Vector<Object>();
            params.addElement(id);
            String result = (String) client.execute("sample.buy", params);
            if (!result.isEmpty()){
                System.out.println("Book Bought: "+ result);
            } else{
                System.out.println("Book not found");
            }

            } catch (Exception e) {
            System.err.println("Client error: " + e);
        }
    }

    /* Client-facing interface: that parses and executes commands line request */
    public static void exceuteClientCommand(XmlRpcClient client){
        // Read in client commands from the terminal:
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("\nEnter a command: search <topic>, lookup <item_number>, buy <item_number>, exit");
            System.out.print("> ");
            String clientCommandInput = scanner.nextLine();

            // Split the input into command parts
            // Split into command and arguments
            String[] commandParts = clientCommandInput.split(" ", 2); 
            if (commandParts.length == 0){
                System.out.println("Please enter a command.");
                continue;
            }
            // Get the command (search, lookup, buy, exit)
            String command = commandParts[0].toLowerCase();  

            // Handle commands using switch-case
            switch (command) {
                // Search for books by topic (String)
                case "search":
                    if (commandParts.length == 2) {
                        // Get the topic for search
                        String topic = commandParts[1];
                        try {
                            searchBooksfromServer(client, topic);
                        } catch (Exception e) {
                            System.out.println("Error while searching for books: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Usage: search <topic>");
                    }
                    break;
                case "lookup":
                    // LookUp for books by an item_number (int)
                    if (commandParts.length == 2) {
                        try {
                            // Get the item number
                            int itemNumber = Integer.parseInt(commandParts[1].trim());
                            lookupBookfromServer(client, itemNumber);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid item number format. Please provide a valid integer item number.");
                        } catch (Exception e) {
                            System.out.println("Error during lookup " + e.getMessage());
                        }
                    } else {
                        System.out.println("Usage: lookup <item_number>");
                    }
                    break;
                case "buy":
                    // Buy books by an item_number (int)
                    if (commandParts.length == 2) {
                        try {
                            // Get the item number
                            int itemNumber = Integer.parseInt(commandParts[1].trim());
                            buyBookfromServer(client, itemNumber);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid item number format. Please provide a valid integer item number.");
                        } catch (Exception e) {
                            System.out.println("Error during buying: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Usage: buy <item_number>");
                    }
                    break;
                case "exit":
                    System.out.println("Exiting the store. Visit Again! :)");
                    return;
                default:
                    System.out.println("Unkown command: search <topic>, lookup <item_number>, buy <item_number>, exit");
                    break;
            }
        }  
    }

    public static void main (String [] args) {
        if(args.length == 0) {
            System.out.println("Usage: java Client <server>");
            System.exit(1);
        }
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        XmlRpcClient client=null;
        try {
            config.setServerURL(new URL("http://" + args[0] + ":" + 8200));
            client = new XmlRpcClient();
            client.setConfig(config);
        } catch (Exception e) {
            System.err.println("Client error: "+ e);
        }
        // Call Interactive Terminal:
        exceuteClientCommand(client);
    }
}
