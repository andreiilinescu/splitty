package client.scenes;

import client.services.I18NService;
import client.services.NotificationService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import jakarta.ws.rs.WebApplicationException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

@Controller
@EnableScheduling
public class EventOverviewCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NotificationService notificationService;
    @FXML
    public TextFlow textFlow;
    @FXML
    public Pane backButton;
    @FXML
    public Button settleDebt;
    @FXML
    public Button sendInvite;
    @FXML
    public Button addExpense;
    @FXML
    public Button addTag;
    @FXML
    public Label expenseLabel;
    @FXML
    public Label participantLabel;
    @FXML
    public Label backButtonLabel;
    @FXML
    public Label allFilter;
    @FXML
    public Label fromFilter;
    @FXML
    public Label toFilter;

    @FXML
    public ImageView flagView;
    @FXML
    public TableColumn all;
    @FXML
    public Button statsBtn;

    @FXML
    private TextField eventTitle;

    @FXML
    private ListView<BorderPane> expensesList;

    @FXML
    private ComboBox<Participant> payerSelector;

    private Event event;

    private List<Expense> expenses;

    private Participant payer;

    private int filter;

    private final I18NService i18n;

    @Inject
    public EventOverviewCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService, I18NService i18n) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.notificationService = notificationService;
        this.event=new Event();
        this.filter=0;
        this.i18n = i18n;
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

        this.sendInvite.setOnAction(event -> sendInvite());

        this.statsBtn.setOnAction(e -> mainCtrl.showStatistics(this.event));
        i18n.update(sendInvite);
        i18n.update(addExpense);
        i18n.update(addTag);
        i18n.update(settleDebt);
        i18n.update(expenseLabel);
        i18n.update(participantLabel);
        i18n.update(eventTitle);
        i18n.update(backButtonLabel);
        this.sendInvite.setOnAction(event -> sendInvite());

        payerSelector.setCellFactory(param -> getPayerListCell());
        payerSelector.setButtonCell(getPayerListCell());

        server.registerForMessages("/topic/events", e -> {
            //print the e as json in pretty format
            System.out.println("MESSAGE RECEIVED");
            System.out.println("EXPENSES FROM WS: " + e.getExpenses());
            System.out.println("MY ID : " + event.getId());
            System.out.println("PARTICIPANTS: " + event.getParticipants().size());
            System.out.println("MESSAGE ID: " + e.getId());
            if (e.getId().equals(this.event.getId())){
                setEvent(e);
                refresh();
            }
        });

    }

//    public ListCell<Expense> getExpenseListCell() {
//        return new ListCell<>() {
//            @Override
//            protected void updateItem(Expense item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null){
//                    setGraphic(null);
//                    setText(null);
//                    return;
//                }
//                if (payer != null  && (! && filter==2)||(filter==1 && !uuids.contains(payer.getId()))){
//                    setGraphic(null);
//                    setText(null);
//                }
//                else {
//                    setGraphic(createRow(item));
//                }
//            }
//        };
//    }
    public ListCell<Participant> getPayerListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Participant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        };
    }

    public Event getEvent(){
        return this.event;
    }

    public void setEvent(Event newEvent){
        this.event=newEvent;
//        eventTitle.setText(this.event.getTitle());
//        reassignParticipants(this.event.getParticipants());
//        this.expenses = server.getExpensesByEvent(this.event.getId());
        System.out.println("---event overview---");
        System.out.println("id: "+this.event.getId());
        System.out.println("title: "+this.event.getTitle());
        System.out.println("participants: "+ this.event.getParticipants().size());
        System.out.println("expenses: "+ this.event.getExpenses().size());
        System.out.println("invite code: "+ this.event.getInviteCode());
        System.out.println("---------");
    }

    public void settleDebt(){
        mainCtrl.showSettleDebt(this.event);
    }

    public void reassignParticipants(List<Participant> participantList){
        System.out.println(participantList.stream().map(Participant::getName).toList());
        Platform.runLater(() -> {
            textFlow.getChildren().clear();
            if (participantList.isEmpty()) {
                textFlow.getChildren().add(new Label("No participants, yet"));
                return;
            }
            for (Participant p : participantList.subList(0, participantList.size() - 1)) {
                Label label = new Label(p.getName());
                label.setOnMouseClicked(e -> editParticipant(p));
                label.setCursor(Cursor.HAND);
                textFlow.getChildren().add(label);
                textFlow.getChildren().add(new Label(", "));
            }
            Label lastLabel = new Label(participantList.getLast().getName());
            lastLabel.setOnMouseClicked(e -> editParticipant(participantList.getLast()));
            textFlow.getChildren().add(lastLabel);
        });
    }

    public void changeTitle(){
        this.event.setName(this.eventTitle.getText());
        try {
            Event updatedEvent = this.server.updateEvent(this.event);
            notificationService.informUser("Title Change", "The title has been successfully changed!", "Success");
            server.send("/app/events", updatedEvent);
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

    public void addTag() {
        mainCtrl.showAddTagScene(event);
    }

    public void addExpense(){
        mainCtrl.showAddExpense();
    }

    public void editExpense(Expense e) {
        mainCtrl.showEditExpense(e);
    }

    public void removeExpenseAction(Expense e){
        if (!notificationService.showConfirmation(i18n.get("event.overview.delete.event"), i18n.get("event.overview.delete.event.notification"))) {
            return;
        }
        server.removeExpense(event.getId(), e);
        this.refresh();
    }


    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void refresh(){
        try {
            Event refreshed = server.getEvent(event.getId());
            System.out.println("LIST OF TAGS:");
            System.out.println(refreshed.getTags());
            System.out.println("refreshing");
            System.out.println("EXPENSES FROM REFRESH: " + refreshed.getExpenses());
            System.out.println("TITLE: " + refreshed.getTitle());
            this.setEvent(refreshed);
            Platform.runLater(() -> {
                payerSelector.setItems(FXCollections.observableArrayList(event.getParticipants()));
                payerSelector.getSelectionModel().selectFirst();
                eventTitle.setText(this.event.getTitle());
                reassignParticipants(this.event.getParticipants());
                this.expenses = server.getExpensesByEvent(this.event.getId());
                this.setAllFilter();
            });

            /* TO DO:
            * - refresh all data related to the event
            * - add functionality to the expense list and filtering*/
        }catch (WebApplicationException e) {
            notificationService.showError(i18n.get("event.overview.showRefreshingEvent"), i18n.get("event.overview.showRefreshingEventMessage"));
        }
    }

    private BorderPane createRow(Expense e) {
        Insets insets = new Insets(0.0, 5.0, 0.0, 5.0);
        BorderPane bp = new BorderPane();
        double convertedAmount = server.convert(e.getAmount(), e.getCurrency(), String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), e.getDate());
        DecimalFormat df = new DecimalFormat("#.00");
        Text text=new Text((e.getTitle() == null ? "NULL" : e.getTitle() +
                            " - " + df.format(convertedAmount) + " " + mainCtrl.getUser().getPrefferedCurrency()));
        text.setFill(Color.WHITESMOKE);
        bp.setLeft(text);
        BorderPane inner2Bp= new BorderPane();
        BorderPane innerBp = new BorderPane();

        Image editImage = new Image("client/icons/pencil.png");
        ImageView edit = new ImageView();
        edit.setImage(editImage);
        edit.setOnMouseClicked(x -> editExpense(e));
        edit.cursorProperty().set(Cursor.HAND);
        edit.setFitHeight(12.0);
        edit.setPickOnBounds(true);
        edit.setFitWidth(12.0);
        BorderPane.setMargin(edit, insets);
        inner2Bp.setLeft(edit);

        Image removeImage = new Image("client/icons/bin-red.png");
        ImageView remove = new ImageView();
        remove.setImage(removeImage);
        remove.setOnMouseClicked(x -> removeExpenseAction(e));
        remove.cursorProperty().set(Cursor.HAND);
        remove.setFitHeight(12.0);
        remove.setPickOnBounds(true);
        remove.setFitWidth(12.0);
        inner2Bp.setRight(remove);
        BorderPane.setMargin(remove, insets);

        HBox hbox = new HBox();
        List<BorderPane> tags = e.getTags().stream().map(this::pretty).toList();
        hbox.getChildren().addAll(tags);
        hbox.setSpacing(5.0);
        hbox.setPadding(insets);
        innerBp.setLeft(hbox);
        innerBp.setRight(inner2Bp);
        bp.setRight(innerBp);
        return bp;
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
    public void refreshLanguage(){
        this.allFilter.setText(i18n.get("general.all"));
        this.fromFilter.setText(i18n.get("general.from"));
        this.toFilter.setText(i18n.get("general.to"));
    }

    public void setToFilter(){
        filter=1;
        payer = payerSelector.getValue();
        expensesList.getItems().clear();
        expensesList.getItems().addAll(expenses.stream().filter((expense) -> {
            List<UUID> uuids=expense.getDebts().stream().map(debt -> debt.getParticipant().getId()).toList();
            return uuids.contains(payer.getId());
        }).map(this::createRow).toList());
    }

    public void setFromFilter(){
        filter=2;
        payer = payerSelector.getValue();
        expensesList.getItems().clear();
        expensesList.getItems().addAll(expenses.stream().filter((expense -> expense.getPaidBy().getId().equals(payer.getId()))).map(this::createRow).toList());
    }

    public void setAllFilter(){
        filter=0;
        payer = payerSelector.getValue();
        expensesList.getItems().clear();
        expensesList.getItems().addAll(expenses.stream().map(this::createRow).toList());
    }

    //    public double calculateIncoming(Participant p){
//        double incoming = 0;
//        for (Expense e : event.getExpenses()){
//            if (!e.getPaidBy().getId().equals(p.getId())){
//                continue;
//            }
//            List<Debt> debts = e.getDebts();
//            for (Debt d : debts) {
//                if (d.isPaid()) {
//                    continue;
//                }
//                incoming += d.getAmount();
//            }
//        }
//        return incoming;
//    }
//
//    public double calculateOutgoing(Participant p){
//        double outgoing = 0;
//        for (Expense e : event.getExpenses()){
//            if (e.getPaidBy().getId().equals(p.getId())){
//                continue;
//            }
//            List<Debt> debts = e.getDebts();
//            for (Debt d : debts) {
//                if (d.isPaid()) {
//                    continue;
//                }
//                if (d.getParticipant().getId().equals(p.getId())){
//                    outgoing+=d.getAmount();
//                }
//            }
//        }
//        return outgoing;
//    }
    public BorderPane pretty(Tag t) {
        Insets insets = new Insets(2.0, 5.0, 2.0, 5.0);

        BorderPane bp = new BorderPane();
        bp.setBackground(Background.fill(Color.web(t.getColor())));
        String style = """
                -fx-background-radius: 30;
                -fx-border-radius: 30;
                -fx-border-width:0.8;
                -fx-border-color: #F5F5F5;
                """;
        style = style + "-fx-background-color: " + t.getColor();
        bp.setStyle(style);
        bp.setMinHeight(8.0);
        bp.setMaxWidth(300.0);

        Text name = new Text(t.getTag());
        name.setFill(Color.WHITESMOKE);
        name.setCursor(Cursor.HAND);
        BorderPane.setMargin(name, insets);

        bp.setLeft(name);

        return bp;
    }

}
