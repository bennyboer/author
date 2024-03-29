package de.bennyboer.author.server;

import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.auth.token.TokenVerifier;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.server.shared.http.HttpApi;
import de.bennyboer.author.server.shared.modules.PluginInstaller;
import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AppConfig {

    @Builder.Default
    boolean isSecure = false;

    @Builder.Default
    String host = "localhost";

    @Builder.Default
    int port = 7070;

    @Builder.Default
    Profile profile = Profile.PRODUCTION;

    @Builder.Default
    Clock clock = Clock.systemUTC();

    @Builder.Default
    HttpApi httpApi = new HttpApi()
            .registerHttpApiUrl(User.TYPE.getValue(), "{HOST}/api/users")
            .registerHttpApiUrl(Project.TYPE.getValue(), "{HOST}/api/projects")
            .registerHttpApiUrl(Structure.TYPE.getValue(), "{HOST}/api/structures");

    TokenGenerator tokenGenerator;

    TokenVerifier tokenVerifier;

    List<PluginInstaller> plugins;

    public URL getHostUrl() {
        String protocol = isSecure ? "https" : "http";

        try {
            return URI.create("%s://%s:%d".formatted(protocol, host, port)).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
