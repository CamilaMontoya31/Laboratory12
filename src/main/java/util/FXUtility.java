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
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
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
    public static <G, V> void drawGraph(  G graph,
                                   Pane pane,
                                   Supplier<List<V>> verticesSupplier,
                                   Function<V, List<EdgeWeight>> edgesSupplier
    ) {
        pane.getChildren().clear();

        List<V> vertices = verticesSupplier.get();
        int n = vertices.size();

        double radius = 100;
        double centerX = pane.getWidth() / 2;
        double centerY = pane.getHeight() / 2;

        Map<V, Point2D> pos = new HashMap<>();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            V v = vertices.get(i);
            pos.put(v, new Point2D(x, y));

            Circle c = new Circle(x, y, 20);
            c.setFill(Color.LIGHTBLUE);
            c.setStroke(Color.BLACK);
            Text txt = new Text(x - 5, y + 5, v.toString());
            pane.getChildren().addAll(c, txt);
        }

        Set<String> seen = new HashSet<>();
        for (V from : vertices) {
            Point2D p1 = pos.get(from);
            for (EdgeWeight ew : edgesSupplier.apply(from)) {
                V to = (V) ew.getEdge();
                Point2D p2 = pos.get(to);
                String key = from.toString() + "->" + to.toString();

                if (seen.contains(key)) continue;
                seen.add(key);

                if (from.equals(to)) {
                    // Bucle: dibujar un arco circular alrededor del nodo
                    QuadCurve loop = new QuadCurve();
                    loop.setStartX(p1.getX());
                    loop.setStartY(p1.getY() - 20);
                    loop.setEndX(p1.getX() + 20);
                    loop.setEndY(p1.getY());
                    loop.setControlX(p1.getX() + 30);
                    loop.setControlY(p1.getY() - 30);
                    loop.setStroke(Color.GRAY);
                    loop.setFill(null);
                    pane.getChildren().add(loop);

                    // Texto del peso
                    if (ew.getWeight() != null) {
                        Text w = new Text(p1.getX() + 25, p1.getY() - 25, ew.getWeight().toString());
                        w.setFill(Color.RED);
                        pane.getChildren().add(w);
                    }

                } else {
                    boolean reverseExists = seen.contains(to.toString() + "->" + from.toString());
                    // Si existe la inversa, dibujar como curva
                    if (reverseExists) {
                        QuadCurve curve = new QuadCurve();
                        curve.setStartX(p1.getX());
                        curve.setStartY(p1.getY());
                        curve.setEndX(p2.getX());
                        curve.setEndY(p2.getY());
                        curve.setControlX((p1.getX() + p2.getX()) / 2 + 20);
                        curve.setControlY((p1.getY() + p2.getY()) / 2 - 20);
                        curve.setStroke(Color.GRAY);
                        curve.setFill(null);
                        pane.getChildren().add(curve);
                    }
                }
            }
        }
    }
                 /*
                // Verificar si es parte del MST
                boolean isInMST = false;
                if (mstEdges != null) {
                    for (Utility.Edge mstEdge : mstEdges) {
                        if ((mstEdge.getSourceO().equals(from) && mstEdge.getDestinationO().equals(to)) ||
                                (mstEdge.getSourceO().equals(to) && mstEdge.getDestinationO().equals(from))) {
                            isInMST = true;
                            break;
                        }
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

                pane.getChildren().addAll(line, weightLabel);*/




    public static <G, V> void drawDirectedGraph(
            G graph,
            Pane pane,
            Supplier<List<V>> verticesSupplier,
            Function<V, List<EdgeWeight>> edgesSupplier
    ) {
        pane.getChildren().clear();

        List<V> vertices = verticesSupplier.get();
        int n = vertices.size();

        double radius = 100;
        double centerX = pane.getWidth() / 2;
        double centerY = pane.getHeight() / 2;

        Map<V, Point2D> pos = new HashMap<>();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            V v = vertices.get(i);
            pos.put(v, new Point2D(x, y));

            Circle c = new Circle(x, y, 20);
            c.setFill(Color.LIGHTBLUE);
            c.setStroke(Color.BLACK);
            Text txt = new Text(x - 5, y + 5, v.toString());
            pane.getChildren().addAll(c, txt);
        }

        Set<String> seen = new HashSet<>();
        for (V from : vertices) {
            Point2D p1 = pos.get(from);
            for (EdgeWeight ew : edgesSupplier.apply(from)) {
                V to = (V) ew.getEdge();
                Point2D p2 = pos.get(to);
                String key = from.toString() + "->" + to.toString();

                if (seen.contains(key)) continue;
                seen.add(key);

                if (from.equals(to)) {
                    // Bucle: dibujar un arco circular alrededor del nodo
                    QuadCurve loop = new QuadCurve();
                    loop.setStartX(p1.getX());
                    loop.setStartY(p1.getY() - 20);
                    loop.setEndX(p1.getX() + 20);
                    loop.setEndY(p1.getY());
                    loop.setControlX(p1.getX() + 30);
                    loop.setControlY(p1.getY() - 30);
                    loop.setStroke(Color.GRAY);
                    loop.setFill(null);
                    pane.getChildren().add(loop);

                    // Texto del peso
                    if (ew.getWeight() != null) {
                        Text w = new Text(p1.getX() + 25, p1.getY() - 25, ew.getWeight().toString());
                        w.setFill(Color.RED);
                        pane.getChildren().add(w);
                    }

                } else {
                    boolean reverseExists = seen.contains(to.toString() + "->" + from.toString());
                    // Si existe la inversa, dibujar como curva
                    if (reverseExists) {
                        QuadCurve curve = new QuadCurve();
                        curve.setStartX(p1.getX());
                        curve.setStartY(p1.getY());
                        curve.setEndX(p2.getX());
                        curve.setEndY(p2.getY());
                        curve.setControlX((p1.getX() + p2.getX()) / 2 + 20);
                        curve.setControlY((p1.getY() + p2.getY()) / 2 - 20);
                        curve.setStroke(Color.GRAY);
                        curve.setFill(null);
                        pane.getChildren().add(curve);

                        // Flecha en curva
                        addArrowHead(pane, curve.getControlX(), curve.getControlY(), p2.getX(), p2.getY());

                        // Peso
                        if (ew.getWeight() != null) {
                            Text w = new Text((p1.getX() + p2.getX()) / 2 + 10, (p1.getY() + p2.getY()) / 2 - 10, ew.getWeight().toString());
                            w.setFill(Color.RED);
                            pane.getChildren().add(w);
                        }

                    } else {
                        // Línea recta con flecha
                        Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                        line.setStroke(Color.GRAY);
                        pane.getChildren().add(line);

                        addArrowHead(pane, p1.getX(), p1.getY(), p2.getX(), p2.getY());

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
        }
    }

    /**
     * Dibuja una flecha simple al final de una arista.
     */
    private static void addArrowHead(Pane pane, double x1, double y1, double x2, double y2) {
        double arrowLength = 10;
        double angle = Math.atan2(y2 - y1, x2 - x1);

        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        // Punto final antes del nodo destino (acortar flecha para que no entre al nodo)
        double px = x2 - cos * 20;
        double py = y2 - sin * 20;

        // Lados del triángulo
        double leftX = px - arrowLength * Math.cos(angle - Math.PI / 6);
        double leftY = py - arrowLength * Math.sin(angle - Math.PI / 6);

        double rightX = px - arrowLength * Math.cos(angle + Math.PI / 6);
        double rightY = py - arrowLength * Math.sin(angle + Math.PI / 6);

        Polygon arrowHead = new Polygon();
        arrowHead.getPoints().addAll(
                px, py,
                leftX, leftY,
                rightX, rightY
        );
        arrowHead.setFill(Color.BLACK);
        pane.getChildren().add(arrowHead);
    }

}//END CLASS


