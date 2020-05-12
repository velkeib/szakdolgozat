package tcr.repository;


import java.util.*;

import tcr.entity.Court;
import tcr.entity.Reservation;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    Optional<Court> findByCustomer(String name);

    @Query("SELECT r FROM Reservation r WHERE r.startDate >= :startDate")
    public List<Reservation> find(@Param("startDate") Calendar startDate);

    @Query("SELECT t FROM Reservation t WHERE t.startDate >= :startDate and t.endDate < :endDate")
    public List<Reservation> getByDay(@Param("startDate") Calendar startDate, @Param("endDate") Calendar endDate);


    @Query("SELECT t FROM Reservation t WHERE (" +
            "(t.startDate >= :startDate and t.endDate <= :endDate) " +
            "or (t.startDate <= :startDate and t.endDate >= :endDate) " +
            "or (t.startDate <= :startDate and t.endDate > :startDate) " +
            "or (:endDate > t.startDate and :endDate <= t.endDate)" +
            ") and :court = courtID and :id <> id")
    public List<Reservation> findInInterval(@Param("startDate") Calendar startDate,
                                            @Param("endDate") Calendar endDate,
                                            @Param("court") Long court,
                                            @Param("id") Long id);

    @Query("SELECT t FROM Reservation t WHERE t.startDate >= :currentDate and :userId = t.customer order by t.startDate asc")
    public List<Reservation> getFutureReservationsByUser(@Param("currentDate") Calendar currentDate, @Param("userId") Long userId);
}