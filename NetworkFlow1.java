

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class NetworkFlow1 {
    private final int nodes;         // Number of nodes
    private final int[][] capacity;  // Capacity matrix
    private final int[][] flow;      // Flow matrix
    private final List<Integer>[] adjacencyList; // Adjacency list representation
    
    // Getters for use in performance analysis
    public int getNodes() {
        return nodes;
    }
    
    public int getCapacity(int from, int to) {
        return capacity[from][to];
    }
    
    /**
     * Constructor for NetworkFlow that initializes the data structures
     * @param nodes Number of nodes in the network
     */
    @SuppressWarnings("unchecked")
    public NetworkFlow1(int nodes) {
        this.nodes = nodes;
        // Initialize capacity and flow matrices
        capacity = new int[nodes][nodes];
        flow = new int[nodes][nodes];
        
        // Initialize adjacency list
        adjacencyList = new List[nodes];
        for (int i = 0; i < nodes; i++) {
            adjacencyList[i] = new ArrayList<>();
        }
    }
    
    /**
     * Adds an edge to the network with the specified source, destination, and capacity
     * @param from Source node
     * @param to Destination node
     * @param cap Capacity of the edge
     */
    public void addEdge(int from, int to, int cap) {
        // Add edge to capacity matrix
        capacity[from][to] = cap;
        
        // Add to adjacency list (both directions for residual graph)
        adjacencyList[from].add(to);
        if (!adjacencyList[to].contains(from)) {
            adjacencyList[to].add(from); // For residual edges
        }
    }
    
    /**
     * Uses Breadth-First Search to find an augmenting path in the residual graph
     * @param source Source node
     * @param sink Sink node
     * @param parent Array to store the path
     * @return True if there is a path from source to sink
     */
    private boolean bfs(int source, int sink, int[] parent) {
        boolean[] visited = new boolean[nodes];
        Queue<Integer> queue = new LinkedList<>();
        
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;
        
        // BFS loop
        while (!queue.isEmpty()) {
            int u = queue.poll();
            
            // Check all adjacent nodes
            for (int v : adjacencyList[u]) {
                // If not visited and has residual capacity
                if (!visited[v] && capacity[u][v] - flow[u][v] > 0) {
                    queue.add(v);
                    parent[v] = u;
                    visited[v] = true;
                }
            }
        }
        
        // Return true if sink was reached
        return visited[sink];
    }
    
    /**
     * Main implementation of the Ford-Fulkerson algorithm
     * @param source Source node
     * @param sink Sink node
     * @param verbose Whether to print detailed steps
     * @return Maximum flow value
     */
    public int fordFulkerson(int source, int sink, boolean verbose) {
        int maxFlow = 0;
        int[] parent = new int[nodes];
        List<String> steps = new ArrayList<>();
        
        // While there is an augmenting path
        while (bfs(source, sink, parent)) {
            // Find the minimum residual capacity along the path
            int pathFlow = Integer.MAX_VALUE;
            StringBuilder pathStr = new StringBuilder();
            
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, capacity[u][v] - flow[u][v]);
                pathStr.insert(0, " → " + v);
            }
            pathStr.insert(0, source);
            
            // Update the residual capacities and flows along the path
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                flow[u][v] += pathFlow;
                flow[v][u] -= pathFlow; // For residual network
            }
            
            maxFlow += pathFlow;
            steps.add("Path: " + pathStr + ", Flow added: " + pathFlow + ", Total flow: " + maxFlow);
        }
        
        // Print the steps of the algorithm if verbose is true
        if (verbose) {
            System.out.println("Ford-Fulkerson Algorithm Steps:");
            for (String step : steps) {
                System.out.println(step);
            }
        }
        
        // Return the final maximum flow
        return maxFlow;
    }
    
    /**
     * Prints the final flow distribution in the network
     */
    public void printFinalFlow() {
        System.out.println("\nFinal Flow Distribution:");
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (capacity[i][j] > 0 && flow[i][j] > 0) {
                    System.out.println("Edge (" + i + "," + j + "): Flow = " + flow[i][j] + 
                                      " / Capacity = " + capacity[i][j]);
                }
            }
        }
    }
    
    /**
     * Static method to parse a network from a file
     * @param filePath Path to the input file
     * @return A NetworkFlow object representing the parsed network
     */
    public static NetworkFlow1 parseFromFile(String filePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filePath));
        
        // Read number of nodes
        int n = scanner.nextInt();
        NetworkFlow1 network = new NetworkFlow1(n);
        
        // Read edges
        while (scanner.hasNext()) {
            int from = scanner.nextInt();
            int to = scanner.nextInt();
            int cap = scanner.nextInt();
            network.addEdge(from, to, cap);
        }
        
        scanner.close();
        return network;
    }
    
    /**
     * Counts the number of edges in the network
     * @return The number of edges
     */
    public int countEdges() {
        int count = 0;
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (capacity[i][j] > 0) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Processes a single network file
     * @param filePath Path to the input file
     * @param verbose Whether to print detailed output
     * @return An array with [nodes, edges, maxFlow, parseTime, algoTime, totalTime]
     */
    public static long[] processFile(String filePath, boolean verbose) {
        long[] results = new long[6]; // [nodes, edges, maxFlow, parseTime, algoTime, totalTime]
        
        try {
            if (verbose) {
                System.out.println("\n----------------------------------------");
                System.out.println("Processing file: " + filePath);
            }
            
            // Time the file parsing
            long startParseTime = System.currentTimeMillis();
            NetworkFlow1 network = parseFromFile(filePath);
            long parseTime = System.currentTimeMillis() - startParseTime;
            
            int source = 0;
            int sink = network.nodes - 1;
            int edges = network.countEdges();
            
            if (verbose) {
                System.out.println("Network loaded with " + network.nodes + " nodes and " + edges + " edges");
                System.out.println("Parsing time: " + parseTime + " ms");
                System.out.println("Calculating maximum flow from node " + source + " to node " + sink);
            }
            
            // Time the algorithm execution
            long startAlgoTime = System.currentTimeMillis();
            int maxFlow = network.fordFulkerson(source, sink, verbose);
            long algoTime = System.currentTimeMillis() - startAlgoTime;
            long totalTime = parseTime + algoTime;
            
            if (verbose) {
                System.out.println("\nMaximum Flow: " + maxFlow);
                System.out.println("Algorithm execution time: " + algoTime + " ms");
                System.out.println("Total time: " + totalTime + " ms");
                
                // Only print detailed flow for smaller networks
                if (network.nodes <= 30) {
                    network.printFinalFlow();
                }
            }
            
            // Store results
            results[0] = network.nodes;
            results[1] = edges;
            results[2] = maxFlow;
            results[3] = parseTime;
            results[4] = algoTime;
            results[5] = totalTime;
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: Input file not found - " + filePath);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error occurred during execution for file: " + filePath);
            e.printStackTrace();
        }
        
        return results;
    }
    
    public static void main(String[] args) {
        // Check if args has any files or directories specified
        if (args.length == 0) {
            System.out.println("Usage options:");
            System.out.println("1. java NetworkFlow1 file1.txt file2.txt ... - Process specific files");
            System.out.println("2. java NetworkFlow1 -d directory - Process all .txt files in directory");
            System.out.println("3. java NetworkFlow1 -a - Process all benchmark files (ladder_*.txt and bridge_*.txt)");
            System.out.println("\nRunning with default file 'network.txt'");
            
            // Process default file
            processFile("network.txt", true);
            return;
        }
        
        List<String> filesToProcess = new ArrayList<>();
        boolean writeToCSV = false;
        
        // Process command line arguments
        if (args[0].equals("-d") && args.length > 1) {
            // Process all txt files in a directory
            File dir = new File(args[1]);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
            
            if (files != null) {
                for (File file : files) {
                    filesToProcess.add(file.getPath());
                }
                writeToCSV = true;
            }
        } else if (args[0].equals("-a")) {
            // Process all benchmark files
            File dir = new File(".");
            File[] files = dir.listFiles((d, name) -> 
                name.endsWith(".txt") && (name.startsWith("ladder_") || name.startsWith("bridge_")));
            
            if (files != null) {
                // Sort files by size for better presentation
                Arrays.sort(files, (f1, f2) -> Long.compare(f1.length(), f2.length()));
                
                for (File file : files) {
                    filesToProcess.add(file.getPath());
                }
                writeToCSV = true;
            }
        } else {
            // Process specific files
            for (String filePath : args) {
                filesToProcess.add(filePath);
            }
            
            if (args.length > 1) {
                writeToCSV = true;
            }
        }
        
        // If no files found
        if (filesToProcess.isEmpty()) {
            System.out.println("No files to process. Please check your arguments.");
            return;
        }
        
        System.out.println("Processing " + filesToProcess.size() + " files...");
        
        // Create CSV file for results if processing multiple files
        if (writeToCSV) {
            try (FileWriter writer = new FileWriter("network_flow_results.csv")) {
                // Write CSV header
                writer.write("File,Nodes,Edges,Max Flow,Parse Time (ms),Algorithm Time (ms),Total Time (ms)\n");
                
                // Process each file
                for (String filePath : filesToProcess) {
                    // Only show detailed output for the first file if there are many
                    boolean verbose = filesToProcess.size() <= 3;
                    
                    long[] results = processFile(filePath, verbose);
                    
                    // Always print a summary
                    if (!verbose) {
                        System.out.println(filePath + ": " + results[0] + " nodes, " + 
                                           results[1] + " edges, Max Flow = " + results[2] + 
                                           ", Time = " + results[5] + " ms");
                    }
                    
                    // Write to CSV
                    writer.write(String.format("%s,%d,%d,%d,%d,%d,%d\n", 
                                  new File(filePath).getName(), 
                                  results[0], results[1], results[2], 
                                  results[3], results[4], results[5]));
                }
                
                System.out.println("\nResults saved to network_flow_results.csv");
                
            } catch (IOException e) {
                System.err.println("Error writing to CSV file");
                e.printStackTrace();
            }
        } else {
            // Process single file with full output
            processFile(filesToProcess.get(0), true);
        }
    }
}

