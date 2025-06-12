package domain;

import domain.list.ListException;
import domain.queue.QueueException;
import domain.stack.StackException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectedAdjacencyMatrixGraphTest {

    @Test
    void test() {
        try {
            DirectedAdjacencyMatrixGraph graph = new DirectedAdjacencyMatrixGraph(50);

            graph.addVertex('1');
            graph.addVertex('2');
            graph.addVertex('3');
            graph.addVertex('4');
            graph.addVertex('5');

            graph.addEdgeWeight('1', '1', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('2', '2', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('3', '3', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('4', '4', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('5', '5', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('1', '2', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('2', '1', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('4', '2', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('5', '4', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('5', '1', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('1', '3', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('1', '4', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('4', '3', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('3', '4', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('4', '5', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('1', '5', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('5', '1', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('2', '4', util.Utility.random(50,300)+2);
            graph.addEdgeWeight('5', '4', util.Utility.random(50,300)+2);


            System.out.println(graph);  //toString
            System.out.println("DFS Transversal Tour: "+graph.dfs());
            System.out.println("BFS Transversal Tour: "+graph.bfs());

            //eliminemos vertices
            System.out.println("\nVertex deleted: 1");
            graph.removeVertex('1');
            System.out.println("\nVertex deleted: 3");
            graph.removeVertex('3');
            System.out.println("\nVertex deleted: 4");
            graph.removeVertex('4');
            System.out.println(graph);  //toString

            //remove todas las aristas de 2
            System.out.println("Edge deleted: 2---2" );
            graph.removeEdge('2', '2');
            System.out.println("Edge deleted: 2---1" );
            graph.removeEdge('2', '1');
            System.out.println("Edge deleted: 1---2" );
            graph.removeEdge('1', '2');
            System.out.println("Edge deleted: 2---4" );
            graph.removeEdge('2', '4');
            System.out.println("Edge deleted: 4---2" );
            graph.removeEdge('4', '2');
            System.out.println("Edge deleted: 4---5");
            //remove todas las aristas de 5
            System.out.println("Edge deleted: 5---5");
            graph.removeEdge('5', '5');
            System.out.println("Edge deleted: 5---1");
            graph.removeEdge('5', '1');
            System.out.println("Edge deleted: 1---5");
            graph.removeEdge('1', '5');
            System.out.println("Edge deleted: 5---4");
            graph.removeEdge('5', '4');
            System.out.println("Edge deleted: 4---5");
            graph.removeEdge('4', '5');
            System.out.println(graph);  //toString

        } catch (GraphException | ListException | StackException | QueueException e) {
            throw new RuntimeException(e);
        }
    }
    }
