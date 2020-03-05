package hello.dto;

import java.util.Calendar;
import lombok.*;
import javax.persistence.*;
import lombok.*;

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


}
