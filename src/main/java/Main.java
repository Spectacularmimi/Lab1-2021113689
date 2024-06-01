import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath = "src/main/java/test.txt";

        try {
            String processedText = TextProcessor.readAndProcessText(filePath);
            Graph graph = new Graph();
            graph.buildGraph(processedText);
            graph.showDirectedGraph("output.png");  // 保存为PNG文件
            System.out.println("Graph saved as output.png");


            System.out.println("Enter word1:");
            String word1 = scanner.nextLine().toLowerCase();
            System.out.println("Enter word2:");
            String word2 = scanner.nextLine().toLowerCase();

            List<String> bridgeWords = Util.stringToList(graph.queryBridgeWords(word1, word2));
            if (bridgeWords == null) {
                System.out.println("No " + word1 + " or " + word2 + " in the graph!");
            } else if (bridgeWords.isEmpty()) {
                System.out.println("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
            } else {
                System.out.println("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" is: " +
                        String.join(", ", bridgeWords));
                String outputName = word1 + "TO" + word2 + ".png";
                graph.highlightGraph(word1, word2, bridgeWords, outputName);
                System.out.println("Highlighted graph saved as " + outputName);
            }


            System.out.println("Enter a new line of text:");
            String newInputText = scanner.nextLine();

            String outputText = graph.generateNewText(newInputText);
            System.out.println("Output text with bridge words inserted:");
            System.out.println(outputText);


            System.out.println("Enter the first word:");
            String word3 = scanner.nextLine().toLowerCase();
            System.out.println("Enter the second word:");
            String word4 = scanner.nextLine().toLowerCase();

            String results = graph.calcShortestPath(word3, word4);
            System.out.println(results);



            String walkResult = graph.randomWalk();
            System.out.println("Random walk result: " + walkResult);
            try (FileWriter writer = new FileWriter("randomWalkOutput.txt")) {
                writer.write(walkResult);
                System.out.println("Walk result written to randomWalkOutput.txt");
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}