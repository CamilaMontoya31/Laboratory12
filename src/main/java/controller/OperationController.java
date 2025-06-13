package controller;

import domain.*;
import domain.list.ListException;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import util.FXUtility;
import util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class OperationController {
    @FXML
    private Canvas canvas;
    @FXML
    private Label label;
    @FXML
    private Pane pane;
    @FXML
    private TextArea textArea;
    @FXML
    private RadioButton btList;
    @FXML
    private RadioButton btLinked;
    @FXML
    private RadioButton btMatrix;
    @FXML
    private ToggleGroup group;

    Graph graph;
    private ArrayList<String> unused;
    private ArrayList<String> used;
    private final int max = 18;
    private double scale = 1.0;
    private final double SCALE_DELTA = 1.1;
    private Alert alert;

    @FXML
    private void initialize() {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Directed Graph Operations -  Error");
        group.selectToggle(btList);
        initGraph();
        pane.setOnScroll(event -> {
            if (event.getDeltaY() == 0) return;
            if (event.getDeltaY() > 0)
                scale *= SCALE_DELTA;
            else
                scale /= SCALE_DELTA;
            scale = limit(scale, 0.1, 10);
            pane.setScaleX(scale);
            pane.setScaleY(scale);
            event.consume();
        });
    }

    // Limits the pane scale
    private double limit(double value, double min, double max) {
        if(value < min) return min;
        if(value > max) return max;
        return value;
    }

    @FXML
    public void handleRandomize() {
        try {
            initGraph();
            for (int i = 0; i < 10; i++) {
                String name = unused.get(Utility.random(unused.size()));
                graph.addVertex(name);
                used.add(name);
                unused.remove(name);
            }
            label.setText("Randomized graph generated.");
            refreshMatrixDisplay();
            drawGraph();
        } catch (GraphException | ListException e) {
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    public void handleClear() {
        initGraph();
        pane.getChildren().clear();
        label.setText("Canvas cleared.");
        textArea.clear();
    }

    @FXML
    public void handleAddVertex() {
        try {
            int size = !graph.isEmpty()? graph.size() : 0;
            if (size > max)
                label.setText("Graph contains too many vertices.");
            else {
                String name = unused.get(Utility.random(unused.size()));
                graph.addVertex(name);
                used.add(name);
                unused.remove(name);
                label.setText("Vertex: [" + name + "] added. Total vertices: " + ++size);
                refreshMatrixDisplay();
                drawGraph();
            }
        } catch (GraphException | ListException e) {
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    public void handleRemoveVertex() {
        if (graph.isEmpty())
            label.setText("No vertices to remove.");
        else try {
            String name = used.get(Utility.random(used.size()));
            graph.removeVertex(name);
            unused.add(name);
            used.remove(name);
            label.setText("Vertex: [" + name + "] removed. Total vertices: " +
                    (!graph.isEmpty()? graph.size() : 0));
            refreshMatrixDisplay();
            drawGraph();
        } catch (GraphException | ListException e) {
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    public void handleAddEdge() {
        if (graph.isEmpty())
            label.setText("No vertices to add edges.");
        else try {
            int size = graph.size();
            boolean added = false;
            String a = "", b = "";
            int weight = 0;
            for (int i = 1; i <= size && !added; i++) {
                for (int j = 1; j <= size && !added; j++) {
                    a = used.get(i - 1);
                    b = used.get(j - 1);
                    if (!graph.containsEdge(a, b)) {
                        weight = Utility.random(1000, 2000);
                        graph.addEdgeWeight(a, b, weight);
                        added = true;
                    }
                }
            }
            if (!added)
                label.setText("All posible edges have been added.");
            else {
                label.setText("Edge between vertex [" + a + "] and [" + b + "] added. Weight: " + weight);
                refreshMatrixDisplay();
                drawGraph();
            }
        } catch (GraphException | ListException e) {
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    public void handleRemoveEdge() {
        if (graph.isEmpty())
            label.setText("No vertices to remove edges.");
        else try {
            int size = graph.size();
            boolean removed = false;
            String a = "", b = "";
            for (int i = 1; i <= size && !removed; i++) {
                for (int j = 1; j <= size && !removed; j++) {
                    a = used.get(i - 1);
                    b = used.get(j - 1);
                    if (graph.containsEdge(a, b)) {
                        graph.removeEdge(a, b);
                        removed = true;
                    }
                }
            }
            if (!removed)
                label.setText("All edges have been removed.");
            else {
                label.setText("Edge between vertex [" + a + "] and [" + b + "] added.");
                refreshMatrixDisplay();
                drawGraph();
            }
        } catch (GraphException | ListException e) {
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }

    private void drawGraph(){
        if (graph.isEmpty())
            pane.getChildren().clear();
        else {
            Supplier<List<Object>> s = () -> {
                try {
                    return graph.getVertices();}
                catch (GraphException | ListException e) {
                    throw new RuntimeException(e);
                }
            };

            Function<Object, List<EdgeWeight>> f = v -> {
                try {
                    return graph.getAdjList(v);
                } catch (GraphException | ListException e) {
                    throw new RuntimeException(e);
                }
            };

            FXUtility.drawDirectedGraph(graph, pane, s, f);
        }
    }

    private void refreshMatrixDisplay() {
        textArea.setText(graph.toString());
    }

    private void initGraph(){
        used = new ArrayList<>();
        if (btList.isSelected()){
            unused = new ArrayList<>();
            for (char i = 'A'; i <= 'Z'; i++)
                unused.add(i + "");
            graph = new DirectedAdjacencyListGraph(max);
        }
        if (btMatrix.isSelected()){
            unused = new ArrayList<>();
            for (int i = 1; i <= 100; i++)
                unused.add(i + "");
            graph = new DirectedAdjacencyMatrixGraph(max);
        }
        if (btLinked.isSelected()){
            unused = new ArrayList<>(Arrays.asList(
                    "Platon", "Aristoteles", "DaVinci", "Galileo", "Newton",
                    "Darwin", "Tesla", "Freud", "Einstein", "Napoleon",
                    "Lincoln", "Bolivar", "Cleopatra", "Socrates", "Voltaire",
                    "Homer", "FridaKahlo", "MarieCurie", "Kepler", "Fermi",
                    "Lavoisier", "Plato", "Turing", "Hammurabi", "Bach",
                    "Mozart", "Beethoven", "Caesar"
            ));
            graph = new DirectedSinglyLinkedListGraph();
        }
        graph.clear();
    }

    @FXML
    public void linkedOnAction() {
        initGraph();
        drawGraph();
        refreshMatrixDisplay();
    }

    @FXML
    public void matrixOnAction() {
        initGraph();
        drawGraph();
        refreshMatrixDisplay();
    }

    @FXML
    public void listOnAction() {
        initGraph();
        drawGraph();
        refreshMatrixDisplay();
    }
}
