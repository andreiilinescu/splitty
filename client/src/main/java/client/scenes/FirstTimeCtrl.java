package client.scenes;

import client.services.I18NService;
import client.utils.ServerUtils;
import client.utils.User;
import com.google.inject.Inject;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import client.utils.User.Currency;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class FirstTimeCtrl implements Initializable{
    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> currency;

    @FXML
    private Labeled start;
    @FXML
    private Labeled setup;

    private final I18NService i18n;


    @Inject
    public FirstTimeCtrl(ServerUtils server, MainCtrl mainCtrl, I18NService i18n) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.i18n = i18n;
    }
    public void initialize(URL location, ResourceBundle resources) {
        i18n.update(start);
        i18n.update(setup);
        List<String> currencies = Arrays.stream(Currency.values()).map(Enum::toString).toList();

        this.currency.getItems().addAll(currencies);
        this.currency.getSelectionModel().selectFirst();
        this.nameField.setOnKeyPressed((event -> {
            switch (event.getCode()) {
                case ENTER -> moveToStart();
                case ESCAPE -> this.nameField.clear();
                default -> {
                }
            }
        }));
    }

    public void moveToStart(){
        User user=new User(nameField.getText(), currency.getValue());
        nameField.clear();
        this.currency.getSelectionModel().selectFirst();
        this.mainCtrl.setUser(user);
        this.mainCtrl.showStartScene();
    }

}
