import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Allan Wang on 12/03/2017.
 * FordFulkerson Tester class
 *
 * Though there may exist more than one valid result,
 * I am basing the results on using listOfEdgesSorted
 *
 * The sorted children set defines which depth path we check first
 *
 * Copy this file in the same directory as the other classes
 * All imports are added, so as long as there are no further package names,
 * you should be able to run the main method directly
 */
public class Comp251A3Tester {

    public static void main(String[] args) {
        deleteTesters(); //clear files since WGraph constructor won't do it
        initTesters(); //create test folder
        //run series of tests
        ff2();
        printResults();
        //deleteTesters(); //to clear the test folder
    }

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
//                .printGraph()
                .fordFulkerson()
                .compare(23,
                        0, 5,
                        6,
                        0, 1, 12,
                        0, 2, 11,
                        1, 3, 12,
                        2, 1, 0,
                        2, 4, 11,
                        3, 2, 0,
                        3, 5, 19,
                        4, 3, 7,
                        4, 5, 4);
    }

    static final String baseFolder = "AWC251A3T"; //just to make sure I don't delete anything important
    static ArrayList<String> errorLog = new ArrayList<>();
    final String fileName;
    final int source, dest;
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

    Comp251A3Tester compare(int maxFlow, int... data) {
        return compare("Failed", maxFlow, data);
    }

    Comp251A3Tester compare(String errorMessage, int maxFlow, int... data) {
        FGraph generated = new FGraph(baseFolder + "/" + getGeneratedFile());
        FGraph graph = new FGraph(maxFlow, data);
        if (!doGraphsMatch(generated, graph)) {
            errorLog.add(String.format("%s - %s", fileName, errorMessage));
            print("\nFailed");
            print("\nAnswer:\n%s", graph);
        } else print("\nSuccess");
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
            print("Success");
        else
            for (String s : errorLog)
                print(s);
    }

    private static void delete(File file) {
        for (File childFile : file.listFiles())
            if (childFile.isDirectory())
                delete(childFile);
            else
                childFile.delete();
        file.delete();
    }

    static void print(String s, Object... o) {
        System.out.println(String.format(s, o));
    }

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
                System.out.println("File not found!");
                System.exit(1);
            }
        }

        @Override
        public String toString() {
            return maxFlow + "\n" + graph.toString();
        }

    }

}
