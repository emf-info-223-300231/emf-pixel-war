package app.presentation;

import app.beans.Pixel;
import app.exceptions.MyDBException;

import app.helpers.JfxPopup;
import app.workers.DbWorker;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import app.workers.DbWorkerItf;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

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
    private Stage stage;

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

        if (dbWrk != null) {
            refresh();
        } else {
            JfxPopup.displayError("ERREUR", null, "Worker null");
        }

    }

    /**
     * Modifier par : Valentino Modification : J'ai réduit le temps
     * d'initialisation de 238ms à 4ms grâce au multi-threading. Mesure
     * effectuer avec System.nanoTime() et profiler de intellij :)
     *
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

        new Thread(() -> {
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
                Color color = colorPicker.getValue();
                p.setColorPixel(colorChanelToHex(color.getRed())
                        + colorChanelToHex(color.getGreen())
                        + colorChanelToHex(color.getBlue()));
                try {
                    dbWrk.modifier(p);
                    pane.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                } catch (MyDBException ex1) {
                    JfxPopup.displayError("Oups une erreur est survenue !!!", "Oups une erreur est survenue.", ex1.toString());
                    refresh();
                }
            }

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
     * Modifier par : Valentino Modification : J'ai ajouté le multi-threading et
     * diviser la liste en deux pour que cela soit plus rapide. L'application
     * démarre presque instantanément ou presque.
     *
     * @param pixels tableau de pixels
     */
    public void draw(List<Pixel> pixels) {
        int size = pixels.size();
        new Thread(() -> {
            pixelMaker(pixels.subList(0, (size + 1) / 2));
        }).start();
        new Thread(() -> {
            pixelMaker(pixels.subList((size + 1) / 2, size));
        }).start();
    }

    /**
     * Ajouter par : Valentino Ajout : Je crée cette méthode pour ne pas avoir
     * du code à double.
     *
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
                System.out.println(ex.toString());
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

    private void refresh() {
        try {
            pixels = dbWrk.lirePixels();
            draw(pixels);
        } catch (MyDBException ex) {
            JfxPopup.displayError("Oups une erreur est survenue !!!", "Oups une erreur est survenue.", ex.toString());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refresh();
    }

    @FXML
    private void handleOpenImage(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Ouvrir une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.*"),
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png")
            );
            File file = fileChooser.showOpenDialog(this.stage);
            if (file != null) {
                BufferedImage bufferedImage = ImageIO.read(file);
                for (int y = 0; y < 64; y++) {
                    for (int x = 0; x < 96; x++) {
                        String RGBA = Integer.toHexString(bufferedImage.getRGB(x, y));
                        RGBA = RGBA.substring(RGBA.length()-6);
                        Pixel p = findPixel(x, y);
                        p.setColorPixel(RGBA);
                        try {
                            dbWrk.modifier(p);
                        } catch (MyDBException ex) {
                            JfxPopup.displayError("Oups une erreur est survenue !!!", "Oups une erreur est survenue.", ex.toString());
                        }
                    }
                }
                JfxPopup.askInfo("Image enregistrée", "Image enregistrée", "Image enregistrée avec succès !");
            }
        } catch (IOException ex) {
            JfxPopup.displayError("Oups une erreur est survenue !!!", "Oups une erreur est survenue.", ex.toString());
        }

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
