package controller;

import domain.*;
import domain.list.ListException;
import domain.list.SinglyLinkedList;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import util.Utility;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;


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

    @FXML
    private Pane paneMatrix, paneList, paneLinked;

    // instanciar los 3 grafos
    AdjacencyMatrixGraph   graphMatrix = new AdjacencyMatrixGraph(10);
    AdjacencyListGraph     graphList   = new AdjacencyListGraph(10);
    SinglyLinkedListGraph  graphLinked = new SinglyLinkedListGraph();

    //INSTANCIAR LISTAS DE ARISTAS
    List<Utility.Edge> aristasM;
    List<Utility.Edge> aristasL;
    List<Utility.Edge> aristasLi;
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

    }


    private Graph getGraph(){
        if (rbAdjacencyMatrix.isSelected())
            return graphMatrix;
        if (rbLinkedList.isSelected())
            return graphList;
        if (rbLinkedList.isSelected()) {
            return graphLinked;
        }
        return null;
    }

    @FXML
    public void randomize(javafx.event.ActionEvent actionEvent) throws GraphException, ListException {
        // 1. Limpiar grafos y listas de aristas
        graphMatrix.clear();
        graphLinked.clear();
        graphList.clear();

        aristasM.clear();
        aristasL.clear();
        aristasLi.clear();

        // 2. Generar 10 vértices únicos entre 0 y 99
        Set<Integer> uniqueNumbers = new HashSet<>();
        while (uniqueNumbers.size() < 10) {
            uniqueNumbers.add(util.Utility.random(0, 99));
        }
        Integer[] numeros = uniqueNumbers.toArray(new Integer[0]);

        // 3. Agregar vértices a los 3 grafos
        for (int num : numeros) {
            graphMatrix.addVertex(num);
            graphList.addVertex(num);
            graphLinked.addVertex(num);
        }

        // 4. Generar todos los pares únicos posibles (sin repetidos ni simétricos)
        edgePairs.clear();
        currentEdgeIndex = 0;

        for (int i = 0; i < numeros.length; i++) {
            for (int j = i + 1; j < numeros.length; j++) {
                edgePairs.add(new int[]{numeros[i], numeros[j]});
            }
        }

        // 5. Mezclar pares
        Collections.shuffle(edgePairs);

        int maxEdges = 10; // Puedes ajustar la cantidad de aristas generadas

        for (int i = 0; i < maxEdges && i < edgePairs.size(); i++) {
            int[] pair = edgePairs.get(i);
            int from = pair[0];
            int to = pair[1];
            int weight = util.Utility.random(10, 100);

            try {
                // Grafo no dirigido, agregamos ambas direcciones
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

                System.out.println("Arista entre " + from + " y " + to + " con peso " + weight);

            } catch (GraphException | ListException e) {
                e.printStackTrace();
            }
        }

        // 6. Lambdas para obtener vértices y aristas (igual que tu código original)
        Supplier<List<Object>> verticesM = () -> {
            List<Object> verts = new ArrayList<>();
            for (int i = 0; i < graphMatrix.counter; i++)
                verts.add(graphMatrix.vertexList[i].data);
            return verts;
        };

        Function<Object, List<EdgeWeight>> edgesM = v -> {
            List<EdgeWeight> out = new ArrayList<>();
            int i = -1;
            for (int k = 0; k < graphMatrix.counter; k++) {
                if (graphMatrix.vertexList[k] != null && graphMatrix.vertexList[k].data.equals(v)) {
                    i = k;
                    break;
                }
            }
            if (i == -1) return out;
            for (int j = 0; j < graphMatrix.counter; j++) {
                Object w = graphMatrix.adjacencyMatrix[i][j];
                if (w != null && !w.equals(0)) {
                    out.add(new EdgeWeight(graphMatrix.vertexList[j].data, w));
                }
            }
            return out;
        };

        Supplier<List<Object>> verticesL = () -> {
            try {
                return new ArrayList<>(graphList.getAllVertices());
            } catch (ListException e) {
                throw new RuntimeException(e);
            }
        };

        Function<Object, List<EdgeWeight>> edgesL = v -> {
            try {
                return graphList.getAdjList(v);
            } catch (GraphException | ListException e) {
                throw new RuntimeException(e);
            }
        };

        Supplier<List<Object>> verticesLi = () -> {
            List<Object> verts = new ArrayList<>();
            try {
                for (int k = 1; k <= graphLinked.vertexList.size(); k++) {
                    verts.add(((Vertex) graphLinked.vertexList.getNode(k).data).data);
                }
            } catch (ListException e) {
                throw new RuntimeException(e);
            }
            return verts;
        };

        Function<Object, List<EdgeWeight>> edgesLi = v -> {
            List<EdgeWeight> out = new ArrayList<>();
            try {
                for (int k = 1; k <= graphLinked.vertexList.size(); k++) {
                    Vertex vert = (Vertex) graphLinked.vertexList.getNode(k).data;
                    if (vert.data.equals(v)) {
                        for (int e = 1; e <= vert.edgesList.size(); e++)
                            out.add((EdgeWeight) vert.edgesList.getNode(e).data);
                        break;
                    }
                }
            } catch (ListException e) {
                throw new RuntimeException(e);
            }
            return out;
        };

        // DIBUJAR GRAFOS Y APLICAR MST
        List<Utility.Edge> mstEdges = new ArrayList<>();

        if (rbAdjacencyMatrix.isSelected()) {
            if (rbKruskal.isSelected()) {
                mstEdges = util.Utility.kruskal(aristasM, 10);
                util.FXUtility.drawGraph(graphMatrix, pane, verticesM, edgesM, mstEdges);
            } else if (rbPrim.isSelected()) {
                mstEdges = util.Utility.prim(graphMatrix);
                util.FXUtility.drawGraph(graphMatrix, pane, verticesM, edgesM, mstEdges);
            } else {
                util.FXUtility.drawGraph(graphMatrix, pane, verticesM, edgesM,mstEdges);
            }
        }

        if (rbAdjacencyList.isSelected()) {
            if (rbKruskal.isSelected()) {
                mstEdges = util.Utility.kruskal(aristasL, 10);
                util.FXUtility.drawGraph(graphList, pane, verticesL, edgesL, mstEdges);
            } else if (rbPrim.isSelected()) {
                mstEdges = util.Utility.prim(graphList);
                util.FXUtility.drawGraph(graphList, pane, verticesL, edgesL, mstEdges);
            } else {
                util.FXUtility.drawGraph(graphList, pane, verticesL, edgesL,mstEdges);
            }
        }

        if (rbLinkedList.isSelected()) {
            if (rbKruskal.isSelected()) {
                mstEdges = util.Utility.kruskal(aristasLi, 10);
                util.FXUtility.drawGraph(graphLinked, pane, verticesLi, edgesLi, mstEdges);
            } else if (rbPrim.isSelected()) {
                mstEdges = util.Utility.prim(graphLinked);
                util.FXUtility.drawGraph(graphLinked, pane, verticesLi, edgesLi, mstEdges);
            } else {
                util.FXUtility.drawGraph(graphLinked, pane, verticesLi, edgesLi,mstEdges);
            }
        }
    }

}

