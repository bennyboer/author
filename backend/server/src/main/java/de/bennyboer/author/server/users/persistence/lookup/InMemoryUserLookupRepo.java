package de.bennyboer.author.server.users.persistence.lookup;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.persistence.readmodel.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.author.user.Mail;
import de.bennyboer.author.user.UserName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class InMemoryUserLookupRepo extends InMemoryEventSourcingReadModelRepo<UserId, LookupUser>
        implements UserLookupRepo {

    @Override
    protected UserId getId(LookupUser readModel) {
        return readModel.getId();
    }

    @Override
    public Mono<UserId> findUserIdByName(UserName name) {
        return Flux.fromIterable(lookup.entrySet())
                .filter(entry -> entry.getValue().getName().equals(name))
                .map(Map.Entry::getKey)
                .next();
    }

    @Override
    public Mono<UserId> findUserIdByMail(Mail mail) {
        return Flux.fromIterable(lookup.entrySet())
                .filter(entry -> entry.getValue().getMail().equals(mail))
                .map(Map.Entry::getKey)
                .next();
    }

    @Override
    public Mono<Long> countUsers() {
        return Mono.fromCallable(() -> (long) lookup.size());
    }

    @Override
    public Mono<Void> update(LookupUser readModel) {
        return assertThatUserDoesNotExistByNameOrMail(readModel.getId(), readModel.getName(), readModel.getMail())
                .then(super.update(readModel));
    }

    private Mono<Void> assertThatUserDoesNotExistByNameOrMail(UserId userId, UserName name, Mail mail) {
        return assertThatUserDoesNotExistByName(userId, name)
                .then(assertThatUserDoesNotExistByMail(userId, mail));
    }

    private Mono<Void> assertThatUserDoesNotExistByName(UserId userId, UserName name) {
        return findUserIdByName(name)
                .filter(uId -> !uId.equals(userId))
                .flatMap(uId -> Mono.error(new IllegalArgumentException(
                        "User with name %s already exists".formatted(name)
                )));
    }

    private Mono<Void> assertThatUserDoesNotExistByMail(UserId userId, Mail mail) {
        return findUserIdByMail(mail)
                .filter(uId -> !uId.equals(userId))
                .flatMap(uId -> Mono.error(new IllegalArgumentException(
                        "User with mail %s already exists".formatted(mail)
                )));
    }

}
