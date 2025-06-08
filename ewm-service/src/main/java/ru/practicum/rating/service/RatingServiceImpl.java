package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repo.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.RatingStats;
import ru.practicum.rating.dto.UserRatingStats;
import ru.practicum.rating.mapper.RatingMapper;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.repo.RatingRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repo.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RatingMapper ratingMapper;

    @Override
    public RatingDto rateEventPrivate(Long userId, Long eventId, Boolean liked) {
        log.info("Добавление лайка/дизлайка событию c id - {} пользователем с id - {}", eventId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не может оценивать свое событие");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Нельзя оценить неопубликованное событие");
        }
        ratingRepository.findByUserIdAndEventId(userId, eventId)
                .ifPresent(rating -> {
                    throw new ConflictException("Пользователь уже оценил это событие");
                });
        Rating rating = new Rating();
        rating.setEvent(event);
        rating.setUser(user);
        rating.setLiked(liked);
        rating.setCreated(LocalDateTime.now());
        updateEventRating(eventId);
        updateUserRating(event.getInitiator().getId());
        return ratingMapper.toDto(ratingRepository.save(rating));
    }

    @Override
    public void deleteRatingPrivate(Long userId, Long eventId, Long ratingId) {
        log.info("Удаление оценки события c id - {} пользователем с id - {}", eventId, userId);
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Оценка с id - " + ratingId + " не найден"));
        if (!rating.getUser().getId().equals(userId)) {
            throw new ConflictException("Пользователь может удалить только свою оценку");
        }
        ratingRepository.deleteById(ratingId);
    }

    @Override
    public RatingDto updateRatePrivate(Long userId, Long eventId, Long ratingId, Boolean liked) {
        log.info("Обновление оценки события c id - {} пользователем с id - {}", eventId, userId);
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Оценка с id " + ratingId + " не найдена"));
        if (!rating.getUser().getId().equals(userId)) {
            throw new ConflictException("Можно изменять только свои оценки");
        }
        rating.setLiked(liked);
        updateEventRating(rating.getEvent().getId());
        updateUserRating(rating.getEvent().getInitiator().getId());
        return ratingMapper.toDto(ratingRepository.save(rating));
    }

    private void updateEventRating(Long eventId) {
        log.info("Обновление рейтинга события c id - {}", eventId);
        RatingStats ratingStats = ratingRepository.findRatingStatsByEventId(eventId)
                .orElse(new RatingStats(0, 0));
        int rating;
        Long score = ratingStats.likes() - ratingStats.dislikes();
        if (ratingStats.likes() + ratingStats.dislikes() == 0) {
            rating = 5;
        } else {
            rating = (int) Math.min(10, Math.max(1, 5 + score / 2));
        }
        eventRepository.updateRating(eventId, rating);
    }

    private void updateUserRating(Long userId) {
        log.info("Обновление рейтинга пользователя c id - {}", userId);
        UserRatingStats userStats = ratingRepository.findRatingStatsByUserId(userId)
                .orElse(new UserRatingStats(0, 0));
        int rating;
        Long score = userStats.likes() - userStats.dislikes();
        if (userStats.likes() + userStats.dislikes() == 0) {
            rating = 5;
        } else {
            rating = (int) Math.min(10, Math.max(1, 5 + score / 2));
        }
        userRepository.updateRating(userId, rating);
    }
}
