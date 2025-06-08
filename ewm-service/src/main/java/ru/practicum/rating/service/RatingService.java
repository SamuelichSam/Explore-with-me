package ru.practicum.rating.service;

import ru.practicum.rating.dto.RatingDto;

public interface RatingService {
    RatingDto rateEventPrivate(Long userId, Long eventId, Boolean liked);

    void deleteRatingPrivate(Long userId, Long eventId, Long ratingId);

    RatingDto updateRatePrivate(Long userId, Long eventId, Long ratingId, Boolean liked);
}
