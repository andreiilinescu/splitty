package client.scenes;

import client.services.I18N;
import client.services.NotificationService;
import client.utils.ServerUtils;
import commons.Event;
import commons.Tag;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class EditTagCtrl implements Initializable {

    @FXML
    public Label backButtonLabel;
    @FXML
    public Label createTagLabel;
    @FXML
    public TextField tagName;
    @FXML
    public Button addButton;
    @FXML
    public Button removeButton;
    @FXML
    public ColorPicker colorPicker;

    private final MainCtrl mainCtrl;

    private final ServerUtils server;

    private final NotificationService notificationService;

    private Tag tag;

    private Event event;

    @Inject
    public EditTagCtrl(MainCtrl mainCtrl, ServerUtils server, NotificationService notificationService, Tag tag) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.notificationService = notificationService;
        this.tag = tag;
    }

    public void setUp(Tag t, Event e) {
        this.tag = t;
        this.event = e;

        tagName.setText(t.getTag());
        colorPicker.setValue(Color.web(t.getColor()));
    }

    public void updateTag() {
        String name = tagName.getText();

        if (name.isEmpty()) {
            String warningMessage = I18N.get("tag.add.error");
            notificationService.showError(I18N.get("general.warning"), warningMessage);
            return;
        }

        if (event.getTags().stream().map(t -> t.getTag()).toList().contains(name) && !tag.getTag().equals(name)) {
            String warningMessage = I18N.get("tag.add.exists");
            notificationService.showError(I18N.get("general.warning"), warningMessage);
            return;
        }
        tag.setColor(AddTagCtrl.toRGBCode(colorPicker.getValue()));
        tag.setTag(name);

        server.updateTag(event.getId(), tag.getId(), tag);
        returnToOverview();
    }

    public void removeTag() {
        server.removeTag(event.getId(), tag.getId());
        returnToOverview();
    }

    public void returnToOverview() {
        mainCtrl.showAddTagScene(event);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        I18N.update(backButtonLabel);
        I18N.update(createTagLabel);
    }
}
