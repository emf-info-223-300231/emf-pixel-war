package main.java.app.presentation;

import main.java.app.exceptions.MyDBException;
import main.java.app.beans.Pixel;

import main.java.app.helpers.JfxPopup;
import main.java.app.workers.DbWorker;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import main.java.app.workers.DbWorkerItf;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

/**
 *
 * @author PA
 */
public class MainCtrl implements Initializable {

    final static private String PU = "emf-pixel-war_appPU";

    private DbWorkerItf dbWrk;
    private List<Pixel> pixels;

    @FXML
    private GridPane gridPixelWar;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Label lblColumn;
    @FXML
    private Label lblRow;

    /*
   * INTIALISATION DE LA VUE
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbWrk = new DbWorker();
        initGrid();
        openDB();
    }

    /*
   * OUVERTURE DE LA DB (NECESSAIRE DANS L'INITIALISATION DE LA VUE)
     */
    private void openDB() {
        try {
            dbWrk.connecter(PU);
            System.out.println("------- DB OK ----------");
        } catch (MyDBException ex) {
            JfxPopup.displayError("ERREUR", "Une erreur s'est produite", ex.getMessage());
            System.exit(1);
        }

        try {
            if(dbWrk != null) {
                pixels = dbWrk.lirePixels();
                draw(pixels);
            }else{
                JfxPopup.displayError("ERREUR", null, "Worker null");
            }
        } catch (MyDBException ex) {
            JfxPopup.displayError("ERREUR", null, ex.getMessage());
        }
    }

    /**
     * Modifier par : Valentino
     * Modification : J'ai réduit le temps d'initialisation de 238ms à 4ms grâce au multi-threading.
     * Mesure effectuer avec System.nanoTime() et profiler de intellij :)
     * @version 1.0.1
     */
    private void initGrid() {
        int numCols = 96;
        int numRows = 64;

        new Thread(() -> {
            for (int i = 0; i < numCols; i++) {
                ColumnConstraints colConstraints = new ColumnConstraints();
                colConstraints.setHgrow(Priority.SOMETIMES);
                colConstraints.setPrefWidth(10.0);
                colConstraints.setMinWidth(10.0);
                gridPixelWar.getColumnConstraints().add(colConstraints);
            }
        }).start();

        new Thread(() -> {
            for (int i = 0; i < numRows; i++) {
                RowConstraints rowConstraints = new RowConstraints();
                rowConstraints.setVgrow(Priority.SOMETIMES);
                rowConstraints.setPrefHeight(10.0);
                rowConstraints.setMinHeight(10.0);
                gridPixelWar.getRowConstraints().add(rowConstraints);
            }
        }).start();

        new Thread(()->{
            for (int i = 0; i < numCols; i++) {
                for (int j = 0; j < numRows; j++) {
                    addPane(i, j);
                }
            }
        }).start();
    }

    private void addPane(int colIndex, int rowIndex) {
        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setOnMouseClicked(e -> {
            Pixel p = findPixel(colIndex, rowIndex);
            if (p != null) {
                try {
                    Color color = colorPicker.getValue();
                    p.setColorPixel(colorChanelToHex(color.getRed())
                            + colorChanelToHex(color.getGreen())
                            + colorChanelToHex(color.getBlue()));
                } catch (Exception ex) {
                    JfxPopup.displayError("Oups une erreur est survenue !!!", "Oups une erreur est survenue.", ex.toString());
                }
                try {
                    dbWrk.modifier(p);
                } catch (MyDBException ex1) {
                    JfxPopup.displayError("Oups une erreur est survenue !!!", "Oups une erreur est survenue.", ex1.getMessage());
                    try {
                        pixels = dbWrk.lirePixels();
                        draw(pixels);
                    } catch (MyDBException ex2) {
                        JfxPopup.displayError("Oups une erreur est survenue !!!", "Oups une erreur est survenue.", ex2.toString());
                    }

                }
            }
            pane.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
        });
        pane.setOnMouseEntered(e -> {
            lblColumn.setText("Colonne : " + colIndex);
            lblRow.setText("Ligne : " + rowIndex);
        });
        gridPixelWar.add(pane, colIndex, rowIndex);
    }

    private static String colorChanelToHex(double chanelValue) {
        String rtn = Integer.toHexString((int) Math.min(Math.round(chanelValue * 255), 255));
        if (rtn.length() == 1) {
            rtn = "0" + rtn;
        }
        return rtn;
    }

    private Pixel findPixel(int column, int row) {
        Pixel found = null;
        for (Pixel p : pixels) {
            if (p.getColumnPixel() == column && p.getRowPixel() == row) {
                found = p;
            }
        }
        return found;
    }

    /**
     * Modifier par : Valentino
     * Modification : J'ai ajouté le multi-threading et diviser la liste en deux pour que cela soit plus rapide.
     * L'application démarre presque instantanément ou presque.
     * @param pixels tableau de pixels
     */
    public void draw(List<Pixel> pixels) {
        int size = pixels.size();
        new Thread(()->{
            pixelMaker(pixels.subList(0,(size+1)/4));
        }).start();
        new Thread(()->{
            pixelMaker(pixels.subList((size+1)/4,(size+1)/2));
        });
        new Thread(()->{
            pixelMaker(pixels.subList((size+1)/2,size-(size/4)));
        }).start();
        new Thread(()->{
            pixelMaker(pixels);
        }).start();
    }

    /**
     * Ajouter par : Valentino
     * Ajout : Je crée cette méthode pour ne pas avoir du code à double.
     * @param p2
     */
    private void pixelMaker(List<Pixel> p2) {
        for (Pixel p : p2) {
            try {
                Pane pane = (Pane) getNodeByRowColumnIndex(p.getRowPixel(), p.getColumnPixel(), gridPixelWar);
                if (pane != null) {
                    pane.setBackground(new Background(new BackgroundFill(Color.web(p.getColorPixel()), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    System.out.println(p.getRowPixel() + " " + p.getColumnPixel());
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        Node result = null;
        ObservableList<Node> childrens = gridPane.getChildren();

        for (Node node : childrens) {
            if (gridPane.getRowIndex(node) == row && gridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }

        return result;
    }

    public void quitter() {
        try {
            dbWrk.deconnecter(); // ne pas oublier !!!
        } catch (MyDBException ex) {
            System.out.println(ex.getMessage());
        }
        Platform.exit();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            pixels = dbWrk.lirePixels();
            draw(pixels);
        } catch (Exception ex) {
            JfxPopup.displayError("Oups une erreur est survenue !!!", "Oups une erreur est survenue.", ex.toString());
        }
    }

}
