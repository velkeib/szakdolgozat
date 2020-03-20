package hello.entity;


import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.GregorianCalendar;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserInformation {

    @Id
    private Long userId;

    @Column(nullable = true)
    private String city;

    @Column(nullable = true)
    private String zipCode;

    @Column(nullable = true)
    private String street;

    @Column(nullable = true)
    private String number;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = true)
    private String isMember;

    @Column(nullable = true)
    private GregorianCalendar startOfMembership;

}
