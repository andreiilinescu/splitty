package server.api;

import commons.Event;
import commons.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.EventService;
import server.services.ExpenseService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events/{eventId}/expenses")
public class ExpenseController {

    //use service instead of repository
    private final ExpenseService service;
    private final EventService eventService;
    @Autowired
    public ExpenseController(ExpenseService service, EventService eventService) {
        this.service = service;
        this.eventService = eventService;
    }
    /*
       Get expenses for an event by event id
    */
    @GetMapping(path = {"", "/"})
    public  ResponseEntity<List<Expense>> getAll(@PathVariable("eventId") UUID eventId){
        try {
            return ResponseEntity.ok(service.getAllExpenses().stream()
                    .filter(e -> e.getEvent().getId()
                            .equals(eventId))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(path = {"", "/"})
    public ResponseEntity<Expense> add(@PathVariable("eventId") UUID eventId, @RequestBody Expense expense) {
        Event event = new Event();
        event.setId(eventId);
        expense.setEvent(event);
        Expense saved = service.addExpense(expense);
//        System.out.println("Server method: " + saved.getPaidBy().getId());
        if (saved != null) {
            eventService.newEventLastActivity(eventId);
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getById(@PathVariable("eventId") UUID eventId, @PathVariable("id") UUID id) {
        Expense expense = service.getExpenseById(id);
        if (expense != null) {
            eventService.newEventLastActivity(eventId);
            return ResponseEntity.ok(expense);
        }
        return ResponseEntity.badRequest().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Expense> remove(@PathVariable("eventId") UUID eventId, @PathVariable("id") UUID id) {
        Expense expense = service.deleteExpense(id);
        if (expense != null) {
            eventService.newEventLastActivity(eventId);
            return ResponseEntity.ok(expense);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("eventId") UUID eventId, @PathVariable("id") UUID id, @RequestBody Expense expense) {
        expense.setId(id);
        expense.setEvent(new Event(eventId));
        service.updateExpense(expense);
        eventService.newEventLastActivity(eventId);
        return ResponseEntity.ok().build();
    }
}
