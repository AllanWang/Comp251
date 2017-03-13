import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Allan Wang on 12/03/2017.
 * <p>
 * Test Code for Comp 251 - A3 - 2017
 * The instructions are still marked as a draft, and some of my solutions are arbitrary
 * This may help identify some issues, but if an error is thrown, check to see if it's actually an error
 * Look at the commit history to see the last update time.
 * Each call may produce many answers
 */
public class Comp251A3DFSTester {

    private WGraph graph = new WGraph();
    private boolean hasSourceEdge = false, hasDestEdge = false;
    private ArrayList<Integer> dfs;
    private String name;
    private static ArrayList<String> errors = new ArrayList<>();

    public static void main(String[] args) {
        test1();
        test2();
        test3();
        bf1();
        printError();
    }

    static void bf1() {
        new Comp251A3DFSTester("BF1", 0, 8)
                .addEdges(0, 1, 10, 0, 2, 12, 1, 2, 9, 1, 3, 8, 2, 4, 3, 2, 5, 1, 3, 4, 7, 3, 6, 11, 3, 7, 5, 4, 5, 3, 5, 7, 6, 6, 7, 9, 6, 8, 2, 7, 8, 11)
                .verify()
//                .printGraph()
                .getDFS().checkDFS(0, 2, 4, 5, 7, 8);
        /*
         * Other valid paths
         * 0, 1, 2, 4, 5, 7, 8
         * 0, 2, 5, 7, 8
         * etc
         */
    }

    // basic test
    static void test1() {
        new Comp251A3DFSTester("Test 1", 0, 3)
                .addEdges(0, 1, 1, 1, 3, 1, 2, 0, 2)
                .verify()
//                .printGraph()
                .getDFS().checkDFS(0, 1, 3);
    }

    //test with cycle
    static void test2() {
        new Comp251A3DFSTester("Test 2", 3, 7)
                .addEdgesConstantWeight(3, 1, 3, 5, 1, 0, 1, 2, 5, 7, 7, 6, 3, 4, 4, 3, 4, 8, 5, 8, 8, 5)
//                .printGraph()
                .verify()
                .getDFS().checkDFS(3, 5, 7);
    }

    //test without a valid path (assuming directions matter)
    static void test3() {
        new Comp251A3DFSTester("Test 3", 1, 8)
                .addEdgesConstantWeight(0, 1, 1, 3, 1, 4, 0, 2, 2, 5, 2, 6, 4, 7, 8, 7, 9, 8)
                .verify()
                .getDFS().checkDFS("should not return a valid path");
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

    Comp251A3DFSTester(String name, int source, int destination) {
        this.name = name;
        print("\n%s", name);
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

    Comp251A3DFSTester addEdges(int... vals) {
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

    Comp251A3DFSTester addEdgesConstantWeight(int... vals) {
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

    Comp251A3DFSTester printGraph() {
        print(graph.toString());
        return this;
    }

    Comp251A3DFSTester getDFS() {
        dfs = FordFulkerson.pathDFS(graph.getSource(), graph.getDestination(), graph);
        print("DFS: %s", dfs.toString());
        return this;
    }

    Comp251A3DFSTester checkDFS(Integer... path) {
        return checkDFS("failed", path);
    }

    Comp251A3DFSTester checkDFS(String errorMessage, Integer... path) {
        if (dfs == null) {
            print("Run dfs before comparing results");
            errors.add(name + " - did not run getDFS()");
            return this;
        }
        boolean result = dfs.equals(new ArrayList<Integer>(Arrays.asList(path)));
        print(result ? "\tSuccess" : "\tMatch failed");
        if (!result) errors.add(name + " - " + errorMessage);
        return this;
    }

    static void print(String s, Object... o) {
        System.out.println(String.format(s, o));
    }

    Comp251A3DFSTester verify() {
        if (!hasSourceEdge) throw new RuntimeException("The source must be one of the nodes");
        if (!hasDestEdge) throw new RuntimeException("The destination must be one of the nodes");
        return this;
    }

}
