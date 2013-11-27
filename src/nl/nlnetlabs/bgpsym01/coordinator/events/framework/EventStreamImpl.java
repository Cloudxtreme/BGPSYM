package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import java.util.NoSuchElementException;

import nl.nlnetlabs.bgpsym01.coordinator.events.Event;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.DisconnectHelper;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;

public class EventStreamImpl implements EventStream {

    private EventBackend backend;

    private boolean finished;

    private Event current;

    private CommandSenderHelper commandSenderHelper;
    
    private DisconnectHelper disconnectHelper;

    public EventStreamImpl() {
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isReady() {
        return getWaitingTime() <= 0;
    }

    public long getWaitingTime() {
        return TimeControllerFactory.getTimeController().getWaitingTime(current.getEventSchedule().getLaunchTime());
    }

    public boolean hasNext() {
        if (finished) {
            return false;
        }
        if (current == null) {
            current = backend.getNext();
            if (current == null) {
                finished = true;
                return false;
            }
        }
        EventSchedule eventSchedule = current.getEventSchedule();
        if (eventSchedule == null) {
            return true;
        }
        if (!eventSchedule.timeIsKnown()) {
            throw new UnsupportedOperationException();
        }

        return true;
    }

    public Event next() {
        if (current == null) {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
        }
        Event output = current;
        enrichEvent(output);
        current = null;
        return output;
    }

    private void enrichEvent(Event event) {
        event.setCommandSenderHelper(commandSenderHelper);
        event.setDisconnectHelper(disconnectHelper);
    }

    public void remove() {
        throw new NotImplementedException();
    }

    public EventBackend getBackend() {
        return backend;
    }

    public void setBackend(EventBackend backend) {
        this.backend = backend;
    }

    public void setCommandSenderHelper(CommandSenderHelper commandSenderHelper) {
        this.commandSenderHelper = commandSenderHelper;
    }
    
    public void setDisconnectHelper(DisconnectHelper disconnectHelper) {
    	this.disconnectHelper = disconnectHelper;
    }

    public void shutdown() {
    }

}
