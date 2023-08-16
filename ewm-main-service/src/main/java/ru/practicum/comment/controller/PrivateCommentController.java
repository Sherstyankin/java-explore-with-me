package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.request.NewCommentDto;
import ru.practicum.dto.request.UpdateCommentDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users/comments")
@RequiredArgsConstructor
@Slf4j
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestBody @Valid NewCommentDto dto) {
        log.info("Добавить комментарий: {}", dto);
        return commentService.addComment(dto);
    }

    @PatchMapping
    public CommentDto updateComment(@RequestBody @Valid UpdateCommentDto dto) {
        log.info("Обновить комментарий: {}", dto);
        return commentService.updateComment(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long id) {
        log.info("Удалить комментарий под ID: {}", id);
        commentService.deleteComment(id);
    }
}
