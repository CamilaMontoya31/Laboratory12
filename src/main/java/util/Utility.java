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
        return 1 + random.nextInt(max - min + 1);
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
    // Clase para el Union-Find (Disjoint Set)
    static class UnionFind {
        int[] padre;

        UnionFind(int n) {
            padre = new int[n];
            for (int i = 0; i < n; i++) {
                padre[i] = i;
            }
        }

        int encontrar(int x) {
            if (padre[x] != x) {
                padre[x] = encontrar(padre[x]); // path compression
            }
            return padre[x];
        }

        boolean unir(int x, int y) {
            int raizX = encontrar(x);
            int raizY = encontrar(y);
            if (raizX == raizY) return false; // ya están en el mismo conjunto
            padre[raizX] = raizY;
            return true;
        }
        boolean unir(Object x, Object y) {
            Object raizX = encontrar((Integer) x);
            Object raizY = encontrar((Integer) y);
            if (raizX == raizY) return false; // ya están en el mismo conjunto
            padre[(int) raizX] = (int) raizY;
            return true;
        }
    }

    // Algoritmo de Kruskal
    // Clase para representar una arista (edge)
    public static class Edge implements Comparable<Edge> {
        public Object origen;
        public Object destino;
        public Object peso;

        public Edge(int origen, int destino, int peso) {
            this.origen = origen;
            this.destino = destino;
            this.peso = peso;
        }
        public Object getFrom() {
            return origen;
        }

        public Object getTo() {
            return destino;
        }

        public Object getWeight() {
            return peso;
        }
        @Override
        public int compareTo(Edge otra) {
            return Integer.compare((Integer) this.peso, (Integer)otra.peso); // ordenar por peso ascendente
        }

        @Override
        public String toString() {
            return "Edge{" +
                    "origen=" + origen +
                    ", destino=" + destino +
                    ", peso=" + peso +
                    '}';
        }
    }

    public static List<Edge> kruskal(List<Edge> aristas, int n) {
        List<Edge> resultado = new ArrayList<>(); // E(1) = aristas del MST
        Collections.sort(aristas); // ordenar por peso ascendente
        UnionFind uf = new UnionFind(10); // n = número de vértices

        for (Edge e : aristas) {
            if (resultado.size() == n - 1) break; // ya tenemos n-1 aristas
            if (uf.unir(e.origen, e.destino)) {
                resultado.add(e);
            }
        }

        return resultado;
    }
    //Prim
    public static List<Edge> prim(Graph graph) throws ListException, GraphException {
        List<Edge> resultado = new ArrayList<>(); // = aristas del MST
        int V = graph.size();
        int[] key = new int[V];
        int[] parent = new int[V];
        boolean[] inMST = new boolean[V];

        Arrays.fill(key, Integer.MAX_VALUE);
        key[0] = 0;
        parent[0] = -1;

        for (int count = 0; count < V - 1; count++) {
            int u = minKey(key, inMST);
            inMST[u] = true;

            for (int v : graph.getNeighbors(u)) {
                int weight = graph.getWeight(u, v);
                if (!inMST[v] && weight < key[v]) {
                    key[v] = weight;
                    parent[v] = u;
                }
                for (int i = 1; i < v ; i++) {
                    resultado.add(new Edge(parent[1],i,graph.getWeight(parent[i],i)));
                }
            }
        }

        printMST(parent, key);

        return resultado;
    }

    private static int minKey(int[] key, boolean[] mstSet) {
        int min = Integer.MAX_VALUE, minIndex = -1;

        for (int v = 0; v < key.length; v++) {
            if (!mstSet[v] && key[v] < min) {
                min = key[v];
                minIndex = v;
            }
        }

        return minIndex;
    }

    private static void printMST(int[] parent, int[] key) {
        System.out.println("Edge \tWeight");
        for (int i = 1; i < parent.length; i++) {
            System.out.println(parent[i] + " - " + i + "\t" + key[i]);
        }
    }



}
