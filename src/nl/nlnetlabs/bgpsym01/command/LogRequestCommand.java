package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.cache.ResultWriterLog;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;

public class LogRequestCommand extends MasterCommand {
    // not serialized
    int count;

	private EventSchedule eventSchedule;

	private long currentTime;

	public LogRequestCommand () {
	}

	public LogRequestCommand (EventSchedule eventSchedule) {
		this.eventSchedule = eventSchedule;
	}

    @Override
    protected int decCount() {
        return --count;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.LOG_REQUEST;
    }

    @Override
    public void process() {
        count = jvm.getProcesses().size();

		//log.info("processing staterequest for: "+count+" processes");

        for (BGPProcess process : jvm.getProcesses().values()) {
			addCommand(process);
        }
    }

	private void addCommand(BGPProcess process) {
    	RunnableUpdate update = new RunnableUpdate () {
            @Override
            public void run(BGPProcess process) {
				//log.info("process StateRequest for: "+process.getASIdentifier().toString()+" at "+ currentTime);

				ResultWriterLog resultWriter = process.getResultWriterLog();
				resultWriter.writeLog(process, currentTime);

				//log.info("calling info for "+process.getASIdentifier());
				sent();
            }
        };

		process.getQueue().addMessage(update);
	}

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LogRequestCommand)) {
            return false;
        }
        return true;
    }

	@Override
	protected void readInternalData(EDataInputStream in) throws IOException {
		 currentTime = in.readLong();
	}

	@Override
	protected void writeInternalData(EDataOutputStream out) throws IOException {
		out.writeLong(eventSchedule.getLaunchTime());
	}
}
