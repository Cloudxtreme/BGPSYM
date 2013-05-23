package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.coordinator.events.Event;

public interface EventStream extends Iterator<Event> {

    public boolean isFinished();

    public void shutdown();

    public boolean isReady();

    public long getWaitingTime();

}
