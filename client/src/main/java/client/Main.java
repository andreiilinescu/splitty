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
package client;

import client.scenes.*;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import static com.google.inject.Guice.createInjector;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    public static void main(String[] args) throws URISyntaxException, IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parameters parameters = getParameters();
        boolean admin = Objects.equals(parameters.getNamed().get("admin"), "1"); // CHECK LAUNCH DIRECTLY AS ADMIN MODE, TO USE ADD --admin=1 TO CLI ARGS.
        var eventOverview = FXML.load(EventOverviewCtrl.class, "client", "scenes", "EventOverview.fxml");
        var start=FXML.load(StartCtrl.class, "client", "scenes", "Start.fxml");
        var firstTime=FXML.load(FirstTimeCtrl.class, "client", "scenes", "FirstTime.fxml");
        var inviteView=FXML.load(InviteViewCtrl.class, "client", "scenes", "InviteView.fxml");
        var addParticipant=FXML.load(AddParticipantCtrl.class, "client", "scenes", "AddParticipant.fxml");
        var editParticipant=FXML.load(EditParticipantCtrl.class, "client", "scenes", "EditParticipant.fxml");
        var addExpense = FXML.load(AddExpenseCtrl.class, "client", "scenes", "AddExpense.fxml");
        var userSettings = FXML.load(UserSettingsCtrl.class, "client", "scenes", "UserSettings.fxml");
        var settings= FXML.load(SettingsCtrl.class, "client", "scenes", "Settings.fxml");
        var adminEvents = FXML.load(AdminEventsCtrl.class, "client", "scenes", "AdminEvents.fxml");
        var addTag = FXML.load(AddTagCtrl.class, "client", "scenes", "AddTags.fxml");
        var editTag = FXML.load(EditTagCtrl.class, "client", "scenes", "EditTags.fxml");
        var languageOptions = FXML.load(LanguageCtrl.class, "client", "scenes", "Language.fxml");
        var statistics = FXML.load(StatisticsCtrl.class, "client", "scenes", "Statistics.fxml");
        var debtResolve = FXML.load(DebtResolveCtrl.class, "client", "scenes", "DebtResolve.fxml");

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, firstTime, eventOverview, addParticipant, start, addExpense, inviteView, editParticipant, userSettings, settings, adminEvents, addTag, editTag, languageOptions, admin, statistics, debtResolve);
        // calls the stop method to stop the listener thread, otherwise it will keep on running.
        primaryStage.setOnCloseRequest(e -> {
            adminEvents.getKey().stop();
        });
    }
}