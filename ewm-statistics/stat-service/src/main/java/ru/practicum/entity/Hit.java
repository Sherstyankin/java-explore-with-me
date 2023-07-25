package ru.practicum.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hits")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String app;

    private String uri;

    private String ip;

    @Column(name = "created")
    private LocalDateTime timestamp;
}
