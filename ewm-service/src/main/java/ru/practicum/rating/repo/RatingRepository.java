package ru.practicum.rating.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.rating.dto.RatingStats;
import ru.practicum.rating.dto.UserRatingStats;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.model.RatingType;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Long countByEventIdAndRatingType(Long eventId, RatingType ratingType);

    Optional<Rating> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT NEW ru.practicum.rating.dto.RatingStats(" +
            "COUNT(CASE WHEN r.ratingType = ru.practicum.rating.model.RatingType.LIKE THEN 1 END), " +
            "COUNT(CASE WHEN r.ratingType = ru.practicum.rating.model.RatingType.DISLIKE THEN 1 END)) " +
            "FROM Rating r WHERE r.event.id = :eventId")
    Optional<RatingStats> findRatingStatsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT NEW ru.practicum.rating.dto.UserRatingStats(" +
            "COUNT(CASE WHEN r.ratingType = ru.practicum.rating.model.RatingType.LIKE THEN 1 END), " +
            "COUNT(CASE WHEN r.ratingType = ru.practicum.rating.model.RatingType.DISLIKE THEN 1 END)) " +
            "FROM Rating r WHERE r.event.initiator.id = :authorId")
    Optional<UserRatingStats> findRatingStatsByUserId(@Param("authorId") Long userId);
}
