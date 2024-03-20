package client.scenes;

import client.utils.ServerUtils;
import commons.Event;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminEventsCtrl implements Initializable {
    private final MainCtrl mainCtrl;

    private final ServerUtils server;

    @FXML
    private ListView<Event> myListView;

    private List<Event> events;

    @Inject
    public AdminEventsCtrl(MainCtrl mainCtrl, ServerUtils server) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        myListView.setCellFactory(param -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                    Image image = new Image("client/icons/bin.png");
                    ImageView remove = new ImageView();
                    remove.setImage(image);
                    remove.setOnMouseClicked(e -> removeEvent(item));
                    remove.cursorProperty().set(Cursor.CLOSED_HAND);
                    remove.setFitHeight(12.0);
                    remove.setFitWidth(12.0);

                    getChildren().add(remove);
                }
            }
        });
    }

    private void removeEvent(Event item) {
        server.removeEvent(item.getId());
        populateList();
    }

    public void populateList() {
        myListView.getItems().clear();
        this.events = server.getEvents();
        myListView.getItems().addAll(events);
        myListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Event>() {
            @Override
            public void changed(ObservableValue<? extends Event> observable, Event oldValue, Event newValue) {
                Event e = myListView.getSelectionModel().getSelectedItem();
            }
        });
    }
}