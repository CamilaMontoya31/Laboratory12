package controller;

import domain.*;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;

import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import java.util.*;
import static util.Utility.random;

public class DirectedGraphContoller {

    @FXML private Text titleText;
    @FXML private Button randomizeButton;
    @FXML private Button containsVertexButton;
    @FXML private Button containsEdgeButton;
    @FXML private Button toStringButton;
    @FXML private Button bfsTourButton;
    @FXML private Button dfsTourButton;
    @FXML private RadioButton adjacencyMatrixRadioButton;
    @FXML private RadioButton adjacencyListRadioButton;
    @FXML private RadioButton linkedListRadioButton;
    @FXML private ToggleGroup graphTypeToggleGroup;
    @FXML private Text chooseOptionText;
    @FXML private Text toStringContentText;
    @FXML private Text toStringTitleText;
    @FXML private TextArea outputTextArea;
    @FXML private Canvas graphCanvas;

    // Solo se llama a las constates cuando se va a usar en drwa
    private static final double RADIO_VERTICE = 25; // Radio del círculo que representa un vértice
    private static final double TAMANO_PUNTA_FLECHA = 10; // Tamaño del triángulo de la punta de flecha
    private static final double FACTOR_ESPACIADO_CIRCULAR = 0.85;
    private static final double RESALTADO_ARISTA = 8.0;
    // por aquello la info:
    private static final int NUM_VERTICES = 10; // Número fijo de vértices en los grafos generados
    private static final int PESO_MIN_MATRIZ = 1; // Peso mínimo para aristas en grafo de matriz
    private static final int PESO_MAX_MATRIZ = 50; // Peso máximo para aristas en grafo de matriz
    private static final int PESO_MIN_LISTA = 51; // Peso mínimo para aristas en grafo de lista
    private static final int PESO_MAX_LISTA = 100; // Peso máximo para aristas en grafo de lista
    private static final int PESO_MIN_LISTA_ENLAZADA = 101; // Peso mínimo para aristas en grafo
    private static final int PESO_MAX_LISTA_ENLAZADA = 150; // Peso máximo para aristas en grafo
    private static final int MAX_CONEXIONES_POR_VERTICE = 5; // Máximo de aristas x vértice

    // --- Clase Interna para la Posición de un Vértice
    // Almacena los datos del vértice y su posición ene canva
    private static class PosicionVertice {
        Object datosVertice; // datos del vertex
        Point2D posicion;   // ya sea pos x o y

        public PosicionVertice(Object datosVertice, Point2D posicion) {
            this.datosVertice = datosVertice;
            this.posicion = posicion;
        }
    }

    // --- Clase Interna para la Información de una Arista
    // Guarda los detalles de una arista que se ha dibujado en el Canva
    private static class InformacionDibujoArista {
        Object origen;      //  origen de la arista
        Object destino;     //  destino de la arista
        int peso;           // Peso
        Point2D puntoInicio; // Punto de inicio ajustado de la línea
        Point2D puntoFin;   // Punto de fin ajustado de la línea
        boolean estaSobre;  // Indica si el ratón está actualmente sobre esta arista

        public InformacionDibujoArista(Object origen, Object destino, int peso, Point2D puntoInicio, Point2D puntoFin) {
            this.origen = origen;
            this.destino = destino;
            this.peso = peso;
            this.puntoInicio = puntoInicio;
            this.puntoFin = puntoFin;
            this.estaSobre = false;
        }

        public boolean isNear(Point2D mouse, double puntLimit) {

            double minX = Math.min(puntoInicio.getX(), puntoFin.getX()) - puntLimit;
            double maxX = Math.max(puntoInicio.getX(), puntoFin.getX()) + puntLimit;
            double minY = Math.min(puntoInicio.getY(), puntoFin.getY()) - puntLimit;
            double maxY = Math.max(puntoInicio.getY(), puntoFin.getY()) + puntLimit;

            if (!((mouse.getX() >= minX && mouse.getX() <= maxX) &&
                    (mouse.getY() >= minY && mouse.getY() <= maxY))) {
                return false;
            }
            double x1 = puntoInicio.getX();
            double y1 = puntoInicio.getY();
            double x2 = puntoFin.getX();
            double y2 = puntoFin.getY();
            double x0 = mouse.getX();
            double y0 = mouse.getY();

            double longitudCuadrado = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
            if (longitudCuadrado == 0.0) {
                return mouse.distance(puntoInicio) <= puntLimit;
            }
            double limite = ((x0 - x1) * (x2 - x1) + (y0 - y1) * (y2 - y1)) / longitudCuadrado;
            limite = Math.max(0, Math.min(1, limite));

            double proyX = x1 + limite * (x2 - x1);
            double proyY = y1 + limite * (y2 - y1);

            return mouse.distance(proyX, proyY) <= puntLimit;
        }
    }

    // Lista para almacenar los datos del vértice y sus pos
    private List<PosicionVertice> listaPosicionesVertices;
    // Lista para almacenar información sobre las aristas
    private List<InformacionDibujoArista> aristasDibujadas;
    // grafo que se ha generado actualmente
    private Object grafoActual;

    // Para zoom
    private double factorZoomActual = 1.0;
    private static final double INCREMENTO_FACTOR_ZOOM = 1.1;
    private static final double ZOOM_MAXIMO = 3.0;
    private static final double ZOOM_MINIMO = 0.5;

// inializa todo
    @FXML
    public void initialize() {

        randomizeButton.setOnAction(e -> generarGrafoAleatorio());
        containsVertexButton.setOnAction(e -> handleContainsVertex());
        containsEdgeButton.setOnAction(e -> handleContainsEdge());
        toStringButton.setOnAction(e -> displayGraphToString());
        bfsTourButton.setOnAction(e -> handleBFSTour());
        dfsTourButton.setOnAction(e -> handleDFSTour());

        listaPosicionesVertices = new ArrayList<>();
        aristasDibujadas = new ArrayList<>(); // Inicializa la lista para las aristas dibujadas
        configZoom();
        setupInteraccionAristas();
        toStringContentText.setText("");
    }

    private void configZoom() {
        graphCanvas.setOnScroll((ScrollEvent evento) -> {
            evento.consume();

            double deltaZoom = evento.getDeltaY() > 0 ? INCREMENTO_FACTOR_ZOOM : 1 / INCREMENTO_FACTOR_ZOOM;
            double nuevoFactorZoom = factorZoomActual * deltaZoom;

            if (nuevoFactorZoom > ZOOM_MAXIMO) nuevoFactorZoom = ZOOM_MAXIMO;
            if (nuevoFactorZoom < ZOOM_MINIMO) nuevoFactorZoom = ZOOM_MINIMO;

            double xRaton = evento.getX();
            double yRaton = evento.getY();

            double factorEscala = nuevoFactorZoom / factorZoomActual;

            graphCanvas.setTranslateX(xRaton - factorEscala * (xRaton - graphCanvas.getTranslateX()));
            graphCanvas.setTranslateY(yRaton - factorEscala * (yRaton - graphCanvas.getTranslateY()));
            graphCanvas.setScaleX(nuevoFactorZoom);
            graphCanvas.setScaleY(nuevoFactorZoom);

            factorZoomActual = nuevoFactorZoom;
        });

        graphCanvas.setOnMousePressed(evento -> {
            if (evento.isMiddleButtonDown()) {
                double inicioX = evento.getSceneX();
                double inicioY = evento.getSceneY();
                double traslacionInicialX = graphCanvas.getTranslateX();
                double traslacionInicialY = graphCanvas.getTranslateY();

                graphCanvas.setOnMouseDragged(eventoArrastre -> {
                    if (eventoArrastre.isMiddleButtonDown()) {

                        double deltaX = eventoArrastre.getSceneX() - inicioX;
                        double deltaY = eventoArrastre.getSceneY() - inicioY;

                        graphCanvas.setTranslateX(traslacionInicialX + deltaX);
                        graphCanvas.setTranslateY(traslacionInicialY + deltaY);
                    }
                });

                graphCanvas.setOnMouseReleased(eventoLiberacion -> {
                    graphCanvas.setOnMouseDragged(null);
                    graphCanvas.setOnMouseReleased(null);
                });
            }
        });
    }
    // conf para aristas
    private void setupInteraccionAristas() {
        graphCanvas.setOnMouseMoved(evento -> {

            double xRatonGrafo = (evento.getX() - graphCanvas.getTranslateX()) / graphCanvas.getScaleX();
            double yRatonGrafo = (evento.getY() - graphCanvas.getTranslateY()) / graphCanvas.getScaleY();
            Point2D puntoRatonGrafo = new Point2D(xRatonGrafo, yRatonGrafo);

            boolean necesitaRedibujar = false; // Bandera para saber si necesitamos redibujar el grafo
            InformacionDibujoArista nuevaAristaSobrepuesta = null; // Para seguir la arista actualmente bajo el ratón

            // Itera sobre todas las aristas dibujadas para ver si el ratón está sobre alguna
            for (InformacionDibujoArista infoArista : aristasDibujadas) {
                boolean ratonSobreEstaArista = infoArista.isNear(puntoRatonGrafo, RESALTADO_ARISTA);

                if (ratonSobreEstaArista) {
                    if (!infoArista.estaSobre) { // Si acaba de pasar a estar sobre la arista
                        infoArista.estaSobre = true;
                        necesitaRedibujar = true; // Marca para redibujar y aplicar el resaltado
                    }
                    nuevaAristaSobrepuesta = infoArista; // Esta es la arista actualmente sobre la que está el ratón
                } else {
                    if (infoArista.estaSobre) { // Si estaba sobre la arista, pero ya no lo está
                        infoArista.estaSobre = false; // Reinicia a no estar sobre ella
                        necesitaRedibujar = true; // Marca para redibujar y quitar el resaltado
                    }
                }
            }
            // Si el estado de resaltado de alguna arista cambió, redibuja
            if (necesitaRedibujar) {
                if (grafoActual != null) {
                    drawGraph(grafoActual);
                }
            }
            // Actualiza el texto
            if (nuevaAristaSobrepuesta != null) {
                toStringContentText.setText("Arista: " + nuevaAristaSobrepuesta.origen + " -> " + nuevaAristaSobrepuesta.destino +
                        " | Peso: " + nuevaAristaSobrepuesta.peso);
            } else {
                // Si no hay ninguna arista sobre la que esté el ratón, limpia el texto
                if (!toStringContentText.getText().isEmpty()) {
                    toStringContentText.setText("");
                }
            }
        });
    }
    //contains
    @FXML
    private void handleContainsVertex() {

        if (grafoActual == null) {
            outputTextArea.setText("Primero selecciona o genera un grafo.");
            return;
        }

        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Verificar Vértice");
        dialogo.setHeaderText("Ingresa el vértice que deseas buscar:");
        dialogo.setContentText("Vértice:");

        Optional<String> resultado = dialogo.showAndWait();
        if (resultado.isPresent()) {
            String entradaVertice = resultado.get().trim();
            try {

                Object verticeBuscado = convertirEntradaAVertice(entradaVertice);

                if (grafoActual instanceof DirectedAdjacencyMatrixGraph matrixGraph) {
                    if (matrixGraph.containsVertex(verticeBuscado)) {
                        outputTextArea.setText("El vértice \"" + entradaVertice + "\" SÍ está en el grafo ✅");
                    } else {
                        outputTextArea.setText("El vértice \"" + entradaVertice + "\" NO está en el grafo ❌");
                    }
                } else if (grafoActual instanceof DirectedAdjacencyListGraph listGraph) {
                    if (listGraph.containsVertex(verticeBuscado)) {
                        outputTextArea.setText("El vértice \"" + entradaVertice + "\" SÍ está en el grafo ✅");
                    } else {
                        outputTextArea.setText("El vértice \"" + entradaVertice + "\" NO está en el grafo ❌");
                    }
                } else if (grafoActual instanceof DirectedSinglyLinkedListGraph linkedGraph) {
                    if (linkedGraph.containsVertex(verticeBuscado)) {
                        outputTextArea.setText("El vértice \"" + entradaVertice + "\" SÍ está en el grafo ✅");
                    } else {
                        outputTextArea.setText("El vértice \"" + entradaVertice + "\" NO está en el grafo ❌");
                    }
                } else {
                    outputTextArea.setText("Tipo de grafo no soportado para esta operación.");
                }

            } catch (IllegalArgumentException e) {
                outputTextArea.setText("Error de entrada: " + e.getMessage());
            } catch (Exception e) {
                outputTextArea.setText("Error al verificar vértice: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    // contains
    @FXML
    private void handleContainsEdge() {

        if (grafoActual == null) {
            outputTextArea.setText("Primero selecciona o genera un grafo.");
            return;
        }
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Verificar Arista");
        dialogo.setHeaderText("Ingresa los vértices de la arista (origen, destino):");
        dialogo.setContentText("Vértices (ejemplo: 10 , 20):");

        Optional<String> resultado = dialogo.showAndWait();
        if (resultado.isPresent()) {
            String[] partes = resultado.get().split(",");
            if (partes.length != 2) {
                outputTextArea.setText("Formato inválido. Ingresa: Origen , Destino");
                return;
            }
            String textoOrigen = partes[0].trim();
            String textoDestino = partes[1].trim();
            try {

                Object origenBuscado = convertirEntradaAVertice(textoOrigen);
                Object destinoBuscado = convertirEntradaAVertice(textoDestino);

                boolean aristaEncontrada = false;
                if (grafoActual instanceof DirectedAdjacencyMatrixGraph matrixGraph) {
                    aristaEncontrada = matrixGraph.containsEdge(origenBuscado, destinoBuscado);
                } else if (grafoActual instanceof DirectedAdjacencyListGraph listGraph) {
                    aristaEncontrada = listGraph.containsEdge(origenBuscado, destinoBuscado);
                } else if (grafoActual instanceof DirectedSinglyLinkedListGraph linkedGraph) {
                    aristaEncontrada = linkedGraph.containsEdge(origenBuscado, destinoBuscado);
                } else {
                    outputTextArea.setText("Tipo de grafo no valido");
                    return;
                }

                if (aristaEncontrada) {
                    outputTextArea.setText("La arista \"" + textoOrigen + " -> " + textoDestino + "\" SÍ está en el grafo ✅");
                } else {
                    outputTextArea.setText("La arista \"" + textoOrigen + " -> " + textoDestino + "\" NO está en el grafo ❌");
                }
            } catch (IllegalArgumentException e) {

                outputTextArea.setText("Error de entrada: " + e.getMessage());
            } catch (Exception e) {

                outputTextArea.setText("Error al verificar arista: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    // para el toStribng
    @FXML
    private void displayGraphToString() {
        if (grafoActual == null) {
            outputTextArea.setText("Primero selecciona o genera un grafo.");
            return;
        }

        outputTextArea.setText(grafoActual.toString());
    }

   // recorrido
    @FXML
    private void handleBFSTour() {
        if (grafoActual == null) {
            outputTextArea.setText("Primero selecciona o genera un grafo para realizar el recorrido BFS.");
            return;
        }

        TextInputDialog dialogoInicio = new TextInputDialog();
        dialogoInicio.setTitle("Recorrido BFS");
        dialogoInicio.setHeaderText("Ingresa el inicio para el recorrido BFS:");
        dialogoInicio.setContentText("Vértice de Inicio:");

        Optional<String> resultadoInicio = dialogoInicio.showAndWait();
        if (resultadoInicio.isPresent()) {
            String entradaInicio = resultadoInicio.get().trim();
            try {
                Object verticeInicio = convertirEntradaAVertice(entradaInicio);

                Queue<Object> cola = new LinkedList<>();

                Set<Object> visitados = new HashSet<>();

                StringBuilder resultadoRecorrido = new StringBuilder("Recorrido BFS: ");

                boolean verticeExiste = false;
                if (grafoActual instanceof DirectedAdjacencyMatrixGraph matrixGraph) {
                    verticeExiste = matrixGraph.containsVertex(verticeInicio);
                } else if (grafoActual instanceof DirectedAdjacencyListGraph listGraph) {
                    verticeExiste = listGraph.containsVertex(verticeInicio);
                } else if (grafoActual instanceof DirectedSinglyLinkedListGraph linkedGraph) {
                    verticeExiste = linkedGraph.containsVertex(verticeInicio);
                }

                if (!verticeExiste) {
                    outputTextArea.setText("Error: El vértice de inicio '" + entradaInicio + "' no existe en el grafo.");
                    return;
                }

                cola.add(verticeInicio);
                visitados.add(verticeInicio);
                resultadoRecorrido.append(verticeInicio);
                while (!cola.isEmpty()) {
                    Object verticeActual = cola.poll();

                    List<Object> vecinos = obtenerVecinosDeVertice(grafoActual, verticeActual);

                    for (Object vecino : vecinos) {

                        if (!visitados.contains(vecino)) {
                            visitados.add(vecino);
                            cola.add(vecino);
                            resultadoRecorrido.append(" -> ").append(vecino);
                        }
                    }
                }
                outputTextArea.setText(resultadoRecorrido.toString());

            } catch (IllegalArgumentException e) {
                outputTextArea.setText("Error de entrada para BFS: " + e.getMessage());
            } catch (Exception e) {
                outputTextArea.setText("Error al realizar el recorrido BFS: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
// recorrido
    @FXML
    private void handleDFSTour() {
        if (grafoActual == null) {
            outputTextArea.setText("Primero selecciona o genera un grafo para realizar el recorrido DFS.");
            return;
        }

        TextInputDialog dialogoInicio = new TextInputDialog();
        dialogoInicio.setTitle("Recorrido DFS");
        dialogoInicio.setHeaderText("Ingresa el inicio para el recorrido DFS:");
        dialogoInicio.setContentText("Vértice de Inicio:");

        Optional<String> resultadoInicio = dialogoInicio.showAndWait();
        if (resultadoInicio.isPresent()) {
            String entradaInicio = resultadoInicio.get().trim();
            try {
                Object verticeInicio = convertirEntradaAVertice(entradaInicio);


                Stack<Object> pila = new Stack<>();

                Set<Object> visitados = new HashSet<>();
                StringBuilder resultadoRecorrido = new StringBuilder("Recorrido DFS: ");


                boolean verticeExiste = false;
                if (grafoActual instanceof DirectedAdjacencyMatrixGraph matrixGraph) {
                    verticeExiste = matrixGraph.containsVertex(verticeInicio);
                } else if (grafoActual instanceof DirectedAdjacencyListGraph listGraph) {
                    verticeExiste = listGraph.containsVertex(verticeInicio);
                } else if (grafoActual instanceof DirectedSinglyLinkedListGraph linkedGraph) {
                    verticeExiste = linkedGraph.containsVertex(verticeInicio);
                }

                if (!verticeExiste) {
                    outputTextArea.setText("Error: El vértice de inicio '" + entradaInicio + "' no existe en el grafo.");
                    return;
                }
                pila.push(verticeInicio);

                while (!pila.isEmpty()) {
                    Object verticeActual = pila.pop();

                    // Si el vértice no ha sido visitado
                    if (!visitados.contains(verticeActual)) {
                        visitados.add(verticeActual); // Márcalo como visitado
                        resultadoRecorrido.append(verticeActual).append(" -> ");

                        List<Object> vecinos = obtenerVecinosDeVertice(grafoActual, verticeActual);
                        Collections.reverse(vecinos);
                        for (Object vecino : vecinos) {
                            if (!visitados.contains(vecino)) {
                                pila.push(vecino);
                            }
                        }
                    }
                }
                if (resultadoRecorrido.length() > "Recorrido DFS: ".length()) {
                    resultadoRecorrido.setLength(resultadoRecorrido.length() - 4); // Quita el último " -> "
                }
                outputTextArea.setText(resultadoRecorrido.toString());

            } catch (IllegalArgumentException e) {
                outputTextArea.setText("Error de entrada para DFS: " + e.getMessage());
            } catch (Exception e) {
                outputTextArea.setText("Error al realizar el recorrido DFS: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private List<Object> obtenerVecinosDeVertice(Object grafo, Object vertice) throws Exception {
        List<Object> vecinos = new ArrayList<>();
        if (grafo instanceof DirectedAdjacencyMatrixGraph grafoMatriz) {
            List<Object> todosLosVertices = grafoMatriz.getVertices();

            for (Object posibleVecino : todosLosVertices) {

                if (grafoMatriz.getWeight(vertice, posibleVecino) != 0) {
                    vecinos.add(posibleVecino);
                }
            }
        } else if (grafo instanceof DirectedAdjacencyListGraph grafoLista) {

            for (DirectedAdjacencyListGraph.EdgeInfo arista : grafoLista.getEdges()) {
                if (Objects.equals(arista.source, vertice)) {
                    vecinos.add(arista.destination);
                }
            }
        } else if (grafo instanceof DirectedSinglyLinkedListGraph grafoEnlazado) {
            for (DirectedSinglyLinkedListGraph.EdgeInfo arista : grafoEnlazado.getEdges()) {
                if (Objects.equals(arista.source, vertice)) {
                    vecinos.add(arista.destination);
                }
            }
        }
        return vecinos;
    }

    private void generarGrafoAleatorio() {
        try {
            if (adjacencyMatrixRadioButton.isSelected()) {
                grafoActual = new DirectedAdjacencyMatrixGraph(NUM_VERTICES);
                generarGrafoConMatrizDeAdyacencia((DirectedAdjacencyMatrixGraph) grafoActual);
            } else if (adjacencyListRadioButton.isSelected()) {
                grafoActual = new DirectedAdjacencyListGraph(NUM_VERTICES);
                generarGrafoConListaDeAdyacencia((DirectedAdjacencyListGraph) grafoActual);
            } else if (linkedListRadioButton.isSelected()) {
                grafoActual = new DirectedSinglyLinkedListGraph();
                generarGrafoConListaEnlazada((DirectedSinglyLinkedListGraph) grafoActual);
            } else {
                outputTextArea.setText("Por favor, selecciona un tipo de grafo dirigido para generar.");
                grafoActual = null;
            }

            if (grafoActual != null) {
                // Reinicia el zoom y el paneo cuando se genera un nuevo grafo
                graphCanvas.setScaleX(1.0);
                graphCanvas.setScaleY(1.0);
                graphCanvas.setTranslateX(0);
                graphCanvas.setTranslateY(0);
                factorZoomActual = 1.0;
                drawGraph(grafoActual); // Dibuja el grafo generado en el Canvas
            }
        } catch (Exception e) {
            outputTextArea.setText("Error al generar el grafo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void generarGrafoConMatrizDeAdyacencia(DirectedAdjacencyMatrixGraph grafo) throws Exception {
        List<Integer> listaVertices = new ArrayList<>();
        while (listaVertices.size() < NUM_VERTICES) {
            int num = random(100); // Números entre 0 y 99
            if (!listaVertices.contains(num)) {
                listaVertices.add(num);
            }
        }
        // Añade los vértices al grafo
        for (Integer num : listaVertices) {
            grafo.addVertex(num);
        }

        // Añade aristas con pesos aleatorios
        for (int i = 0; i < NUM_VERTICES; i++) {
            for (int j = 0; j < NUM_VERTICES; j++) {
                if (i != j && random(2) == 1) { // 50% de probabilidad de añadir una arista, evitando bucles
                    int peso = random(PESO_MAX_MATRIZ - PESO_MIN_MATRIZ + 1) + PESO_MIN_MATRIZ;
                    grafo.addEdgeWeight(listaVertices.get(i), listaVertices.get(j), peso);
                }
            }
        }
        outputTextArea.setText(grafo.toString());
    }

    private void generarGrafoConListaDeAdyacencia(DirectedAdjacencyListGraph grafo) throws Exception {
        // Genera vértices de tipo caracter únicos
        List<Character> listaVertices = new ArrayList<>();
        while (listaVertices.size() < NUM_VERTICES) {
            char letra = (char) ('A' + random(26)); // Letras de 'A' a 'Z'
            if (!listaVertices.contains(letra)) {
                listaVertices.add(letra);
            }
        }

        for (Character letra : listaVertices) {
            grafo.addVertex(letra);
        }

        for (int i = 0; i < NUM_VERTICES; i++) {
            int conexiones = random(MAX_CONEXIONES_POR_VERTICE); // Número aleatorio de conexiones (0-4)
            for (int j = 0; j < conexiones; j++) {
                int indiceDestino = random(NUM_VERTICES); // Índice aleatorio para el vértice de destino
                if (indiceDestino != i) {
                    int peso = random(PESO_MAX_LISTA - PESO_MIN_LISTA + 1) + PESO_MIN_LISTA;
                    grafo.addEdgeWeight(listaVertices.get(i), listaVertices.get(indiceDestino), peso);
                }
            }
        }
        outputTextArea.setText(grafo.toString());
    }

    private void generarGrafoConListaEnlazada(DirectedSinglyLinkedListGraph grafo) throws Exception {
        // Lista predefinida de monumentos para elegir
        String[] todosLosMonumentos = {
                "Coliseo", "Taj Mahal", "Machu Picchu", "Gran Muralla", "Petra",
                "Cristo Redentor", "Chichen Itza", "Stonehenge", "Torre Eiffel", "Partenón",
                "Pirámides de Giza", "Ópera de Sídney", "Estatua de la Libertad", "Big Ben", "Kremlin"
        };
        List<String> monumentosMezclados = new ArrayList<>(Arrays.asList(todosLosMonumentos));
        Collections.shuffle(monumentosMezclados); // Mezcla para seleccionar monumentos únicos aleatorios

        List<String> verticesSeleccionados = new ArrayList<>();
        for (int i = 0; i < NUM_VERTICES; i++) {
            verticesSeleccionados.add(monumentosMezclados.get(i));
        }

        // Añade los vértices al grafo
        for (String monumento : verticesSeleccionados) {
            grafo.addVertex(monumento);
        }

        // Añade aristas con pesos aleatorios
        for (int i = 0; i < verticesSeleccionados.size(); i++) {
            int conexiones = random(MAX_CONEXIONES_POR_VERTICE);
            for (int j = 0; j < conexiones; j++) {
                int indiceDestino = random(verticesSeleccionados.size());
                if (indiceDestino != i) { // Evita bucles
                    int peso = random(PESO_MAX_LISTA_ENLAZADA - PESO_MIN_LISTA_ENLAZADA + 1) + PESO_MIN_LISTA_ENLAZADA;
                    grafo.addEdgeWeight(verticesSeleccionados.get(i), verticesSeleccionados.get(indiceDestino), peso);
                }
            }
        }
        outputTextArea.setText(grafo.toString());
    }

    private void drawGraph(Object grafo) {
        GraphicsContext contextoGrafico = graphCanvas.getGraphicsContext2D();
        contextoGrafico.clearRect(0, 0, graphCanvas.getWidth(), graphCanvas.getHeight()); // Limpia el Canvas
        listaPosicionesVertices.clear(); // Limpia las posiciones de vértices anteriores


        List<InformacionDibujoArista> aristasDibujadasAnteriores = new ArrayList<>(aristasDibujadas); // Copia el estado actual
        aristasDibujadas.clear(); // Ahora sí, limpia para el nuevo ciclo de dibujo

        //  Dibujar Vértices
        contextoGrafico.setStroke(Color.BLACK); // Color del borde del vértice
        contextoGrafico.setLineWidth(1.5); // Grosor del borde
        contextoGrafico.setFill(Color.LIGHTBLUE); // Color de relleno del vértice
        contextoGrafico.setFont(new Font("Arial", 12)); // Fuente del texto del vértice
        contextoGrafico.setTextAlign(TextAlignment.CENTER); // Alineación del texto
        contextoGrafico.setTextBaseline(VPos.CENTER); // Línea base del texto

        List<Object> listaVertices = null; // Lista para contener los datos de los vértices del grafo

        // Obtiene los vértices del grafo según su tipo
        try {
            if (grafo instanceof DirectedAdjacencyMatrixGraph grafoMatriz) {
                listaVertices = grafoMatriz.getVertices();
            } else if (grafo instanceof DirectedAdjacencyListGraph grafoLista) {
                listaVertices = grafoLista.getVertices();
            } else if (grafo instanceof DirectedSinglyLinkedListGraph grafoEnlazado) {
                listaVertices = grafoEnlazado.getVertices();
            }
        } catch (Exception e) {
            outputTextArea.setText("Error al obtener los vértices del grafo: " + e.getMessage());
            e.printStackTrace();
            return; // Sale si no se pueden obtener los vértices
        }

        if (listaVertices == null || listaVertices.isEmpty()) {
            outputTextArea.setText("No hay vértices para dibujar en el grafo.");
            return; // No hay vértices para dibujar
        }

        // Posiciones de los Vértices
        double centroX = graphCanvas.getWidth() / 2; // Centro X del Canvas
        double centroY = graphCanvas.getHeight() / 2; // Centro Y del Canvas
        double radio = Math.min(centroX, centroY) * FACTOR_ESPACIADO_CIRCULAR;

        for (int i = 0; i < listaVertices.size(); i++) {
            Object datosVertice = listaVertices.get(i);
            double angulo = 2 * Math.PI * i / listaVertices.size(); // Distribuye los vértices en un círculo
            double x = centroX + radio * Math.cos(angulo); // Posición X del vértice
            double y = centroY + radio * Math.sin(angulo); // Posición Y del vértice
            Point2D posicion = new Point2D(x, y);

            listaPosicionesVertices.add(new PosicionVertice(datosVertice, posicion));

            contextoGrafico.strokeOval(posicion.getX() - RADIO_VERTICE, posicion.getY() - RADIO_VERTICE, RADIO_VERTICE * 2, RADIO_VERTICE * 2);
            contextoGrafico.fillOval(posicion.getX() - RADIO_VERTICE, posicion.getY() - RADIO_VERTICE, RADIO_VERTICE * 2, RADIO_VERTICE * 2);

            contextoGrafico.setFill(Color.BLACK); // Establece el color del texto
            String etiquetaVertice = String.valueOf(datosVertice);
            // Abbrevia etiquetas de tipo String muy largas para que quepan
            if (etiquetaVertice.length() > 6 && datosVertice instanceof String) {
                etiquetaVertice = etiquetaVertice.substring(0, 5) + "...";
            }
            contextoGrafico.fillText(etiquetaVertice, posicion.getX(), posicion.getY());
            contextoGrafico.setFill(Color.LIGHTBLUE); // Restaura el color de relleno para los siguientes círculos
        }

        // Dibujar Aristas
        contextoGrafico.setFont(new Font("Arial", 10)); // Fuente para los pesos de las aristas (aunque no se dibujen)

        try {
            if (grafo instanceof DirectedAdjacencyMatrixGraph grafoMatriz) {
                List<Object> verticesMatriz = grafoMatriz.getVertices();
                for (int i = 0; i < verticesMatriz.size(); i++) {
                    for (int j = 0; j < verticesMatriz.size(); j++) {
                        Object datosVerticeOrigen = verticesMatriz.get(i);
                        Object datosVerticeDestino = verticesMatriz.get(j);

                        int peso = grafoMatriz.getWeight(datosVerticeOrigen, datosVerticeDestino);
                        if (peso != 0) { // Suponiendo que 0 significa "no hay arista"
                            Point2D inicio = findVertexPosition(datosVerticeOrigen);
                            Point2D fin = findVertexPosition(datosVerticeDestino);
                            if (inicio != null && fin != null) {
                                // Calcula los puntos ajustados para la flecha (desde/hacia el borde del círculo)
                                double deltaX = fin.getX() - inicio.getX();
                                double deltaY = fin.getY() - inicio.getY();
                                double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                                if (distancia == 0) continue; // Evita división por cero
                                double unidadX = deltaX / distancia;
                                double unidadY = deltaY / distancia;

                                double inicioAjustadoX = inicio.getX() + unidadX * RADIO_VERTICE;
                                double inicioAjustadoY = inicio.getY() + unidadY * RADIO_VERTICE;
                                double finAjustadoX = fin.getX() - unidadX * RADIO_VERTICE;
                                double finAjustadoY = fin.getY() - unidadY * RADIO_VERTICE;

                                InformacionDibujoArista infoArista = new InformacionDibujoArista(datosVerticeOrigen, datosVerticeDestino, peso,
                                        new Point2D(inicioAjustadoX, inicioAjustadoY),
                                        new Point2D(finAjustadoX, finAjustadoY));
                                // Mantiene el estado de resaltado si esta arista ya estaba resaltada antes de redibujar
                                for (InformacionDibujoArista aristaExistente : aristasDibujadasAnteriores) {
                                    // Compara usando Objects.equals para manejar nulos y diferentes tipos de objetos correctamente
                                    if (Objects.equals(aristaExistente.origen, infoArista.origen) &&
                                            Objects.equals(aristaExistente.destino, infoArista.destino) &&
                                            aristaExistente.peso == infoArista.peso) {
                                        infoArista.estaSobre = aristaExistente.estaSobre;
                                        break;
                                    }
                                }
                                aristasDibujadas.add(infoArista); // Añade a la nueva lista de aristas dibujadas

                                // Establece las propiedades de dibujo (color y grosor) según el estado de resaltado actual
                                if (infoArista.estaSobre) {
                                    contextoGrafico.setStroke(Color.RED); // Resalta en rojo
                                    contextoGrafico.setLineWidth(5.0); // Más grueso
                                } else {
                                    contextoGrafico.setStroke(Color.DARKGRAY); // Color normal
                                    contextoGrafico.setLineWidth(1.0); // Grosor normal
                                }

                                drawArrow(contextoGrafico, inicio.getX(), inicio.getY(), fin.getX(), fin.getY(), RADIO_VERTICE);
                                // Los pesos no se dibujan en las aristas
                            }
                        }
                    }
                }
            } else if (grafo instanceof DirectedAdjacencyListGraph grafoLista) {
                List<DirectedAdjacencyListGraph.EdgeInfo> aristas = grafoLista.getEdges();
                for (DirectedAdjacencyListGraph.EdgeInfo arista : aristas) {
                    Point2D inicio = findVertexPosition(arista.source);
                    Point2D fin = findVertexPosition(arista.destination);
                    if (inicio != null && fin != null) {
                        // Calcula los puntos ajustados para la flecha
                        double deltaX = fin.getX() - inicio.getX();
                        double deltaY = fin.getY() - inicio.getY();
                        double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                        if (distancia == 0) continue;
                        double unidadX = deltaX / distancia;
                        double unidadY = deltaY / distancia;

                        double inicioAjustadoX = inicio.getX() + unidadX * RADIO_VERTICE;
                        double inicioAjustadoY = inicio.getY() + unidadY * RADIO_VERTICE;
                        double finAjustadoX = fin.getX() - unidadX * RADIO_VERTICE;
                        double finAjustadoY = fin.getY() - unidadY * RADIO_VERTICE;

                        // Crea la información de la arista para la interacción
                        InformacionDibujoArista infoArista = new InformacionDibujoArista(arista.source, arista.destination, arista.weight,
                                new Point2D(inicioAjustadoX, inicioAjustadoY),
                                new Point2D(finAjustadoX, finAjustadoY));
                        // Mantiene el estado de resaltado
                        for (InformacionDibujoArista aristaExistente : aristasDibujadasAnteriores) {
                            if (Objects.equals(aristaExistente.origen, infoArista.origen) &&
                                    Objects.equals(aristaExistente.destino, infoArista.destino) &&
                                    aristaExistente.peso == infoArista.peso) {
                                infoArista.estaSobre = aristaExistente.estaSobre;
                                break;
                            }
                        }
                        aristasDibujadas.add(infoArista);

                        // Establece las propiedades de dibujo según el estado de resaltado
                        if (infoArista.estaSobre) {
                            contextoGrafico.setStroke(Color.RED);
                            contextoGrafico.setLineWidth(5.0);
                        } else {
                            contextoGrafico.setStroke(Color.DARKGRAY);
                            contextoGrafico.setLineWidth(1.0);
                        }

                        drawArrow(contextoGrafico, inicio.getX(), inicio.getY(), fin.getX(), fin.getY(), RADIO_VERTICE);
                        // Los pesos no se dibujan
                    }
                }
            } else if (grafo instanceof DirectedSinglyLinkedListGraph grafoEnlazado) {
                List<DirectedSinglyLinkedListGraph.EdgeInfo> aristas = grafoEnlazado.getEdges();
                for (DirectedSinglyLinkedListGraph.EdgeInfo arista : aristas) {
                    Point2D inicio = findVertexPosition(arista.source);
                    Point2D fin = findVertexPosition(arista.destination);
                    if (inicio != null && fin != null) {
                        // Calcula los puntos ajustados para la flecha
                        double deltaX = fin.getX() - inicio.getX();
                        double deltaY = fin.getY() - inicio.getY();
                        double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                        if (distancia == 0) continue;
                        double unidadX = deltaX / distancia;
                        double unidadY = deltaY / distancia;

                        double inicioAjustadoX = inicio.getX() + unidadX * RADIO_VERTICE;
                        double inicioAjustadoY = inicio.getY() + unidadY * RADIO_VERTICE;
                        double finAjustadoX = fin.getX() - unidadX * RADIO_VERTICE;
                        double finAjustadoY = fin.getY() - unidadY * RADIO_VERTICE;

                        // Crea la información de la arista para la interacción
                        InformacionDibujoArista infoArista = new InformacionDibujoArista(arista.source, arista.destination, arista.weight,
                                new Point2D(inicioAjustadoX, inicioAjustadoY),
                                new Point2D(finAjustadoX, finAjustadoY));
                        // Mantiene el estado de resaltado
                        for (InformacionDibujoArista aristaExistente : aristasDibujadasAnteriores) {
                            if (Objects.equals(aristaExistente.origen, infoArista.origen) &&
                                    Objects.equals(aristaExistente.destino, infoArista.destino) &&
                                    aristaExistente.peso == infoArista.peso) {
                                infoArista.estaSobre = aristaExistente.estaSobre;
                                break;
                            }
                        }
                        aristasDibujadas.add(infoArista);
                        if (infoArista.estaSobre) {
                            contextoGrafico.setStroke(Color.RED);
                            contextoGrafico.setLineWidth(5.0);
                        } else {
                            contextoGrafico.setStroke(Color.DARKGRAY);
                            contextoGrafico.setLineWidth(1.0);
                        }

                        drawArrow(contextoGrafico, inicio.getX(), inicio.getY(), fin.getX(), fin.getY(), RADIO_VERTICE);
                        // Los pesos no se dibujan
                    }
                }
            }
        } catch (Exception e) {
            outputTextArea.setText("Error al dibujar las aristas del grafo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Point2D findVertexPosition(Object datosVertice) {
        for (PosicionVertice pv : listaPosicionesVertices) {
            if (util.Utility.compare(pv.datosVertice, datosVertice) == 0) {
                return pv.posicion;
            }
        }
        return null; // Vértice no encontrado
    }

    private void drawArrow(GraphicsContext contextoGrafico, double inicioX, double inicioY, double finX, double finY, double radioVertice) {
        double deltaX = finX - inicioX;
        double deltaY = finY - inicioY;
        double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distancia == 0) return;

        double unidadX = deltaX / distancia;
        double unidadY = deltaY / distancia;
        double inicioAjustadoX = inicioX + unidadX * radioVertice;
        double inicioAjustadoY = inicioY + unidadY * radioVertice;
        double finAjustadoX = finX - unidadX * radioVertice;
        double finAjustadoY = finY - unidadY * radioVertice;

        contextoGrafico.strokeLine(inicioAjustadoX, inicioAjustadoY, finAjustadoX, finAjustadoY);

        double anguloFlecha = Math.atan2(deltaY, deltaX); // Ángulo de la línea de la flecha

        double punto1X = finAjustadoX - TAMANO_PUNTA_FLECHA * Math.cos(anguloFlecha - Math.PI / 6);
        double punto1Y = finAjustadoY - TAMANO_PUNTA_FLECHA * Math.sin(anguloFlecha - Math.PI / 6);
        double punto2X = finAjustadoX - TAMANO_PUNTA_FLECHA * Math.cos(anguloFlecha + Math.PI / 6);
        double punto2Y = finAjustadoY - TAMANO_PUNTA_FLECHA * Math.sin(anguloFlecha + Math.PI / 6);

        contextoGrafico.fillPolygon(new double[]{finAjustadoX, punto1X, punto2X}, new double[]{finAjustadoY, punto1Y, punto2Y}, 3);
    }


    private Object convertirEntradaAVertice(String entrada) throws IllegalArgumentException {
        if (grafoActual instanceof DirectedAdjacencyMatrixGraph) {
            try {
                return Integer.parseInt(entrada); // entero
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El vértice debe ser un NÚMERO ENTERO para el grafo de Matriz de Adyacencia.");
            }
        } else if (grafoActual instanceof DirectedAdjacencyListGraph) {
            if (entrada.length() == 1) {
                return entrada.charAt(0); // letras una por una
            } else {
                throw new IllegalArgumentException("El vértice debe ser UN SOLO CARÁCTER para el grafo de Lista de Adyacencia.");
            }
        } else if (grafoActual instanceof DirectedSinglyLinkedListGraph) {
            return entrada; // cadena texto
        }
        throw new IllegalArgumentException("Tipo de grafo no reconocido para la conversión de vértice.");
    }
}
