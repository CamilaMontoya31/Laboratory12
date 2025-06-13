package controller;

import domain.*;
import domain.list.ListException;
import domain.list.SinglyLinkedList;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

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

    @FXML
    private void initialize() {
        ToggleGroup group1 = new ToggleGroup();
        rbLinkedList.setToggleGroup(group1);
        rbAdjacencyMatrix.setToggleGroup(group1);
        rbAdjacencyList.setToggleGroup(group1);

        ToggleGroup group2 = new ToggleGroup();
        rbPrim.setToggleGroup(group2);
        rbKruskal.setToggleGroup(group2);

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
        graphMatrix.clear(); // Limpiar el grafo actual
        graphLinked.clear();
        graphList.clear();
        
        int[] numeros = new int[10];
        int count = 0;

        for (; count < 10;) {
            int num = util.Utility.random(0,99); // entre 0 y 99
            boolean repetido = false;

            // Verificar si ya existe
            for (int i = 0; i < count; i++) {
                if (numeros[i] == num) {
                    repetido = true;
                    break;
                }
            }

            if (!repetido) {
                numeros[count] = num;
                count++;
            }
        }

        // colocar los numeros en los vertices del grafico opción 1, no es uno por una
        for (int i = 0; i < 10; i++) {
            graphMatrix.addVertex(numeros[i]);
            graphList.addVertex(numeros[i]);
            graphLinked.addVertex(numeros[i]);
        }
        /*
        //agregar aristas opción 1, no es una por una
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10 ; j++) {
                graph.addEdgeWeight(numeros[i],numeros[j],util.Utility.randomMinMax(1,50));
            }
        }*/
        //opción 2 generar pares aleatorios posibles (sin repetir ni simétricos)
        edgePairs.clear();
        currentEdgeIndex = 0;

        // Generar todos los pares únicos de vértices
        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                edgePairs.add(new int[]{numeros[i], numeros[j]});
            }
        }

        Collections.shuffle(edgePairs); // orden aleatorio

        int maxEdges = 10; // cantidad aleatoria de aristas
        for (int i = 0; i < maxEdges && i < edgePairs.size(); i++) {
            int[] pair = edgePairs.get(i);
            int from = pair[0];
            int to = pair[1];
            int weight = util.Utility.random(10, 100);

            try {
                // Agregamos ambas direcciones porque es no dirigido
                graphMatrix.addEdgeWeight(from, to, weight);
                graphMatrix.addEdgeWeight(to, from, weight);

                graphList.addEdgeWeight(from, to, weight);
                graphList.addEdgeWeight(to, from, weight);

                graphLinked.addEdgeWeight(from, to, weight);
                graphLinked.addEdgeWeight(to, from, weight);

                System.out.println("Arista entre " + from + " y " + to + " con peso " + weight);
            } catch (GraphException | ListException e) {
                e.printStackTrace();
            }
        }



        //para dibujar el grafo
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
            if (i == -1) return out; // no lo encontró
            for (int j = 0; j < graphMatrix.counter; j++) {
                Object w = graphMatrix.adjacencyMatrix[i][j];
                if (w != null && !w.equals(0)) {
                    out.add(new EdgeWeight(graphMatrix.vertexList[j].data, w));
                }
            }
            return out;
        };

        //Lambdas para AdjacencyListGraph
        Supplier<List<Object>> verticesL = () -> {
            // asumo que tus vértices están en graphList.vertexList
            try {
                return new ArrayList<>(graphList.getAllVertices());
            } catch (ListException e) {
                throw new RuntimeException(e);
            }
        };
        Function<Object, List<EdgeWeight>> edgesL = v -> {
            try {
                return graphList.getAdjList(v);
            } catch (GraphException e) {
                throw new RuntimeException(e);
            } catch (ListException e) {
                throw new RuntimeException(e);
            }
        };

        // Lambdas para SinglyLinkedListGraph
        Supplier<List<Object>> verticesLi = () -> {
            List<Object> verts = new ArrayList<>();

            try {
                for (int k = 1; k <= graphLinked.vertexList.size(); k++) {
                    verts.add(((Vertex)graphLinked.vertexList.getNode(k).data).data);
                }} catch (ListException e) {
                throw new RuntimeException(e);
            }

            return verts;
        };
        Function<Object, List<EdgeWeight>> edgesLi = v -> {
            List<EdgeWeight> out = new ArrayList<>();
            try{
                for (int k = 1; k <= graphLinked.vertexList.size(); k++) {
                    Vertex vert = (Vertex)graphLinked.vertexList.getNode(k).data;
                    if (vert.data.equals(v)) {
                        for (int e = 1; e <= vert.edgesList.size(); e++)
                            out.add((EdgeWeight)vert.edgesList.getNode(e).data);
                        break;
                    }
                }} catch (ListException e) {
                throw new RuntimeException(e);
            }
            return out;
        };
        System.out.println("Dibujando aristas para: " + rbAdjacencyMatrix.isSelected());
        System.out.println("Vertices: " + verticesM.get());
        System.out.println("Aristas desde 0: " + edgesM.apply(verticesM.get().get(0)));

        //generar los distintos grafos
        if (rbAdjacencyMatrix.isSelected())
            util.FXUtility.drawGraph(graphMatrix, pane, verticesM, edgesM);
        if (rbAdjacencyList.isSelected())
            util.FXUtility.drawGraph(graphList,   pane,   verticesL, edgesL);
        if (rbLinkedList.isSelected()) {
            util.FXUtility.drawGraph(graphLinked, pane, verticesLi, edgesLi);
        }

        //encontrar el arbol de expansión minima
        if (rbKruskal.isSelected())
            util.FXUtility.drawGraph(graphMatrix, pane, verticesM, edgesM);
        if (rbPrim.isSelected())
            util.FXUtility.drawGraph(graphList,   pane,   verticesL, edgesL);

    }

    public void algorithms(){
        List<EdgeWeight> allEdges = new ArrayList<>();
        for (Integer u : getGraph().getAllVertices()) {
            for (EdgeWeight ew : graphList.getAdjList(u)) {
                allEdges.add(new EdgeWeight(u, (Integer)ew.getVertex(), (Integer)ew.getWeight()));
            }
        }

    }


}

