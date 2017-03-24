import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Allan Wang on 12/03/2017.
 * FordFulkerson & BellmanFord Tester class
 * <p>
 * Checks for correct maxflow in FordFulkerson, as well as valid net flows
 * Checks for correct shortest path in BellmanFord, or if exception is thrown when needed
 * <p>
 * Copy this file in the same directory as the other classes
 * All imports are added, so as long as there are no further package names,
 * you should be able to run the main method directly
 * <p>
 * Have a look at the commit history for update timestamps
 */
public class Comp251A3Tester {

    public static void main(String[] args) {
        deleteTesters(); //clear files since WGraph constructor won't do it
        initTesters(); //create test folder
        //run series of tests
        ff2();
        bf1();
        bf2();
        bf3();
        bf4();
        printResults();
        //deleteTesters(); //to clear the test folder
    }

    //data from ff2.txt
    static void ff2() {
        new Comp251A3Tester("ff2",
                0, 5,
                6,
                0, 1, 16,
                0, 2, 13,
                1, 3, 12,
                2, 1, 4,
                2, 4, 14,
                3, 2, 9,
                3, 5, 20,
                4, 3, 7,
                4, 5, 4)
                //.printGraph()
                .fordFulkerson()
                .compareFFloosely(23);
    }

    static void bf1() {
        new Comp251A3Tester("bf1",
                0, 8,
                9,
                0, 1, 10,
                0, 2, 12,
                1, 2, 9,
                1, 3, 8,
                2, 4, 3,
                2, 5, 1,
                3, 4, 7,
                3, 6, 11,
                3, 7, 5,
                4, 5, 3,
                5, 7, 6,
                6, 7, 9,
                6, 8, 2,
                7, 8, 11)
                .bellmanFord()
                .compareBF(0, 2, 5, 7, 8);
    }

    static void bf2() {
        new Comp251A3Tester("bf2",
                0, 7,
                8,
                1, 2, 1,
                0, 1, 2,
                0, 2, 3,
                0, 4, 4,
                4, 1, 5,
                1, 3, 6,
                3, 4, 7,
                6, 3, 4,
                5, 4, 8,
                5, 6, 2,
                6, 7, 9)
                .bellmanFord()
                .assertBFException("No path exists from source 0 to destination 7");
    }

    static void bf3() {
        new Comp251A3Tester("bf3",
                0, 7,
                8,
                1, 2, 1,
                0, 1, 2,
                0, 2, 3,
                0, 4, 4,
                4, 1, 5,
                1, 3, 6,
                3, 4, 7,
                6, 3, 4,
                5, 4, 8,
                6, 7, 9)
                .bellmanFord()
                .assertBFException("No path exists from source 0 to destination 7");
    }

    static void bf4() {
        new Comp251A3Tester("bf4",
                0, 8,
                9,
                0, 1, 10,
                0, 2, 12,
                1, 2, 9,
                1, 3, 8,
                2, 4, 3,
                2, 5, 1,
                3, 4, 7,
                3, 6, 11,
                3, 7, 5,
                4, 5, 3,
                5, 7, 6,
                7, 6, 9,
                6, 8, 2,
                7, 8, 11)
                .bellmanFord()
                .compareBF(0, 2, 5, 7, 6, 8);
    }

    /*
     * Implementation starts here
     * If you want to add more test cases, you don't need to modify the contents below
     */

    static final String baseFolder = "AWC251A3T"; //just to make sure I don't delete anything important
    static ArrayList<String> errorLog = new ArrayList<>();
    final String fileName;
    final int source, dest;
    int[] bellmanFordPath;
    String bellmanFordException;
    WGraph graph;

    /**
     * Create graph file with given inputs
     *
     * @param fileName relative to local root
     * @param data     int values for graph
     */
    Comp251A3Tester(String fileName, int... data) {
        this.fileName = fileName;
        graph = createGraph(data);
        print("\nTest - %s", fileName);
        try {
            Field gSource = WGraph.class.getDeclaredField("source");
            gSource.setAccessible(true);
            source = (int) gSource.get(graph);
            Field gDest = WGraph.class.getDeclaredField("destination");
            gDest.setAccessible(true);
            dest = (int) gDest.get(graph);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract graph data");
        }
    }

    Comp251A3Tester printGraph() {
        print(graph.toString());
        return this;
    }

    Comp251A3Tester fordFulkerson() {
        FordFulkerson.fordfulkerson(source, dest, graph, baseFolder + "/" + fileName);
        return this;
    }

    Comp251A3Tester bellmanFord() {
        return bellmanFord(dest);
    }

    Comp251A3Tester bellmanFord(int dest) {
        try {
            bellmanFordPath = new BellmanFord(graph, source).shortestPath(dest);
            print("BF shortest path: %s", Arrays.toString(bellmanFordPath));
        } catch (Exception e) {
            bellmanFordException = e.getMessage();
        }
        return this;
    }

    Comp251A3Tester assertBFException(String message) {
        if (bellmanFordException == null)
            error("BF should throw exception: %s", message);
        return this;
    }

    Comp251A3Tester compareBF(int... data) {
        if (data == null || data.length == 0) return this; //data not set
        if (bellmanFordPath == null)
            error("BellmanFord not called");
        else if (!Arrays.equals(bellmanFordPath, data))
            error("BF mismatch: found %s, should be %s", Arrays.toString(bellmanFordPath), Arrays.toString(data));
        return this;
    }

    Comp251A3Tester compareFF(int maxFlow, int... data) {
        return compareFF("Failed", maxFlow, data);
    }

    Comp251A3Tester compareFF(String errorMessage, int maxFlow, int... data) {
        FGraph generated = new FGraph(baseFolder + "/" + getGeneratedFile());
        FGraph graph = new FGraph(maxFlow, data);
        if (!doGraphsMatch(generated, graph)) {
            error(errorMessage);
            print("\nFailed");
            print("\nAnswer:\n%s", graph);
        } else print("\nSuccess");
        return this;
    }

    /**
     * Rather than comparing the full graph, we only compare the max flow and ensure that
     * the total flow in = total flow out = max flow
     *
     * @param maxFlow val
     * @return this
     */
    Comp251A3Tester compareFFloosely(int maxFlow) {
        FGraph generated = new FGraph(baseFolder + "/" + getGeneratedFile());
        if (generated.maxFlow != maxFlow) {
            error("FF Incorrect maxflow: %d, should be %d", generated.maxFlow, maxFlow);
            return this; //stop checking further
        }
        WGraph graph = generated.graph;
        int sFlow = 0, dFlow = 0;
        for (Edge e : graph.getEdges()) {
            if (e.nodes[0] == source) sFlow += e.weight;
            if (e.nodes[1] == dest) dFlow += e.weight;
        }

        if (sFlow != dFlow || sFlow != maxFlow) {
            error("FF Mismatched flow: flow in %d, flow out %d, max flor %d", sFlow, dFlow, maxFlow);
        }
        return this;
    }

    boolean doGraphsMatch(FGraph g1, FGraph g2) {
        return g1.toString().trim().equals(g2.toString().trim());
    }

    String getGeneratedFile() {
        File dir = new File(baseFolder);
        for (File file : dir.listFiles())
            if (file.getName().startsWith(fileName))
                return file.getName();
        return null;
    }

    WGraph createGraph(int... data) {
        if (data.length < 6 || data.length % 3 != 0)
            throw new RuntimeException("Bad graph data; check that it matches the format");
        WGraph graph = new WGraph();
        try {
            Field gSource = WGraph.class.getDeclaredField("source");
            gSource.setAccessible(true);
            gSource.set(graph, data[0]);
            Field gDest = WGraph.class.getDeclaredField("destination");
            gDest.setAccessible(true);
            gDest.set(graph, data[1]);
            Field gNumNodes = WGraph.class.getDeclaredField("nb_nodes");
            gNumNodes.setAccessible(true);
            gNumNodes.set(graph, 0);
            for (int i = 3; i < data.length - 2; i += 3)
                graph.addEdge(new Edge(data[i], data[i + 1], data[i + 2]));
            //sanity check
            if (graph.getNbNodes() != data[2])
                throw new RuntimeException("Graph data numNodes mismatch");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new Graph");
        }
        return graph;
    }

    //static helpers

    static void initTesters() {
        File dir = new File(baseFolder);
        if (dir.exists()) return;
        dir.mkdirs();
    }

    static void deleteTesters() {
        delete(new File(baseFolder));
    }

    static void printResults() {
        print("\n");
        if (errorLog.isEmpty())
            print("Success; All Tests Passed");
        else
            for (String s : errorLog)
                print(s);
    }

    private static void delete(File file) {
        if (file == null || file.listFiles() == null) return;
        for (File childFile : file.listFiles())
            if (childFile.isDirectory())
                delete(childFile);
            else
                childFile.delete();
        file.delete();
    }

    private void error(String error, Object... o) {
        String s = String.format(error, o);
        print(s);
        errorLog.add(String.format("%s - %s", fileName, s));
    }

    static void print(String s, Object... o) {
        System.out.println(String.format(s, o));
    }

    /**
     * Inner class wrapper for WGraph + maxFlow
     */
    class FGraph {
        int maxFlow;
        WGraph graph;

        int[] toIntArray(List<Integer> list) {
            int[] ret = new int[list.size()];
            int i = 0;
            for (Integer e : list)
                ret[i++] = e;
            return ret;
        }

        FGraph(int maxFlow, int... data) {
            this.maxFlow = maxFlow;
            graph = createGraph(data);
        }

        FGraph(String file) {
            try {
                Scanner f = new Scanner(new File(file));
                maxFlow = f.nextInt();
                List<Integer> data = new ArrayList<>();
                while (f.hasNextInt())
                    data.add(f.nextInt());
                graph = createGraph(toIntArray(data));
                f.close();
            } catch (FileNotFoundException e) {
                print("File not found!");
                System.exit(1);
            }
        }

        @Override
        public String toString() {
            return maxFlow + "\n" + graph.toString(); //match file output layout
        }

    }

}
