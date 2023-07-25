package ru.practicum;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class StatClient {
    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${service.url}")
    private final String baseUrl;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public ResponseEntity<Object> getStatistics(String start,
                                                String end,
                                                String urisString,
                                                boolean unique) throws IOException, InterruptedException {
        URI url = URI.create(baseUrl + "/stats" + "?start=" + start + "&end=" + end + "&uris=" + urisString
                + "&unique=" + unique);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new ResponseEntity<>(response.body(), HttpStatusCode.valueOf(response.statusCode()));
        //ResponseEntity<Object> mappedResponse = mapper.readValue((JsonParser) response, ResponseEntity.class);
        //return mappedResponse;
    }

    public int saveHit(HitDto hit) throws IOException, InterruptedException {
        URI url = URI.create(baseUrl + "/hit");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(String.valueOf(hit));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }
}