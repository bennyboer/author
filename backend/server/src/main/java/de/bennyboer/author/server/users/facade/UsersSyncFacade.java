package de.bennyboer.author.server.users.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.server.users.persistence.lookup.LookupUser;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupRepo;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserService;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Value
@AllArgsConstructor
public class UsersSyncFacade {

    UserService userService;

    UserLookupRepo userLookupRepo;

    public Mono<Void> updateUserLookupById(UserId userId) {
        return userService.get(userId)
                .map(this::toLookupUser)
                .flatMap(userLookupRepo::update)
                .then();
    }

    public Mono<Void> removeUserFromLookup(UserId userId) {
        return userLookupRepo.remove(userId);
    }

    private LookupUser toLookupUser(User user) {
        return LookupUser.of(
                user.getId(),
                user.getName(),
                user.getMail()
        );
    }

    public Mono<Void> sendMailUpdateConfirmationMail(UserId userId, Version version) {
        return userService.get(userId, version)
                .doOnNext(user -> {
                    log.info(
                            """
                                    [TODO] Send mail update confirmation mail to user '{}' with name '{}' and new mail '{}' and token '{}'.
                                    Visit '{}' to confirm the mail.
                                    """,
                            user.getId(),
                            user.getName(),
                            user.getPendingMail().orElseThrow(),
                            user.getMailConfirmationToken().orElseThrow(),
                            "http://localhost:4200/users/mail/confirmation?userId=%s&mail=%s&token=%s".formatted(
                                    user.getId().getValue(),
                                    URLEncoder.encode(
                                            user.getPendingMail().orElseThrow().getValue(),
                                            StandardCharsets.UTF_8
                                    ),
                                    user.getMailConfirmationToken().orElseThrow().getValue()
                            )
                    );
                })
                .then();
    }

}
