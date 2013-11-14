package nl.nlnetlabs.bgpsym01.coordinator.events.framework;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;

import nl.nlnetlabs.bgpsym01.coordinator.events.Event;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;

import org.apache.log4j.Logger;

public class EventBackendXStreamImpl implements EventBackend {

    private static Logger log = Logger.getLogger(EventBackendXStreamImpl.class);

    private ObjectInputStream ois;

    public EventBackendXStreamImpl(Reader reader) throws IOException {
        ois = XStreamFactory.getXStream().createObjectInputStream(reader);
    }

    public Event getNext() {
        Event event;
        try {
            event = (Event) ois.readObject();
        } catch (EOFException e) {
            // stream is finished
            return null;
        } catch (IOException e) {
            log.error(e);
            throw new BGPSymException(e);
        } catch (ClassNotFoundException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
        return event;
    }

}
