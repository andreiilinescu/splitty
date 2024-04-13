package client.scenes;

import client.services.I18N;
import client.services.NotificationHelper;
import client.utils.ServerUtils;
import commons.Event;
import commons.Participant;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class AddParticipantCtrl implements Initializable {
    private final MainCtrl mainCtrl;

    private final ServerUtils server;

    @FXML
    private TextField email;

    @FXML
    private TextField name;

    @FXML
    private TextField iban;

    @FXML
    private TextField bic;

    @FXML
    private Button addButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label nameLabel;
    @FXML
    private Label addParticipant;
    private Event event;

    @Inject
    public AddParticipantCtrl(MainCtrl mainCtrl, Event event, ServerUtils server) {
        this.mainCtrl = mainCtrl;
        this.event = event;
        this.server = server;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        I18N.update(addButton);
        I18N.update(cancelButton);
        I18N.update(nameLabel);
        I18N.update(addParticipant);
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void addParticipantButton() {
        //Participant toAdd = new Participant();
        String participantName = name.getText();
        String participantEmail = email.getText();
        String participantIban = iban.getText();
        String participantBic = bic.getText();
        if (participantName.isEmpty()) {
            NotificationHelper notificationHelper = new NotificationHelper();
            String warningMessage = I18N.get("participant.add.error");
            if (participantName.isEmpty()){
                warningMessage += I18N.get("participant.add.error.name") + " ";
            }
            warningMessage += ")";
            notificationHelper.showError(I18N.get("general.warning"), warningMessage);
            return;
        }

        if (!participantEmail.isBlank()&&!participantEmail.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            String warningMessage = I18N.get("participant.add.error.message.email");
            NotificationHelper notificationHelper = new NotificationHelper();
            notificationHelper.showError(I18N.get("general.warning"), warningMessage);
            return;
        }
        if (!participantIban.isBlank()&&participantIban.length() != 34) {
            String warningMessage = I18N.get("participant.add.error.message.iban");
            NotificationHelper notificationHelper = new NotificationHelper();
            notificationHelper.showError(I18N.get("general.warning"), warningMessage);
            return;
        }
        Participant p = new Participant(
                participantName,
                this.event,
                participantIban,
                participantEmail,
                participantBic
        );
        server.addParticipant(
                event.getId(),
                p
        );
        this.event.addParticipant(p);
        server.send("/app/events", this.event);
        name.clear();
        email.clear();
        iban.clear();
        bic.clear();
        returnToOverview();
    }


    public void returnToOverview() {
        mainCtrl.showEventOverviewScene(event);
    }

    public Event getEvent() {
        return event;
    }
}
