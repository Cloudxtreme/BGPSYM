package nl.nlnetlabs.bgpsym01.coordinator.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeController;
import nl.nlnetlabs.bgpsym01.primitives.timers.TimeControllerFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.ShutdownadbleThread;
import nl.nlnetlabs.bgpsym01.primitives.types.StaticThread;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("noise")
public class EventNoise extends Event {

    private static final int DEFAULT_ASES_COUNT = 10000;

    @XStreamOmitField
    private long sleepTime;

    @XStreamAlias("schedule")
    private EventSchedule eventSchedule;

    @XStreamAsAttribute
    private int asesCount;

    private class NoiseThread extends ShutdownadbleThread {

        private boolean shutdown;

        private int prefix = XProperties.getInstance().bogusPrefixMin + 1;

        private Random random = new Random();

        private TimeController timeController = TimeControllerFactory.getTimeController();

        @Override
        public void run() {
            setName("NOISE");
            getCommandSenderHelper().attach(this);
            int count = asesCount == 0 ? DEFAULT_ASES_COUNT : asesCount;
            sleepTime = XProperties.getInstance().noiseSleepTime;
            while (!shutdown) {
                ASIdentifier asId = ASFactory.getInstance(random.nextInt(count));
                List<Prefix> prefixList = new ArrayList<Prefix>();
                prefixList.add(Prefix.getInstance(prefix++));
                getCommandSenderHelper().sendUpdate(prefixList, null, asId);
                StaticThread.sleep(timeController.realWaitingTime(timeController.getRealMS(timeController.getCurrentTime() + sleepTime), false));
            }
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

    }

    @Override
    public EventSchedule getEventSchedule() {
        return eventSchedule;
    }

    @Override
    public void process() {
        new NoiseThread().start();
    }

    @Override
    public String toString() {
        return "make some noise :)";
    }

}
