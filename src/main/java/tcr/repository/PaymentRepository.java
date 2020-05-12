package tcr.repository;


import java.util.*;
import tcr.entity.Payment;
import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<Payment, Long> {
    Optional<Payment> findById(Long id);
}
