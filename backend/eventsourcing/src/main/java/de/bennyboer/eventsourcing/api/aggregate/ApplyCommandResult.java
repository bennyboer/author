package de.bennyboer.eventsourcing.api.aggregate;

import de.bennyboer.eventsourcing.api.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplyCommandResult {

    /**
     * The events that were emitted by the aggregate as an result of the command.
     * There may be zero, one or multiple events.
     */
    List<Event> events;

    public static ApplyCommandResult of(List<Event> events) {
        if (events == null) {
            throw new IllegalArgumentException("Events must not be null");
        }

        return new ApplyCommandResult(events);
    }

    public static ApplyCommandResult of(Event... events) {
        return ApplyCommandResult.of(List.of(events));
    }

    public static ApplyCommandResult of() {
        return ApplyCommandResult.of(List.of());
    }

}
