package client.scenes;

import client.services.I18N;
import client.services.NotificationService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Participant;
import commons.Tag;
import jakarta.ws.rs.WebApplicationException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class StartCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NotificationService notificationService;
    @FXML
    private TextField createEventField;
    @FXML
    private TextField joinEventField;

    @FXML
    private GridPane recentEventsGrid;

    @FXML
    private Label newEventLabel;

    @FXML
    private Button buttonCreate;
    @FXML
    private Labeled joinEvent;
    @FXML
    private Labeled joinButton;
    @FXML
    private Labeled recent;
    @FXML
    private ImageView flagView;
    @Inject
    public StartCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.notificationService = notificationService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        I18N.update(newEventLabel);
        I18N.update(buttonCreate);
        I18N.update(joinEvent);
        I18N.update(joinButton);
        I18N.update(recent);
        createEventField.setOnKeyPressed((event -> {
            switch (event.getCode()) {
                case ENTER -> createEvent();
                case ESCAPE -> clearFields();
                default -> {
                }
            }
        }));
        joinEventField.setOnKeyPressed((event -> {
            switch (event.getCode()) {
                case ENTER -> joinEventAction();
                case ESCAPE -> clearFields();
                default -> {
                }
            }
        }));
    }

    /**
     * Action pointed to by the createEvent button
     */
    public void createEvent(){
        String title=createEventField.getText();
        if (title.isBlank()){
            notificationService.showError("Error creating event", "Title cannot be empty");
            return;
        }

        Participant creator=new Participant(this.mainCtrl.getUser().getName());
        Event newEvent= new Event(title, creator);
        Tag food = new Tag("food", "#8DE137", newEvent);
        Tag entraceFees = new Tag("entrance fees", "#38C4D5", newEvent);
        Tag travel = new Tag("travel", "#D53838", newEvent);
        try {
            newEvent=server.addEvent(newEvent);
            server.addTag(newEvent.getId(), food);
            server.addTag(newEvent.getId(), entraceFees);
            server.addTag(newEvent.getId(), travel);
        } catch (WebApplicationException e) {
            notificationService.showError("Error creating event", e.getMessage());
            return;
        }
        UUID participantId=newEvent.getParticipants().getFirst().getId();
        this.mainCtrl.addUserEvent(newEvent.getId(), participantId);
        mainCtrl.showEventOverviewScene(newEvent);
        clearFields();
    }

    /**
     * Add/Refresh links to joining recent events
     */
    public  void addRecentEvents(){
        this.recentEventsGrid.getChildren().clear();
        System.out.println(this.mainCtrl.getUser().getEvents()
                .size() + " big blana");
        List<UUID> eventIDs=this.mainCtrl.getUser().getEvents();
        int i=0;
        for (int j=eventIDs.size()-1; j>=0 && i<3; j--){
            try {
                Event currentEvent = server.getEvent(eventIDs.get(j));
                Hyperlink newEventLink=new Hyperlink(currentEvent.getName());
                newEventLink.setOnMouseClicked(event -> joinRecentEvent(currentEvent.getId())
                );
                newEventLink.setOnAction(event -> joinRecentEvent(currentEvent.getId()));
                this.recentEventsGrid.add(newEventLink, 0, i++);
            } catch (Exception ignored) {
            }

        }
    }

    /**
     * Action executed by the join event button
     */
    public void joinEventAction(){
        String inviteCode=this.joinEventField.getText();
        if (inviteCode.isBlank()){
            notificationService.showError("Error joining event", "Invite code cannot be empty");
            return;
        }
        try {
            Event joined = server.joinEvent(inviteCode);
            if (!this.mainCtrl.getUser().eventExists(joined.getId())) {
                //Add user to event if it is not already part of
                Participant participant = this.mainCtrl.getUser().createParticipant();
                participant = server.addParticipant(joined.getId(), participant);
                mainCtrl.addUserEvent(joined.getId(), participant.getId());
                server.send("/app/events", joined);
            }
            //move to correct scene
            mainCtrl.showEventOverviewScene(joined);
            clearFields();
        }catch (WebApplicationException e) {
            System.out.println(e.getMessage());
            notificationService.showError("Error joining event", "Wrong invite code");
        }
    }

    /**
     * Join an event based on its id. Issue server request for getting the event.
     * @param id id of the event
     */
    public  void joinRecentEvent(UUID id){ //add handling for deleted event
        Event currEvent=server.getEvent(id);
        mainCtrl.showEventOverviewScene(currEvent);
        clearFields();
    }
    /**
     * Move to the Settings scene when the icon is pressed
     */
    public void moveToSettings(){
        mainCtrl.showSettings();
    }

    /**
     * Clear the text fields
     */
    public  void clearFields(){
        this.createEventField.clear();
        this.joinEventField.clear();
    }

    public void shortCuts(){
        notificationService.informUser("Shortcuts",
                """
                        ALT + H  |  show this help message
                        ESCAPE   |  abort an action, go back to the previous scene
                        ALT + S  |  go to the settings scene
                        ALT + 1  |  (if in event overview) go to the main scene
                        ALT + E  |  (if in event overview) create new expense
                        ALT + P  |  (if in event overview) create new participant
                        ALT + I  |  (if in event overview) open invite code dialog
                        ALT + T  |  (if in event overview) open statistics
                        HOME KEY |  exit the event overview page
                        """,
                "Shortcuts");
    }
    public void setFlag(String language){
        this.flagView.setImage(new Image("client/icons/flag-"+language+".png"));
    }
    public void changeLanguage(){
        switch (this.mainCtrl.getUser().getLanguage()){
            case "english" -> this.mainCtrl.switchToDutch();
            case "dutch" -> this.mainCtrl.switchToRomanian();
            case "romanian" -> this.mainCtrl.switchToEnglish();
            default -> System.out.println("Unsupported language "+this.mainCtrl.getUser().getLanguage());
        }
    }
}
