package client.utils;

import commons.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// extends DebtResolve to be able to access private methods
public class DebtResolveTest extends DebtResolve {

    Event event;

    List<Participant> participants;

    Tag tag;

    @BeforeEach
    public void setup() {
        this.event = new Event();
        this.tag = new Tag();

        this.participants = new ArrayList<>();
        for ( int i = 0; i < 10; i++) {
            this.participants.add(new Participant("" + i, this.event));
        }

        for ( int x = 0; x < 10; x++) {

            Expense expense = new Expense(
                    "test"+x+","+"y",
                    0.0,
                    Instant.EPOCH,
                    this.participants.get(x),
                    this.event,
                    new ArrayList<Debt>(),
                    new ArrayList<Tag>()
            );

            List<Debt> debts = new ArrayList<>();
            double total = 0.0;

            for ( int y = 0; y < 10; y ++) {
                if ( x == y ) continue;
                debts.add(new Debt(expense, participants.get(y), (double) x+y));
            }
            expense.setAmount(total);
            expense.setDebts(debts);

            event.addExpense(expense);
        }

        this.event.setParticipants(this.participants);
    }

    @Test
    public void full_test() {
        System.out.println("full_test start");
        DebtResolve.resolve(this.event);
        System.out.println("full_test end");
    }
}
