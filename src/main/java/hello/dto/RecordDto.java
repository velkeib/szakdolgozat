package hello.dto;

import java.util.Calendar;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
public class RecordDto {

    private Long id;
    private String customer;
    private String startDate;
    private String endDate;
    private Long courtID;
    private String reason;


}
