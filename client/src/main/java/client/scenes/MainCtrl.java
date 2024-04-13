/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.services.I18N;
import client.services.I18NService;
import client.services.NotificationHelper;
import client.services.NotificationService;
import client.utils.Config;
import client.utils.User;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Locale;
import java.util.UUID;

public class MainCtrl {

    private User user;
    public boolean admin;

    private Stage primaryStage;
    private FirstTimeCtrl firstTimeCtrl;

    private Scene firstTime;

    private EventOverviewCtrl eventOverviewCtrl;
    private Scene eventOverview;

    private AddParticipantCtrl addParticipantCtrl;
    private Scene addParticipant;


    private EditParticipantCtrl editParticipantCtrl;
    private Scene editParticipant;

    private StartCtrl startCtrl;
    private Scene start;

    private AddExpenseCtrl addExpenseCtrl;
    private Scene addExpense;
    private Scene inviteView;
    private InviteViewCtrl inviteViewCtrl;
    private UserSettingsCtrl userSettingsCtrl;
    private Scene userSettings;

    private SettingsCtrl settingsCtrl;
    private Scene settings;

    private LanguageCtrl languageCtrl;
    private Scene languages;
    private Scene statistics;
    private StatisticsCtrl statisticsCtrl;

    private DebtResolveCtrl debtResolveCtrl;
    private Scene debtResolve;

    private AdminEventsCtrl adminEventsCtrl;
    private Scene adminEvents;

    private AddTagCtrl addTagCtrl;
    private Scene addTag;

    private EditTagCtrl editTagCtrl;
    private Scene editTag;

    private NotificationService notificationService;

    private final I18NService i18n = new I18N();

    public void initialize(Stage primaryStage, Pair<FirstTimeCtrl, Parent> firstTime,
                           Pair<EventOverviewCtrl, Parent> eventOverview,
                           Pair<AddParticipantCtrl, Parent> addParticipant,
                           Pair<StartCtrl, Parent> start,
                           Pair<AddExpenseCtrl, Parent> addExpense,
                           Pair<InviteViewCtrl, Parent> inviteView,
                           Pair<EditParticipantCtrl, Parent> editParticipant,
                           Pair<UserSettingsCtrl, Parent> userSettings,
                           Pair<SettingsCtrl, Parent> settings,
                           Pair<AdminEventsCtrl, Parent> adminEvents,
                           Pair<AddTagCtrl, Parent> addTag,
                           Pair<EditTagCtrl, Parent> editTag,
                           Pair<LanguageCtrl, Parent> languages,
                           boolean adminMode,
                            Pair<StatisticsCtrl, Parent> statistics
    ) {
        this.admin=false;
        this.user = new User();
        this.primaryStage = primaryStage;

        this.firstTimeCtrl = firstTime.getKey();
        this.firstTime = new Scene(firstTime.getValue());

        this.eventOverviewCtrl = eventOverview.getKey();
        this.eventOverview = new Scene(eventOverview.getValue());

        this.startCtrl = start.getKey();
        this.start = new Scene(start.getValue());

        this.addExpenseCtrl = addExpense.getKey();
        this.addExpense = new Scene(addExpense.getValue());

        this.user = new User();

        //showStartScene();
        //primaryStage.show();
        this.addParticipantCtrl = addParticipant.getKey();
        this.addParticipant = new Scene(addParticipant.getValue());

        this.editParticipantCtrl = editParticipant.getKey();
        this.editParticipant = new Scene(editParticipant.getValue());

        this.inviteView = new Scene(inviteView.getValue());
        this.inviteViewCtrl = inviteView.getKey();

        this.userSettingsCtrl = userSettings.getKey();
        this.userSettings = new Scene(userSettings.getValue());

        this.settingsCtrl = settings.getKey();
        this.settings = new Scene(settings.getValue());

        this.languageCtrl = languages.getKey();
        this.languages=new Scene(languages.getValue());

        this.adminEventsCtrl = adminEvents.getKey();
        this.adminEvents = new Scene(adminEvents.getValue());

        this.addTagCtrl = addTag.getKey();
        this.addTag = new Scene(addTag.getValue());

        this.editTagCtrl = editTag.getKey();
        this.editTag = new Scene(editTag.getValue());

        this.statisticsCtrl = statistics.getKey();
        this.statistics = new Scene(statistics.getValue());

        primaryStage.getIcons().add(new Image("client/icons/app-icon.png"));

        chooseFirstPage(adminMode);

        // in eventoverview, press alt+1 to go back to start
        eventOverview.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.isAltDown()){
                    switch (event.getCode()) {
                        case KeyCode.DIGIT1, KeyCode.HOME -> showStartScene();
                        case KeyCode.S -> showSettings();
                        case KeyCode.E -> showAddExpense();
                        case KeyCode.P -> showAddParticipantScene(eventOverviewCtrl.getEvent());
                        case KeyCode.I -> {
                            inviteViewCtrl.setEvent(eventOverviewCtrl.getEvent());
                            showInviteView(eventOverviewCtrl.getEvent());
                        }
                        case KeyCode.T -> showStatistics(eventOverviewCtrl.getEvent());
                        default -> {
                        }
                    }

                }
            }
        });
        initShortcuts(start, settings, inviteView, userSettings, addParticipant, editParticipant, languages, addExpense, statistics);
        this.notificationService = new NotificationHelper();
    }

    private void initShortcuts(
            Pair<StartCtrl, Parent> start,
            Pair<SettingsCtrl, Parent> settings,
            Pair<InviteViewCtrl, Parent> inviteView,
            Pair<UserSettingsCtrl, Parent> userSettings,
            Pair<AddParticipantCtrl, Parent> addParticipant,
            Pair<EditParticipantCtrl, Parent> editParticipant,
            Pair<LanguageCtrl, Parent> languages,
            Pair<AddExpenseCtrl, Parent> addExpense,
            Pair<StatisticsCtrl, Parent> statistics
    ) {
        statistics.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    showEventOverviewScene(statisticsCtrl.getEvent());
                }
            }
        });
        // in start, press alt+s to go to settings
        start.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.isAltDown() && event.getCode() == KeyCode.S) {
                    showSettings();
                }
                if (event.isAltDown() && event.getCode() == KeyCode.H) {
                    startCtrl.shortCuts();
                }
            }
        });

        // in settings, press alt+1 to go back to start
        settings.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.isAltDown() && event.getCode() == KeyCode.ESCAPE) {
                    showStartScene();
                }
            }
        });
        inviteView.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    showEventOverviewScene(inviteViewCtrl.getEvent());
                }
            }
        });

        userSettings.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    showSettings();
                }
            }
        });

        editParticipant.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    showEventOverviewScene(editParticipantCtrl.getEvent());
                }
            }
        });
        settings.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    showStartScene();
                }
            }
        });

        addParticipant.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    showEventOverviewScene(addParticipantCtrl.getEvent());
                }
            }
        });


        addExpense.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    showEventOverviewScene(addExpenseCtrl.getEvent());
                }
            }
        });

        languages.getValue().addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    showSettings();
                }
            }
        });

    }

    public void showInviteView(Event event){
        primaryStage.setTitle(i18n.get("window.invite"));
        inviteViewCtrl.setEvent(event);
        primaryStage.setScene(inviteView);
    }
    public void chooseLanguage(){
        switch (this.getUser().getLanguage()){
            case "english"-> this.switchToEnglish();
            case "dutch" -> this.switchToDutch();
            default -> System.out.println("language not implemented");
        }
    }

    /**
     * Choose the first page to show
     *
     * @param adminMode - if adminMode is true, directly show admin page and skip normal user page
     */
    public void chooseFirstPage(boolean adminMode) {
        this.user = Config.readUserConfigFile();
        if (adminMode) {
            this.showAdminEventsScene();
            primaryStage.show();
        } else {
            if (user == null) {
                this.showFirstTimeScene();
                primaryStage.show();

            } else {
                chooseLanguage();
                this.showStartScene();
                primaryStage.show();
            }
        }
    }
    public void showFirstTimeScene(){
        primaryStage.setTitle(i18n.get("window.setup"));
        primaryStage.setScene(this.firstTime);
    }

    public void showStartScene() {
        primaryStage.setTitle(i18n.get("window.start"));
        startCtrl.addRecentEvents();
        primaryStage.setScene(start);
    }

    public  void showSettings(){
        primaryStage.setTitle(i18n.get("window.settings"));
        primaryStage.setScene(settings);
    }

    public void showLanguageOptions() {
        primaryStage.setTitle(i18n.get("window.settings.language"));
        primaryStage.setScene(languages);
    }

    public void switchToEnglish() {
        i18n.setLocale(Locale.ENGLISH);
        startCtrl.setFlag("english");
        eventOverviewCtrl.setFlag("english");
        eventOverviewCtrl.refreshLanguage();
        this.user.setLanguage("english");
        Config.writeUserConfigFile(this.user);
    }

    public void switchToDutch() {
        Locale dutch = i18n.getSupportedLocales().get(1);
        i18n.setLocale(dutch);
        startCtrl.setFlag("dutch");
        eventOverviewCtrl.setFlag("dutch");
        eventOverviewCtrl.refreshLanguage();
        this.user.setLanguage("dutch");
        Config.writeUserConfigFile(this.user);
    }

    public void switchToRomanian(){
        Locale romanian = i18n.getSupportedLocales().get(2);
        i18n.setLocale(romanian);
        startCtrl.setFlag("romanian");
        eventOverviewCtrl.setFlag("romanian");
        eventOverviewCtrl.refreshLanguage();
        this.user.setLanguage("romanian");
        Config.writeUserConfigFile(this.user);
    }

    public void switchToUserLanguage() {
        Locale userLang = i18n.getSupportedLocales().get(3);
        i18n.setLocale(userLang);
        eventOverviewCtrl.refreshLanguage();
        this.user.setLanguage("Custom Language");
        Config.writeUserConfigFile(this.user);
    }

    public void uponLanguageSwitch(){
        primaryStage.setTitle(i18n.get("window.settings.language"));
        String switchLanguageHeader = i18n.get("language.infoLanguages");
        String switchLanguageTitle = i18n.get("language.switchTitle");
        String switchLanguageMessage = i18n.get("language.switchMessage");
        NotificationHelper notificationHelper = new NotificationHelper();
        notificationHelper.informUser(switchLanguageTitle, switchLanguageMessage, switchLanguageHeader);
    }

    public  void showUserSettings(){
        primaryStage.setTitle(i18n.get("window.settings.profile"));
        userSettingsCtrl.refreshFields();
        primaryStage.setScene(userSettings);

    }

    // TODO Both setEvent and refresh call the setEvent function
    public void showEventOverviewScene(Event newEvent){
        primaryStage.setTitle(i18n.get("window.event.overview"));
        eventOverviewCtrl.refreshLanguage();
        eventOverviewCtrl.setEvent(newEvent);
        eventOverviewCtrl.refresh();
        primaryStage.setScene(eventOverview);
    }
    public void showAddParticipantScene(Event event) {
        primaryStage.setTitle(i18n.get("window.event.participant.add"));
        addParticipantCtrl.setEvent(event);
        primaryStage.setScene(addParticipant);
    }

    public void showEditParticipantScene(Event event, Participant p) {
        primaryStage.setTitle(i18n.get("window.event.participant.edit"));
        editParticipantCtrl.setEvent(event);
        editParticipantCtrl.setParticipant(p);
        editParticipantCtrl.refresh();
        primaryStage.setScene(editParticipant);
    }

    public void showAdminEventsScene() {
        primaryStage.setTitle(i18n.get("window.event.admin"));
        adminEventsCtrl.populateList();
        primaryStage.setScene(adminEvents);
    }

    public void showAddTagScene(Event e) {
        primaryStage.setTitle(i18n.get("window.tags"));
        addTagCtrl.setUp(e.getId());
        primaryStage.setScene(addTag);
    }

    public void showEditTagScene(Tag t, Event e) {
        primaryStage.setTitle(i18n.get("window.edit.tags"));
        editTagCtrl.setUp(t, e);
        primaryStage.setScene(editTag);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public User getUser(){
        return this.user;
    }

    public  void setUser(User user){
        this.user=user;
        Config.writeUserConfigFile(user);
    }

    public void showStatistics(Event event){
        primaryStage.setTitle("Statistics");
        statisticsCtrl.setEvent(event);
        statisticsCtrl.refresh();
        primaryStage.setScene(statistics);
    }

    public void addUserEvent(UUID event, UUID participant){
        this.user.addEventParticipant(event, participant);
        Config.writeUserConfigFile(user);
        System.out.println(Config.readUserConfigFile());
    }

    public void showAddExpense(){
        primaryStage.setTitle(i18n.get("window.expense"));
        addExpenseCtrl.setup(eventOverviewCtrl.getEvent(), null);
        primaryStage.setScene(addExpense);
    }

    public void showEditExpense(Expense e){
        primaryStage.setTitle("Splitty: Edit Expense");
        addExpenseCtrl.setup(eventOverviewCtrl.getEvent(), e);
        primaryStage.setScene(addExpense);
    }

    public void showSettleDebt(Event event) {
        primaryStage.setTitle(i18n.get("window.debt"));
        debtResolveCtrl.setEvent(event);
        debtResolveCtrl.refresh();
        primaryStage.setScene(debtResolve);
    }

    public void deleteAllData(){
        if (!notificationService.showConfirmation("Delete data", "Are you sure you want to delete your data? This action cannot be undone.")){
            return;
        }
        Config.deleteUserConfigFile();
        this.chooseFirstPage(false);
    }

    public void loginAdmin(){
        admin=true;
        showAdminEventsScene();
    }

    //hardcoded temporary exchange rates
    public double getUsdToEur(){
        return 0.92;
    }

    public double getRonToEur() {
        return 0.2;
    }
}