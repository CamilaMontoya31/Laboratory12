package domain;

import domain.list.ListException;
import domain.queue.QueueException;
import domain.stack.StackException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectedAdjacencyListGraphTest {

    @Test
    void test() {
        try {
            DirectedAdjacencyListGraph graph = new DirectedAdjacencyListGraph(50);
            for (char i = 'A'; i <= 'M'; i++) {
                graph.addVertex(i);
            }
            graph.addEdgeWeight('A', 'B', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('A', 'C', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('A', 'D', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('B', 'E', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('E', 'H', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('H', 'K', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('C',  'F', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('F',  'I', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('I',  'L', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('D',  'G', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('G',  'J', util.Utility.random(10,50)+2);
            graph.addEdgeWeight('J',  'M', util.Utility.random(10,50)+2);

            System.out.println(graph);  //toString
            System.out.println("DFS Transversal Tour: "+graph.dfs());
            System.out.println("BFS Transversal Tour: "+graph.bfs());

            //eliminemos vertices
            System.out.println("\nVertex deleted: E");
            graph.removeVertex('E');
            System.out.println("\nVertex deleted: F");
            graph.removeVertex('F');
            System.out.println("\nVertex deleted: G");
            graph.removeVertex('G');

            System.out.println(graph);  //toString
            System.out.println("Edge deleted: H---K");
            graph.removeEdge('H', 'K');
            System.out.println("\nEdge deleted: I---L");
            graph.removeEdge('I', 'L');
            System.out.println("\nEdge deleted: J---M");
            graph.removeEdge('J', 'M');
            System.out.println(graph);  //toString

        } catch (GraphException | ListException | StackException | QueueException e) {
            throw new RuntimeException(e);
        }
    }
    }