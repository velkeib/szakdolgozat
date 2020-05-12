package tcr.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.*;
import tcr.entity.Membership;
import org.springframework.data.repository.CrudRepository;

public interface MembershipRepository extends CrudRepository<Membership, Long> {

    //@Query("SELECT m FROM Membership m WHERE m.userId = :userId ORDER by m.endOfMembership")
    //public List<Membership> getLastMembershipByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Membership m WHERE m.userId = :userId order by m.endOfMembership desc")
    public List<Membership> getAllMembershipByUserId(@Param("userId") Long userId);

    //Optional<User> findByU ;
    //Optional<User> findById(Long id);
}