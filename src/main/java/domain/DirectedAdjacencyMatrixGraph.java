package domain;

import domain.list.ListException;
import domain.list.SinglyLinkedList;
import domain.queue.LinkedQueue;
import domain.queue.QueueException;
import domain.stack.LinkedStack;
import domain.stack.StackException;

import java.util.ArrayList;
import java.util.List;

public class DirectedAdjacencyMatrixGraph implements Graph {
    private Vertex[] vertexList; //arreglo de objetos tupo vértice
    private Object[][] adjacencyMatrix; //arreglo bidimensional
    private int n; //max de elementos
    private int counter; //contador de vertices

    //para los recorridos dfs, bfs
    private LinkedStack stack;
    private LinkedQueue queue;

    //Constructor
    public DirectedAdjacencyMatrixGraph(int n) {
        if (n <= 0) System.exit(1); //sale con status==1 (error)
        this.n = n;
        this.counter = 0;
        this.vertexList = new Vertex[n];
        this.adjacencyMatrix = new Object[n][n];
        this.stack = new LinkedStack();
        this.queue = new LinkedQueue();
        initMatrix(); //inicializa matriz de objetos con cero
    }

    private void initMatrix() {

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                this.adjacencyMatrix[i][j] = 0; //init con ceros
    }

    @Override
    public int size() throws ListException {
        return counter;
    }

    @Override
    public void clear() {
        this.vertexList = new Vertex[n];
        this.adjacencyMatrix = new Object[n][n];
        this.counter = 0; //inicializo contador de vértices
        this.initMatrix();
    }

    @Override
    public boolean isEmpty() {
        return counter == 0;
    }

    @Override
    public boolean containsVertex(Object element) throws GraphException, ListException {
        if (isEmpty())
            throw new GraphException("Directed Adjacency Matrix Graph is Empty");
        //opcion-1
       /* for (int i = 0; i < counter; i++) {
            if(util.Utility.compare(vertexList[i].data, element)==0)
                return true;
        }*/
        //opcion-2
        return indexOf(element) != -1;
        //return false;
    }

    @Override
    public boolean containsEdge(Object a, Object b) throws GraphException, ListException {
        if (isEmpty())
            throw new GraphException("Directed Directed Adjacency Matrix Graph Graph Graph is Empty");
        return !(util.Utility.compare(adjacencyMatrix[indexOf(a)][indexOf(b)], 0) == 0);
    }

    @Override
    public void addVertex(Object element) throws GraphException, ListException {
        if (counter >= vertexList.length)
            throw new GraphException("Directed Directed Adjacency Matrix Graph Graph Graph is Full");
        vertexList[counter++] = new Vertex(element);
    }

    @Override
    public void addEdge(Object a, Object b) throws GraphException, ListException {
        if (!containsVertex(a) || !containsVertex(b))
            throw new GraphException("Cannot add edge between vertexes [" + a + "] y [" + b + "]");
        adjacencyMatrix[indexOf(a)][indexOf(b)] = 1; //hay una arista
        vertexList[indexOf(a)].edgesList.add(new EdgeWeight(b, null));
    }

    @Override
    public int indexOfGeneral(Object element) {
        for (int i = 0; i < counter; i++) {
            if (util.Utility.compare(vertexList[i].data, element) == 0)
                return i+1; //retorna la pos en el arreglo de objectos vertexList
        }
        return -1; //significa q la data de todos los vertices no coinciden con element
    }

    public int indexOf(Object element) {
        for (int i = 0; i < counter; i++) {
            if (util.Utility.compare(vertexList[i].data, element) == 0)
                return i; //retorna la pos en el arreglo de objectos vertexList
        }
        return -1; //significa q la data de todos los vertices no coinciden con element
    }

    @Override
    public void addWeight(Object a, Object b, Object weight) throws GraphException, ListException {
        if (!containsEdge(a, b))
            throw new GraphException("There is no edge between the vertexes[" + a + "] y [" + b + "]");
        adjacencyMatrix[indexOf(a)][indexOf(b)] = weight; //hay una arista

    }

    @Override
    public void addEdgeWeight(Object a, Object b, Object weight) throws GraphException, ListException {
        if (!containsVertex(a) || !containsVertex(b))
            throw new GraphException("Cannot add edge between vertexes [" + a + "] y [" + b + "]");
        adjacencyMatrix[indexOf(a)][indexOf(b)] = weight; //hay una arista
        vertexList[indexOf(a)].edgesList.add(new EdgeWeight(b, weight));
    }

    @Override
    public void removeVertex(Object element) throws GraphException, ListException {
        if (isEmpty())
            throw new GraphException("Directed Directed Adjacency Matrix Graph Graph Graph is Empty");
        int index = indexOf(element);
        if (index != -1) { //si existe el indice
            for (int i = index; i < counter - 1; i++) {
                vertexList[i] = vertexList[i + 1];
                //elimino el vertice, tambien debo eliminar todas las aristas

                //movemos todas las filas, una posición hacia arriba
                for (int j = 0; j < counter; j++)
                    adjacencyMatrix[i][j] = adjacencyMatrix[i + 1][j];
            }
            //ahora, movemos todas las columnas, una posición a la izq
            for (int i = 0; i < counter; i++) {
                for (int j = index; j < counter - 1; j++) {
                    adjacencyMatrix[i][j] = adjacencyMatrix[i][j + 1];
                }
            }
            counter--; //decrementamos el contador de vertices agregados
        }
        //que pasa, si ya no quedan vertices
        if (counter == 0) initMatrix();
    }

    @Override
    public void removeEdge(Object a, Object b) throws GraphException, ListException {
        if (!containsVertex(a) || !containsVertex(b))
            throw new GraphException("There's no some of the vertexes");
        int i = indexOf(a);
        int j = indexOf(b);
        if (i != -1 && j != -1) {
            adjacencyMatrix[i][j] = 0;
            vertexList[indexOf(a)].edgesList.remove(new EdgeWeight(b, null));
        }
    }

    // Recorrido en profundidad (DFS)
    @Override
    public String dfs() throws GraphException, StackException, ListException {
        if (isEmpty()) throw new GraphException("DFS: Graph is Empty");
        setVisited(false); // marca todos los vertices como no visitados
        // inicia en el vertice 0
        String info = "";
        try {
            info += vertexList[0].data + ", ";
            vertexList[0].setVisited(true); // lo marca
            stack.clear();
            stack.push(0); // lo apila
            while (!stack.isEmpty()) {
                // obtiene un vertice adyacente no visitado,
                // el que esta en el tope de la pila
                int index = adjacentVertexNotVisited((int) stack.top());
                if (index == -1) // no lo encontro
                    stack.pop();
                else {
                    vertexList[index].setVisited(true); // lo marca
                    info += vertexList[index].data + ", "; // lo muestra
                    stack.push(index); // inserta la posicion
                }
            }
        } catch (StackException e) {
            // Manejar las excepciones si es necesario
            throw new GraphException("DFS traversal error: " + e.getMessage());
        }
        return info;
    }

    // Recorrido en amplitud (BFS)
    @Override
    public String bfs() throws GraphException, QueueException, ListException {
        if (isEmpty()) throw new GraphException("BFS: Graph is Empty");
        setVisited(false); // marca todos los vertices como no visitados
        // inicia en el vertice 0
        String info = "";
        try {
            info += vertexList[0].data + ", ";
            vertexList[0].setVisited(true); // lo marca
            queue.clear();
            queue.enQueue(0); // encola el elemento
            int v2;
            while (!queue.isEmpty()) {
                int v1 = (int) queue.deQueue(); // remueve el vertice de la cola
                // hasta que no tenga vecinos sin visitar
                while ((v2 = adjacentVertexNotVisited(v1)) != -1) {
                    // obtiene uno
                    vertexList[v2].setVisited(true); // lo marca
                    info += vertexList[v2].data + ", "; // lo muestra
                    queue.enQueue(v2); // lo encola
                }
            }
        } catch (QueueException e) {
            // Manejar las excepciones si es necesario
            throw new GraphException("BFS traversal error: " + e.getMessage());
        }
        return info;
    }

    @Override
    public List<Integer> getNeighbors(int vertex) throws ListException {
        return List.of();
    }

    @Override
    public int getWeight(int from, int to) throws GraphException, ListException {
        return 0;
    }


    //setteamos el atributo visitado del vertice respectivo
    private void setVisited(boolean value) {
        for (int i = 0; i < counter; i++) {
            vertexList[i].setVisited(value); //value==true o false
        }//for
    }

    private int adjacentVertexNotVisited(int index) {
        for (int i = 0; i < counter; i++) {
            if (!adjacencyMatrix[index][i].equals(0)
                    && !vertexList[i].isVisited())
                return i;//retorna la posicion del vertice adyacente no visitado
        }//for i
        return -1;
    }

    @Override
    public String toString() {
        String result = "Directed Directed Adjacency Matrix Graph Graph Graph Content...";
        //se muestran todos los vértices del grafo
        for (int i = 0; i < counter; i++) {
            result+="\nThe vextex in the position: "+i+" is: "+vertexList[i].data;
        }
        //agregamos la info de las aristas y pesos
        for (int i = 0; i < counter; i++) {
            for (int j = 0; j < counter; j++) {
                if(util.Utility.compare(adjacencyMatrix[i][j], 0)!=0) {//si existe arista
                    //si existe una arista
                    result+="\nThere is edge between the vertexes: "+vertexList[i].data+"...."
                            +vertexList[j].data;
                    //si existe peso que lo muestre
                    if(util.Utility.compare(adjacencyMatrix[i][j], 1)!=0){
                        //si matriz[fila][col] !=1 existe un peso agregado
                        result+="_____WEIGHT: "+adjacencyMatrix[i][j];
                    }
                }
            }
        }

        return result;
    }

    @Override
    public List<Object> getVertices() {
        List<Object> vertices = new ArrayList<>();
        for (int i = 0; i < counter; i++) {
            vertices.add(vertexList[i].data);
        }
        return vertices;
    }

    public int getWeight(Object source, Object destination) throws GraphException, ListException {
        if (!containsVertex(source)) {
            throw new GraphException("Source vertex [" + source + "] does not exist.");
        }
        if (!containsVertex(destination)) {
            throw new GraphException("Destination vertex [" + destination + "] does not exist.");
        }

        int sourceIndex = indexOf(source);
        int destIndex = indexOf(destination);

        // Asegúrate de que el valor en adjacencyMatrix[sourceIndex][destIndex] sea un Integer.
        if (adjacencyMatrix[sourceIndex][destIndex] instanceof Integer) {
            return (int) adjacencyMatrix[sourceIndex][destIndex];
        }
        // Si no es Integer (lo cual no debería pasar si siempre usas Integer para pesos y 0 para no arista),
        // o si es un tipo inesperado, asumimos que no hay arista para el dibujo.
        return 0;
    }

    @Override
    public List<EdgeWeight> getAdjList(Object v) throws GraphException, ListException {
        // Buscar el índice del vértice
        int idx = indexOf(v);
        if (idx == -1)
            throw new GraphException("Vértice no encontrado: " + v);
        List<EdgeWeight> edges = new ArrayList<>();
        // Acceder a su lista enlazada de aristas
        for (int j = 0; j < counter; j++) {
            if (util.Utility.compare(adjacencyMatrix[idx][j], 0)!=0)
                edges.add(new EdgeWeight(vertexList[j].data, adjacencyMatrix[idx][j]));
        }
        return edges;
    }

    @Override
    public SinglyLinkedList getVertexList() {
        SinglyLinkedList list = new SinglyLinkedList();
        for (int i = 0; i < counter; i++)
            list.add(vertexList[i]);
        return list;
    }
}