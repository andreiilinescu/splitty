package commons;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue
    private long id;
    private String tag;

    private double[] colorValues = new double[4];

    @ManyToOne
    private Event event;

    public Tag(String tag, Event event, double[] colorValues) {
        this.tag = tag;
        this.event = event;
        this.colorValues = colorValues;
    }

    public Tag() {
    }

    public Tag(String tagName, double[] colorValues) {
        this.tag = tagName;
        this.colorValues = colorValues;
    }

    public String getTag() {
        return tag;
    }

    public Event getEvent() {
        return event;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        Tag tag1 = (Tag) o;
        return Objects.equals(tag, tag1.getTag()) && Objects.equals(event, tag1.getEvent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, event);
    }

    public double[] getColorValues() {
        return colorValues;
    }

    public void setColorValues(double[] colorValues) {
        this.colorValues = colorValues;
    }
}
