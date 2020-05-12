package tcr.dto;

import lombok.*;
import javax.persistence.*;

@Data
public class ChargeRequest {

    public enum Currency {
        EUR, USD, HUF;
    }
    private String id;
    private String description;
    private int amount;
    private Currency currency;
    private String stripeEmail;
    private String stripeToken;
}