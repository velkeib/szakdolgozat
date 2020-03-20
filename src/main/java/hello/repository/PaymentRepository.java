package hello.repository;


import java.util.*;
import hello.entity.Payment;
import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<Payment, Long> {
    Optional<Payment> findById(Long id);
}
