package client.scenes;

import client.services.I18N;
import client.services.NotificationService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.*;

public class StatisticsCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final NotificationService notificationService;
    @FXML
    public Label expenseLabel;
    @FXML
    public Label backButtonLabel;
    @FXML
    public TableColumn<StatsRow, String> tFrom;
    @FXML
    public TableColumn<StatsRow, Double> tTo;
    @FXML
    public TableColumn<StatsRow, Double> tAmount;
    @FXML
    public TableColumn<StatsRow, String> tExpenseName;
    @FXML
    public TableView<StatsRow> tableView;
    @FXML
    public Pane piechartPane;
    @FXML
    public TableView sharesTable;
    @FXML
    public TableColumn tParticipantShare;
    @FXML
    public TableColumn tShare;
    @FXML
    public TableColumn tOwes;
    @FXML
    public TableColumn tOwed;
    private Event event;

//    @FXML
//    private PieChart pieStats;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        I18N.update(backButtonLabel);
        //Debts table
        tFrom.setCellValueFactory(new PropertyValueFactory<>("from"));
        tTo.setCellValueFactory(new PropertyValueFactory<>("to"));
        tAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        tExpenseName.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        //Share table
        tShare.setCellValueFactory(new PropertyValueFactory<>("shareAmount"));
        tParticipantShare.setCellValueFactory(new PropertyValueFactory<>("shareFrom"));
        tOwes.setCellValueFactory(new PropertyValueFactory<>("owes"));
        tOwed.setCellValueFactory(new PropertyValueFactory<>("owed"));
    }

    @Inject
    public StatisticsCtrl(ServerUtils server, MainCtrl mainCtrl, NotificationService notificationService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.notificationService = notificationService;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Updates the Table for the shares per person.
     */
    public void updateShares(){
        sharesTable.getItems().clear();
        ObservableList<ShareRow> data = FXCollections.observableArrayList();
        for (Participant p : event.getParticipants()){
            data.add(new ShareRow(p.getName(), getSharePerParticipant(p), calculateOutgoing(p), calculateIncoming(p)));
        }
        sharesTable.setItems(data);
    }

    public double calculateIncoming(Participant p){
        double incoming = 0;
        for (Expense e : event.getExpenses()){
            if (!e.getPaidBy().getId().equals(p.getId())){
                continue;
            }
            List<Debt> debts = e.getDebts();
            for (Debt d : debts) {
                if (d.isPaid()) {
                    continue;
                }
                incoming += server.convert(d.getAmount(), e.getCurrency(), String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), e.getDate());
            }
        }
        return incoming;
    }

    public double calculateOutgoing(Participant p){
        double outgoing = 0;
        for (Expense e : event.getExpenses()){
            if (e.getPaidBy().getId().equals(p.getId())){
                continue;
            }
            List<Debt> debts = e.getDebts();
            for (Debt d : debts) {
                if (d.isPaid()) {
                    continue;
                }
                if (d.getParticipant().getId().equals(p.getId())){
                    outgoing+=server.convert(d.getAmount(), e.getCurrency(), String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), e.getDate());
                }
            }
        }
        return outgoing;
    }


    public void updatePieChart() {
        PieChart pieStats = new PieChart();
        pieStats.setStyle("""
                -fx-text-fill: white;
                -fx-border-width: 0;
                -fx-padding: 0;
                -fx-background-color: #242424
                """);
        //set colors for the pie chart slices
        pieStats.setCache(false);
        pieStats.getData().clear();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        Map<String, Double> stats = new HashMap<>();
        for (Expense e : event.getExpenses()) {
            for (Tag t : e.getTags()) {
                double amount;
                if (e.getCurrency().equals(String.valueOf(mainCtrl.getUser().getPrefferedCurrency()))){
                    amount = e.getAmount();
                } else {
                    amount = server.convert(e.getAmount(), e.getCurrency(), String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), e.getDate());
                }
                stats.put(t.getTag(), stats.getOrDefault(t.getTag(), 0.0) + amount);
            }
        }
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + ": " +
                  Math.round(entry.getValue() / getTotalSumOfExpenses() * 100.0)+
                    "%", entry.getValue()));
        }
        pieStats.setData(pieChartData);
        //set colors for the pie chart slices
        for (int i = 0; i < pieChartData.size(); i++) {
            PieChart.Data data = pieChartData.get(i);
            data.getNode().setStyle("-fx-pie-color: " + searchColor(data.getName().split(":")[0], event.getTags()));
        }

        pieStats.setLabelsVisible(true);
        pieStats.setLegendVisible(false);
        piechartPane.getChildren().clear();
        piechartPane.getChildren().add(pieStats);
    }
    public String searchColor(String tagName, List<Tag> tags){
        for (Tag t : tags){
            if (t.getTag().equals(tagName)){
                return t.getColor();
            }
        }
        return "#000000";
    }

    public void setParticipantStats() {
        tableView.getItems().clear();
        ObservableList<StatsRow> data = FXCollections.observableArrayList();
        for (Expense e : event.getExpenses()) {
            for (Debt d : e.getDebts()) {
                if (d.isPaid()) {
                    continue;
                }
                double amount;
                if (e.getCurrency().equals(String.valueOf(mainCtrl.getUser().getPrefferedCurrency()))){
                    amount = d.getAmount();
                } else {
                    amount = server.convert(d.getAmount(), e.getCurrency(), String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), e.getDate());
                }
                data.add(new StatsRow(d.getParticipant().getName(), e.getPaidBy().getName(), amount, e.getTitle()));
            }
        }
        tableView.setItems(data);
    }

    /**
     * Used while developing to generate dummy data for the pie chart
     * @return a dummy event with expenses
     */
    public Event getDummyPieChartData() {
        List<Tag> fakeTags = new ArrayList<>();
        fakeTags.add(new Tag("Food"));
        fakeTags.add(new Tag("Drinks"));
        fakeTags.add(new Tag("Transport"));
        fakeTags.add(new Tag("Accommodation"));
        fakeTags.add(new Tag("Activities"));


        Event e = new Event("Event ");

        List<Tag> tags = new ArrayList<>();
        //add tags randomly
        int maxTags = new Random().nextInt(3);
        for (int j = 0; j < maxTags; j++) {
            tags.add(fakeTags.get(new Random().nextInt(fakeTags.size())));
        }

        for (int j = 0; j < 5; j++) {
            Expense exp = new Expense(
                    "Expense " + j,
                    10,
                    new Date().toInstant(),
                    null,
                    e,
                    new ArrayList<>(),
                    tags
            );
            exp.addTag(fakeTags.get(j));
            e.addExpense(exp);
        }
        return e;

    }

    /**
     * Share per participant is defined as : Expenses paid by p + debts to be paid by p.
     * @param p participant to check the share for.
     * @return total in user preferred currency
     *
     */
    public double getSharePerParticipant(Participant p){
        //For p
        //Sum of expenses this participant paid.
        double total = 0; //in user preferred currency.
        for (Expense expense : event.getExpenses()){
            if (expense.getPaidBy().getId().equals(p.getId())){
                total+=server.convert(expense.getAmount(), expense.getCurrency(),  String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), expense.getDate()); //add the money this participant paid.
            }
        }
        return total;
    }
    public double getTotalSumOfExpenses(){
        double sum = 0;
        for (Expense e : event.getExpenses()) {
            if (e.getCurrency().equals(String.valueOf(mainCtrl.getUser().getPrefferedCurrency()))){
                sum += e.getAmount();
                continue;
            }
            double convertedAmount = server.convert(e.getAmount(), e.getCurrency(), String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), e.getDate());
            sum += convertedAmount;
        }
        return sum;
    }
    public void setSumOfExpenses() {
        double sum = 0;
        for (Expense e : event.getExpenses()) {
            if (e.getCurrency().equals(String.valueOf(mainCtrl.getUser().getPrefferedCurrency()))){
                sum += e.getAmount();
                continue;
            }
            double convertedAmount = server.convert(e.getAmount(), e.getCurrency(), String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), e.getDate());
            sum += convertedAmount;
        }
        expenseLabel.setText("Total expenses: " + String.format("%.2f", sum) + " " + mainCtrl.getUser().getPrefferedCurrency());
    }

    public void back() {
        mainCtrl.showEventOverviewScene(event);
    }

    public void refresh() {
        updatePieChart();
        setSumOfExpenses();
        setParticipantStats();
        updateShares();
    }

    public Event getEvent() {
        return event;
    }

    public class ShareRow{
        private final String shareFrom;
        private final String shareAmount;
        private final String owes;
        private final String owed;
        public ShareRow(String shareFrom, double shareAmount, double owes, double owed){
            this.shareFrom=shareFrom;
            this.shareAmount= shareAmount + " " + mainCtrl.getUser().getPrefferedCurrency();
            this.owes = owes + " " + mainCtrl.getUser().getPrefferedCurrency();
            this.owed = owed + " " + mainCtrl.getUser().getPrefferedCurrency();
        }

        public String getShareFrom() {
            return shareFrom;
        }

        public String getShareAmount() {
            return shareAmount;
        }
        public String getOwes() {
            return owes;
        }
        public String getOwed() {
            return owed;
        }
    }

    public class StatsRow {
        private final String from;
        private final String to;
        private final String amount;
        private final String expenseName;

        public StatsRow(String name, String to, double amount, String expenseName) {
            this.from = name;
            this.to = to;
            this.amount = (Math.round(amount * 100.0) / 100.0) + " " + mainCtrl.getUser().getPrefferedCurrency();
            this.expenseName = expenseName;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getAmount() {
            return amount;
        }

        public String getExpenseName() {
            return expenseName;
        }
    }
}

