package nl.nlnetlabs.bgpsym01.coordinator.events;

import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.CommandSenderHelper;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.DisconnectHelper;
import nl.nlnetlabs.bgpsym01.coordinator.helpers.ConnectHelper;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * An abstract basic event. TODO !
 */
public abstract class Event {

    @Override
    public String toString() {
        return "toString not implemented, class=" + getClass().getName();
    }

    @XStreamAsAttribute
    private boolean showOnScreen;

    @XStreamOmitField
    private CommandSenderHelper commandSenderHelper;

	@XStreamOmitField
	private DisconnectHelper disconnectHelper;

	@XStreamOmitField
	private ConnectHelper connectHelper;

    abstract public void process();

    public CommandSenderHelper getCommandSenderHelper() {
        return commandSenderHelper;
    }

    public void setCommandSenderHelper(CommandSenderHelper commandSenderHelper) {
        this.commandSenderHelper = commandSenderHelper;
    }

	public DisconnectHelper getDisconnectHelper() {
		return disconnectHelper;
	}

	public void setDisconnectHelper(DisconnectHelper disconnectHelper) {
		this.disconnectHelper = disconnectHelper;
	}

	public ConnectHelper getConnectHelper() {
		return connectHelper;
	}

	public void setConnectHelper (ConnectHelper connectHelper) {
		this.connectHelper = connectHelper;
	}

    abstract public EventSchedule getEventSchedule();

    public boolean isShowOnScreen() {
        return showOnScreen;
    }

}
