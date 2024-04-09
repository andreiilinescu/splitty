package client.scenes;

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
    private Event event;

//    @FXML
//    private PieChart pieStats;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Debts table
        tFrom.setCellValueFactory(new PropertyValueFactory<>("from"));
        tTo.setCellValueFactory(new PropertyValueFactory<>("to"));
        tAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        tExpenseName.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        //Share table
        tShare.setCellValueFactory(new PropertyValueFactory<>("shareAmount"));
        tParticipantShare.setCellValueFactory(new PropertyValueFactory<>("shareFrom"));
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
            data.add(new ShareRow(p.getName(), getSharePerParticipant(p)));
        }
        sharesTable.setItems(data);
    }

    public void updatePieChart() {
        PieChart pieStats = new PieChart();
        pieStats.setStyle("""
                -fx-text-fill: white;
                -fx-border-width: 0;
                -fx-padding: 0;
                -fx-background-color: #ffffff
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
        List<String> colors = Arrays.asList(
                "#FF6347",
                "#FFD700",
                "#40E0D0",
                "#EE82EE",
                "#ADFF2F");
        int colorIndex = 0;
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChartData.add(data);
            colorIndex++;
        }
        pieStats.setData(pieChartData);

        // we can set the color for each slice.
        colorIndex = 0;
        for (PieChart.Data data : pieStats.getData()) {
            data.getNode().setStyle("-fx-pie-color: " + colors.get(colorIndex % colors.size()) + ";");
            colorIndex++;
        }

        pieStats.setLabelsVisible(true);
        pieStats.setLegendVisible(false);
        piechartPane.getChildren().clear();
        piechartPane.getChildren().add(pieStats);
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
        //+ How much this person owes others.
        double total = 0; //in user preferred currency.
        for (Expense expense : event.getExpenses()){
            if (!expense.getPaidBy().getId().equals(p.getId())){
                //check for debts by this participant.
                for (Debt debtor : expense.getDebts()){
                    if (debtor.getParticipant().getId().equals(p.getId())){ //if this debt belongs to this participant then add it to his share.
                        total+=server.convert(debtor.getAmount(), expense.getCurrency(),  String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), expense.getDate());
                    }
                }
            }else {
                total+=server.convert(expense.getAmount(), expense.getCurrency(),  String.valueOf(mainCtrl.getUser().getPrefferedCurrency()), expense.getDate()); //add the money this participant paid.
            }
        }
        return total;
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
        private final double shareAmount;
        public ShareRow(String shareFrom, double shareAmount){
            this.shareFrom=shareFrom;
            this.shareAmount=shareAmount;
        }

        public String getShareFrom() {
            return shareFrom;
        }

        public double getShareAmount() {
            return shareAmount;
        }
    }

    public class StatsRow {
        private final String from;
        private final String to;
        private final double amount;
        private final String expenseName;

        public StatsRow(String name, String to, double amount, String expenseName) {
            this.from = name;
            this.to = to;
            this.amount = Math.round(amount * 100.0) / 100.0;
            this.expenseName = expenseName;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public double getAmount() {
            return amount;
        }

        public String getExpenseName() {
            return expenseName;
        }
    }
}

