package client.utils;

import commons.Debt;
import commons.Event;
import commons.Expense;
import commons.Participant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtResolve {
    public static List<DebtResolveResult> resolve(Event event) {
        Map<Participant, Amounts> amounts = new HashMap<>();
        List<Expense> expenses = event.getExpenses();

        System.out.println(expenses);

        for (Expense e : expenses) {
            List<Debt> debts = e.getDebts();
            amounts.merge(e.getPaidBy(),
                    new Amounts(e.getAmount(), 0.),
                    (start, end) -> new Amounts(
                            start.giving() + end.giving(),
                            start.getting() + end.getting()
                    )
            );
            for (Debt d : debts) {
                amounts.merge(d.getParticipant(),
                        new Amounts(0., d.getAmount()),
                        (start, end) -> new Amounts(
                                start.giving() + end.giving(),
                                start.getting() + end.getting()
                        )
                );
            }
        }

        System.out.println(amounts);

        Map<Participant, Double> netAmounts = new HashMap<>();

        for (Map.Entry<Participant, Amounts> a : amounts.entrySet()) {
            // calculate net spent
            Double net = a.getValue().net();
            netAmounts.put(a.getKey(), net);
        }

        // gotta get negative infinity
        Double maxAmount = Double.NEGATIVE_INFINITY;

        for (Map.Entry<Participant, Double> a : netAmounts.entrySet()) {
            if (a.getValue() > maxAmount) {
                maxAmount = a.getValue();
            }
        }

        List<DebtResolveResult> res = new ArrayList<>();

        netAmounts.forEach((key, value) -> res.add(new DebtResolveResult(key, expenses.getFirst().getPaidBy(), value)));

        return res;
    }
}

record Amounts(Double giving, Double getting) {
    public Amounts add(Amounts one, Amounts two) {
        return new Amounts(
                one.giving + two.giving,
                one.getting + two.getting
        );
    }

    public Double net() {
        return this.giving - this.getting;
    }
}
