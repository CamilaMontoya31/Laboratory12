package controller;

import domain.*;
import domain.list.ListException;
import domain.list.SinglyLinkedList;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
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
    public void initialize(URL location, ResourceBundle resources) throws ListException{

        Supplier<List<Object>> verticesM = () -> {
            List<Object> verts = new ArrayList<>();
            for (int i = 0; i < graphMatrix.counter; i++)
                verts.add(graphMatrix.vertexList[i].data);
            return verts;
        };
        Function<Object, List<EdgeWeight>> edgesM = v -> {
            List<EdgeWeight> out = new ArrayList<>();
            int i = Arrays.asList(graphMatrix.vertexList).indexOf(new Vertex(v));
            for (int j = 0; j < graphMatrix.counter; j++) {
                Object w = graphMatrix.adjacencyMatrix[i][j];
                if (w != null && !w.equals(0))
                    out.add(new EdgeWeight(graphMatrix.vertexList[j].data, w));
            }
            return out;
        };

        // 3) Lambdas para AdjacencyListGraph
        Supplier<List<Object>> verticesL = () -> {
            // asumo que tus vértices están en graphList.vertexList
            return Collections.singletonList(new SinglyLinkedList());
        };
        Function<Object, List<EdgeWeight>> edgesL = v -> {
            // asumo método que devuelve List<EdgeWeight>
            try {
                return graphList.getAdjList(v);
            } catch (GraphException e) {
                throw new RuntimeException(e);
            } catch (ListException e) {
                throw new RuntimeException(e);
            }
        };

        // 4) Lambdas para SinglyLinkedListGraph
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

        // 5. LLAMAR al dibujador para cada pane
        util.FXUtility.drawGraph(graphMatrix, paneMatrix, verticesM, edgesM);
        util.FXUtility.drawGraph(graphList,   paneList,   verticesL, edgesL);
        util.FXUtility.drawGraph(graphLinked, paneLinked, verticesLi, edgesLi);
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
            getGraph().addVertex(numeros[i]);
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
        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                edgePairs.add(new int[]{numeros[i], numeros[j]});
            }
        }
        Collections.shuffle(edgePairs); // orden aleatorio


        //para dibujar el grafo
        Supplier<List<Object>> verticesM = () -> {
            List<Object> verts = new ArrayList<>();
            for (int i = 0; i < graphMatrix.counter; i++)
                verts.add(graphMatrix.vertexList[i].data);
            return verts;
        };
        Function<Object, List<EdgeWeight>> edgesM = v -> {
            List<EdgeWeight> out = new ArrayList<>();
            int i = Arrays.asList(graphMatrix.vertexList).indexOf(new Vertex(v));
            for (int j = 0; j < graphMatrix.counter; j++) {
                Object w = graphMatrix.adjacencyMatrix[i][j];
                if (w != null && !w.equals(0))
                    out.add(new EdgeWeight(graphMatrix.vertexList[j].data, w));
            }
            return out;
        };

        // 3) Lambdas para AdjacencyListGraph
        Supplier<List<Object>> verticesL = () -> {
            // asumo que tus vértices están en graphList.vertexList
            return Collections.singletonList(new SinglyLinkedList());
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

        // 4) Lambdas para SinglyLinkedListGraph
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

        util.FXUtility.drawGraph(getGraph(), pane, verticesL, edgesL);
    }



}

