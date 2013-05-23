package nl.nlnetlabs.bgpsym01.coordinator.helpers;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventProcessor;

public class EventsSenderHelper implements PropagationHelper {

    private EventProcessor processor;

    public void changeLoad(int value) {
        // TODO
    }

    public void end() {
        processor.shutdown();
    }

    public void propagatePrefixes() {
        processor.start();
        try {
            while (processor.isAlive()) {
                processor.join();
            }
        } catch (InterruptedException e) {
        }
    }

    public EventProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(EventProcessor processor) {
        this.processor = processor;
    }

}
