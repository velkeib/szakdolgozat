package hello.repository;


import java.util.*;
import hello.entity.UserInformation;
import org.springframework.data.repository.CrudRepository;

public interface UserInformationRepository extends CrudRepository<UserInformation, Long> {
        Optional<UserInformation> findByUserId(Long userId);
}
