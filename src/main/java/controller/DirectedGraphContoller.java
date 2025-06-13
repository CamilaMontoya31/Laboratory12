package controller;


import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Text;

public class DirectedGraphContoller {

    @FXML
    private Text titleText;
    @FXML
    private Button randomizeButton;
    @FXML
    private Button containsVertexButton;
    @FXML
    private Button containsEdgeButton;
    @FXML
    private Button toStringButton;
    @FXML
    private Button bfsTourButton;
    @FXML
    private Button dfsTourButton;
    @FXML
    private RadioButton adjacencyMatrixRadioButton;
    @FXML
    private RadioButton adjacencyListRadioButton;
    @FXML
    private RadioButton linkedListRadioButton;
    @FXML
    private ToggleGroup graphTypeToggleGroup;
    @FXML
    private Text chooseOptionText;
    @FXML
    private Text toStringContentText;
    @FXML
    private Text toStringTitleText;
    @FXML
    private TextArea outputTextArea;
    @FXML
    private Canvas graphCanvas;


    @FXML
    public void initialize() {
        randomizeButton.setOnAction(e -> generarGrafoAleatorio());
    }

    @FXML
    private void generarGrafoAleatorio() {
        if (adjacencyMatrixRadioButton.isSelected()) {
            generarGrafoConMatrizDeAdyacencia();
        } else if (adjacencyListRadioButton.isSelected()) {
            generarGrafoConListaDeAdyacencia();
        } else if (linkedListRadioButton.isSelected()) {
            generarGrafoConListaEnlazadaEspecial();
        } else {
            outputTextArea.setText("Por favor, seleccione un tipo de grafo.");
        }
    }

    private void generarGrafoConMatrizDeAdyacencia() {
        // Ejemplo de generación de grafo con matriz de adyacencia
        outputTextArea.setText("Generando grafo con matriz de adyacencia...");
        // Aquí puedes llamar a tu clase que maneja la lógica de ese tipo de grafo
    }

    private void generarGrafoConListaDeAdyacencia() {
        outputTextArea.setText("Generando grafo con lista de adyacencia...");
        // Aquí puedes llamar a tu clase que maneja la lógica de ese tipo de grafo
    }

    private void generarGrafoConListaEnlazadaEspecial() {
        outputTextArea.setText("Generando grafo con lista enlazada especial...");
        // Aquí puedes llamar a tu clase que maneja la lógica de ese tipo de grafo

    }





}
