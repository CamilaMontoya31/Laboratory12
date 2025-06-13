package util;

import domain.AdjacencyListGraph;
import domain.EdgeWeight;
import domain.GraphException;
import domain.list.ListException;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static util.Utility.kruskal;
import static util.Utility.Edge;

class UtilityTest {

    @Test
    void testMST() throws ListException, GraphException {
        List<Edge> aristas = new ArrayList<Edge>();
        aristas.add(new Edge(0, 1, 4));
        aristas.add(new Edge(0, 2, 3));
        aristas.add(new Edge(1, 2, 1));
        aristas.add(new Edge(1, 3, 2));
        aristas.add(new Edge(2, 3, 4));
        aristas.add(new Edge(3, 4, 2));
        aristas.add(new Edge(4, 5, 6));

        int numVertices = 6; // vértices de 0 a 5

        List<Edge> mst1 = kruskal(aristas, numVertices);
        System.out.println("Aristas del Árbol de Expansión Mínima con Kruskal:");
        for (Edge e : mst1) {
            System.out.println("[" + e.origen + " - " + e.destino + "] peso: " + e.peso);
        }

        AdjacencyListGraph graph = new AdjacencyListGraph(50);
        for (int i = 1; i <= 5; i++) {
            graph.addVertex(i);
        }
        graph.addEdgeWeight(1, 2, util.Utility.random(20)+2);
        graph.addEdgeWeight(1, 3, util.Utility.random(20)+2);
        graph.addEdgeWeight(1, 4, util.Utility.random(20)+2);
        graph.addEdgeWeight(2, 5, util.Utility.random(20)+2);
        graph.addEdgeWeight(3, 4, util.Utility.random(20)+2);
        graph.addEdgeWeight(3, 5, util.Utility.random(20)+2);
        Utility.prim(graph);

        System.out.println("Aristas del Árbol de Expansión Mínima con Prim:");
        System.out.println(graph);
    }
}