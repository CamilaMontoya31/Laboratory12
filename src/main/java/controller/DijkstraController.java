package controller;

import domain.*;
import domain.list.ListException;
import domain.list.SinglyLinkedList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import util.FXUtility;
import util.GraphUtil;
import util.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DijkstraController {
    @FXML
    private Pane pane;
    @FXML
    private RadioButton btList;
    @FXML
    private TableColumn<Result, String> vertex;
    @FXML
    private RadioButton btLinked;
    @FXML
    private RadioButton btMatrix;
    @FXML
    private TableColumn<Result, Integer> position;
    @FXML
    private TableView<Result> listVertex;
    @FXML
    private ToggleGroup group;
    @FXML
    private TableColumn<Result, String> path;

    Graph graph;
    private ArrayList<String> unused;
    private ArrayList<String> used;
    private final int max = 18;
    private double scale = 1.0;
    private final double SCALE_DELTA = 1.1;
    private Alert alert;

    @FXML
    public void initialize() {
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
        vertex.setCellValueFactory(new PropertyValueFactory<>("vertex"));
        path.setCellValueFactory(new PropertyValueFactory<>("path"));
        position.setCellValueFactory(new PropertyValueFactory<>("position"));
    }

    // Limits the pane scale
    private double limit(double value, double min, double max) {
        if(value < min) return min;
        if(value > max) return max;
        return value;
    }

    @FXML
    public void randomizeOnAction() {
        try {
            initGraph();
            addRandomVertex(10);
            GraphUtil.addRandomEdges(graph, 15);
            drawGraph();
            List<Object> vertices = graph.getVertices();
            ObservableList<Result> results = FXCollections.observableArrayList();
            for (int i = 0; i < vertices.size(); i++) {
                SinglyLinkedList path = GraphUtil.dijkstra(vertices.getFirst(), vertices.get(i), graph);
                StringBuilder result = new StringBuilder();
                for (int j = 1; j <= path.size(); j++) {
                    result.append(path.getNode(j).data);
                    if (j < path.size()) result.append(" -> ");
                }
                results.add(new Result(i, vertices.get(i).toString(), result.toString()));
            }
            path.setPrefWidth(300);
            this.listVertex.setItems(results);
        } catch (Exception e) {
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }

    private void addRandomVertex(int n) throws GraphException, ListException {
        for (int i = 0; i < n; i++) {
            String name = unused.get(Utility.random(unused.size()));
            graph.addVertex(name);
            used.add(name);
            unused.remove(name);
        }
    }

    @FXML
    public void linkedOnAction() {
        initGraph();
        drawGraph();
    }

    @FXML
    public void matrixOnAction() {
        initGraph();
        drawGraph();
    }

    @FXML
    public void listOnAction() {
        initGraph();
        drawGraph();
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

    private void initGraph(){
        listVertex.getItems().clear();
        used = new ArrayList<>();
        unused = new ArrayList<>();
        path.setPrefWidth(174);
        for (int i = 0; i < 99; i++)
            unused.add(""+i);
        if (btList.isSelected())
            graph = new DirectedAdjacencyListGraph(max);
        if (btMatrix.isSelected())
            graph = new DirectedAdjacencyMatrixGraph(max);
        if (btLinked.isSelected())
            graph = new DirectedSinglyLinkedListGraph();
        graph.clear();
    }

    public class Result {
        private int position;
        private String vertex;
        private String path;
        public Result(int position, String vertex, String path) {
            this.position = position;
            this.vertex = vertex;
            this.path = path;
        }
        public int getPosition() {
            return position;
        }
        public void setPosition(int position) {
            this.position = position;
        }
        public String getVertex() {
            return vertex;
        }
        public void setVertex(String vertex) {
            this.vertex = vertex;
        }
        public String getPath() {
            return path;
        }
        public void setPath(String path) {
            this.path = path;
        }
    }
}
