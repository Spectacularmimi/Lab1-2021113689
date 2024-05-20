import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static guru.nidi.graphviz.model.Factory.*;

public class Graph {
    private MutableGraph graph = mutGraph("example").setDirected(true);
    private Map<String, Map<String, Integer>> adjList = new HashMap<>();

    public void addEdge(String src, String dest) {
        adjList.putIfAbsent(src, new HashMap<>());
        adjList.putIfAbsent(dest, new HashMap<>());
        Map<String, Integer> neighbors = adjList.get(src);
        neighbors.put(dest, neighbors.getOrDefault(dest, 0) + 1);

        MutableNode from = mutNode(src);
        MutableNode to = mutNode(dest);
        from.addLink(to(to).with("label", String.valueOf(neighbors.get(dest))));
        graph.add(from);
    }

    public void buildGraph(String text) {
        String[] words = text.split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            addEdge(words[i], words[i + 1]);
        }
    }

    public String queryBridgeWords(String word1, String word2) {
        if (!adjList.containsKey(word1) || !adjList.containsKey(word2)) {
            return null;
        }

        List<String> bridges = new ArrayList<>();
        Map<String, Integer> potentialBridges = adjList.get(word1);
        for (String mid : potentialBridges.keySet()) {
            if (adjList.containsKey(mid) && adjList.get(mid).containsKey(word2)) {
                bridges.add(mid);
            }
        }
        return Util.listToString(bridges);
    }

    public void highlightGraph(String word1, String word2, List<String> bridges, String filename) throws IOException {
        MutableGraph highlightGraph = graph.copy();

        for (MutableNode node : highlightGraph.nodes()) {
            if (node.name().toString().equals(word1) || node.name().toString().equals(word2)) {
                node.add(Color.rgb("d8fc7c")).add(Style.FILLED);
            }
            if (bridges != null && bridges.contains(node.name().toString())) {
                node.add(Color.rgb("74d4fc")).add(Style.FILLED);
            }
        }
        Graphviz.fromGraph(highlightGraph).width(800).render(Format.PNG).toFile(new File(filename));
    }

    public void showDirectedGraph(String G) throws IOException {
        Graphviz.fromGraph(graph).width(800).render(Format.PNG).toFile(new File(G));
    }

    public String generateNewText(String inputText) {
        String[] words = inputText.split("\\s+");
        StringBuilder newText = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i].toLowerCase();
            String word2 = words[i + 1].toLowerCase();
            newText.append(word1).append(" ");

            List<String> bridges = Util.stringToList(queryBridgeWords(word1, word2));
            if (bridges != null && !bridges.isEmpty()) {
                // 随机选择一个桥接词插入
                int index = rand.nextInt(bridges.size());
                newText.append(bridges.get(index)).append(" ");
            }
        }

        newText.append(words[words.length - 1]); // 添加最后一个词
        return newText.toString();
    }


    public String calcShortestPath(String word1, String word2) throws IOException {
        if (!adjList.containsKey(word1)) {
            return "Source word '" + word1 + "' does not exist in the graph!";
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, List<List<String>>> paths = new HashMap<>();
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());

        // 初始化距离和路径
        for (String node : adjList.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
            paths.put(node, new ArrayList<>());
        }
        distances.put(word1, Integer.MAX_VALUE);
        paths.get(word1).add(Collections.singletonList(word1));
        pq.offer(new AbstractMap.SimpleEntry<>(word1, 0));

        while (!pq.isEmpty()) {
            Map.Entry<String, Integer> current = pq.poll();
            String currentNode = current.getKey();
            int currentDistance = current.getValue();

            if (currentDistance > distances.get(currentNode)) {
                continue;
            }

            for (Map.Entry<String, Integer> neighbor : adjList.get(currentNode).entrySet()) {
                String nextNode = neighbor.getKey();
                int weight = neighbor.getValue();
                int newDistance = currentDistance + weight;

                if (newDistance < distances.get(nextNode)) {
                    distances.put(nextNode, newDistance);
                    paths.get(nextNode).clear();
                    paths.get(nextNode).addAll(extendPaths(paths.get(currentNode), nextNode));
                    pq.offer(new AbstractMap.SimpleEntry<>(nextNode, newDistance));
                } else if (newDistance == distances.get(nextNode)) {
                    paths.get(nextNode).addAll(extendPaths(paths.get(currentNode), nextNode));
                }
            }
        }

        return formatPaths(paths, word1, word2);
    }


    private List<List<String>> extendPaths(List<List<String>> currentPaths, String nextNode) {
        List<List<String>> newPaths = new ArrayList<>();
        for (List<String> path : currentPaths) {
            List<String> newPath = new ArrayList<>(path);
            newPath.add(nextNode);
            newPaths.add(newPath);
        }
        return newPaths;
    }

    private String formatPaths(Map<String, List<List<String>>> paths, String word1, String word2) throws IOException {
        StringBuilder result = new StringBuilder();
        if (word2.isEmpty()) {
            paths.forEach((key, value) -> {
                List<String> temp = value.get(value.size() - 1);
                if (!value.isEmpty()) {
                    result.append("Shortest paths from '").append(word1).append("' to '")
                            .append(temp.get(temp.size() - 1)).append("':\n");
                    value.forEach(path -> result.append(String.join(" -> ", path)).append("\n"));
                    result.append("\n");
                }
            });
        } else {
            // Output paths from word1 to word2
            List<List<String>> targetPaths = paths.getOrDefault(word2, new ArrayList<>());
            if (targetPaths.isEmpty()) {
                return "No path or unreachable from '" + word2 + "'!";
            }
            result.append("Shortest paths from '").append(word1).append("' to '").append(word2).append("':\n");
            targetPaths.forEach(path -> result.append(String.join(" -> ", path)).append("\n"));
            List<List<String>> shortestPaths = paths.get(word2);
            highlightPaths(shortestPaths, "highlightedPaths.png", shortestPaths.get(0).size() - 1);
            System.out.println("Highlighted graph saved as highlightedPaths.png");
        }
        return result.toString().trim();
    }


    public void highlightPaths(List<List<String>> paths, String filename, int totalDistance) throws IOException {
        MutableGraph highlightGraph = graph.copy();
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.VIOLET}; // 颜色数组
        int colorIndex = 0;

        // Highlight all paths
        for (List<String> path : paths) {
            Color pathColor = colors[colorIndex % colors.length];
            colorIndex++;

            for (int i = 0; i < path.size() - 1; i++) {
                String src = path.get(i);
                String dest = path.get(i + 1);

                // Finding the edge and change color to a unique path color
                highlightGraph.nodes().stream()
                        .filter(n -> n.name().toString().equals(src))
                        .flatMap(n -> n.links().stream())
                        .filter(l -> l.to().name().toString().equals(dest))
                        .forEach(l -> {
                            l.add(pathColor);
                            l.add(Style.BOLD);
                        });
            }
        }

        // Highlight only start and end nodes
        String startNode = paths.get(0).get(0);
        String endNode = paths.get(0).get(paths.get(0).size() - 1);
        highlightGraph.nodes().stream()
                .filter(n -> n.name().toString().equals(startNode) || n.name().toString().equals(endNode))
                .forEach(n -> n.add(Color.rgb("d8fc7c")).add(Style.FILLED));

        // Add total distance label
        highlightGraph.graphAttrs().add(Label.of("Total Distance: " + totalDistance));

        Graphviz.fromGraph(highlightGraph).width(800).render(Format.PNG).toFile(new File(filename));
    }

    public String randomWalk() {
        Random rand = new Random();
        List<String> nodes = new ArrayList<>(adjList.keySet());
        if (nodes.isEmpty()) {
            return "The graph is empty.";
        }

        Scanner scanner = new Scanner(System.in);
        String current = nodes.get(rand.nextInt(nodes.size()));
        Set<String> visitedEdges = new HashSet<>();
        StringBuilder walkResult = new StringBuilder(current);

        while (true) {
            if (!adjList.containsKey(current) || adjList.get(current).isEmpty()) {
                walkResult.append(" (No outgoing edges from ").append(current).append(")");
                break;
            }

            List<String> possibleNextNodes = new ArrayList<>(adjList.get(current).keySet());
            String next = possibleNextNodes.get(rand.nextInt(possibleNextNodes.size()));
            String edge = current + " -> " + next;
            walkResult.append(" -> ").append(next);
            if (visitedEdges.contains(edge)) {
                walkResult.append(" (Repeated edge: ").append(edge).append(")");
                break;
            }

            visitedEdges.add(edge);
            current = next;

            // Ask the user whether to continue
            System.out.println("Current walk: " + walkResult.toString());
            System.out.println("Press 'c' and enter to continue, any other key and enter to stop:");
            String input = scanner.nextLine();
            if (!input.equalsIgnoreCase("c")) {
                walkResult.append(" (Stopped by user)");
                break;
            }
        }
        scanner.close();
        return walkResult.toString();
    }
}
