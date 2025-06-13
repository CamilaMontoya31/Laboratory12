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

   //constantes para dibujar la figura: asi solo llamo a la constante
    private static final double RADIO_VERTICE = 25;
    private static final double TAMANO_PUNTA_FLECHA = 10;
    private static final double ESPACIADO_CIRCULAR = 0.85;
    private static final double RESALTADO_ARISTA = 8.0;
    private static final int NUM_VERTICES = 10;
    private static final int PESO_MIN_MATRIZ = 1;
    private static final int PESO_MAX_MATRIZ = 50;
    private static final int PESO_MIN_LISTA = 51;
    private static final int PESO_MAX_LISTA = 100;
    private static final int PESO_MIN_LISTA_ENLAZADA = 101;
    private static final int PESO_MAX_LISTA_ENLAZADA = 150;
    private static final int MAX_CONEXIONES_POR_VERTICE = 5;

    // --- Clase Interna para la Posición de un Vértice ---
    // Almacena los datos del vértice y su posición en el lienzo para dibujarlo.
    private static class PosicionVertice {
        Object datosVertice;
        Point2D posicion;

        public PosicionVertice(Object datosVertice, Point2D posicion) {
            this.datosVertice = datosVertice;
            this.posicion = posicion;
        }
    }

    // --- Clase Interna para la Información de una Arista Dibujada ---
    // Guarda los detalles de una arista que se ha dibujado en el Canvas,
    // incluyendo sus puntos de inicio y fin ajustados para la interacción.
    private static class InformacionDibujoArista {
        Object origen;
        Object destino;
        int peso;
        Point2D puntoInicio;
        Point2D puntoFin;
        boolean estaSobre;

        public InformacionDibujoArista(Object origen, Object destino, int peso, Point2D puntoInicio, Point2D puntoFin) {
            this.origen = origen;
            this.destino = destino;
            this.peso = peso;
            this.puntoInicio = puntoInicio;
            this.puntoFin = puntoFin;
            this.estaSobre = false;
        }


        public boolean isNear(Point2D mouse, double puntoMax) {

            double minX = Math.min(puntoInicio.getX(), puntoFin.getX()) - puntoMax;
            double maxX = Math.max(puntoInicio.getX(), puntoFin.getX()) + puntoMax;
            double minY = Math.min(puntoInicio.getY(), puntoFin.getY()) - puntoMax;
            double maxY = Math.max(puntoInicio.getY(), puntoFin.getY()) + puntoMax;

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
                return mouse.distance(puntoInicio) <= puntoMax;
            }
            double parametroT = ((x0 - x1) * (x2 - x1) + (y0 - y1) * (y2 - y1)) / longitudCuadrado;

            parametroT = Math.max(0, Math.min(1, parametroT));
            double proyX = x1 + parametroT * (x2 - x1);
            double proyY = y1 + parametroT * (y2 - y1);
            return mouse.distance(proyX, proyY) <= puntoMax;
        }
    }

    private List<PosicionVertice> listaPosicionesVertices;

    private List<InformacionDibujoArista> aristasDibujadas;

    private Object grafoActual;

    private double factorZoomActual = 1.0;
    private static final double INCREMENTO_FACTOR_ZOOM = 1.1;
    private static final double ZOOM_MAXIMO = 3.0;
    private static final double ZOOM_MINIMO = 0.5;


    @FXML
    public void initialize() {

        randomizeButton.setOnAction(e -> generarGrafoAleatorio());
        listaPosicionesVertices = new ArrayList<>();
        aristasDibujadas = new ArrayList<>();

        setupZoom();
        setupInteraccionAristas();


        toStringContentText.setText("");
    }


    private void setupZoom() {

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

    private void setupInteraccionAristas() {
        // Configura el evento de movimiento del ratón sobre el Canvas
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

            if (necesitaRedibujar) {
                if (grafoActual != null) {
                    drawGraph(grafoActual);
                }
            }

            // Actualiza el texto en toStringContentText según la arista actualmente sobre la que está el ratón
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
        // Genera vértices de tipo entero únicos
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

        // Añade los vértices al grafo
        for (Character letra : listaVertices) {
            grafo.addVertex(letra);
        }

        // Añade aristas con pesos aleatorios
        for (int i = 0; i < NUM_VERTICES; i++) {
            int conexiones = random(MAX_CONEXIONES_POR_VERTICE); // Número aleatorio de conexiones (0-4)
            for (int j = 0; j < conexiones; j++) {
                int indiceDestino = random(NUM_VERTICES); // Índice aleatorio para el vértice de destino
                if (indiceDestino != i) { // Evita bucles (aristas de un vértice a sí mismo)
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

        // Selecciona NUM_VERTICES monumentos únicos
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

        double centroX = graphCanvas.getWidth() / 2; // Centro X del Canvas
        double centroY = graphCanvas.getHeight() / 2; // Centro Y del Canvas
        // Calcula el radio del círculo en el que se colocarán los vértices.
        // Se basa en la dimensión más pequeña del Canvas para asegurar que los vértices encajen.
        // Se multiplica por ESPACIADO_CIRCULAR para ajustar el espacio entre los nodos.
        double radio = Math.min(centroX, centroY) * ESPACIADO_CIRCULAR;

        for (int i = 0; i < listaVertices.size(); i++) {
            Object datosVertice = listaVertices.get(i);
            double angulo = 2 * Math.PI * i / listaVertices.size(); // Distribuye los vértices en un círculo
            double x = centroX + radio * Math.cos(angulo); // Posición X del vértice
            double y = centroY + radio * Math.sin(angulo); // Posición Y del vértice
            Point2D posicion = new Point2D(x, y);

            // Almacena la posición del vértice
            listaPosicionesVertices.add(new PosicionVertice(datosVertice, posicion));

            // Dibuja el círculo del vértice
            contextoGrafico.strokeOval(posicion.getX() - RADIO_VERTICE, posicion.getY() - RADIO_VERTICE, RADIO_VERTICE * 2, RADIO_VERTICE * 2);
            contextoGrafico.fillOval(posicion.getX() - RADIO_VERTICE, posicion.getY() - RADIO_VERTICE, RADIO_VERTICE * 2, RADIO_VERTICE * 2);

            // Dibuja la etiqueta del vértice (el texto)
            contextoGrafico.setFill(Color.BLACK); // Establece el color del texto
            String etiquetaVertice = String.valueOf(datosVertice);
            // Abbrevia etiquetas de tipo String muy largas para que quepan
            if (etiquetaVertice.length() > 6 && datosVertice instanceof String) {
                etiquetaVertice = etiquetaVertice.substring(0, 5) + "...";
            }
            contextoGrafico.fillText(etiquetaVertice, posicion.getX(), posicion.getY());
            contextoGrafico.setFill(Color.LIGHTBLUE); // Restaura el color de relleno para los siguientes círculos
        }


        contextoGrafico.setFont(new Font("Arial", 10));

        // --- Dibuja las Aristas ---
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

                                // Crea la información de la arista para la interacción
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
        return null;
    }

    private void drawArrow(GraphicsContext contextoGrafico, double inicioX, double inicioY, double finX, double finY, double radioVertice) {
        double deltaX = finX - inicioX;
        double deltaY = finY - inicioY;
        double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distancia == 0) return; // Evita división por cero para vértices en la misma ubicación

        // Normaliza el vector para obtener un vector unitario
        double unidadX = deltaX / distancia;
        double unidadY = deltaY / distancia;

        // Ajusta los puntos de inicio y fin para dibujar desde/hacia el borde del círculo
        double inicioAjustadoX = inicioX + unidadX * radioVertice;
        double inicioAjustadoY = inicioY + unidadY * radioVertice;
        double finAjustadoX = finX - unidadX * radioVertice;
        double finAjustadoY = finY - unidadY * radioVertice;

        // Dibuja la línea principal de la flecha
        contextoGrafico.strokeLine(inicioAjustadoX, inicioAjustadoY, finAjustadoX, finAjustadoY);

        // Calcula y dibuja la punta de flecha (triángulo)
        double anguloFlecha = Math.atan2(deltaY, deltaX); // Ángulo de la línea de la flecha

        // Calcula los dos puntos para el triángulo de la punta de flecha
        double punto1X = finAjustadoX - TAMANO_PUNTA_FLECHA * Math.cos(anguloFlecha - Math.PI / 6);
        double punto1Y = finAjustadoY - TAMANO_PUNTA_FLECHA * Math.sin(anguloFlecha - Math.PI / 6);
        double punto2X = finAjustadoX - TAMANO_PUNTA_FLECHA * Math.cos(anguloFlecha + Math.PI / 6);
        double punto2Y = finAjustadoY - TAMANO_PUNTA_FLECHA * Math.sin(anguloFlecha + Math.PI / 6);

        // Dibuja el triángulo de la punta de flecha relleno
        contextoGrafico.fillPolygon(new double[]{finAjustadoX, punto1X, punto2X}, new double[]{finAjustadoY, punto1Y, punto2Y}, 3);
    }

}
