package nl.nlnetlabs.bgpsym01.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.nlnetlabs.bgpsym01.cache.PrefixCacheImplBlock;
import nl.nlnetlabs.bgpsym01.cache.PrefixInfo;
import nl.nlnetlabs.bgpsym01.coordinator.events.framework.EventSchedule;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreMapImpl;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class RIBRequestCommand extends MasterCommand {
	private static final String OUTPUT_FILENAME_PREFIX = "rib_";
	
    // not serialized
    int count;

	private EventSchedule eventSchedule;

	private long currentTime;

	public RIBRequestCommand () {
	}

	public RIBRequestCommand (EventSchedule eventSchedule) {
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
    
    OutputStream getStream(ASIdentifier asIdentifier) {
        try {
            return new FileOutputStream(new File(getFilename(asIdentifier)), true);
        } catch (FileNotFoundException e) {
            throw new BGPSymException(e);
        }
    }
    
	File getDirectory () {
		return new File(XProperties.getInstance().getResultDirectory() + File.separator + Tools.getInstance().getStartAsString());
	}

	String getFilename(ASIdentifier asId) {
		return getDirectory().getAbsolutePath() + File.separator + OUTPUT_FILENAME_PREFIX + asId.toString();
	}

	private void addCommand(BGPProcess process) {
    	RunnableUpdate update = new RunnableUpdate () {
            @Override
            public void run(BGPProcess process) {
            	writeRIB(process);
            	sent();
            }
        };

		process.getQueue().addMessage(update);
	}
	
	private void writeRIB (BGPProcess process) {
		OutputStream stream = getStream(process.getAsIdentifier());
		
		StringBuffer result = new StringBuffer();
		
		PrefixStoreMapImpl store = (PrefixStoreMapImpl) process.getStore();
		PrefixCacheImplBlock cache = (PrefixCacheImplBlock) store.getCache();
		LinkedHashMap<Prefix, PrefixInfo> table = cache.getTable();
		
		try {
			try {
				result.append("{'prefixes':[");
				
				Iterator<Entry<Prefix, PrefixInfo>> iterator = table.entrySet().iterator();
				Entry<Prefix, PrefixInfo> current;
				while (iterator.hasNext()) {
					current = iterator.next();
					PrefixInfo info = current.getValue();
					appendInfo(result, info, iterator.hasNext());
				}
				
				result.append("]}");
				stream.write(result.toString().getBytes());
			}
			finally {
				stream.close();
			 }
		} catch(IOException e) {
			throw new BGPSymException(e);
		}
	}
	
	private void appendNeighbors(StringBuffer result, Map<ASIdentifier, PrefixTableEntry> neighborsMap) {
		result.append("'ns':[");
		Iterator<Entry<ASIdentifier, PrefixTableEntry>> iterator = neighborsMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<ASIdentifier, PrefixTableEntry> current = iterator.next();
			ASIdentifier asId = current.getKey();
			PrefixTableEntry value = current.getValue();
			result.append("{").append("'as':").append(asId.getInternalId()).append(",");
			result.append("'r':").append(value.getRoute().toStringFormat());
			result.append("'o':").append(value.getOriginator().getInternalId());
			if (iterator.hasNext()) {
				result.append("},");
			}
			else {
				result.append("}");
			}
		}
		
		result.append("],");
	}
	
	private void appendCurrent(StringBuffer result, PrefixTableEntry current) {
		result.append("'c': {");
		if (current.getRoute() != null) {
			result.append("'r':").append(current.getRoute().toStringFormat()).append(",");
		}
		result.append("'o':").append(current.getOriginator().getInternalId()).append("}");
	}
	
	private void appendInfo (StringBuffer result, PrefixInfo info, boolean hasNext) {
		result.append("{").append("'p':").append(info.getPrefix().getNum()).append(",");
		
		appendNeighbors(result, info.getNeighborsMap());
		appendCurrent(result, info.getCurrentEntry());
		
		if (hasNext) {
			result.append("},");
		}
		else {
			result.append("}");
		}
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
