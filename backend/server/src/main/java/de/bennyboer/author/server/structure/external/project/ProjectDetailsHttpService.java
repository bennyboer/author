package de.bennyboer.author.server.structure.external.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bennyboer.author.server.shared.http.Auth;
import de.bennyboer.author.server.shared.http.HttpApi;
import io.javalin.json.JsonMapper;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class ProjectDetailsHttpService implements ProjectDetailsService {

    private final HttpApi api;

    private final JsonMapper jsonMapper;

    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @Override
    public Mono<String> getProjectName(String projectId) {
        String baseUrl = api.getUrlByAggregateType("PROJECT");
        String url = String.format("%s/%s", baseUrl, projectId);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Auth.getSystemToken().getValue())
                .GET()
                .build();

        CompletableFuture<String> future = http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);

        return Mono.fromFuture(future)
                .map(payload -> {
                    ProjectDetails details = jsonMapper.fromJsonString(payload, ProjectDetails.class);
                    return details.name;
                });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProjectDetails {
        public String name;
    }

}
