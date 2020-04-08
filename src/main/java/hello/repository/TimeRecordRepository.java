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

    @Query("SELECT t FROM TimeRecord t WHERE t.startDate >= :startDate and t.endDate < :endDate")
    public List<TimeRecord> getByDay(@Param("startDate") Calendar startDate, @Param("endDate") Calendar endDate);


    @Query("SELECT t FROM TimeRecord t WHERE (" +
            "(t.startDate >= :startDate and t.endDate <= :endDate) " +
            "or (t.startDate <= :startDate and t.endDate >= :endDate) " +
            "or (t.startDate <= :startDate and t.endDate > :startDate) " +
            "or (:endDate > t.startDate and :endDate <= t.endDate)" +
            ") and :court = courtID and :id <> id")
    public List<TimeRecord> findInInterval(@Param("startDate") Calendar startDate, @Param("endDate") Calendar endDate, @Param("court") Long court, @Param("id") Long id);

    @Query("SELECT t FROM TimeRecord t WHERE t.startDate >= :currentDate and :userId = t.customer order by t.startDate asc")
    public List<TimeRecord> getFutureReservationsByUser(@Param("currentDate") Calendar currentDate, @Param("userId") Long userId);
}