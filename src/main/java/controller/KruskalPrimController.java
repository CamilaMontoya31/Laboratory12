package controller;

import domain.*;
import domain.list.ListException;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import util.Utility;

import java.net.Inet4Address;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static util.FXUtility.drawGraph;


public class KruskalPrimController {

    @FXML
    private Pane pane;

    @FXML
    private RadioButton rbAdjacencyList;

    @FXML
    private RadioButton rbAdjacencyMatrix;

    @FXML
    private RadioButton rbKruskal;

    @FXML
    private RadioButton rbLinkedList;

    @FXML
    private RadioButton rbPrim;

    private List<int[]> edgePairs = new ArrayList<>();
    private int currentEdgeIndex = 0;

    // instanciar los 3 grafos
    AdjacencyMatrixGraph graphMatrix = new AdjacencyMatrixGraph(10);
    AdjacencyListGraph graphList = new AdjacencyListGraph(10);
    SinglyLinkedListGraph graphLinked = new SinglyLinkedListGraph();

    //INSTANCIAR LISTAS DE ARISTAS
    List<Utility.Edge> aristasM;
    List<Utility.Edge> aristasL;
    List<Utility.Edge> aristasLi;
//para que no se repitan vertices
    Map<Integer, Integer> idToIndex;
    List<Integer> vertexIDs;
    @FXML
    private void initialize() {
        ToggleGroup group1 = new ToggleGroup();
        rbLinkedList.setToggleGroup(group1);
        rbAdjacencyMatrix.setToggleGroup(group1);
        rbAdjacencyList.setToggleGroup(group1);

        ToggleGroup group2 = new ToggleGroup();
        rbPrim.setToggleGroup(group2);
        rbKruskal.setToggleGroup(group2);

        aristasM = new ArrayList<Utility.Edge>();
        aristasL = new ArrayList<Utility.Edge>();
        aristasLi = new ArrayList<Utility.Edge>();

        // Seleccionar valores por defecto
        rbAdjacencyMatrix.setSelected(true);
        rbKruskal.setSelected(true);

        idToIndex = new HashMap<>();
        vertexIDs = new ArrayList<>();
    }

    private Graph getGraph() {
        if (rbAdjacencyMatrix.isSelected())
            return graphMatrix;
        if (rbAdjacencyList.isSelected())
            return graphList;
        if (rbLinkedList.isSelected())
            return graphLinked;
        return null;
    }

        @FXML
        public void randomize(javafx.event.ActionEvent actionEvent) throws GraphException {
            try {
                clearAll();

                // 1. Generar 10 vértices únicos
                List<Integer> vertices = generateUniqueVertices(10, 0, 99);
                printVertexMapping();

                // 2. Construir árbol de expansión mínima inicial
                buildSpanningTree(vertices);

                // 3. Añadir aristas adicionales
                addRandomEdges(vertices, 10);

                // 4. Verificar conectividad
               // verifyConnectivity();

                // 5. Dibujar el grafo con MST
                drawGraph();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error: " + e.getMessage());
            }
        }

    private void clearAll() throws GraphException, ListException {
        pane.getChildren().clear();
        graphMatrix.clear();
        graphList.clear();
        graphLinked.clear();
        aristasM.clear();
        aristasL.clear();
        aristasLi.clear();
    }

    private List<Integer> generateUniqueVertices(int count, int min, int max) throws GraphException {
        List<Integer> vertices = new ArrayList<>();
        while (vertices.size() < count) {
            int rnd = util.Utility.random(min, max);
            if (!vertices.contains(rnd)) {
                vertices.add(rnd);
                graphMatrix.addVertex(rnd);
                graphList.addVertex(rnd);
                graphLinked.addVertex(rnd);
            }
        }
        return vertices;
    }

    private void buildSpanningTree(List<Integer> vertices) throws GraphException, ListException {
        List<Integer> connected = new ArrayList<>();
        List<Integer> unconnected = new ArrayList<>(vertices);

        // Empezar con un vértice aleatorio
        int first = unconnected.remove(util.Utility.random(0, unconnected.size()-1));
        connected.add(first);

        // Conectar todos los vértices
        while (!unconnected.isEmpty()) {
            int newVertex = unconnected.remove(util.Utility.random(0, unconnected.size()-1));
            int existingVertex = connected.get(util.Utility.random(0, connected.size()-1));
            int weight = util.Utility.random(10, 100);

            addEdgeToAllGraphs(existingVertex, newVertex, weight);
            connected.add(newVertex);
        }
    }

    private void addRandomEdges(List<Integer> vertices, int maxEdges) throws GraphException, ListException {
        List<int[]> possibleEdges = new ArrayList<>();

        // Generar todas las posibles aristas que no existen
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i+1; j < vertices.size(); j++) {
                int from = vertices.get(i);
                int to = vertices.get(j);
                if (!graphMatrix.edgeExists(from, to)) {
                    possibleEdges.add(new int[]{from, to});
                }
            }
        }

        // Barajar y agregar algunas
        Collections.shuffle(possibleEdges);
        int edgesToAdd = Math.min(maxEdges, possibleEdges.size());

        for (int i = 0; i < edgesToAdd; i++) {
            int[] edge = possibleEdges.get(i);
            int weight = util.Utility.random(10, 100);
            addEdgeToAllGraphs(edge[0], edge[1], weight);
        }
    }

    private void addEdgeToAllGraphs(int from, int to, int weight) throws GraphException, ListException {
        // Agregar en ambos sentidos (grafo no dirigido)
        graphMatrix.addEdgeWeight(from, to, weight);
        graphMatrix.addEdgeWeight(to, from, weight);
        aristasM.add(new Utility.Edge(from, to, weight));
        aristasM.add(new Utility.Edge(to, from, weight));

        graphList.addEdgeWeight(from, to, weight);
        graphList.addEdgeWeight(to, from, weight);
        aristasL.add(new Utility.Edge(from, to, weight));
        aristasL.add(new Utility.Edge(to, from, weight));

        graphLinked.addEdgeWeight(from, to, weight);
        graphLinked.addEdgeWeight(to, from, weight);
        aristasLi.add(new Utility.Edge(from, to, weight));
        aristasLi.add(new Utility.Edge(to, from, weight));
    }

    private void verifyConnectivity() throws GraphException, ListException {
        // Verificar en todos los grafos
        if (!isGraphConnected(graphMatrix)) {
            throw new IllegalStateException("Matriz de adyacencia no es conexa");
        }
        if (!isGraphConnected(graphList)) {
            throw new IllegalStateException("Lista de adyacencia no es conexa");
        }
        if (!isGraphConnected(graphLinked)) {
            throw new IllegalStateException("Lista enlazada no es conexa");
        }
        System.out.println("Todos los grafos son conexos");
    }

    private boolean isGraphConnected(Graph graph) throws GraphException, ListException {
        if (graph.size() == 0) return true;

        List<Object> allVertices = graph.getVertices();
        if (allVertices.isEmpty()) return false;

        Set<Object> visited = new HashSet<>();
        Queue<Object> queue = new LinkedList<>();

        Object startVertex = allVertices.get(0);
        queue.add(startVertex);
        visited.add(startVertex);

        while (!queue.isEmpty()) {
            Object currentVertex = queue.poll();
            List<Integer> neighbors = graph.getNeighbors(currentVertex);

            for (Object neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited.size() == allVertices.size();
    }
    private List<Integer> getSafeNeighbors(Graph graph, Object vertex) {
        try {
            return graph.getNeighbors(vertex);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error de índice al obtener vecinos: " + e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error al obtener vecinos: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    private void drawGraph() {
        try {
            pane.getChildren().clear();
            List<Utility.Edge> mstEdges = new ArrayList<>();
            Graph currentGraph = getSelectedGraph();

            if (currentGraph == null) {
                System.out.println("No se ha seleccionado ningún tipo de grafo");
                return;
            }

            // Ejecutar algoritmo MST seleccionado
            if (rbKruskal.isSelected()) {
                if (rbAdjacencyMatrix.isSelected()) {
                    mstEdges = util.Utility.kruskal(aristasM, 100);
                } else if (rbAdjacencyList.isSelected()) {
                    mstEdges = util.Utility.kruskal(aristasL, 100);
                } else if (rbLinkedList.isSelected()) {
                    mstEdges = util.Utility.kruskal(aristasLi, 100);
                }
            } else if (rbPrim.isSelected()) {
                mstEdges = util.Utility.prim(currentGraph);
            }

            // Dibujar el grafo
            if (rbAdjacencyMatrix.isSelected()) {
                util.FXUtility.drawGraph(
                        graphMatrix,
                        pane,
                        () -> {
                            try {
                                return graphMatrix.getVertices();
                            } catch (GraphException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        v -> {
                            try {
                                return graphMatrix.getAdjList(v);
                            } catch (GraphException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
            } else if (rbAdjacencyList.isSelected()) {
                util.FXUtility.drawGraph(
                        graphList,
                        pane,
                        () -> {
                            try {
                                return graphList.getVertices();
                            } catch (GraphException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        v -> {
                            try {
                                return graphList.getAdjList(v);
                            } catch (GraphException | ListException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
            } else if (rbLinkedList.isSelected()) {
                util.FXUtility.drawGraph(
                        graphLinked,
                        pane,
                        () -> {
                            try {
                                List<Object> vertices = new ArrayList<>();
                                for (int i = 1; i <= graphLinked.vertexList.size(); i++) {
                                    vertices.add(((Vertex)graphLinked.vertexList.getNode(i).data).data);
                                }
                                return vertices;
                            } catch (ListException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        v -> {
                            try {
                                List<EdgeWeight> edges = new ArrayList<>();
                                for (int i = 1; i <= graphLinked.vertexList.size(); i++) {
                                    Vertex vertex = (Vertex) graphLinked.vertexList.getNode(i).data;
                                    if (vertex.data.equals(v)) {
                                        for (int j = 1; j <= vertex.edgesList.size(); j++) {
                                            edges.add((EdgeWeight) vertex.edgesList.getNode(j).data);
                                        }
                                        break;
                                    }
                                }
                                return edges;
                            } catch (ListException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al dibujar: " + e.getMessage());
        }
    }

    private Graph getSelectedGraph() {
        if (rbAdjacencyMatrix.isSelected()) return graphMatrix;
        if (rbAdjacencyList.isSelected()) return graphList;
        if (rbLinkedList.isSelected()) return graphLinked;
        return null;
    }
    public void printVertexMapping() {
        System.out.println("Mapeo de Vértices (Valor -> Índice):");
        for (int i = 0; i < graphMatrix.counter; i++) {
            System.out.println(graphMatrix.vertexList[i].data + " -> " + i);
        }
    }
}





