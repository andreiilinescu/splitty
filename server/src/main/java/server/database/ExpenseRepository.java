package server.database;

import commons.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("SELECT e FROM Expense e WHERE e.event.id = :eventId")
    List<Expense> getExpensesFromEvent(UUID eventId);

    @Query("SELECT e FROM Expense e WHERE e.event.id = :eventId AND e.id = :id")
    Expense getExpenseByIdInEvent(UUID eventId, UUID id);

    @Modifying
    @Query("DELETE Expense e WHERE e.event.id = :eventId AND e.id = :id")
    void deleteExpenseFromEvent(UUID id, UUID eventId);
}
