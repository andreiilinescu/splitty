package client.scenes;

import client.services.I18N;
import client.services.NotificationHelper;
import client.services.NotificationService;
import client.utils.ServerUtils;
import client.utils.User;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.*;


public class UserSettingsCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NotificationService notificationService;

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ibanField;
    @FXML
    private TextField bicField;
    @FXML
    private ComboBox<String> currencyComboBox;

    @FXML
    private Labeled profileSettings;
    @FXML
    private Labeled nameLabel;
    @FXML
    private Labeled emailLabel;
    @FXML
    private Labeled ibanLabel;
    @FXML
    private Labeled bicLabel;
    @FXML
    private Labeled sneButton;
    @FXML
    private Labeled cancelButton;
    @FXML
    private Labeled currencyOption;
    @Inject
    public UserSettingsCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.notificationService = notificationService;
    }

    public void refreshFields(){
        User user=this.mainCtrl.getUser();
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        ibanField.setText(user.getIban());
        bicField.setText(user.getBic());
        currencyComboBox.getSelectionModel().select(user.getPrefferedCurrency().toString());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<String> currencies = Arrays.stream(User.Currency.values()).map(Enum::toString).toList();
        currencyComboBox.getItems().addAll(currencies);
        I18N.update(profileSettings);
        I18N.update(nameLabel);
        I18N.update(emailLabel);
        I18N.update(ibanLabel);
        I18N.update(bicLabel);
        I18N.update(sneButton);
        I18N.update(cancelButton);
        I18N.update(currencyOption);
    }

    public void back(){
        mainCtrl.showSettings();
    }

    public void save(){
        var name=nameField.getText();
        var email=emailField.getText();
        var iban=ibanField.getText();
        var bic=bicField.getText();
        if (name.isEmpty() || email.isEmpty() || iban.isEmpty()
                || bic.isEmpty()) {
            NotificationHelper notificationHelper = new NotificationHelper();
            String warningMessage = "You haven't filled in the following fields: ( ";
            if (name.isEmpty()){
                warningMessage += "name ";
            }
            if (email.isEmpty()){
                warningMessage += "email ";
            }
            if (iban.isEmpty()){
                warningMessage += "iban ";
            }
            if (bic.isEmpty()){
                warningMessage += "bic";
            }
            warningMessage += ")";
            notificationHelper.showError("Warning!", warningMessage);
            return;
        }
        User newUser=new User(name, email, iban, bic);
        newUser.setLanguage(this.mainCtrl.getUser().getLanguage());
        LinkedHashMap<UUID, UUID> tmp =(LinkedHashMap<UUID, UUID>) this.mainCtrl.getUser().getEventParticipant();
        newUser.setEventParticipant(tmp);
        this.mainCtrl.setUser(newUser);
        back();
    }
}
