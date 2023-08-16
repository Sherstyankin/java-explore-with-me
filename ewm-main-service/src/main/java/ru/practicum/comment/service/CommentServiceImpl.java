package ru.practicum.comment.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.request.NewCommentDto;
import ru.practicum.dto.request.UpdateCommentDto;
import ru.practicum.entity.Comment;
import ru.practicum.entity.Event;
import ru.practicum.entity.QComment;
import ru.practicum.entity.User;
import ru.practicum.enums.AdminCommentStateAction;
import ru.practicum.enums.CommentState;
import ru.practicum.enums.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(List<Long> authors,
                                        List<CommentState> states,
                                        List<Long> events,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Integer from,
                                        Integer size) {
        Pageable page = PageRequest.of(from, size, Sort.by("created"));
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(byAuthors(authors))
                .and(byStates(states))
                .and(byEvents(events))
                .and(byRangeStartAndRangeEnd(rangeStart, rangeEnd));
        List<Comment> comments = commentRepository.findAll(predicate, page).getContent();
        return CommentMapper.mapToCommentDto(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(Long id) {
        Comment comment = findCommentById(id);
        return CommentMapper.mapToCommentDto(comment);
    }

    @Override
    public CommentDto moderateComment(Long id, AdminCommentStateAction action) {
        Comment comment = findCommentById(id);
        if (action == AdminCommentStateAction.PUBLISH_COMMENT) {
            comment.setState(CommentState.PUBLISHED);
            comment.setPublished(LocalDateTime.now());
        } else {
            comment.setState(CommentState.REJECTED);
        }
        Comment moderatedComment = commentRepository.save(comment);
        return CommentMapper.mapToCommentDto(moderatedComment);
    }

    @Override
    public CommentDto addComment(NewCommentDto dto) {
        User author = userService.getUserById(dto.getAuthor());
        Event event = findEventById(dto.getEvent());
        checkIfEventPublished(event);
        Comment comment = commentRepository.save(CommentMapper.mapToComment(dto, event, author));
        return CommentMapper.mapToCommentDto(comment);
    }

    @Override
    public CommentDto updateComment(UpdateCommentDto dto) {
        Comment comment = findCommentById(dto.getId());
        checkIfUpdateAvailable(comment);
        comment.setText(dto.getText());
        comment.setState(CommentState.PENDING);
        comment.setPublished(null);
        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.mapToCommentDto(updatedComment);
    }

    @Override
    public void deleteComment(Long id) {
        findCommentById(id);
        commentRepository.deleteById(id);
    }

    @Override
    public Map<Long, List<Comment>> getAndMapCommentsByEventIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentRepository.findAllByPublishedStateAndEventIn(eventIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getEvent().getId()));
    }

    @Override
    public List<Comment> getCommentsByEventId(Long eventId) {
        if (eventId == null) {
            return Collections.emptyList();
        }
        return commentRepository.findAllByPublishedStateAndEventId(eventId);
    }

    private Comment findCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с ID:" + id + " не найден."));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие по ID: " + eventId + " не найдено."));
    }

    private void checkIfEventPublished(Event event) {
        if (event.getState() != EventState.PUBLISHED) {
            log.error("Добавление комментария невозможно, так как " +
                    "событие с ID:{} еще не опубликовано.", event.getId());
            throw new ConflictException("Добавление комментария невозможно, так как " +
                    "событие еще не опубликовано.");
        }
    }

    private void checkIfUpdateAvailable(Comment comment) {
        if (comment.getPublished() != null &&
                comment.getPublished().until(LocalDateTime.now(), ChronoUnit.HOURS) > 24) {
            log.error("Обновление комментария невозможно, так как " +
                    "прошло более 24 часов с момента публикации.");
            throw new ConflictException("Обновление комментария невозможно, так как " +
                    "прошло более 24 часов с момента публикации.");
        }
    }

    private BooleanExpression byAuthors(List<Long> authors) {
        return authors != null ? QComment.comment.author.id.in(authors) : null;
    }

    private BooleanExpression byStates(List<CommentState> states) {
        return states != null ? QComment.comment.state.in(states) : null;
    }

    private BooleanExpression byEvents(List<Long> events) {
        return events != null ? QComment.comment.event.id.in(events) : null;
    }

    private BooleanExpression byRangeStartAndRangeEnd(LocalDateTime rangeStart,
                                                      LocalDateTime rangeEnd) {
        return rangeStart != null && rangeEnd != null ?
                QComment.comment.created.between(rangeStart, rangeEnd) : null;
    }
}
