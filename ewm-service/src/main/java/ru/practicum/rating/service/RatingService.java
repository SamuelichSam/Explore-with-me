package ru.practicum.rating.service;

import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.model.RatingType;

public interface RatingService {
    RatingDto rateEventPrivate(Long userId, Long eventId, RatingType ratingType);

    void deleteRatingPrivate(Long userId, Long eventId, Long ratingId);

    RatingDto updateRatePrivate(Long userId, Long eventId, Long ratingId, RatingType ratingType);
}
