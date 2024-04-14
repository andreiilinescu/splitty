package client.utils;

public record DebtResolveTableEntry(String from, String to, Double amount) {
    public static DebtResolveTableEntry from_result(DebtResolveResult res) {
        return new DebtResolveTableEntry(
            res.from().getName(),
            res.to().getName(),
            res.amount()
        );
    }

    public String getFrom() {
        return this.from;
    }

    public String getTo() {
        return this.to;
    }

    public Double getAmount() {
        return this.amount;
    }
}
