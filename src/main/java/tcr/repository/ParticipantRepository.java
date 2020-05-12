package tcr.repository;


import java.util.*;

import tcr.entity.Participant;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;

public interface ParticipantRepository extends CrudRepository<Participant, Long> {
    List<Participant> findByRecordId(Long recordId);
    List<Participant> deleteByRecordId(Long recordId);
}