package hello.repository;


import java.util.*;

import hello.entity.Court;
import hello.entity.TimeRecord;
import hello.entity.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimeRecordRepository extends CrudRepository<TimeRecord, Long> {
    Optional<Court> findByCustomer(String name);

    @Query("SELECT t FROM TimeRecord t WHERE t.startDate >= :startDate")
    public List<TimeRecord> find(@Param("startDate") Calendar startDate);
}