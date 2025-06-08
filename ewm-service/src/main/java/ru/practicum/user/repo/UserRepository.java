package ru.practicum.user.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.user.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByIdIn(List<Integer> ids, Pageable pageable);

    Boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.rating = :rating WHERE u.id = :userId")
    void updateRating(@Param("userId") Long userId, @Param("rating") Integer rating);

    @Query("SELECT u FROM User u ORDER BY u.rating DESC LIMIT :count")
    List<User> findTopUsers(@Param("count") int count);
}
