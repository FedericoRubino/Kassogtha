package org.mbari;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ListView;
import javafx.geometry.Insets;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.time.Duration;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;

/**
 * JavaFX App
 */
public class App extends Application {

    private IO io;

    @Override
    public void start(Stage stage) {

        // Parent root = FXMLLoader(getClass().getResource("src/main/java/org/resources/App.fxml"))
        // Scene scence = new Scene(root, 1000, 700);

        // stage.setScene(scene);
        // stage.show();

        var table = new TableView<Localization>();
        table.setEditable(false);

        var conceptCol = new TableColumn<Localization, String>("Concept");
        conceptCol.setCellValueFactory(new PropertyValueFactory<Localization, String>("concept"));
        
        conceptCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));

        var timeCol = new TableColumn<Localization, Duration>("ElapsedTime");
        timeCol.setCellValueFactory(new PropertyValueFactory<Localization, Duration>("elapsedTime"));
        timeCol.setCellFactory(column -> {
                return new TableCell<Localization, Duration>() {
                    @Override
                    protected void updateItem(Duration item, boolean empty) {
                        if (item == null || empty) {
                            setText(null);
                        }
                        else {
                            setText(formatDuration(item));
                        }
                    }
                };
            });
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.7));

        conceptCol.setResizable(false);
        timeCol.setResizable(false);

        table.getColumns().addAll(timeCol, conceptCol);


        // VBox vBox = new VBox();
        // var sceneEmpty = new Scene(vBox, 480, 480);
        ListView listview = new ListView();
        listview.getItems().add("ITEM1");
        listview.getItems().add("ITEM2");
        listview.getItems().add("ITEM3");
        listview.getItems().add("ITEM4");

        HBox hBox = new HBox(listview, table);
        hBox.setMargin(listview, new Insets(20,20,20,20));
        hBox.setMargin(table, new Insets(20,20,20,20));


        Button saveBtn = new Button("Save");
        Button downLoadBtn = new Button("Download");
        Button upLoadBtn = new Button("Upload");
        HBox hButtonBox = new HBox(saveBtn, downLoadBtn, upLoadBtn);


        VBox containerBox = new VBox(hBox, hButtonBox);


        var scene = new Scene(containerBox, 700, 520);
        // scene.setResizable(false);
        stage.setTitle("Kassogtha");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        initComms(table);
    }

    private String formatDuration(Duration duration) {
        return String.format("%d:%02d:%02d:%03d", 
                                duration.toHours(), 
                                duration.toMinutesPart(), 
                                duration.toSecondsPart(),
                                duration.toMillisPart());
    }

    private void initComms(TableView<Localization> table) {
        var incomingPort = 5561;   // ZeroMQ subscriber port
        var outgoingPort = 5562;   // ZeroMQ publisher port
        var incomingTopic = "localization";
        var outgoingTopic = "localization";
        io = new IO(outgoingPort, incomingPort, outgoingTopic, incomingTopic);

        var items = io.getController().getLocalizations();
        items.addListener((ListChangeListener.Change<? extends Localization> c) -> {
            System.out.println("Received: " + c);
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(loc -> System.out.println(loc));
                }
            }
        });
        table.setItems(items);
    }

    public static void main(String[] args) {
        launch();
    }

}