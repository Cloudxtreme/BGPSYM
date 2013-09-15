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

import org.json.JSONArray;
import org.json.JSONObject;

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

	private static final String FILE_EXT = ".json";
	
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
		return getDirectory().getAbsolutePath() + File.separator + OUTPUT_FILENAME_PREFIX + asId.toString() + FILE_EXT;
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
		
		PrefixStoreMapImpl store = (PrefixStoreMapImpl) process.getStore();
		PrefixCacheImplBlock cache = (PrefixCacheImplBlock) store.getCache();
		LinkedHashMap<Prefix, PrefixInfo> table = cache.getTable();
		
		try {
			try {
				JSONArray prefixes = new JSONArray();
				
				Iterator<Entry<Prefix, PrefixInfo>> iterator = table.entrySet().iterator();
				Entry<Prefix, PrefixInfo> current;
				while (iterator.hasNext()) {
					current = iterator.next();
					PrefixInfo info = current.getValue();
					prefixes.put(getPrefix(info, iterator.hasNext()));
				}
				
				stream.write(prefixes.toString().getBytes());
			}
			finally {
				stream.close();
			 }
		} catch(IOException e) {
			throw new BGPSymException(e);
		}
	}
	
	private JSONArray getNeighbors(Map<ASIdentifier, PrefixTableEntry> neighborsMap) {
		JSONArray neighbors = new JSONArray();
		
		Iterator<Entry<ASIdentifier, PrefixTableEntry>> iterator = neighborsMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<ASIdentifier, PrefixTableEntry> current = iterator.next();
			ASIdentifier asId = current.getKey();
			PrefixTableEntry value = current.getValue();
			JSONObject neighbor = new JSONObject();
			neighbor.put("as", asId.getInternalId());
			neighbor.put("r", value.getRoute().toJSONArray());
			neighbor.append("o", value.getOriginator().getInternalId());
		}
		
		return neighbors;
	}
	
	private JSONObject getCurrent(PrefixTableEntry current) {
		JSONObject result = new JSONObject();
		
		if (current.getRoute() != null) {
			result.put("r", current.getRoute().toJSONArray());
		}
		result.put("o", current.getOriginator().getInternalId());
		
		return result;
	}
	
	private JSONObject getPrefix (PrefixInfo info, boolean hasNext) {
		JSONObject result = new JSONObject();
		result.put("p", info.getPrefix().getNum());
		result.put("ns", getNeighbors(info.getNeighborsMap()));
		result.put("c", getCurrent(info.getCurrentEntry()));
		
		return result;
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
