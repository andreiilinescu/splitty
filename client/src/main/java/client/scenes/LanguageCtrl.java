package client.scenes;

import client.services.I18N;
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

    @Inject
    public LanguageCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.notificationService = notificationService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        I18N.update(englishButton);
        I18N.update(dutchButton);
        I18N.update(romanianButton);
        I18N.update(userLanguageButton);
        I18N.update(customLanguageButton);
        I18N.update(languageLabel);
        I18N.update(backButtonLabel);
        I18N.update(downloadTemplateButton);
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
        fileChooser.setTitle(I18N.get("language.addLanguagePrompt"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Properties", "*.properties"));
        File selectedFile = fileChooser.showOpenDialog(mainCtrl.getPrimaryStage());
        if (selectedFile == null) {
            notificationService.showError(I18N.get("admin.event.import.error"), I18N.get("language.addLanguage.error"));
            return;
        }
        String languageResourceBundle = "client/src/main/resources/languages_user.properties";
        Path targetPath = new File(languageResourceBundle).toPath();

        try {
            Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            notificationService.showError(I18N.get("admin.event.import.error.readFile"), I18N.get("language.addLanguage.errorMessage"));
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
            notificationService.showError(I18N.get("admin.event.import.error.writeFile"), I18N.get("admin.event.import.errorMessage.writeFile") + exception);
        }
    }

    private File getDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(I18N.get("admin.chooser.title"));
        File dir = chooser.showDialog(mainCtrl.getPrimaryStage());
        return dir;
    }
}
