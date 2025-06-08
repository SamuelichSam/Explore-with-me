package ru.practicum.event.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    Boolean existsByCategoryId(Long categoryId);

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long userId, Long eventId);

    Optional<Event> findByIdAndState(Long eventId, State eventState);

    @Modifying
    @Query("UPDATE Event e SET e.rating = :rating WHERE e.id = :eventId")
    void updateRating(@Param("eventId") Long eventId, @Param("rating") Integer rating);

    @Query("SELECT e FROM Event e WHERE e.state = 'PUBLISHED' ORDER BY e.rating DESC, e.views DESC LIMIT :count")
    List<Event> findTopEvents(@Param("count") int count);
}
