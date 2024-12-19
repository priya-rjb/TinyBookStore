import xmlrpc.client, sys

# Check if the server address is provided as a command-line argument
if len(sys.argv) < 2:
    print("Usage: python client.py <server>")
    sys.exit(1)

# Search for books by topic
def searchBooksfromServer(proxy, topic):
    try:
        print("\nBooks under '%s':" % topic)
        result = proxy.sample.search(topic)
        if result:
            for book in result:
                print(book)
        else:
            print("No books found under this topic.")
    except Exception as e:
        print("Error during search:", e)

# Lookup a book by ID
def lookupBookfromServer(proxy, item_number):
    try:
        result = proxy.sample.lookup(item_number)
        if result:
            print("\nBook with item number '%d' found:" % item_number)
            print(result)
        else:
            print("\nBook with item number '%d' not found:" % item_number)
        
    except Exception as e:
        print("Error during lookup: ", e)

# Buy a book by ID
def buyBookfromServer(proxy, item_number):
    try:    
        print("\n Book with item number '%d' bought:" % item_number)
        result = proxy.sample.buy(item_number)
        print(result)
    except Exception as e:
        print("Error buying: ", e)

# Client-facing interface: that parses and executes commands line request
def exceuteClientCommands(proxy):
    # Interactive loop for Client-facing interface:
    while True:
        # Read in client commands from the terminal:
        clientCommandInput = input("\nEnter a command: search <topic>, lookup <item_number>, buy <item_number>, exit \n> ")
        # Split the command and arguments
        commandParts = clientCommandInput.split(" ", 1)

        if len(commandParts) == 0:
            print("Please enter a command.")
            continue

        command = commandParts[0].lower()
        # Search for books by topic
        if command == "search":
            if len(commandParts) ==2:
                try:
                    topic = commandParts[1]
                    searchBooksfromServer(proxy, topic)
                except Exception as e:
                    print("Error during search: ", e)
            else:
                print("usage: search <topic>")
        # Lookup a book by ID 
        elif command == "lookup":
            if len(commandParts) ==2:
                try:
                    item_number = int(commandParts[1])
                    lookupBookfromServer(proxy, item_number)  
                except ValueError:
                    # This block catches errors if the input is not a valid integer
                    print("Invalid item number format. Please provide a valid integer item number.")
                except Exception as e:
                    print("Error during lookup: ", e)
            else:
                print("usage: lookup <item_number>")
        # Buy a book by ID
        elif command == "buy":
            if len(commandParts) ==2:
                try:    
                    item_number = int(commandParts[1])
                    buyBookfromServer(proxy, item_number)
                except ValueError:
                    # This block catches errors if the input is not a valid integer
                    print("Invalid item number format. Please provide a valid integer item number.")
                except Exception as e:
                    print("Error buying: ", e)
            else:
                print("usage: buy <item_number>")
        #'exit' command
        elif command == "exit":
            print("Exiting the store. Visit again!")
            break
        # invalid commands
        else:
            print("Unknown command. Available commands: search <topic>, lookup <item_number>, buy <item_number>, exit")
# Connect to the XML-RPC server
with xmlrpc.client.ServerProxy("http://"+sys.argv[1]+":8200") as proxy:
    exceuteClientCommands(proxy)
