package client.scenes;

import client.services.I18NService;
import client.services.NotificationHelper;
import client.services.NotificationService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputDialog;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NotificationService notificationService;

    private TextInputDialog passwordPrompt;

    @FXML
    private Labeled settings;
    @FXML
    private Labeled edit;
    @FXML
    private Labeled language;
    @FXML
    private Labeled adminMode;
    @FXML
    private Labeled nukeEmData;
    @FXML
    private Labeled back;
    private final I18NService i18n;

    @Inject
    public SettingsCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService, I18NService i18n) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.notificationService = notificationService;
        this.i18n = i18n;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        i18n.update(settings);
        i18n.update(edit);
        i18n.update(language);
        i18n.update(adminMode);
        i18n.update(nukeEmData);
        i18n.update(back);
        this.prepareAdminLogin();
    }

    public void editProfileAction(){
        this.mainCtrl.showUserSettings();
    }
    public void language() {
        this.mainCtrl.showLanguageOptions();
    }
    public void deleteDataAction(){
        this.mainCtrl.deleteAllData();
    }
    public void backAction(){
        this.mainCtrl.showStartScene();
    }

    private void prepareAdminLogin(){
        passwordPrompt=new TextInputDialog();
        passwordPrompt.setTitle(i18n.get("window.event.admin.login"));
        passwordPrompt.setContentText(i18n.get("settings.admin.password"));
        passwordPrompt.setHeaderText("");
        passwordPrompt.setGraphic(null);
    }
    public void adminAction(){
        this.prepareAdminLogin();
        passwordPrompt.getEditor().clear(); // Clear Dialogue Input
        Optional<String> result = passwordPrompt.showAndWait();
        result.ifPresent(this::checkPassword); // if ok is pressed check password
    }

    private void checkPassword(String password){
        if (password.isEmpty()){
            NotificationHelper notificationHelper = new NotificationHelper();
            notificationHelper.showError(i18n.get("general.warning"), i18n.get("settings.admin.warning"));
            return;
        }
        try {
            String x=this.server.checkPassword(password);
            if (Objects.equals(x, password)){
                this.mainCtrl.loginAdmin();
            }
        }
        catch (Exception E){
            NotificationHelper notificationHelper = new NotificationHelper();
            notificationHelper.showError(i18n.get("general.error"), i18n.get("settings.admin.wrongPassword"));
        }
    }
}
