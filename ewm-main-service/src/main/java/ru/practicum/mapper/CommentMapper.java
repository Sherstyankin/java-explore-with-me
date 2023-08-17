package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.request.NewCommentDto;
import ru.practicum.entity.Comment;
import ru.practicum.entity.Event;
import ru.practicum.entity.User;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CommentMapper {

    public Comment mapToComment(NewCommentDto dto, Event event, User author) {
        return Comment.builder()
                .text(dto.getText())
                .event(event)
                .author(author)
                .build();
    }

    public CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventTitle(comment.getEvent().getTitle())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .published(comment.getPublished())
                .state(comment.getState())
                .build();
    }

    public List<CommentDto> mapToCommentDto(Iterable<Comment> comments) {
        List<CommentDto> dtos = new ArrayList<>();
        for (Comment comment : comments) {
            dtos.add(mapToCommentDto(comment));
        }
        return dtos;
    }
}
