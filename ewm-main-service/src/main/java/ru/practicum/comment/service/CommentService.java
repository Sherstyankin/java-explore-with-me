package ru.practicum.comment.service;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.request.NewCommentDto;
import ru.practicum.dto.request.UpdateCommentDto;
import ru.practicum.enums.AdminCommentStateAction;
import ru.practicum.enums.CommentState;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    List<CommentDto> getComments(List<Long> authors,
                                 List<CommentState> states,
                                 List<Long> events,
                                 LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd,
                                 Integer from,
                                 Integer size);

    CommentDto getCommentById(Long id);

    CommentDto moderateComment(Long id, AdminCommentStateAction action);

    CommentDto addComment(NewCommentDto dto);

    CommentDto updateComment(UpdateCommentDto dto);

    void deleteComment(Long id);
}
