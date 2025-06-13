package util;

import domain.EdgeWeight;
import domain.Graph;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import ucr.lab.HelloApplication;

import javafx.geometry.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

public class FXUtility {

    public static void loadPage(String className, String page, BorderPane bp) {
        try {
            Class cl = Class.forName(className);
            FXMLLoader fxmlLoader = new FXMLLoader(cl.getResource(page));
            cl.getResource("bp");
            bp.setCenter(fxmlLoader.load());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Alert alert(String title, String headerText){
        Alert myalert = new Alert(Alert.AlertType.INFORMATION);
        myalert.setTitle(title);
        myalert.setHeaderText(headerText);
        DialogPane dialogPane = myalert.getDialogPane();
        String css = HelloApplication.class.getResource("dialog.css").toExternalForm();
        dialogPane.getStylesheets().add(css);
        dialogPane.getStyleClass().add("myDialog");
        return myalert;
    }

    public static TextInputDialog dialog(String title, String headerText){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        //String css = HelloApplication.class.getResource("moderna.css").toExternalForm();
        //dialog.getEditor().getStylesheets().add(css);
        return dialog;
    }

    public static String alertYesNo(String title, String headerText, String contextText){
        Alert myalert = new Alert(Alert.AlertType.CONFIRMATION);
        myalert.setTitle(title);
        myalert.setHeaderText(headerText);
        myalert.setContentText(contextText);
        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        myalert.getDialogPane().getButtonTypes().clear(); //quita los botones defaults
        myalert.getDialogPane().getButtonTypes().add(buttonTypeYes);
        myalert.getDialogPane().getButtonTypes().add(buttonTypeNo);
        //dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        DialogPane dialogPane = myalert.getDialogPane();
        String css = HelloApplication.class.getResource("dialog.css").toExternalForm();
        dialogPane.getStylesheets().add(css);
        Optional<ButtonType> result = myalert.showAndWait();
        //if((result.isPresent())&&(result.get()== ButtonType.OK)) {
        if((result.isPresent())&&(result.get()== buttonTypeYes))
            return "YES";
        else return "NO";
    }

    //Dibuja en el pane un grafo//
    public static void drawGraph(Graph graph, Pane pane,
                                 Supplier<List<Object>> vertices,
                                 Function<Object, List<EdgeWeight>> edges,
                                 List<Utility.Edge> mstEdges) {
        pane.getChildren().clear();

        List<Object> vertexList = vertices.get();
        int radius = 100;
        double centerX = pane.getWidth() / 2;
        double centerY = pane.getHeight() / 2;
        int n = vertexList.size();

        Map<Object, Circle> vertexCircles = new HashMap<>();
        Map<Object, Point2D> vertexPositions = new HashMap<>();

        // Calcular posiciones circulares
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            Object v = vertexList.get(i);
            vertexPositions.put(v, new Point2D(x, y));

            Circle circle = new Circle(x, y, 18);
            circle.setFill(Color.LIGHTBLUE);
            circle.setStroke(Color.DARKBLUE);

            Text label = new Text(x - 10, y + 5, v.toString());

            vertexCircles.put(v, circle);
            pane.getChildren().addAll(circle, label);
        }

        // Dibujar aristas
        Set<String> drawnEdges = new HashSet<>();

        for (Object from : vertexList) {
            Point2D fromPos = vertexPositions.get(from);
            for (EdgeWeight edge : edges.apply(from)) {
                Object to = edge.getTo();
                Point2D toPos = vertexPositions.get(to);

                // Evitar duplicados (no dirigidos)
                String edgeId = from.toString() + "-" + to.toString();
                String reverseId = to.toString() + "-" + from.toString();
                if (drawnEdges.contains(edgeId) || drawnEdges.contains(reverseId)) continue;
                drawnEdges.add(edgeId);

                // Verificar si es parte del MST
                boolean isInMST = false;
                if (mstEdges != null) {
                    for (Utility.Edge mstEdge : mstEdges) {
                        if ((mstEdge.origen.equals(from) && mstEdge.destino.equals(to)) ||
                                (mstEdge.origen.equals(to) && mstEdge.destino.equals(from))) {
                            isInMST = true;
                            break;
                        }
                    }
                }

                Line line = new Line(fromPos.getX(), fromPos.getY(), toPos.getX(), toPos.getY());
                line.setStroke(isInMST ? Color.RED : Color.GRAY);
                line.setStrokeWidth(isInMST ? 3.5 : 1.5);

                // Mostrar peso
                double midX = (fromPos.getX() + toPos.getX()) / 2;
                double midY = (fromPos.getY() + toPos.getY()) / 2;
                Text weightLabel = new Text(midX, midY, edge.getWeight().toString());
                weightLabel.setFill(Color.BLACK);
                weightLabel.setFont(Font.font(12));

                pane.getChildren().addAll(line, weightLabel);
            }
        }
    }


}//END CLASS


