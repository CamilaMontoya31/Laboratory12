package util;

import domain.*;
import domain.list.ListException;

import java.text.DecimalFormat;
import java.util.*;

public class Utility {
    private static Random random;
    //static init
    static {
        // semilla para el random
        long seed = System.currentTimeMillis();
        random = new Random(seed);

    }

    public static String format(double value){
        return new DecimalFormat("###,###,###.##").format(value);
    }
    public static String $format(double value){
        return new DecimalFormat("$###,###,###.##").format(value);
    }

    public static void fill(int[] a, int bound) {
        for (int i = 0; i < a.length; i++) {
            a[i] = new Random().nextInt(bound);
        }
    }

    public static int random(int bound) {
        return new Random().nextInt(bound);
    }

    public static int random(int min, int max) {
        // Generación de un número aleatorio en el rango [min, max]
        return min + random.nextInt(max - min + 1);
    }

    public static double random(double min, double max) {
        // Generación de un número aleatorio en el rango [min, max]
        double value = min + (max - min) * random.nextDouble();
        return Math.round(value * 100.0) / 100.0;
    }

    public static int compare(Object a, Object b) {
        switch (instanceOf(a, b)){
            case "Integer":
                Integer int1 = (Integer)a; Integer int2 = (Integer)b;
                return int1 < int2 ? -1 : int1 > int2 ? 1 : 0; //0 == equal
            case "String":
                String st1 = (String)a; String st2 = (String)b;
                return st1.compareTo(st2)<0 ? -1 : st1.compareTo(st2) > 0 ? 1 : 0;
            case "Character":
                Character c1 = (Character)a; Character c2 = (Character)b;
                return c1.compareTo(c2)<0 ? -1 : c1.compareTo(c2)>0 ? 1 : 0;
            case "EdgeWeight":
                EdgeWeight ew1 = (EdgeWeight) a; EdgeWeight ew2 = (EdgeWeight) b;
                return compare(ew1.getEdge(), ew2.getEdge());
            case "Vertex":
                Vertex v1 = (Vertex) a; Vertex v2 = (Vertex) b;
                return compare(v1.data, v2.data);
        }
        return 2; //Unknown
    }

    private static String instanceOf(Object a, Object b) {
        if(a instanceof Integer && b instanceof Integer) return "Integer";
        if(a instanceof String && b instanceof String) return "String";
        if(a instanceof Character && b instanceof Character) return "Character";
        if(a instanceof EdgeWeight && b instanceof EdgeWeight) return "EdgeWeight";
        if(a instanceof Vertex && b instanceof Vertex) return "Vertex";
        return "Unknown";
    }

    public static int maxArray(int[] a) {
        int max = a[0]; //first element
        for (int i = 1; i < a.length; i++) {
            if(a[i]>max){
                max=a[i];
            }
        }
        return max;
    }

    public static int[] getIntegerArray(int n) {
        int[] newArray = new int[n];
        for (int i = 0; i < n; i++) {
            newArray[i] = random(9999);
        }
        return newArray;
    }


    public static int[] copyArray(int[] a) {
        int n = a.length;
        int[] newArray = new int[n];
        for (int i = 0; i < n; i++) {
            newArray[i] = a[i];
        }
        return newArray;
    }

    public static String show(int[] a, int n) {
        String result="";
        for (int i = 0; i < n; i++) {
            result+=a[i]+" ";
        }
        return result;
    }
    public static String getName() {
        String[] names = {
                "Alana", "Pablo", "Ana", "María", "Victoria", "Nicole",
                "Mateo", "Fabiana", "Natalia", "Valeria",
                "Luis", "Elena", "Raúl", "César", "Lucas",
                "Clara", "Diego", "Sara", "Iván", "Julia",
                "David", "Noa", "Bruno", "Emma", "Luz",
                "Gael", "Iris", "Hugo", "Vera", "Leo"
        };
        return names[random(names.length-1)];
    }

    //Algoritmo de Kruskal
    // Clase Union-Find mejorada
    static class UnionFind {
        private int[] parent;
        private int[] rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // Path compression
            }
            return parent[x];
        }

        public boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX == rootY) return false; // Already in the same set

            // Union by rank
            if (rank[rootX] < rank[rootY]) {
                parent[rootX] = rootY;
            } else if (rank[rootX] > rank[rootY]) {
                parent[rootY] = rootX;
            } else {
                parent[rootY] = rootX;
                rank[rootX]++;
            }
            return true;
        }
    }

    // Clase Edge mejorada con tipos genéricos
    public static class Edge implements Comparable<Edge> {
        private final Object source;
        private final Object destination;
        private final Object weight;

        public Edge(int source, int destination, int weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }

        public int getSource() {
            return (int) source;
        }

        public int getDestination() {
            return (int) destination;
        }
        public Object getSourceO() {
            return source;
        }

        public Object getDestinationO() {
            return destination;
        }

        public Object getWeight() {
            return weight;
        }

        @Override
        public int compareTo(Edge other) {
            return compare(this.weight, other.weight);
        }

        @Override
        public String toString() {
            return String.format("%d - %d (%d)", source, destination, weight);
        }
    }

    // Algoritmo de Kruskal mejorado
    public static List<Edge> kruskal(List<Edge> edges, int vertexCount) {
        List<Edge> mst = new ArrayList<>();
        UnionFind uf = new UnionFind(vertexCount);

        // Ordenar aristas por peso ascendente
        Collections.sort(edges);

        for (Edge edge : edges) {
            if (mst.size() == vertexCount - 1) break;

            if (uf.union(edge.getSource(), edge.getDestination())) {
                mst.add(edge);
            }
        }

        // Verificar si se formó un MST completo
        //if (mst.size() != vertexCount - 1) {
         //   throw new IllegalArgumentException("El grafo no es conexo. No se puede construir un MST completo.");
        //}

        return mst;
    }

    // Algoritmo de Prim mejorado
    public static List<Edge> prim(Graph graph) throws GraphException, ListException {
        int vertexCount = graph.size();
        List<Edge> mst = new ArrayList<>();

        // Estructuras para el algoritmo
        int[] parent = new int[vertexCount];
        int[] key = new int[vertexCount];
        boolean[] inMST = new boolean[vertexCount];

        Arrays.fill(key, Integer.MAX_VALUE);
        key[0] = 0;
        parent[0] = -1;

        PriorityQueue<Node> minHeap = new PriorityQueue<>(vertexCount, Comparator.comparingInt(n -> n.key));
        minHeap.add(new Node(0, key[0]));

        while (!minHeap.isEmpty()) {
            int u = minHeap.poll().vertex;

            if (inMST[u]) continue;
            inMST[u] = true;

            // Agregar arista al MST (excepto para el primer nodo)
            if (parent[u] != -1) {
                int weight = graph.getWeight(parent[u], u);
                mst.add(new Edge(parent[u], u, weight));
            }

            // Obtener todos los vértices adyacentes
            List<Integer> neighbors = graph.getNeighbors(u);
            for (int v : neighbors) {
                int weight = graph.getWeight(u, v);

                if (!inMST[v] && weight < key[v]) {
                    parent[v] = u;
                    key[v] = weight;
                    minHeap.add(new Node(v, key[v]));
                }
            }
        }

        // Verificar si se formó un MST completo
        if (mst.size() != vertexCount - 1) {
            throw new GraphException("El grafo no es conexo. No se puede construir un MST completo.");
        }

        return mst;
    }

    // Clase auxiliar para el heap de Prim
    private static class Node {
        int vertex;
        int key;

        public Node(int vertex, int key) {
            this.vertex = vertex;
            this.key = key;
        }
    }

}
