package client.scenes;

import client.services.NotificationService;
import client.utils.DebtResolve;
import client.utils.ServerUtils;
import client.utils.DebtResolveTableEntry;
import com.google.inject.Inject;
import commons.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;


public class DebtResolveCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    private Event event;

    @FXML
    private TableView<DebtResolveTableEntry> debtTable;

    @FXML
    private TableColumn<DebtResolveTableEntry, String> personColumn;

    @FXML
    private TableColumn<DebtResolveTableEntry, Double> amountColumn;

    private ObservableList<DebtResolveTableEntry> tableEntries = FXCollections.observableArrayList();

    @Inject
    public DebtResolveCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        personColumn.setCellValueFactory(new PropertyValueFactory<>("from"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        debtTable.setItems(tableEntries);
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void refresh() {

        tableEntries.clear();
        DebtResolve.resolve(this.event)
                .stream()
                .map(item -> new DebtResolveTableEntry(item.from().getName(), item.to().getName(), item.amount()))
                .forEach(tableEntries::add);
    }
}


