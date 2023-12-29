package de.bennyboer.author.eventsourcing.aggregate;

import de.bennyboer.author.eventsourcing.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplyCommandResult {

    /**
     * The events that were emitted by the aggregate as an result of the command.
     * There may be zero, one or multiple events.
     */
    List<Event> events;

    public static ApplyCommandResult of(List<Event> events) {
        checkNotNull(events, "Events must be given");

        return new ApplyCommandResult(events);
    }

    public static ApplyCommandResult of(Event... events) {
        return ApplyCommandResult.of(List.of(events));
    }

    public static ApplyCommandResult of() {
        return ApplyCommandResult.of(List.of());
    }

}
