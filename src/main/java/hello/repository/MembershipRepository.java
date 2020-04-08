package hello.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.*;
import hello.entity.Membership;
import hello.entity.TimeRecord;
import org.springframework.data.repository.CrudRepository;

public interface MembershipRepository extends CrudRepository<Membership, Long> {

    //@Query("SELECT m FROM Membership m WHERE m.userId = :userId ORDER by m.endOfMembership")
    //public List<Membership> getLastMembershipByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Membership m WHERE m.userId = :userId order by m.endOfMembership desc")
    public List<Membership> getAllMembershipByUserId(@Param("userId") Long userId);

    //Optional<User> findByUsername(String username);
    //Optional<User> findById(Long id);
}