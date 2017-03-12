import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Allan Wang on 12/03/2017.
 *
 * Test Code for Comp 251 - A3 - 2017
 * The instructions are still marked as a draft, and some of my solutions are arbitrary
 * This may help identify some issues, but if an error is thrown, check to see if it's actually an error
 * Look at the commit history to see the last update time.
 */
public class Comp251A3Tester {

    private WGraph graph = new WGraph();
    private boolean hasSourceEdge = false, hasDestEdge = false;
    private ArrayList<Integer> dfs;
    private static ArrayList<String> errors = new ArrayList<>();

    public static void main(String[] args) {
        test1();
        test2();
        test3();
        printError();
    }

    // basic test
    static void test1() {
        new Comp251A3Tester(1, 5)
                .addEdges(1, 2, 1, 2, 5, 1, 3, 1, 2)
                .verify()
//                .printGraph()
                .getDFS().checkDFS("Test 1 failed", 1, 2, 5);
    }

    //test with cycle
    static void test2() {
        new Comp251A3Tester(3, 7)
                .addEdgesConstantWeight(3, 1, 3, 5, 1, 0, 1, 2, 5, 7, 7, 6, 3, 9, 9, 3, 9, 8, 5, 8, 8, 5)
//                .printGraph()
                .verify()
                .getDFS().checkDFS("Test 2 failed", 3, 5, 7);
    }

    //test without a valid path (assuming directions matter)
    static void test3() {
        new Comp251A3Tester(1, 9)
                .addEdgesConstantWeight(1, 2, 2, 4, 2, 5, 1, 3, 3, 6, 3, 7, 5, 8, 9, 8, 10, 9)
                .verify()
                .getDFS().checkDFS("Test 3 should not return a valid path");
    }

    static void printError() {
        print("\n");
        if (errors.isEmpty()) {
            print("No Errors");
            return;
        }
        print("Errors");
        for (String s : errors) {
            print("\t%s", s);
        }
    }

    Comp251A3Tester(int source, int destination) {
        try {
            Field gSource = WGraph.class.getDeclaredField("source");
            gSource.setAccessible(true);
            gSource.set(graph, source);
            Field gDest = WGraph.class.getDeclaredField("destination");
            gDest.setAccessible(true);
            gDest.set(graph, destination);
        } catch (Exception e) {
            print("Make sure you are using the new WGraph, not the one in a2");
        }
    }

    Comp251A3Tester addEdges(int... vals) {
        if (vals.length % 3 != 0)
            print("addEdges requires the source, dest, and weight for each edge; size should be multiple of 3");
        for (int i = 0; i < vals.length / 3; i++) {
            int j = i * 3;
            graph.addEdge(new Edge(vals[j], vals[j + 1], vals[j + 2]));
            if (vals[j] == graph.getSource()) hasSourceEdge = true;
            if (vals[j + 1] == graph.getDestination()) hasDestEdge = true;
        }
        return this;
    }

    Comp251A3Tester addEdgesConstantWeight(int... vals) {
        if (vals.length % 2 != 0)
            print("addEdgesConstantWeight requires the source, and dest for each edge; size should be multiple of 2");
        for (int i = 0; i < vals.length / 2; i++) {
            int j = i * 2;
            graph.addEdge(new Edge(vals[j], vals[j + 1], 1));
            if (vals[j] == graph.getSource()) hasSourceEdge = true;
            if (vals[j + 1] == graph.getDestination()) hasDestEdge = true;
        }
        return this;
    }

    Comp251A3Tester printGraph() {
        print(graph.toString());
        return this;
    }

    Comp251A3Tester getDFS() {
        dfs = FordFulkerson.pathDFS(graph.getSource(), graph.getDestination(), graph);
        print("DFS: %s", dfs.toString());
        return this;
    }

    Comp251A3Tester checkDFS(String errorMessage, Integer... path) {
        if (!checkDFS(path)) errors.add(errorMessage);
        return this;
    }

    boolean checkDFS(Integer... path) {
        if (dfs == null) {
            print("Run dfs before comparing results");
            return false;
        }
        boolean result = dfs.equals(new ArrayList<Integer>(Arrays.asList(path)));
        print(result ? "\tSuccess" : "\tMatch failed");
        return result;
    }

    static void print(String s, Object... o) {
        System.out.println(String.format(s, o));
    }

    Comp251A3Tester verify() {
        if (!hasSourceEdge) throw new RuntimeException("The source must be one of the nodes");
        if (!hasDestEdge) throw new RuntimeException("The destination must be one of the nodes");
        return this;
    }

}
