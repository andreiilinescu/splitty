package client.scenes;

import client.services.NotificationHelper;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class AddExpenseCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private Event event;
    @FXML
    private CheckBox allBox, someBox;
    @FXML
    private ComboBox<String> paidBySelector, currencySelector;
    @FXML
    private TextField howMuchField, tagField;
    @FXML
    private DatePicker whenField;
    @FXML
    private VBox tagSelector, createTagBox, partialPaidSelector;

    @FXML
    ColorPicker colorPicker;

    @Inject
    public AddExpenseCtrl(ServerUtils server, MainCtrl mainCtrl, Event event) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.event = event;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        partialPaidSelector.setVisible(true);
    }

    public void setup(Event event){
        this.event = event;
        createTagBox.setVisible(false);
        paidBySelector.setItems(FXCollections.observableList(event.getParticipants().stream().map(Participant::getName).toList()));
        paidBySelector.setValue(event.getParticipants().get(0).getName());

        howMuchField.setText("");
        someBox.setSelected(false);
        allBox.setSelected(false);

        currencySelector.setItems(FXCollections.observableList(Stream.of("EUR", "USD", "RON").toList()));
        currencySelector.setValue("EUR");
        currencySelector.setVisible(true);

        tagSelector.getChildren().clear();
        for (int i = 0; i<event.getTags().size(); i++){
            renderTag(event.getTags().get(i));
        }

        partialPaidSelector.setVisible(false);
        partialPaidSelector.getChildren().clear();
        for (Participant p : event.getParticipants()){
            partialPaidSelector.getChildren().add(new CheckBox(p.getName()));
        }
    }

    public void createExpense(){
        //get participants
        List<Participant> participantList = new ArrayList<>(event.getParticipants());

        //store who paid
        Participant paidBy = findParticipant(paidBySelector.getValue());
        if (paidBy == null) {
            NotificationHelper notificationHelper = new NotificationHelper();
            String warningMessage = """
                    Your payee information is incorrect
                    please add a valid payee
                    """;
            notificationHelper.showError("Warning", warningMessage);
            return;
        }

        LocalDate date = whenField.getValue();
        if (date == null){
            NotificationHelper notificationHelper = new NotificationHelper();
            String warningMessage = """
                    You have not selected any date
                    please select a valid date
                    """;
            notificationHelper.showError("Warning", warningMessage);
            return;
        }

        if (!someBox.isSelected() && !allBox.isSelected()){
            NotificationHelper notificationHelper = new NotificationHelper();
            String warningMessage = """
                    You have not selected any split options
                    please select how you wish to split
                    or if you wish to split with the whole group
                    """;
            notificationHelper.showError("Warning", warningMessage);
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
            NotificationHelper notificationHelper = new NotificationHelper();
            String warningMessage = """
                    You have not selected an amount to pay
                    please type in an amount
                    """;
            notificationHelper.showError("Warning", warningMessage);
            return;
        }

        if (Integer.parseInt(howMuchField.getText()) < 0){
            NotificationHelper notificationHelper = new NotificationHelper();
            String warningMessage = """
                    You cannot select a negative amount
                    please type in a positive number
                    """;
            notificationHelper.showError("Warning", warningMessage);
            return;
        }

        List<Debt> debts = createDebts(toEur(Double.parseDouble(howMuchField.getText()), currencySelector.getValue()), participantList);

        //create the list of tags (God bless the creator of stream() :) )
        List<Tag> tags = tagSelector.getChildren().stream().filter(n -> n.getClass() == CheckBox.class).map(n -> ((CheckBox) n)).filter(CheckBox::isSelected).map(Labeled::getText).map(this::findTag).toList();

        if (tags.isEmpty()){
            NotificationHelper notificationHelper = new NotificationHelper();
            String warningMessage = """
                    You have not selected any tags
                    please create a tag.
                    Or select a pre-existing one.
                    """;
            notificationHelper.showError("Warning", warningMessage);
            return;
        }

        //create the expense
        Expense newExpense = new Expense(paidBy.getName() + " paid for " + tags.get(0).getTag(), Double.parseDouble(howMuchField.getText()), date.atStartOfDay(), paidBy, event, debts, tags);
        for (Debt d : newExpense.getDebts()){
            d.setExpense(newExpense); //setup each debt's expense pointer
        }
        event.addExpense(newExpense);
        //server.updateEvent(event);
        //the line above yields a HTTP 500 error
        mainCtrl.showEventOverviewScene(event);
    }

    private List<Debt> createDebts(double amount, List<Participant> participants){
        List<Debt> debts = new ArrayList<>();
        for (Participant p : participants) {
            debts.add(new Debt(new Expense(), p, amount/participants.size() + 1));
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

    public void createTag(){
        String tagName = tagField.getText();
        if (tagName == null || tagName.isEmpty() || event.getTags().stream().map(Tag::getTag).toList().contains(tagName)){
            NotificationHelper notificationHelper = new NotificationHelper();
            String warningMessage = """
                    Invalid or already existent tag.
                    Please choose another name for your tag.
                    """;
            notificationHelper.showError("Warning", warningMessage);
        }
        else {
            Color color = colorPicker.getValue();
            Tag tag = new Tag(tagName, new double[]{color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity()});
            event.addTag(tag);
            renderTag(tag);
            closeCreateTag();
        }
    }

    public void openTagScene(){
        createTagBox.setVisible(true);
    }

    public void closeCreateTag(){
        createTagBox.setVisible(false);
    }
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

    private void renderTag(Tag tag){
        CheckBox checkbox = new CheckBox(tag.getTag());
        Rectangle rectangle = new Rectangle(-5, 0, 100, 20);
        Group g = new Group();
        g.getChildren().add(checkbox);
        g.getChildren().add(rectangle);
        rectangle.setFill(new Color(tag.getColorValues()[0], tag.getColorValues()[1], tag.getColorValues()[2], tag.getColorValues()[3]));
        rectangle.toBack();
        rectangle.setOpacity(0.5);
        rectangle.setArcWidth(15);
        rectangle.setArcHeight(15);
        tagSelector.getChildren().add(g);
    }
}
