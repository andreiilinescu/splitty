package client.scenes;

import client.services.I18NService;
import client.services.NotificationService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;


public class AddExpenseCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NotificationService notificationService;
    private Event event;
    private Expense expense;

    private TextInputDialog tagDialog;

    @FXML
    private Button submitButton;
    @FXML
    private CheckBox allBox, someBox;
    @FXML
    private ComboBox<String> paidBySelector, currencySelector;
    @FXML
    private TextField howMuchField;
    @FXML
    private DatePicker whenField;
    @FXML
    private VBox tagSelector, partialPaidSelector;
    @FXML
    private TextField titleField;

    @FXML
    private Text title;
    @FXML
    private Text paid;
    @FXML
    private Text when;
    @FXML
    private Text amount;
    @FXML
    private Text how;
    @FXML
    private Button abortButton;

    private final I18NService i18n;



    @Inject
    public AddExpenseCtrl(ServerUtils server, MainCtrl mainCtrl, Event event, NotificationService notificationService, I18NService i18n) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.event = event;
        this.i18n = i18n;
        this.notificationService=notificationService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        i18n.update(paid);
        i18n.update(when);
        i18n.update(amount);
        i18n.update(how);
        i18n.update(abortButton);
        i18n.update(allBox);
        i18n.update(someBox);
        //this.prepareTagDialog();
    }
    @FXML
    public void backToOverview(){
        mainCtrl.showEventOverviewScene(event);
    }

    @FXML
    public void checkAll(){
        someBox.setSelected(false);
        partialPaidSelector.setVisible(false);
    }

    @FXML
    public void checkSome(){
        System.out.println(paidBySelector.getValue());
        allBox.setSelected(false);
        partialPaidSelector.setVisible(someBox.isSelected());
    }

    public void setup(Event event, Expense e){
        this.event = event;
        this.expense = e;
        paidBySelector.setItems(FXCollections.observableList(event.getParticipants().stream().map(Participant::getName).toList()));
        paidBySelector.setValue(event.getParticipants().get(0).getName());

        howMuchField.setText("");
        someBox.setSelected(false);
        allBox.setSelected(false);

        currencySelector.setItems(FXCollections.observableList(Stream.of("EUR", "USD", "CHF", "RON").toList()));
        currencySelector.setValue("EUR");
        currencySelector.setVisible(true);

        tagSelector.getChildren().clear();
        for (int i = 1; i<event.getTags().size() + 1; i++){
            CheckBox checkbox = new CheckBox(event.getTags().get(i-1).getTag());
            Platform.runLater(() -> tagSelector.getChildren().add(checkbox));
        }
        tagSelector.setVisible(true);

        partialPaidSelector.setVisible(false);
        partialPaidSelector.getChildren().clear();
        for (Participant p : event.getParticipants()){
            partialPaidSelector.getChildren().add(new CheckBox(p.getName()));
        }
        if (expense == null) {
            showNewExpense();
        } else {
            setupExistingExpense(expense);
        }
    }

    private void showNewExpense() {
        i18n.update(submitButton, "general.add");
    }

    private void setupExistingExpense(Expense expense){
        i18n.update(submitButton, "general.save");

        titleField.setText(expense.getTitle());
        paidBySelector.setValue(expense.getPaidBy().getName());
        howMuchField.setText(String.valueOf(expense.getAmount()));
        whenField.setValue(expense.getDate().atZone(ZoneId.systemDefault()).toLocalDate());

        //check partial debtors if any
        boolean partialPay = false;
        Participant whoPaid = expense.getPaidBy();
        List<Participant> debtors = expense.getDebts().stream().map(Debt::getParticipant).toList();
        for (Participant p : event.getParticipants()){
            if (!p.getId().equals(whoPaid.getId()) && !debtors.contains(p)){
                System.out.println("This happens " + p.getEvent() + " " + whoPaid.getEvent());
                checkSome();
                partialPay = true;
            }
        }
        if (partialPay){
            for (Participant p : debtors){
                for (Node c : partialPaidSelector.getChildren()){
                    if (c.getClass() == CheckBox.class){
                        if (((CheckBox) c).getText().equals(p.getName())){
                            ((CheckBox) c).setSelected(true);
                        }
                    }
                }
            }
            someBox.setSelected(true);
            checkSome();
        }
        else {
            allBox.setSelected(true);
            checkAll();
        }

        //check selected tags
        for (Tag t : expense.getTags()){
            for (Node c : tagSelector.getChildren()){
                if (c.getClass() == CheckBox.class){
                    if (((CheckBox) c).getText().equals(t.getTag())){
                        ((CheckBox) c).setSelected(true);
                    }
                }
            }
        }
    }

    public void createExpense(){
        //get participants
        List<Participant> participantList = new ArrayList<>(event.getParticipants());

        //store who paid
        Participant paidBy = findParticipant(paidBySelector.getValue());
        if (paidBy == null) {
            String warningMessage = i18n.get("expense.add.error.emptyPayee");
            notificationService.showError(i18n.get("general.warning"), warningMessage);
            return;
        }

        LocalDate date = whenField.getValue();
        if (date == null){
            String warningMessage = i18n.get("expense.add.error.emptyDate");
            notificationService.showError(i18n.get("general.warning"), warningMessage);
            return;
        }

        if (!someBox.isSelected() && !allBox.isSelected()){
            String warningMessage = i18n.get("expense.add.error.emptySplit");
            notificationService.showError(i18n.get("general.warning"), warningMessage);
            return;
        }

        //create a list of debtors
        participantList.remove(paidBy);
        if (someBox.isSelected()){
            for (Node c : partialPaidSelector.getChildren()){
                if (c.getClass() == CheckBox.class && !((CheckBox) c).isSelected()){
                    participantList.remove(findParticipant(((CheckBox) c).getText()));
                }
            }
        }

        if (howMuchField.getText() == null || howMuchField.getText().isEmpty()){
            String warningMessage = i18n.get("expense.add.error.emptyAmount");
            notificationService.showError(i18n.get("general.warning"), warningMessage);
            return;
        }

        if (Double.parseDouble(howMuchField.getText()) < 0.0){
            String warningMessage = i18n.get("expense.add.error.negativeAmount");
            notificationService.showError(i18n.get("general.warning"), warningMessage);
            return;
        }

        List<Debt> debts = createDebts(toEur(Double.parseDouble(howMuchField.getText()), currencySelector.getValue()), participantList);

        //create the list of tags (God bless the creator of stream() :) )
        List<Tag> tags = tagSelector.getChildren().stream()
                .filter(n -> n.getClass() == CheckBox.class)
                .map(n -> ((CheckBox) n)).filter(CheckBox::isSelected)
                .map(Labeled::getText).map(this::findTag).toList();
        String title=titleField.getText();
        if (title.isBlank()){
            title=paidBy.getName() + " paid";
        }
        //create the expense
        Expense newExpense = new Expense(title,
                Double.parseDouble(howMuchField.getText()),
                currencySelector.getValue(),
                Instant.from(date.atStartOfDay(
                        java.time.ZoneId.systemDefault()
                )),
                paidBy,
                event,
                debts,
                tags
                );
        for (Debt d : newExpense.getDebts()){
            d.setExpense(newExpense); //setup each debt's expense pointer
        }

        Expense e;
        if (expense == null) {
            expense = server.addExpense(event.getId(), newExpense);
        } else {
            server.updateExpense(event.getId(), expense.getId(), newExpense);
        }
        for (Tag t : tags) {
            server.addExpenseTag(event.getId(), t.getId(), expense.getId());
        }
        server.send("/app/events", event);
        mainCtrl.showEventOverviewScene(event);
    }
    private List<Debt> createDebts(double amount, List<Participant> participants){
        List<Debt> debts = new ArrayList<>();
        for (Participant p : participants) {
            debts.add(new Debt(new Expense(), p, amount/(participants.size()+1)));
        }
        return debts;
    }

    //temporary method to convert other currencies in EUR
    private double toEur(double amount, String curr){
        return switch (curr) {
            case "USD" -> amount * mainCtrl.getUsdToEur();
            case "RON" -> amount * mainCtrl.getRonToEur();
            default -> amount;
        };
    }

    public void createTag(String tagName){
        Tag tag = new Tag(tagName);
        event.addTag(tag);
        tagSelector.getChildren().add(new CheckBox(tag.getTag()));
    }

//    public void openTagDialog(){
//        tagDialog.getEditor().clear();
//        Optional<String> result = tagDialog.showAndWait();
//        result.ifPresent(this::createTag); // if ok is pressed check password
//    }

//    public void prepareTagDialog(){
//        tagDialog=new TextInputDialog();
//        tagDialog.setTitle(I18N.get("expense.add.createTag"));
//        tagDialog.setContentText(I18N.get("expense.add.createTagText"));
//        tagDialog.setHeaderText("");
//        tagDialog.setGraphic(null);
//    }

    public Participant findParticipant(String name){
        Participant r = null;
        for (Participant p : event.getParticipants()){
            if (p.getName().equals(name)){
                r = p;
            }
        }
        return r;
    }

    public Tag findTag(String name){
        Tag t = null;
        for (Tag tag : event.getTags()){
            if (tag.getTag().equals(name)){
                t = tag;
            }
        }
        return t;
    }

    public Event getEvent() {
        return event;
    }
}
