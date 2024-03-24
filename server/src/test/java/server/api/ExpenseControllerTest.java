package server.api;


import commons.Expense;
import commons.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.database.ExpenseRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ExpenseControllerTest {
    @Mock
    private ExpenseRepository expenseRepository;
    @InjectMocks
    ExpenseController expenseController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
//    @Test
//    public void addExpense(){
//        Expense expense = new Expense();
//        expense.setTitle("Test");
//        expense.setAmount(100);
//        expense.setId(UUID.randomUUID());
//        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
//        when(expenseRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(expense));
//
//        ResponseEntity<Expense> responseEntity = expenseController.add(expense);
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//        assertEquals(expense, responseEntity.getBody());
//    }
//
//    @Test
//    public void getExpense(){
//        Expense expense = new Expense();
//        expense.setTitle("Test");
//        expense.setAmount(100);
//        UUID id = UUID.randomUUID();
//        expense.setId(id);
//
//        when(expenseRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(expense));
//
//        ResponseEntity<Expense> responseEntity = expenseController.getById(id);
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//        assertEquals(expense, responseEntity.getBody());
//
//    }

//    @Test
//    public void deleteExpense(){
//        Expense expense = new Expense();
//        expense.setTitle("Test");
//        expense.setAmount(100);
//        UUID id = UUID.randomUUID();
//        expense.setId(id);
//
//        when(expenseRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(expense));
//        ResponseEntity<Expense> responseEntity = expenseController.remove(id);
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//        assertEquals(expense, responseEntity.getBody());
//
//        verify(expenseRepository, times(1)).deleteById(any(UUID.class));
//    }

//    @Test
//    public void updateExpense(){
//        Expense expense = new Expense();
//        expense.setTitle("Test");
//        expense.setAmount(100);
//
//        expense.setPaidBy(new Participant());
//        UUID id = UUID.randomUUID();
//        expense.setId(id);
//
//        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
//        when(expenseRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(expense));
//
//        ResponseEntity<Expense> responseEntity = expenseController.update(id, expense);
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//        assertEquals(expense, responseEntity.getBody());
//
//        verify(expenseRepository, times(1)).deleteById(any(UUID.class));
//
//    }

    @Test
    public void getAllExpenses(){
        List<Expense> expenses = List.of(new Expense(), new Expense());
        expenses.get(0).setTitle("Test1");
        expenses.get(1).setTitle("Test2");
        expenses.get(0).setId(UUID.randomUUID());
        expenses.get(1).setId(UUID.randomUUID());

        when(expenseRepository.findAll()).thenReturn(expenses);

        ResponseEntity<List<Expense>> responseEntity = expenseController.getAll();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expenses, responseEntity.getBody());
    }

//    @Test
//    public void deleteExpenseFail(){
//
//        UUID id = UUID.randomUUID();
//
//        when(expenseRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.empty());
//
//        ResponseEntity<Expense> responseEntity = expenseController.remove(id);
//        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
//        assertNull(responseEntity.getBody());
//
//        verify(expenseRepository, times(0)).deleteById(any(UUID.class));
//    }
//
//    @Test
//    public void getExpenseFail(){
//        UUID id = UUID.randomUUID();
//
//        when(expenseRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.empty());
//
//        ResponseEntity<Expense> responseEntity = expenseController.getById(id);
//        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
//        assertNull(responseEntity.getBody());
//
//    }

//    @Test
//    public void updateExpenseFail(){
//
//        UUID id = UUID.randomUUID();
//
//        when(expenseRepository.existsById(any(UUID.class))).thenReturn(false);
//
//        ResponseEntity<Expense> responseEntity = expenseController.update(id, new Expense());
//        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
//        assertNull(responseEntity.getBody());
//
//        verify(expenseRepository, times(0)).deleteById(any(UUID.class));
//    }
}
