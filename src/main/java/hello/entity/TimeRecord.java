package hello.entity;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
/*import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;*/
import lombok.*;
import javax.persistence.*;
import java.util.*;

import java.util.Calendar;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TimeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customer;

    @Column(nullable = false)
    private GregorianCalendar startDate;

    @Column(nullable = false)
    private GregorianCalendar endDate;

    @Column(nullable = false)
    private Long courtID;

    @Column(nullable = false)
    private String reason;

    public TimeRecord(Long customer, GregorianCalendar startDate, GregorianCalendar endDate, Long courtID, String reason){
        this.customer = customer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.courtID = courtID;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return String.format(
                "Tennis Court[id=%d, customer='%d', start date='%s', end date='%s', courtID='%d', reason='%s']",
                id, customer, startDate.toString(), endDate.toString(), courtID, reason);
    }
}
