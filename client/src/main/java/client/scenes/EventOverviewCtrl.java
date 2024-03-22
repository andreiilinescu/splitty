package client.scenes;

import client.services.NotificationService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import jakarta.ws.rs.WebApplicationException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class EventOverviewCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NotificationService notificationService;
    @FXML
    public Button sendInviteButton;

//    @FXML
//    private Label participantsLabel;

    @FXML
    public TextFlow textFlow;

    @FXML
    private TextField eventTitle;

    private Event event;

    @Inject
    public EventOverviewCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.notificationService = notificationService;
        this.event=new Event();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.eventTitle.setOnKeyPressed((event -> {
            switch (event.getCode()) {
                case ENTER -> changeTitle();
                case ESCAPE -> this.eventTitle.setText(this.event.getTitle());
                default -> {
                }
            }
        }));
        this.sendInviteButton.setOnAction(event -> sendInvite());
        System.out.println("EventOverviewCtrl initialized");
    }

    public Event getEvent(){
        return this.event;
    }

    public void setEvent(Event newEvent){
        this.event=newEvent;
        eventTitle.setText(this.event.getTitle());
        reassignParticipants(this.event.getParticipants());
    }

    public void reassignParticipants(List<Participant> participantList){
        System.out.println(participantList.stream().map(x -> x.getName()).toList());
        textFlow.getChildren().clear();
        if (participantList.isEmpty()) {
            textFlow.getChildren().add(new Label("No participants, yet"));
            return;
        }
        for (Participant p : participantList.subList(0, participantList.size() - 1)) {
            Label label = new Label(p.getName());
            label.setOnMouseClicked(e -> editParticipant(p));
            textFlow.getChildren().add(label);
            textFlow.getChildren().add(new Label(", "));
        }
        Label lastLabel = new Label(participantList.get(participantList.size() - 1).getName());
        lastLabel.setOnMouseClicked(e -> editParticipant(participantList.get(participantList.size() - 1)));
        textFlow.getChildren().add(lastLabel);
    }

    public void changeTitle(){
        this.event.setName(this.eventTitle.getText());
        try {
            this.server.updateEvent(this.event);
        }
        catch (WebApplicationException e){
            notificationService.showError("Error updating event", "Could not update event title");
        }
    }

    public void sendInvite(){
        mainCtrl.showInviteView(this.event);
    }

    public void backToStart(){
        mainCtrl.showStartScene();
    }

    public void addParticipant(){
        mainCtrl.showAddParticipantScene(event);
    }

    public void editParticipant(Participant p){
        mainCtrl.showEditParticipantScene(event, p);
    }

    public void addExpense(){
        mainCtrl.showAddExpense();
    }

    public void refresh(){
        try {
            Event refreshed = server.getEvent(event.getId());
            this.setEvent(refreshed);

            /* TO DO:
            * - refresh all data related to the event
            * - add functionality to the expense list and filtering*/
        }catch (WebApplicationException e) {
            notificationService.showError("Error refreshing event", "Could not refresh event data");
        }
    }
}
