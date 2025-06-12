package domain;

import domain.list.ListException;
import domain.queue.QueueException;
import domain.stack.StackException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectedSinglyLinkedListGraphTest {

    @Test
    void test() {
        try {
            DirectedSinglyLinkedListGraph graph = new DirectedSinglyLinkedListGraph();
            for (char i = 'A'; i <= 'J'; i++) {
                graph.addVertex(i);
            }
            graph.addEdgeWeight('A', 'B', util.Utility.getName()+2);
            graph.addEdgeWeight('A', 'C', util.Utility.getName()+2);
            graph.addEdgeWeight('A', 'D', util.Utility.getName()+2);
            graph.addEdgeWeight('B', 'F', util.Utility.getName()+2);
            graph.addEdgeWeight('C', 'G', util.Utility.getName()+2);
            graph.addEdgeWeight('D', 'H', util.Utility.getName()+2);
            graph.addEdgeWeight('F', 'E', util.Utility.getName()+2);
            graph.addEdgeWeight('G', 'J', util.Utility.getName()+2);
            graph.addEdgeWeight('H',  'J', util.Utility.getName()+2);
            graph.addEdgeWeight('H',  'I', util.Utility.getName()+2);

            System.out.println(graph);  //toString
            System.out.println("DFS Transversal Tour: "+graph.dfs());
            System.out.println("BFS Transversal Tour: "+graph.bfs());

            //eliminemos vertices
            System.out.println("\nVertex deleted: E");
            graph.removeVertex('E');
            System.out.println("\nVertex deleted: J");
            graph.removeVertex('J');
            System.out.println("\nVertex deleted: I");
            graph.removeVertex('I');
            System.out.println(graph);  //toString
            //eliminamos aristas
            System.out.println("Edge deleted: C---G");
            graph.removeEdge('C', 'G');
            System.out.println("Edge deleted: D---H");
            graph.removeEdge('D', 'H');
            System.out.println("Edge deleted: A---B");
            graph.removeEdge('A', 'B');

        } catch (GraphException | ListException | StackException | QueueException e) {
            throw new RuntimeException(e);
        }
    }
    }
