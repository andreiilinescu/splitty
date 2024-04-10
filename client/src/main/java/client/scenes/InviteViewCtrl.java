package client.scenes;

import client.services.I18N;
import client.services.NotificationService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class InviteViewCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NotificationService notificationService;
    @FXML
    public Text eventCode;
    @FXML
    public Button copyToClipboardBtn;
    @FXML
    public TextArea textArea;
    @FXML
    public Button sendInviteBtn;
    @FXML
    public Button cancelBtn;
    @FXML
    public Button testBtn;
    @FXML
    public Labeled inviteInstr;
    @FXML
    public Labeled emailLabel;
    private Event event;
    private boolean validEmailConfig;


    @Inject
    public InviteViewCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.notificationService = notificationService;
        this.event=new Event();
        validEmailConfig = server.getMailConfig() != null;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        I18N.update(copyToClipboardBtn);
        I18N.update(inviteInstr);
        I18N.update(emailLabel);
        I18N.update(sendInviteBtn);
        I18N.update(cancelBtn);
        I18N.update(testBtn);
        this.copyToClipboardBtn.setOnAction(event -> copyToClipboard());
        this.sendInviteBtn.setOnAction(event -> sendInvite());
        this.cancelBtn.setOnAction(event -> backToEvent());
        if (server.getMailConfig() == null) {
            testBtn.setStyle("-fx-background-color: #808080");
            sendInviteBtn.setStyle("-fx-background-color: #808080");
        }
    }

    public void testEmail() {
        if (!validEmailConfig) {
            System.out.println("NO CONFIG AVAILABLE");
            return;
        }
        server.sendEmail(server.getMailConfig().getUsername(), "Test", "This is a test email. If you see it, then everything works fine!");
    }

    public void testEmail() {
        server.sendEmail(server.getMailConfig().getUsername(), "Test", "This is a test email. If you see it, then everything works fine!");
    }

    private void sendInvite() {
        if (!validEmailConfig) {
            System.out.println("NO CONFIG AVAILABLE");
            return;
        }
        if (textArea.getText().isEmpty()) {
            notificationService.showError("No addresses", "Please enter at least one address");
            return;
        }
        List<String> addresses = List.of(textArea.getText().split("\n|\r\n"));
        for (String email : addresses) {
            if (!validateEmail(email)) {
                notificationService.showError("Invalid addresses",
                        "Please make sure that all of the entered email addresses are valid.");
                return;
            }
        }
        for (String email : addresses) {
            server.sendEmailInvitation(email, eventCode.getText(), mainCtrl.getUser().getName());
        }
        backToEvent();
    }

    public static boolean validateEmail(String email) {
        return email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    private void copyToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(eventCode.getText());
        clipboard.setContent(content);
    }

    public void setEvent(Event newEvent){
        this.event=newEvent;
        eventCode.setText(this.event.getInviteCode());

    }
    public void backToEvent(){
        mainCtrl.showEventOverviewScene(event);
    }

    public Event getEvent() {
        return event;
    }
}
