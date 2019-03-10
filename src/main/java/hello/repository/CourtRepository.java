package hello.repository;


import java.util.*;

import hello.entity.Court;
import hello.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface CourtRepository extends CrudRepository<Court, Long> {
    Optional<Court> findByName(String name);
}