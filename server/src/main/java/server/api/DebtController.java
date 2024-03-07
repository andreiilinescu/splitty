package server.api;

import commons.Debt;
import commons.Expense;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.DebtRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    private final DebtRepository repo;
    public DebtController(DebtRepository repo) {
        this.repo = repo;
    }

    /**
     * Get all debts
     * @return - list of all debts
     */
    @GetMapping(path = { "", "/" })
    public List<Debt> getAll() {
        return repo.findAll();
    }

    /**
     * Add debt
     * @param debt - debt to add
     * @return - added debt
     */
    @PostMapping(path = { "", "/" })
    public ResponseEntity<Debt> add(@RequestBody Debt debt) {
        Debt saved = repo.save(debt);
        return ResponseEntity.ok(saved);
    }

    /**
     * Get debt by id
     * @param id - id of debt
     * @return - debt with id
     */
    @GetMapping("/{id}")
    public ResponseEntity<Debt> getById(@PathVariable("id") UUID id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        if (repo.findById(id).isPresent()){
            return ResponseEntity.ok(repo.findById(id).get());
        }else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update debt
     * @param id - id of debt
     * @param debt - debt to update
     * @return - updated debt
     */
    @PutMapping("/{id}")
    public ResponseEntity<Debt> update(@PathVariable("id") UUID id, @RequestBody Debt debt) {
        if (!repo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        Debt saved = repo.save(debt);
        return ResponseEntity.ok(saved);
    }

    /**
     * delete debt
     * @param id - id of debt
     * @return - deleted debt
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Debt> remove(@PathVariable("id") UUID id) {
        if (repo.findById(id).isPresent()) {
            ResponseEntity<Debt> ret = ResponseEntity.ok(repo.findById(id).get());
            repo.deleteById(id);
            return ret;
        }
        return ResponseEntity.badRequest().build();
    }
}
