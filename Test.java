import java.util.*;
import java.net.URL;    
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {

    // XML-RPC Client
    private static XmlRpcClient client;

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            System.out.println("Usage: java Test <server>");
            System.exit(1);
        }

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL("http://" + args[0] + ":" + 8200));
            client = new XmlRpcClient();  // Assign the static client
            client.setConfig(config);
        } catch (Exception e) {
            System.err.println("Client error: " + e);
            e.printStackTrace();
        }

        // Check if client is initialized
        if (client == null) {
            System.err.println("Failed to initialize the client.");
            System.exit(1);  // Exit if client is not initialized
        }

        // Sequential Requests
        issueSequentialBuys(5);  // Issue 5 sequential buys
        issueSequentialSearches("distributed systems");

        // Concurrent Requests (using threads)
        issueConcurrentBuys(500);  // Issue 10 concurrent buys
        issueConcurrentSearches("college life", 5); // Issue 5 concurrent searches
    }

    // Method to issue sequential buy requests
    public static void issueSequentialBuys(int numBuys) throws Exception {
        for (int i = 0; i < numBuys; i++) {
            Vector<Integer> params = new Vector<>();
            params.add(53477);  // Assume we're buying a book with ID 53477
            String result = (String) client.execute("sample.buy", params);
            System.out.println("Sequential Buy #" + (i + 1) + ": " + result);
        }
    }

    // Method to issue sequential search requests
    public static void issueSequentialSearches(String topic) throws Exception {
        Vector<String> params = new Vector<>();
        params.add(topic);
        
        // Execute the search and get the result as Object[]
        Object[] results = (Object[]) client.execute("sample.search", params);

        System.out.println("Sequential Search Results: ");
        
        // Iterate through the results and cast each element to String
        for (Object result : results) {
            System.out.println((String) result);  // Safe casting to String
        }
    }

    // Method to issue concurrent buy requests
    public static void issueConcurrentBuys(int numBuys) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numBuys);

        for (int i = 0; i < numBuys; i++) {
            executorService.submit(() -> {
                try {
                    Vector<Integer> params = new Vector<>();
                    params.add(53477);  // Assume we're buying the same book with ID 53477
                    String result = (String) client.execute("sample.buy", params);
                    System.out.println("Concurrent Buy: " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
    }

    // Method to issue concurrent search requests
    public static void issueConcurrentSearches(String topic, int numSearches) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numSearches);

        for (int i = 0; i < numSearches; i++) {
            executorService.submit(() -> {
                try {
                    Vector<String> params = new Vector<>();
                    params.add(topic);
                    
                    // Execute the search and get the result as Object[]
                    Object[] results = (Object[]) client.execute("sample.search", params);

                    System.out.println("Concurrent Search Results: ");
                    
                    // Iterate through the results and cast each element to String
                    for (Object result : results) {
                        System.out.println((String) result);  // Safe casting to String
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
    }
}