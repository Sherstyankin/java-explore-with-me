package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.entity.Request;

import java.util.List;

public interface EventRequestRepository extends JpaRepository<Request, Long> {
    @Query("select r " +
            "from Request r " +
            "where r.event.id in ?1 AND r.status like 'CONFIRMED'")
    List<Request> findByEventIdsAndConfirmedStatus(List<Long> eventIds);

    @Query("select count(r.id) " +
            "from Request r " +
            "where r.event.id = ?1 AND r.status like 'CONFIRMED'")
    Long findCountedRequestsByEventIdAndConfirmedStatus(Long eventId);

    List<Request> findByEventId(Long eventId);

    List<Request> findByRequesterId(Long userId);
}
