package controller;

import domain.*;
import domain.list.ListException;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KruskalPrimController {

    @FXML
    private Pane paneGraph;

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

    AdjacencyMatrixGraph graphMatrix = new AdjacencyMatrixGraph(10);
    AdjacencyListGraph graphList = new AdjacencyListGraph(10);
    SinglyLinkedListGraph graphLinked= new SinglyLinkedListGraph();

    private List<int[]> edgePairs = new ArrayList<>();
    private int currentEdgeIndex = 0;

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

       util.FXUtil.drawGraph(graph, paneGraph);
    }
}