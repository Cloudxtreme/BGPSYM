package nl.nlnetlabs.bgpsym01.primitives.timers;

public class TimeControllerFactory {

    public static TimeController timeController = new TimeControllerImpl();

    public static TimeController oldInstance;

    public static TimeController getTimeController() {
        return timeController;
    }

    public static void setTimeController(TimeController timeController) {
        oldInstance = TimeControllerFactory.timeController;
        TimeControllerFactory.timeController = timeController;
    }

    public static void reload() {
        if (oldInstance != null) {
            timeController = oldInstance;
        }
    }

}
