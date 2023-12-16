package de.bennyboer.author.testing;

import jakarta.annotation.Nullable;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

public class TestClock extends Clock {

    private final Clock clock;

    @Nullable
    private Instant now;

    public TestClock(Clock clock) {
        this.clock = clock;
    }

    public TestClock() {
        this(Clock.systemUTC());
    }

    public void reset() {
        now = null;
    }

    public void setNow(Instant now) {
        this.now = now;
    }

    public void add(Duration duration) {
        initNow();
        now = now.plus(duration);
    }

    public void subtract(Duration duration) {
        initNow();
        now = now.minus(duration);
    }

    private void initNow() {
        if (now == null) {
            now = Instant.now();
        }
    }

    @Override
    public ZoneId getZone() {
        return clock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new TestClock(clock.withZone(zone));
    }

    @Override
    public Instant instant() {
        return Optional.ofNullable(now).orElse(clock.instant());
    }

}
