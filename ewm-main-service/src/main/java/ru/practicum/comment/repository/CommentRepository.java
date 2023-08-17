package ru.practicum.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, QuerydslPredicateExecutor<Comment> {
    @Query("select c " +
            "from Comment c " +
            "where c.event.id in ?1 AND c.state like 'PUBLISHED'")
    List<Comment> findAllByPublishedStateAndEventIn(List<Long> eventIds);

    @Query("select c " +
            "from Comment c " +
            "where c.event.id = ?1 AND c.state like 'PUBLISHED'")
    List<Comment> findAllByPublishedStateAndEventId(Long eventId);
}
