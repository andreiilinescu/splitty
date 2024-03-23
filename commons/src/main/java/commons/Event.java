package commons;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import org.hibernate.annotations.ValueGenerationType;

import java.lang.annotation.Retention;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id")
    private UUID id;

    @Expose
    private String name;

    @Column(nullable = false, unique = true)
    @INVITECODE String inviteCode;
    @Expose
    private String title; //fix response issue for now

    @OneToMany(mappedBy = "event", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonManagedReference ("event-participants")
    @Expose
    private List<Participant> participants;

    @OneToMany(mappedBy = "event", orphanRemoval = true)
    @JsonManagedReference("event-expenses")
    @Expose
    private List<Expense> expenses;

    @OneToMany(mappedBy = "event")
    @JsonManagedReference("event-tags")
    @Expose
    private List<Tag> tags;

    public Event(UUID id) {
        this();
        this.id = id;
    }

    public Event() {
        this.participants=new ArrayList<>();
        this.expenses = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Event(String name){
        this();
        this.name = name;
    }

    public Event(String name, Participant creator){
        this(name);
        this.participants.add(creator);
    }

    public Event(String name, List<Participant> participants) {
        this(name);
        this.participants = participants;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    /**
     * Sets all participants,
     * may be used by creator while event is being created
     * or if they want to edit the current participants.
     * @param participants that will be added
     */
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public void addParticipant(Participant participant){
        this.participants.add(participant);
    }

    public void removeParticipant(Participant participant){
        this.participants.remove(participant);
    }

    public List<Participant> getParticipants(){
        return this.participants;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    /**
     * Sets list of expenses.
     * May be used by creator while event is being created
     * or if they want to edit the current list of expenses.
     * @param expenses that will be added
     */
    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public void addExpense(Expense expense){
        this.expenses.add(expense);
    }

    public void removeExpense(Expense expense){
        this.expenses.remove(expense);
    }

    public List<Expense> getExpenses(){
        return this.expenses;
    }

    public void setTitle(String name){
        this.name = name;
    }

    public String getTitle(){
        return this.name;
    }

    public UUID getId(){
        return this.id;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event event)) {
            return false;
        }
        return getId() == event.getId()
                && Objects.equals(name, event.name)
                && Objects.equals(getParticipants(), event.getParticipants())
                && Objects.equals(getExpenses(), event.getExpenses())
                && Objects.equals(getTags(), event.getTags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(),
                name,
                getParticipants(),
                getExpenses(),
                getTags());
    }
    public void addTag(Tag tag){
        tags.add(tag);
    }


    public static String generateInviteCode(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder codeBuilder = new StringBuilder(8);
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < 8; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
}
@ValueGenerationType(generatedBy =InviteCodeGenerator.class)
@Retention(RUNTIME)
@interface INVITECODE {}