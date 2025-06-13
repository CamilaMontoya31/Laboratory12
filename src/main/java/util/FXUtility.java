package util;

import domain.EdgeWeight;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
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

    /**
     * Dibuja en el pane un grafo cualquiera, siempre y cuando:
     *  - sepas cómo sacarle la lista de vértices (verticesSupplier)
     *  - sepas cómo sacarle las aristas de cada vértice (edgesSupplier)
     *
     * @param graph             tu objeto grafo (puede ser cualquier clase)
     * @param pane              el Pane de JavaFX donde se dibuja
     * @param verticesSupplier  lambda que devuelve List<V> de todos los vértices
     * @param edgesSupplier     lambda que para cada V devuelve List<EdgeWeight>
     * @param <G>               tipo de tu clase de grafo
     * @param <V>               tipo de los vértices (ej. String, Integer, tu clase Vertex, etc.)
     */
    public static <G, V> void drawGraph(
            G graph,
            Pane pane,
            Supplier<List<V>> verticesSupplier,
            Function<V, List<EdgeWeight>> edgesSupplier
    ) {
        pane.getChildren().clear();

        // 1) obtenemos la lista de vértices
        List<V> vertices = verticesSupplier.get();
        int n = vertices.size();

        // parámetros de posicionamiento
        double radius = 100;
        double centerX = pane.getWidth()  / 2;
        double centerY = pane.getHeight() / 2;

        // 2) calculamos y guardamos posiciones
        Map<V, Point2D> pos = new HashMap<>();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            V v = vertices.get(i);
             pos.put(v, new Point2D(x, y));

            // 3) dibujamos el nodo
            Circle c = new Circle(x, y, 20);
            c.setFill(Color.LIGHTBLUE);
            c.setStroke(Color.BLACK);
            Text txt = new Text(x - 5, y + 5, v.toString());

            pane.getChildren().addAll(c, txt);
        }

        // 4) dibujamos aristas sin duplicar (no dirigido)
        Set<String> seen = new HashSet<>();
        for (V from : vertices) {
            Point2D p1 = pos.get(from);
            for (EdgeWeight ew : edgesSupplier.apply(from)) {
                V to = (V) ew.getEdge();
                String key = from.toString() + "→" + to.toString();
                String rev = to.toString() + "→" + from.toString();
                if (seen.contains(key) || seen.contains(rev)) continue;
                seen.add(key);

                Point2D p2 = pos.get(to);
                Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                line.setStroke(Color.GRAY);
                pane.getChildren().add(line);

                // si hay peso, lo ponemos al medio
                if (ew.getWeight() != null) {
                    double mx = (p1.getX() + p2.getX()) / 2;
                    double my = (p1.getY() + p2.getY()) / 2;
                    Text w = new Text(mx, my, ew.getWeight().toString());
                    w.setFill(Color.RED);
                    pane.getChildren().add(w);
                }
            }
        }
    }

}//END CLASS


