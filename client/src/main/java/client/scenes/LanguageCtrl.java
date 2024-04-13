package client.scenes;

import client.services.I18NService;
import client.services.NotificationService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

public class LanguageCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NotificationService notificationService;
    @FXML
    private Button englishButton;
    @FXML
    private Button dutchButton;
    @FXML
    private Button romanianButton;
    @FXML
    private Button userLanguageButton;
    @FXML
    private Button customLanguageButton;
    @FXML
    private Button downloadTemplateButton;
    @FXML
    private Label languageLabel;
    @FXML
    private Label backButtonLabel;

    private final I18NService i18n;

    @Inject
    public LanguageCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService, I18NService i18n) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.notificationService = notificationService;
        this.i18n = i18n;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        i18n.update(englishButton);
        i18n.update(dutchButton);
        i18n.update(romanianButton);
        i18n.update(userLanguageButton);
        i18n.update(customLanguageButton);
        i18n.update(languageLabel);
        i18n.update(backButtonLabel);
        i18n.update(downloadTemplateButton);
    }

    public void backAction(){
        this.mainCtrl.showSettings();
    }

    public void switchToEnglish(){
        this.mainCtrl.switchToEnglish();
        this.mainCtrl.uponLanguageSwitch();
    }

    public void switchToDutch(){
        this.mainCtrl.switchToDutch();
        this.mainCtrl.uponLanguageSwitch();
    }

    public void switchToRomanian(){
        this.mainCtrl.switchToRomanian();
        this.mainCtrl.uponLanguageSwitch();
    }

    public void switchToUserLanguage(){
        this.mainCtrl.switchToUserLanguage();
        this.mainCtrl.uponLanguageSwitch();
    }

    public void addLanguage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(i18n.get("language.addLanguagePrompt"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Properties", "*.properties"));
        File selectedFile = fileChooser.showOpenDialog(mainCtrl.getPrimaryStage());
        if (selectedFile == null) {
            notificationService.showError(i18n.get("admin.event.import.error"), i18n.get("language.addLanguage.error"));
            return;
        }
        String languageResourceBundle = "client/src/main/resources/languages_user.properties";
        Path targetPath = new File(languageResourceBundle).toPath();

        try {
            Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            notificationService.showError(i18n.get("admin.event.import.error.readFile"), i18n.get("language.addLanguage.errorMessage"));
        }
    }

    public  void  downloadTemplate(){
        File selectedDirectory = getDirectory();
        String path = selectedDirectory.getAbsolutePath()
                + (System.getProperty("os.name").startsWith("Windows") ? "\\" : "/")
                + "template.properties";
        Path newFile = new File(path).toPath();
        Path templatePath=new File("client/src/main/resources/template.properties").toPath();
        try {
            Files.copy(templatePath, newFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            notificationService.showError(i18n.get("admin.event.import.error.writeFile"), i18n.get("admin.event.import.errorMessage.writeFile") + exception);
        }
    }

    private File getDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(i18n.get("admin.chooser.title"));
        File dir = chooser.showDialog(mainCtrl.getPrimaryStage());
        return dir;
    }
}
