package hello.repository;


import java.util.*;

import hello.entity.Participant;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;

public interface ParticipantRepository extends CrudRepository<Participant, Long> {
    List<Participant> findByRecordId(Long recordId);
    List<Participant> deleteByRecordId(Long recordId);
}