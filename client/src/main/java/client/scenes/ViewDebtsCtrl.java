package client.scenes;

import client.utils.ServerUtils;
import commons.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class ViewDebtsCtrl implements Initializable {

    private final MainCtrl mainCtrl;
    private final ServerUtils server;
    private Event event;

    @FXML
    private Label event_name;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @Inject
    public ViewDebtsCtrl(MainCtrl mainCtrl, Event event, ServerUtils server) {
        this.mainCtrl = mainCtrl;
        this.event = event;
        this.server = server;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void refresh() {
        this.event_name.setText(event.getName());
    }
}
