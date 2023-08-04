package diver;

import datastructures.PQueue;
import datastructures.SlowPQueue;
import game.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * This is the place for your implementation of the {@code SewerDiver}.
 */
public class McDiver implements SewerDiver {


    /**
     * See {@code SewerDriver} for specification.
     */
    @Override
    public void seek(SeekState state) {
        // TODO : Look for the ring and return.
        Set<Long> set = new HashSet<>();
        seekhelper(state, set);

        // DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
        // Instead, write your method (it may be recursive) elsewhere, with a
        // good specification, and call it from this one.
        //
        // Working this way provides you with flexibility. For example, write
        // one basic method, which always works. Then, make a method that is a
        // copy of the first one and try to optimize in that second one.
        // If you don't succeed, you can always use the first one.
        //
        // Use this same process on the second method, scram.
    }

    /**
     * Helper function for seek. Finds the path to the ring
     * @param state
     * @param visited
     */
    private void seekhelper(SeekState state, Set<Long> visited) {
        long location = state.currentLocation();
        visited.add(state.currentLocation());
        if (state.distanceToRing() == 0) {
            return;
        }
        ArrayList<NodeStatus> neighbors = (ArrayList<NodeStatus>) state.neighbors();
        Collections.sort(neighbors);
        for (NodeStatus node : neighbors) {
            if (visited.contains(node.getId())) {
                continue;
            }
            state.moveTo(node.getId());
            seekhelper(state, visited);

            if (state.distanceToRing() == 0) {
                return;
            }
            if (state.distanceToRing() != 0) {
                state.moveTo(location);
            }
        }


    }

    /**
     * See {@code SewerDriver} for specification.
     */
    @Override
    public void scram(ScramState state) {
        // TODO: Get out of the sewer system before the steps are used up.
        scramhelper(state);
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.
    }

    /**
     * Main helper function for the scram function. Finds the best path to acquire the highest
     * potential score
     * @param state
     */
    private void scramhelper(ScramState state) {
        LinkedList<Node> coinvalue = new LinkedList<>();
        Map<Node, Edge> bestEdges = djohn(state.currentNode(), coinvalue);
        LinkedList<Edge> path = new LinkedList<>();
        LinkedList<Edge> interpath = new LinkedList<>();
        LinkedList<Edge> bestpath = new LinkedList<>();
        int beststeps = 0;
        int steps = 0;
        Node n = state.currentNode();
        for (int i = 0; i < coinvalue.size() - 1; i++) {
            Node joe = n;
            Map<Node, Edge> x = djohn(joe, new LinkedList<>());
            Collections.sort(coinvalue, Comparator.comparingInt(o ->
                    (int) (-o.getTile().originalCoinValue() * 0.05 + buildpath(o, joe, x,
                            new LinkedList<>()))));
            int steps1 = buildpath(coinvalue.get(0), n, djohn(n, new LinkedList<>()), path);
            int steps2 = buildpath(state.exit(), coinvalue.get(0),
                    djohn(coinvalue.get(0), new LinkedList<>()), interpath);
            steps += steps1 + steps2;
            if (steps + 1 > state.stepsToGo()) {
                path.clear();
                interpath.clear();
                path.addAll(bestpath);
                steps = beststeps;
                coinvalue.removeFirst();
                continue;
            } else {
                interpath.clear();
                steps -= steps2;
                beststeps = steps;
                bestpath.clear();
                bestpath.addAll(path);
            }
            n = coinvalue.get(0);
            coinvalue.removeFirst();

        }
        steps += buildpath(state.exit(), n, djohn(n, new LinkedList<>()), path);
        if (steps + 1 > state.stepsToGo()) {
            buildpath(state.exit(), state.currentNode(), bestEdges, path);
        }

        LinkedList<Node> movepath = new LinkedList<>();
        for (int i = path.size() - 1; i >= 0; i--) {
            movepath.add(0, path.get(i).destination());
        }

        for (Node j : movepath) {
            state.moveTo(j);
        }
    }

    /**
     * Helper function for scramhelper. Builds the path for the miner to take.
     * @param destination
     * @param source
     * @param bestEdges
     * @param path
     * @return Returns the number of steps used in the created path
     */
    private int buildpath(Node destination, Node source, Map<Node, Edge> bestEdges,
            LinkedList<Edge> path) {
        LinkedList<Edge> tmp = new LinkedList<>();
        int steps = 0;
        Node v = destination;
        while (true) {
            Edge e = bestEdges.get(v);
            if (e == null) {
                return -1;
            }
            steps += e.length();
            tmp.addFirst(e);
            if (e.source() == source) {
                break;
            }
            v = e.source();

        }
        path.addAll(tmp);
        return steps;
    }

    /**
     * Helper function for scramhelper. Algorithm to find the best path.
     * @param state
     * @param coinvalue
     */
    private Map<Node, Edge> djohn(Node state, LinkedList<Node> coinvalue) {
        PQueue<Node> frontier = new SlowPQueue<>();
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Edge> bestEdges = new HashMap<>();
        distances.put(state, 0.0);
        frontier.add(state, 0);
        while (!frontier.isEmpty()) {
            Node g = frontier.extractMin();

            for (Node r : g.getNeighbors()) {
                Edge john = g.getEdge(r);
                double d = john.length();
                Node v = r;
                if (distances.get(v) == null) {
                    distances.put(v, distances.get(g) + d);
                    frontier.add(v, d);
                    bestEdges.put(v, john);
                } else {
                    if (distances.get(g) + d < distances.get(v)) {
                        distances.put(v, distances.get(g) + d);
                        try {
                            frontier.add(v, d);
                            bestEdges.put(v, john);
                        } catch (IllegalArgumentException E) {
                            frontier.changePriority(v, d);
                            bestEdges.put(v, john);
                        }
                    }

                }
            }
        }
        for (Node key : distances.keySet()) {
            if (key.getTile().originalCoinValue() > 0) {
                coinvalue.add(key);

            }
        }
        Collections.sort(coinvalue, Comparator.comparingInt(o -> o.getTile().originalCoinValue()));
        Collections.reverse(coinvalue);
        return bestEdges;
    }
}


