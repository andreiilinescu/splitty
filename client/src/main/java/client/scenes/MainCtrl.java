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

import client.utils.Config;
import client.utils.User;
import commons.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.UUID;

public class MainCtrl {

    private  User user;

    private Stage primaryStage;
    private FirstTimeCtrl firstTimeCtrl;

    private  Scene firstTime;

    private EventOverviewCtrl eventOverviewCtrl;
    private Scene eventOverview;

    private AddParticipantCtrl addParticipantCtrl;
    private Scene addParticipant;

    private StartCtrl startCtrl;
    private Scene start;

    private AddExpenseCtrl addExpenseCtrl;
    private Scene addExpense;
    private Scene inviteView;
    private InviteViewCtrl inviteViewCtrl;

    public void initialize(Stage primaryStage, Pair<FirstTimeCtrl, Parent> firstTime, Pair<EventOverviewCtrl, Parent> eventOverview, Pair<AddParticipantCtrl, Parent> addParticipant, Pair<StartCtrl, Parent> start, Pair<AddExpenseCtrl, Parent> addExpense, Pair<InviteViewCtrl, Parent> inviteView) {

        this.primaryStage = primaryStage;

        this.firstTimeCtrl=firstTime.getKey();
        this.firstTime=new Scene(firstTime.getValue());

        this.eventOverviewCtrl=eventOverview.getKey();
        this.eventOverview= new Scene(eventOverview.getValue());

        this.startCtrl=start.getKey();
        this.start= new Scene(start.getValue());

        this.addExpenseCtrl = addExpense.getKey();
        this.addExpense = new Scene(addExpense.getValue());

        this.user = new User();

        //showStartScene();
        //primaryStage.show();
        this.addParticipantCtrl = addParticipant.getKey();
        this.addParticipant = new Scene(addParticipant.getValue());

        this.inviteViewCtrl=inviteView.getKey();
        this.inviteView=new Scene(inviteView.getValue());

        chooseFirstPage();
    }

    public void showInviteView(Event event){
        primaryStage.setTitle("Splitty: Invite View");
        inviteViewCtrl.setEvent(event);
        primaryStage.setScene(inviteView);
    }

    public void chooseFirstPage(){
        this.user=Config.readUserConfigFile();
        if (user == null) {
            this.showFirstTimeScene();
            primaryStage.show();
        }
        else {
            this.showStartScene();
            System.out.println(user);
            primaryStage.show();
        }
    }
    public  void showFirstTimeScene(){
        primaryStage.setTitle("Splitty: Setup");
        primaryStage.setScene(this.firstTime);
    }

    public void showStartScene() {
        primaryStage.setTitle("Splitty: Start");
        startCtrl.addRecentEvents();
        primaryStage.setScene(start);
    }
    public  void showEventOverviewScene(Event newEvent){
        primaryStage.setTitle("Splitty: Event Overview");
        eventOverviewCtrl.setEvent(newEvent);
        eventOverviewCtrl.refresh();
        primaryStage.setScene(eventOverview);
    }
    public void showAddParticipantScene(Event event) {
        primaryStage.setTitle("Splitty: Add Participant");
        addParticipantCtrl.setEvent(event);
        primaryStage.setScene(addParticipant);
    }
    public User getUser(){
        return this.user;
    }

    public  void setUser(User user){
        this.user=user;
        Config.writeUserConfigFile(user);
    }

    public void addUserEvent(UUID event, UUID participant){
        this.user.addEventParticipant(event, participant);
        Config.writeUserConfigFile(user);
        System.out.println(Config.readUserConfigFile());
    }

    public void showAddExpense(){
        primaryStage.setTitle("Splitty: Add/Edit Expense");
        addExpenseCtrl.setup(eventOverviewCtrl.getEvent());
        primaryStage.setScene(addExpense);
    }

    //hardcoded temporary exchange rates
    public double getUsdToEur(){
        return 0.92;
    }

    public double getRonToEur() {
        return 0.2;
    }
}