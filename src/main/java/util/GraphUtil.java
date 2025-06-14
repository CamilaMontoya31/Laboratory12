package util;

import domain.*;
import domain.list.ListException;
import domain.list.SinglyLinkedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GraphUtil {
    public static void addRandomEdges(Graph graph, int maxEdges) throws ListException, GraphException {
        int totalVertices = graph.getVertexList().size();
        int edgesAdded = 0;
        Random random = new Random();

        // Crear lista de pares posibles (i ≠ j)
        List<int[]> pares = new ArrayList<>();
        for (int i = 1; i <= totalVertices; i++) {
            for (int j = 1; j <= totalVertices; j++) {
                if (i != j) pares.add(new int[]{i, j});
            }
        }

        // Barajar todos los pares posibles
        Collections.shuffle(pares, random);

        for (int[] par : pares) {
            if (edgesAdded >= maxEdges) break;
            int i = par[0];
            int j = par[1];
            Vertex from = (Vertex) graph.getVertexList().getNode(i).data;
            Vertex to = (Vertex) graph.getVertexList().getNode(j).data;
            if (!graph.containsEdge(from.data, to.data) &&
                    !isReachable(graph, to.data, from.data)) {
                double weight = Utility.random(10, 100);
                graph.addEdgeWeight(from.data, to.data, weight);
                edgesAdded++;
            }
        }
    }

    public static boolean isReachable(Graph graph, Object start, Object target) throws ListException {
        boolean[] visited = new boolean[graph.getVertexList().size() + 1];
        return dfs(graph, graph.indexOfGeneral(start), graph.indexOfGeneral(target), visited);
    }

    private static boolean dfs(Graph graph, int current, int target, boolean[] visited) throws ListException {
        if (current == target) return true;
        visited[current] = true;
        Vertex vertex = (Vertex) graph.getVertexList().getNode(current).data;
        int size = vertex.edgesList.isEmpty()? 0 : vertex.edgesList.size();
        for (int i = 1; i <= size; i++) {
            EdgeWeight edge = (EdgeWeight) vertex.edgesList.getNode(i).data;
            int next = graph.indexOfGeneral(edge.getEdge());
            if (!visited[next] && dfs(graph, next, target, visited))
                return true;
        }
        return false;
    }

    public static double getEdgeWeight(EdgeWeight edge) {
        return Double.parseDouble(edge.getWeight().toString());
    }

    private static SinglyLinkedList reconstructPath(int[] anterior, int destino, Graph graph, double distanciaFinal) throws ListException {
        SinglyLinkedList camino = new SinglyLinkedList();
        if (destino == 0) destino = 1;
        int actual = destino;

        while (actual != -1) {
            Vertex v = (Vertex) graph.getVertexList().getNode(actual).data;
            camino.addFirst(v.data);
            actual = anterior[actual];
        }

        camino.add("Total: " + distanciaFinal);
        return camino;
    }

    public static SinglyLinkedList dijkstra(Object origen, Object destino, Graph graph) throws Exception {
        if (graph.getVertexList().isEmpty())
            throw new Exception("El grafo está vacío");

        int indiceOrigen = graph.indexOfGeneral(origen);
        int indiceDestino = graph.indexOfGeneral(destino);
        if (indiceOrigen == -1 || indiceDestino == -1)
            throw new Exception("El vértice origen o destino no existe");

        if (!isReachable(graph, origen, destino)) {
            SinglyLinkedList noPath = new SinglyLinkedList();
            noPath.add("No hay camino desde " + origen + " hasta " + destino);
            return noPath;
        }

        int n = graph.getVertexList().size();
        double[] distancia = new double[n + 1];
        boolean[] visitado = new boolean[n + 1];
        int[] anterior = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            distancia[i] = Double.MAX_VALUE;
            visitado[i] = false;
            anterior[i] = -1;
        }

        distancia[indiceOrigen] = 0;

        for (int count = 1; count <= n; count++) {
            int u = minDistance(distancia, visitado, n);
            if (u == -1 || u == indiceDestino) break;

            visitado[u] = true;
            Vertex verticeU = (Vertex) graph.getVertexList().getNode(u).data;
            int sizeU = verticeU.edgesList.isEmpty()? 0 : verticeU.edgesList.size();
            for (int i = 1; i <= sizeU; i++) {
                EdgeWeight arista = (EdgeWeight) verticeU.edgesList.getNode(i).data;
                int v = graph.indexOfGeneral(arista.getEdge());
                if (v == -1 || visitado[v]) continue;

                double peso = getEdgeWeight(arista);
                if (distancia[u] + peso < distancia[v]) {
                    distancia[v] = distancia[u] + peso;
                    anterior[v] = u;
                }
            }
        }

        return reconstructPath(anterior, indiceDestino, graph, distancia[indiceDestino]);
    }

    private static int minDistance(double[] distancia, boolean[] visitado, int n) {
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 1; i <= n; i++) {
            if (!visitado[i] && distancia[i] < min) {
                min = distancia[i];
                minIndex = i;
            }
        }
        return minIndex;
    }
}
