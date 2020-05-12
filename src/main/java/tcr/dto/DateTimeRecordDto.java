package tcr.dto;

import java.util.List;

import lombok.*;
import javax.persistence.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateTimeRecordDto {

    private String customer;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private Long courtID;
    private String reason;
    private List<String> participants;


}
