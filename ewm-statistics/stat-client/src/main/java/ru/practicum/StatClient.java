package ru.practicum;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StatClient {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String BASE_URL = "http://stats-server:9090"; // пробовал через переменную в properties, но не подтягивается
    private final WebClient webClient = WebClient.builder().build();

    public ResponseEntity<Object> saveHit(HitDto dto) {

        return webClient.post()
                .uri(BASE_URL + "/hit")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(dto)
                .retrieve()
                .toEntity(Object.class)
                .block();
    }

    public List<ViewStatsDto> getStatistics(LocalDateTime start,
                                            LocalDateTime end,
                                            List<String> urisList,
                                            boolean unique) {
        String uri;
        if (urisList.isEmpty()) {
            uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/stats")
                    .queryParam("start", start.format(formatter))
                    .queryParam("end", end.format(formatter))
                    .queryParam("unique", unique)
                    .build()
                    .toUriString();
        } else {
            uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/stats")
                    .queryParam("start", start.format(formatter))
                    .queryParam("end", end.format(formatter))
                    .queryParam("uris", urisList)
                    .queryParam("unique", unique)
                    .build()
                    .toUriString();
        }

        return Objects.requireNonNull(webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntityList(ViewStatsDto.class)
                .block()).getBody();
    }
}
