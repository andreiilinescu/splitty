package client.scenes;

import client.services.I18N;
import client.services.NotificationService;
import commons.Event;
import commons.Tag;
import javafx.fxml.FXML;
import client.utils.ServerUtils;

import javax.inject.Inject;

import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.List;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class AddTagCtrl implements Initializable {
    private final MainCtrl mainCtrl;

    private final ServerUtils server;

    @FXML
    public Label manageTags;
    @FXML
    public Label backButtonLabel;
    @FXML
    public Label availableTagsLabel;
    @FXML
    public Label createTagLabel;
    @FXML
    public ColorPicker colorPicker;
    @FXML
    public TextField tagName;
    @FXML
    public Button addButton;
    @FXML
    public FlowPane tagsPane;

    private Event event;

    private final NotificationService notificationService;

    @Inject
    public AddTagCtrl(MainCtrl mainCtrl, Event event, ServerUtils server, NotificationService notificationService) {
        this.mainCtrl = mainCtrl;
        this.event = event;
        this.server = server;
        this.notificationService = notificationService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        I18N.update(manageTags);
        I18N.update(backButtonLabel);
        I18N.update(availableTagsLabel);
        I18N.update(createTagLabel);
    }

    public void setUp(UUID e) {
        this.event = server.getEvent(e);
        List<BorderPane> tags = event.getTags().stream().map(t -> pretty(t)).toList();

        tagsPane.getChildren().clear();
        tagsPane.getChildren().addAll(tags);
        tagsPane.setHgap(10);
        tagsPane.setVgap(10);
    }

    public BorderPane pretty(Tag t) {
        Insets insets = new Insets(5.0, 5.0, 5.0, 5.0);

        BorderPane bp = new BorderPane();
        bp.setOnMouseClicked(e -> editTag(t));
        bp.setBackground(Background.fill(Color.web(t.getColor())));
        String style = """
                -fx-background-radius: 30;
                -fx-border-radius: 30;
                -fx-border-width:0.8;
                -fx-border-color: #F5F5F5;
                """;
        style = style + "-fx-background-color: " + t.getColor();
        bp.setStyle(style);
        bp.setMinHeight(8.0);
        bp.setMaxWidth(300.0);

        Text name = new Text(t.getTag());
        name.setFill(Color.WHITESMOKE);
        name.setCursor(Cursor.HAND);
        BorderPane.setMargin(name, insets);

        bp.setLeft(name);

        return bp;
    }

    public void editTag(Tag t) {
        mainCtrl.showEditTagScene(t, event);
    }

    public void addTag() {
        Color color = colorPicker.getValue();
        String name = tagName.getText();

        if (name.isEmpty()) {
            String warningMessage = I18N.get("tag.add.error");
            notificationService.showError(I18N.get("general.warning"), warningMessage);
            return;
        }

        if (event.getTags().stream().map(t -> t.getTag()).toList().contains(name)) {
            String warningMessage = I18N.get("tag.add.exists");
            notificationService.showError(I18N.get("general.warning"), warningMessage);
            return;
        }

        server.addTag(
                event.getId(),
                new Tag(name, toRGBCode(color), event)
        );

        setUp(event.getId());
    }

    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    public void setEvent(Event event) {
        this.event = event;
    }

    public void returnToOverview() {
        mainCtrl.showEventOverviewScene(event);
    }
}
