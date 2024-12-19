JAVAC = javac
JAVA = java
PYTHON = python3

# Apache XML-RPC .jar dependencies (place these in the lib/ folder)
JAR_PATH = lib/xmlrpc-server-3.1.3.jar:lib/xmlrpc-client-3.1.3.jar:lib/xmlrpc-common-3.1.3.jar:lib/ws-commons-util-1.0.2.jar

# Java main classes
SERVER_MAIN_CLASS = Server
CLIENT_MAIN_CLASS = Client

# Default restock amount and interval for the server
RESTOCK_AMOUNT = 5
RESTOCK_INTERVAL = 60000

# Targets

# Compile both Server.java and Client.java
all: compile_server compile_client

# Compile the server
compile_server:
	$(JAVAC) -cp $(JAR_PATH) $(SERVER_MAIN_CLASS).java

# Compile the client
compile_client:
	$(JAVAC) -cp $(JAR_PATH) $(CLIENT_MAIN_CLASS).java

# Run the server with default restock amount and interval
run_server:
	$(JAVA) -cp .:$(JAR_PATH) $(SERVER_MAIN_CLASS) $(RESTOCK_AMOUNT) $(RESTOCK_INTERVAL)

# Run the Java client (connecting to localhost)
run_java_client:
	$(JAVA) -cp .:$(JAR_PATH) $(CLIENT_MAIN_CLASS) localhost

# Run the Python client (connecting to localhost)
run_python_client:
	$(PYTHON) client.py localhost

# Run the Python client with a custom server IP
run_python_client_with_server:
	$(PYTHON) client.py $(SERVER)

# Clean up all compiled .class files
clean:
	rm -f *.class