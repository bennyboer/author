package de.bennyboer.author.server.users.transformer;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.*;
import de.bennyboer.author.user.anonymize.AnonymizedEvent;
import de.bennyboer.author.user.create.CreatedEvent;
import de.bennyboer.author.user.image.ImageUpdatedEvent;
import de.bennyboer.author.user.login.LoggedInEvent;
import de.bennyboer.author.user.login.LoginFailedEvent;
import de.bennyboer.author.user.mail.MailUpdateConfirmedEvent;
import de.bennyboer.author.user.mail.MailUpdateRequestedEvent;
import de.bennyboer.author.user.password.PasswordChangedEvent;
import de.bennyboer.author.user.remove.RemovedEvent;
import de.bennyboer.author.user.rename.RenamedFirstNameEvent;
import de.bennyboer.author.user.rename.RenamedLastNameEvent;
import de.bennyboer.author.user.snapshot.SnapshottedEvent;
import de.bennyboer.author.user.usernamechange.UserNameChangedEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserEventTransformer {

    public static Map<String, Object> toApi(Event event) {
        return switch (event) {
            case CreatedEvent createdEvent -> Map.of(
                    "name", createdEvent.getName().getValue()
            );
            case UserNameChangedEvent userNameChangedEvent -> Map.of(
                    "newName", userNameChangedEvent.getNewName().getValue()
            );
            case RenamedFirstNameEvent renamedFirstNameEvent -> Map.of(
                    "firstName", renamedFirstNameEvent.getFirstName().getValue()
            );
            case RenamedLastNameEvent renamedLastNameEvent -> Map.of(
                    "lastName", renamedLastNameEvent.getLastName().getValue()
            );
            case MailUpdateRequestedEvent mailUpdateRequestedEvent -> Map.of(
                    "mail", mailUpdateRequestedEvent.getMail().getValue()
            );
            case MailUpdateConfirmedEvent mailUpdateConfirmedEvent -> Map.of(
                    "mail", mailUpdateConfirmedEvent.getMail().getValue()
            );
            case ImageUpdatedEvent imageUpdatedEvent -> {
                var result = new HashMap<String, Object>(Map.of(
                        "imageId", imageUpdatedEvent.getImageId().getValue()
                ));

                imageUpdatedEvent.getOldImageId().ifPresent(it -> result.put("oldImageId", it.getValue()));

                yield result;
            }
            default -> Map.of();
        };
    }

    public static Map<String, Object> toSerialized(Event event) {
        return switch (event) {
            case CreatedEvent createdEvent -> Map.of(
                    "name", createdEvent.getName().getValue(),
                    "mail", createdEvent.getMail().getValue(),
                    "firstName", createdEvent.getFirstName().getValue(),
                    "lastName", createdEvent.getLastName().getValue(),
                    "password", createdEvent.getPassword().getValue()
            );
            case UserNameChangedEvent userNameChangedEvent -> Map.of(
                    "newName", userNameChangedEvent.getNewName().getValue()
            );
            case RenamedFirstNameEvent renamedFirstNameEvent -> Map.of(
                    "firstName", renamedFirstNameEvent.getFirstName().getValue()
            );
            case RenamedLastNameEvent renamedLastNameEvent -> Map.of(
                    "lastName", renamedLastNameEvent.getLastName().getValue()
            );
            case SnapshottedEvent snapshottedEvent -> {
                var result = new HashMap<String, Object>(Map.of(
                        "name", snapshottedEvent.getName().getValue(),
                        "mail", snapshottedEvent.getMail().getValue(),
                        "firstName", snapshottedEvent.getFirstName().getValue(),
                        "lastName", snapshottedEvent.getLastName().getValue(),
                        "password", snapshottedEvent.getPassword().getValue(),
                        "createdAt", snapshottedEvent.getCreatedAt().toString()
                ));

                snapshottedEvent.getPendingMail().ifPresent(it -> result.put("pendingMail", it.getValue()));
                snapshottedEvent.getToken().ifPresent(it -> result.put("mailConfirmationToken", it.getValue()));
                snapshottedEvent.getRemovedAt().ifPresent(it -> result.put("removedAt", it.toString()));
                snapshottedEvent.getImageId().ifPresent(it -> result.put("imageId", it.getValue()));

                yield result;
            }
            case PasswordChangedEvent passwordChangedEvent -> Map.of(
                    "password", passwordChangedEvent.getPassword().getValue()
            );
            case MailUpdateRequestedEvent mailUpdateRequestedEvent -> Map.of(
                    "mail", mailUpdateRequestedEvent.getMail().getValue(),
                    "token", mailUpdateRequestedEvent.getToken().getValue()
            );
            case MailUpdateConfirmedEvent mailUpdateConfirmedEvent -> Map.of(
                    "mail", mailUpdateConfirmedEvent.getMail().getValue()
            );
            case ImageUpdatedEvent imageUpdatedEvent -> {
                var result = new HashMap<String, Object>(Map.of(
                        "imageId", imageUpdatedEvent.getImageId().getValue()
                ));

                imageUpdatedEvent.getOldImageId().ifPresent(it -> result.put("oldImageId", it.getValue()));

                yield result;
            }
            case LoggedInEvent ignoredEvent -> Map.of();
            case LoginFailedEvent ignoredEvent -> Map.of();
            case RemovedEvent ignoredEvent -> Map.of();
            case AnonymizedEvent ignoredEvent -> Map.of();
            default -> throw new IllegalArgumentException("Unknown event: " + event.getEventName());
        };
    }

    public static Event fromSerialized(Map<String, Object> payload, EventName eventName, Version ignoredVersion) {
        UserEvent event = UserEvent.fromName(eventName);

        return switch (event) {
            case CREATED -> CreatedEvent.ofStored(
                    UserName.of(payload.get("name").toString()),
                    Mail.of(payload.get("mail").toString()),
                    FirstName.of(payload.get("firstName").toString()),
                    LastName.of(payload.get("lastName").toString()),
                    Password.of(payload.get("password").toString())
            );
            case LOGGED_IN -> LoggedInEvent.of();
            case LOGIN_FAILED -> LoginFailedEvent.of();
            case REMOVED -> RemovedEvent.of();
            case SNAPSHOTTED -> SnapshottedEvent.of(
                    UserName.of(payload.get("name").toString()),
                    Mail.of(payload.get("mail").toString()),
                    Optional.ofNullable(payload.get("pendingMail")).map(it -> Mail.of(it.toString())).orElse(null),
                    Optional.ofNullable(payload.get("mailConfirmationToken"))
                            .map(it -> MailConfirmationToken.of(it.toString()))
                            .orElse(null),
                    FirstName.of(payload.get("firstName").toString()),
                    LastName.of(payload.get("lastName").toString()),
                    Password.of(payload.get("password").toString()),
                    Optional.ofNullable(payload.get("imageId")).map(it -> ImageId.of(it.toString())).orElse(null),
                    Instant.parse(payload.get("createdAt").toString()),
                    Optional.ofNullable(payload.get("removedAt")).map(it -> Instant.parse(it.toString())).orElse(null)
            );
            case USERNAME_CHANGED -> UserNameChangedEvent.of(
                    UserName.of(payload.get("newName").toString())
            );
            case RENAMED_FIRST_NAME -> RenamedFirstNameEvent.of(
                    FirstName.of(payload.get("firstName").toString())
            );
            case RENAMED_LAST_NAME -> RenamedLastNameEvent.of(
                    LastName.of(payload.get("lastName").toString())
            );
            case PASSWORD_CHANGED -> PasswordChangedEvent.ofStored(
                    Password.of(payload.get("password").toString())
            );
            case MAIL_UPDATE_REQUESTED -> MailUpdateRequestedEvent.of(
                    Mail.of(payload.get("mail").toString()),
                    MailConfirmationToken.of(payload.get("token").toString())
            );
            case MAIL_UPDATE_CONFIRMED -> MailUpdateConfirmedEvent.of(
                    Mail.of(payload.get("mail").toString())
            );
            case IMAGE_UPDATED -> ImageUpdatedEvent.of(
                    ImageId.of(payload.get("imageId").toString()),
                    Optional.ofNullable(payload.get("oldImageId")).map(it -> ImageId.of(it.toString())).orElse(null)
            );
            case ANONYMIZED -> AnonymizedEvent.of();
        };
    }

}
