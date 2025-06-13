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

    public enum Algorithm { KRUSKAL, PRIM }

    public static Graph computeMST(Graph graph, Algorithm algo)
            throws GraphException, ListException {

        if (graph instanceof AdjacencyListGraph) {
            return computeMSTList((AdjacencyListGraph) graph, algo);
        } else if (graph instanceof AdjacencyMatrixGraph) {
            return computeMSTList(((AdjacencyMatrixGraph) graph).toAdjacencyListGraph(), algo);
        } else if (graph instanceof SinglyLinkedListGraph) {
            return computeMSTList(((SinglyLinkedListGraph) graph).toAdjacencyListGraph(), algo);
        } else {
            throw new IllegalArgumentException("Tipo de grafo no soportado: " + graph.getClass());
        }
    }

    private static Graph computeMSTList(AdjacencyListGraph graph, Algorithm algo)
            throws GraphException, ListException {
        switch (algo) {
            case KRUSKAL:
                return kruskalMST(graph);
            case PRIM:
                return primMST(graph);
            default:
                throw new IllegalArgumentException("Algoritmo no soportado: " + algo);
        }
    }

    private static AdjacencyListGraph kruskalMST(AdjacencyListGraph graph)
            throws GraphException, ListException {
        List<Integer> vertices = graph.getAllVertices();
        List<EdgeTriple> edges = new ArrayList<>();

        for (Integer u : vertices) {
            for (EdgeWeight ew : graph.getAdjList(u)) {
                int v = (Integer) ew.getVertex();
                int w = (Integer) ew.getWeight();
                if (u < v) {
                    edges.add(new EdgeTriple(u, v, w));
                }
            }
        }
        Collections.sort(edges);

        UnionFind uf = new UnionFind();
        uf.makeSet(vertices);

        AdjacencyListGraph mst = new AdjacencyListGraph();
        for (Integer v : vertices) mst.addVertex(v);

        int needed = vertices.size() - 1;
        for (EdgeTriple e : edges) {
            if (uf.union(e.from, e.to)) {
                mst.addEdgeWeight(e.from, e.to, e.weight);
                mst.addEdgeWeight(e.to, e.from, e.weight);
                if (--needed == 0) break;
            }
        }
        return mst;
    }

    private static AdjacencyListGraph primMST(AdjacencyListGraph graph)
            throws GraphException, ListException {
        List<Integer> vertices = graph.getAllVertices();
        if (vertices.isEmpty()) return new AdjacencyListGraph();

        AdjacencyListGraph mst = new AdjacencyListGraph();
        Set<Integer> visited = new HashSet<>();
        int start = vertices.get(0);
        mst.addVertex(start);
        visited.add(start);

        PriorityQueue<EdgeTriple> pq = new PriorityQueue<>();
        addEdgesOf(graph, start, visited, pq);

        while (!pq.isEmpty() && visited.size() < vertices.size()) {
            EdgeTriple minEdge = pq.poll();
            if (visited.contains(minEdge.to)) continue;

            visited.add(minEdge.to);
            mst.addVertex(minEdge.to);
            mst.addEdgeWeight(minEdge.from, minEdge.to, minEdge.weight);
            mst.addEdgeWeight(minEdge.to, minEdge.from, minEdge.weight);

            addEdgesOf(graph, minEdge.to, visited, pq);
        }
        return mst;
    }

    private static void addEdgesOf(AdjacencyListGraph graph, int u,
                                   Set<Integer> visited, PriorityQueue<EdgeTriple> pq)
            throws GraphException, ListException {
        for (EdgeWeight ew : graph.getAdjList(u)) {
            int v = (Integer) ew.getVertex();
            int w = (Integer) ew.getWeight();
            if (!visited.contains(v)) {
                pq.offer(new EdgeTriple(u, v, w));
            }
        }
    }
}

class UnionFind {
    private final Map<Integer, Integer> parent = new HashMap<>();
    private final Map<Integer, Integer> rank   = new HashMap<>();

    public void makeSet(Collection<Integer> vertices) {
        for (Integer v : vertices) {
            parent.put(v, v);
            rank.put(v, 0);
        }
    }

    public int find(int x) {
        if (parent.get(x) != x) {
            parent.put(x, find(parent.get(x)));
        }
        return parent.get(x);
    }

    public boolean union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (rank.get(ra) < rank.get(rb)) {
            parent.put(ra, rb);
        } else if (rank.get(ra) > rank.get(rb)) {
            parent.put(rb, ra);
        } else {
            parent.put(rb, ra);
            rank.put(ra, rank.get(ra) + 1);
        }
        return true;
    }
}

class EdgeTriple implements Comparable<EdgeTriple> {
    public final int from, to, weight;
    public EdgeTriple(int f, int t, int w) {
        this.from = f;
        this.to = t;
        this.weight = w;
    }
    @Override
    public int compareTo(EdgeTriple o) {
        return Integer.compare(this.weight, o.weight);
    }
}
