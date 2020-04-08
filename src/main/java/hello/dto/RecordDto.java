package hello.dto;

import java.util.Calendar;

import hello.entity.Participant;
import lombok.*;
import javax.persistence.*;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordDto {

    private String id;
    private String customer;
    private String startDate;
    private String endDate;
    private Long courtID;
    private String reason;
    private List<Participant> participants;


}
