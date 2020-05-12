package tcr.repository;


import java.util.*;

import tcr.entity.Court;
import org.springframework.data.repository.CrudRepository;

public interface CourtRepository extends CrudRepository<Court, Long> {
    Optional<Court> findByName(String name);
}