import xmlrpc.client, sys
import time

# Function to perform a search request
def search_books_from_server(proxy, topic, request_number):
    try:
        print(f"Request {request_number}: Searching for books under '{topic}'")
        result = proxy.sample.search(topic)
        if result:
            for book in result:
                print(book)
        else:
            print("No books found under this topic.")
    except Exception as e:
        print("Error during search:", e)

# Function to perform a lookUp request
def lookupBookfromServer(proxy, item_number, request_number):
    try:
        print(f"Request {request_number}: Looking Up book with item number {item_number}")
        result = proxy.sample.lookup(item_number)
        if result:
            print("\nBook with item number '%d' found:" % item_number)
            print(result)
        else:
            print("\nBook with item number '%d' not found:" % item_number)
        
    except Exception as e:
        print(f"Error during lookUp in request {request_number}: {e}")

# Function to perform a buy request
def buy_book_from_server(proxy, item_number, request_number):
    try:
        print(f"Request {request_number}: Buying book with item number {item_number}")
        result = proxy.sample.buy(item_number)
        print(f"Buy result: {result}")
    except Exception as e:
        print(f"Error during buy in request {request_number}: {e}")

# Function to execute 500 search and buy requests sequentially
def execute_sequential_requests(proxy, topic, item_number, num_requests):
    # Start the timer for the entire 500 requests
    start_time = time.time()

    for i in range(1, num_requests + 1):
        # Perform search and buy sequentially
        #search_books_from_server(proxy, topic, i)
        lookupBookfromServer(proxy, item_number, i)
        #buy_book_from_server(proxy, item_number, i)

    # End the timer after all requests are done
    end_time = time.time()
    
    # Calculate the total time taken for all requests
    total_duration = end_time - start_time
    print(f"\nTotal time for {num_requests} sequential search/lookup/buy requests: {total_duration:.4f} seconds")

# Connect to the XML-RPC server
topic = "distributed systems"  # Example topic to search for
item_number = 12498  # Example item number for buying
num_requests = 500  # Number of sequential requests

# Connect to the server and execute the requests
with xmlrpc.client.ServerProxy("http://"+sys.argv[1]+":8200") as proxy:
    execute_sequential_requests(proxy, topic, item_number, num_requests)
