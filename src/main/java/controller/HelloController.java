package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import ucr.lab.HelloApplication;

import java.io.IOException;

public class HelloController {
    @FXML
    private Text txtMessage;
    @FXML
    private BorderPane bp;
    @FXML
    private AnchorPane ap;

    public void load(String form) {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(form));
        try {
            this.bp.setCenter(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void Home(ActionEvent actionEvent) {
        this.txtMessage.setText("Laboratory No. 12" +
                "\n Click on the bottons in your left!");
        this.bp.setCenter(ap);
    }

    public void DirectedGraph(ActionEvent actionEvent) {
        load("/ucr/lab/directedGraph.fxml");
    }

    public void Operations(ActionEvent actionEvent) {
        load("/ucr/lab/graphOperation.fxml");
    }

    public void KruskalPrim(ActionEvent actionEvent) {
        load("/ucr/lab/kruskal_prim.fxml");
    }

    public void Dijkstra(ActionEvent actionEvent) {
        load("/ucr/lab/dijkstra.fxml");
    }

    @FXML
    public void Exit(ActionEvent actionEvent) {
        System.exit(0);
    }
}