package domain;

import domain.list.ListException;
import domain.list.SinglyLinkedList;
import domain.queue.LinkedQueue;
import domain.queue.QueueException;
import domain.stack.LinkedStack;
import domain.stack.StackException;

import java.util.ArrayList;
import java.util.List;

public class DirectedAdjacencyListGraph implements Graph {
    private Vertex[] vertexList; //arreglo de objetos tupo vértice
    private int n; //max de elementos
    private int counter; //contador de vertices

    //para los recorridos dfs, bfs
    private LinkedStack stack;
    private LinkedQueue queue;

    //Constructor
    public DirectedAdjacencyListGraph(int n) {
        if (n <= 0) System.exit(1); //sale con status==1 (error)
        this.n = n;
        this.counter = 0;
        this.vertexList = new Vertex[n];
        this.stack = new LinkedStack();
        this.queue = new LinkedQueue();
    }

    @Override
    public int size() throws ListException {
        return counter;
    }

    @Override
    public void clear() {
        this.vertexList = new Vertex[n];
        this.counter = 0; //inicializo contador de vértices
    }

    @Override
    public boolean isEmpty() {
        return counter == 0;
    }

    @Override
    public boolean containsVertex(Object element) throws GraphException, ListException {
        if(isEmpty())
            throw new GraphException("Directed  Adjacency List Graphis Empty");
        //opcion-1
       /* for (int i = 0; i < counter; i++) {
            if(util.Utility.compare(vertexList[i].data, element)==0)
                return true;
        }*/
        //opcion-2
        return indexOf(element)!=-1;
        //return false;
    }

    @Override
    public boolean containsEdge(Object a, Object b) throws GraphException, ListException {
        if(isEmpty())
            throw new GraphException("Directed Adjacency List Graph Graph is Empty");
        return (!vertexList[indexOf(a)].edgesList.isEmpty()
                && vertexList[indexOf(a)].edgesList.contains(new EdgeWeight(b, null)));
    }

    @Override
    public void addVertex(Object element) throws GraphException, ListException {
        if(counter>=vertexList.length)
            throw new GraphException("Directed Adjacency List Graph is Full");
        vertexList[counter++] = new Vertex(element);
    }

    @Override
    public void addEdge(Object a, Object b) throws GraphException, ListException {
        if(!containsVertex(a)||!containsVertex(b))
            throw new GraphException("Cannot add edge between vertexes ["+a+"] y ["+b+"]");
       vertexList[indexOf(a)].edgesList.add(new EdgeWeight(b, null));

    }

    @Override
    public int indexOfGeneral(Object element){
        for (int i = 0; i < counter; i++) {
            if(util.Utility.compare(vertexList[i].data, element)==0)
                return i+1; //retorna la pos en el arreglo de objectos vertexList
        }
        return -1; //significa q la data de todos los vertices no coinciden con element
    }

    public int indexOf(Object element){
        for (int i = 0; i < counter; i++) {
            if(util.Utility.compare(vertexList[i].data, element)==0)
                return i; //retorna la pos en el arreglo de objectos vertexList
        }
        return -1; //significa q la data de todos los vertices no coinciden con element
    }

    @Override
    public void addWeight(Object a, Object b, Object weight) throws GraphException, ListException {
        if(!containsEdge(a, b))
            throw new GraphException("There is no edge between the vertexes["+a+"] y ["+b+"]");
        updateEdgesListEdgeWeight(a, b, weight);

    }

    private void updateEdgesListEdgeWeight(Object a, Object b, Object weight) throws ListException {
        EdgeWeight ew = (EdgeWeight) vertexList[indexOf(a)].edgesList
                .getNode(new EdgeWeight(b, null)).getData();
        //setteo el peso en el campo respectivo
        ew.setWeight(weight);
        //ahora actualizo la info en la lista de aristas correspondiente
        vertexList[indexOf(a)].edgesList.getNode(new EdgeWeight(b, null))
                .setData(ew);
    }

    @Override
    public void addEdgeWeight(Object a, Object b, Object weight) throws GraphException, ListException {
        if(!containsVertex(a)||!containsVertex(b))
            throw new GraphException("Cannot add edge between vertexes ["+a+"] y ["+b+"]");
        if(!containsEdge(a, b)){ //si no existe la arista
            vertexList[indexOf(a)].edgesList.add(new EdgeWeight(b, weight));

        }
    }

    @Override
    public void removeVertex(Object element) throws GraphException, ListException {
        if(isEmpty())
            throw new GraphException("Directed Adjacency List Graph is Empty");
        if(containsVertex(element)){
            for (int i = 0; i < counter; i++) {
                if(util.Utility.compare(vertexList[i].data, element)==0) {
                    //ya lo encontro, ahora
                    //se debe suprimir el vertice a eliminar de todas las listas
                    //enlazadas de los otros vértices
                    for (int j = 0; j < counter; j++) {
                        if(containsEdge(vertexList[j].data, element))
                            removeEdge(vertexList[j].data, element);
                    }

                    //ahora, debemos suprimir el vértice
                    for (int j = i; j < counter-1; j++) {
                        vertexList[j] = vertexList[j+1];
                    }
                    counter--; //decrementamos el contador de vértices
                }
            }
        }
    }

    @Override
    public void removeEdge(Object a, Object b) throws GraphException, ListException {
        if(!containsVertex(a)||!containsVertex(b))
            throw new GraphException("There's no some of the vertexes");
        if(!vertexList[indexOf(a)].edgesList.isEmpty()) {
            vertexList[indexOf(a)].edgesList.remove(new EdgeWeight(b, null));
        }
    }

    // Recorrido en profundidad
    //TODO arreglar los tours
    @Override
    public String dfs() throws GraphException, StackException, ListException {
        setVisited(false);//marca todos los vertices como no vistados
        // inicia en el vertice 0
        String info = vertexList[0].data + ", ";
        vertexList[0].setVisited(true); // lo marca
        stack.clear();
        stack.push(0); //lo apila
        while (!stack.isEmpty()) {
            // obtiene un vertice adyacente no visitado,
            //el que esta en el tope de la pila
            int index = adjacentVertexNotVisited((int) stack.top());
            if (index == -1) // no lo encontro
                stack.pop();
            else {
                vertexList[index].setVisited(true); // lo marca
                info += vertexList[index].data + ", "; //lo muestra
                stack.push(index); //inserta la posicion
            }
        }
        return info;
    }

    //Recorrido en amplitud
    @Override
    public String bfs() throws GraphException, QueueException, ListException {
        setVisited(false);//marca todos los vertices como no visitados
        // inicia en el vertice 0
        String info = vertexList[0].data + ", ";
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
                info += vertexList[v2].data + ", "; //lo muestra
                queue.enQueue(v2); // lo encola
            }
        }
        return info;
    }

    @Override
    public List<Integer> getNeighbors(Object vertex) throws ListException {
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

    private int adjacentVertexNotVisited(int index) throws ListException {
        SinglyLinkedList edges = vertexList[index].edgesList;

        // Verificar si la lista de aristas está vacía
        if (edges.isEmpty()) {
            return -1;
        }

        for (int i = 0; i < edges.size(); i++) {
            EdgeWeight edge = (EdgeWeight) edges.get(i);
            Object adjacentData = edge.getEdge();

            int adjacentIndex = indexOf(adjacentData);
            if (!vertexList[adjacentIndex].isVisited()) {
                return adjacentIndex;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        String result = "Directed Adjacency List Graph Content...";
        //se muestran todos los vértices del grafo
        for (int i = 0; i < counter; i++) {
            result+="\nThe vextex in the position: "+i+" is: "+vertexList[i].data;
            if(!vertexList[i].edgesList.isEmpty())
                result+="\n......EDGES AND WEIGHTS: "+vertexList[i].edgesList.toString();
        }
        return result;
    }

    @Override
    public List<Object> getVertices() {
        List<Object> vertices = new ArrayList<>();
        for (int i = 0; i < counter; i++) {
            if (vertexList[i] != null) {
                vertices.add(vertexList[i].data);
            }
        }
        return vertices;
    }

    public static class EdgeInfo {
        public Object source;
        public Object destination;
        public Integer weight;

        public EdgeInfo(Object source, Object destination, Integer weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }
    }


    public List<EdgeInfo> getEdges() throws ListException {
        List<EdgeInfo> edges = new ArrayList<>();
        for (int i = 0; i < counter; i++) {
            if (vertexList[i] != null) {
                Object sourceVertexData = vertexList[i].data;
                SinglyLinkedList currentVertexEdges = vertexList[i].edgesList;

                if (currentVertexEdges != null && !currentVertexEdges.isEmpty()) {
                    for (int j = 0; j < currentVertexEdges.size(); j++) {
                        EdgeWeight ew = (EdgeWeight) currentVertexEdges.get(j);
                        Object destinationVertexData = ew.getEdge();
                        Integer weight = (Integer) ew.getWeight();

                        edges.add(new EdgeInfo(sourceVertexData, destinationVertexData, weight));
                    }
                }
            }
        }
        return edges;
    }

    @Override
    public List<EdgeWeight> getAdjList(Object v) throws GraphException, ListException {
        // Buscar el índice del vértice
        int idx = indexOf(v);
        if (idx == -1)
            throw new GraphException("Vértice no encontrado: " + v);
        List<EdgeWeight> edges = new ArrayList<>();
        // Acceder a su lista enlazada de aristas
        Vertex vert = vertexList[idx];
        int size = vert.edgesList.isEmpty()? 0 : vert.edgesList.size();
        for (int j = 1; j <= size; j++) {
            edges.add((EdgeWeight) vert.edgesList.getNode(j).data);
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

