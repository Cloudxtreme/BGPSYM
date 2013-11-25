package nl.nlnetlabs.bgpsym01.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.command.RouteViewDataResponse;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.PrefixTableEntry;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.route.PeerRelation;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreMapImpl;

import org.apache.log4j.Logger;
import org.json.XML;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

public class ResultWriterLog {
	private static final String OUTPUT_FILENAME_PREFIX = "log_";

	private static Logger log = Logger.getLogger(ResultWriterLog.class);

	private ASIdentifier asId;

	private OutputStream stream;
	
	private List<String> logs;

	private XStream xStream;

	//private List<Collection<RouteViewDataResponse>> responseList;
	
	private List<Map<Prefix,PrefixInfo>> responseList; 
	
	private List<Map<Neighbor, List<Prefix>>> filtered;
	
	private Map<String, Integer> values;
	
	public ResultWriterLog(ASIdentifier asId) {
		this.asId = asId;
		this.logs = new ArrayList<String>();
		xStream = XStreamFactory.getXStream();
		//responseList = new ArrayList<Collection<RouteViewDataResponse>>();
		responseList = new ArrayList<Map<Prefix,PrefixInfo>>();
		filtered = new ArrayList<Map<Neighbor, List<Prefix>>>();
		values = new HashMap<String, Integer>();
		
		// p w u up uw ub
		values.put("p", 0);
		values.put("w", 0);
		values.put("u", 0);
		values.put("up", 0);
		values.put("uw", 0);
		values.put("ub", 0);
	}

    OutputStream getStream() {
        try {
            return new FileOutputStream(new File(getFilename(this.asId)), true);
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
    }

	File getDirectory () {
		return new File(XProperties.getInstance().getResultDirectory() + File.separator + Tools.getInstance().getStartAsString());
	}

	String getFilename(ASIdentifier asId) {
		return getDirectory().getAbsolutePath() + File.separator + OUTPUT_FILENAME_PREFIX + asId.toString();
	}
	
	public Map<Neighbor, List<Prefix>> clone (Map<Neighbor, List<Prefix>> filteredPrefixes) {
		Iterator<Entry<Neighbor, List<Prefix>>> iterator = filteredPrefixes.entrySet().iterator();
		
		Map<Neighbor, List<Prefix>> clone = new TreeMap<Neighbor, List<Prefix>>();
		
		while (iterator.hasNext()) {
			Entry<Neighbor, List<Prefix>> entry = iterator.next();
			Neighbor key = entry.getKey();
			List<Prefix> value = entry.getValue();
			
			List<Prefix> backup = new ArrayList<Prefix>();
			
			for (Prefix pr : value) {
				backup.add(pr);
			}
			
			clone.put(key, backup);
			
		}
		
		return clone;
	}

	public void writeLog (BGPProcess process, long currentTime) {
			values.put("p", process.getReceivedPrefixes()-values.get("p"));
			values.put("w", process.getReceivedWithdrawals()-values.get("w"));
			values.put("u", process.getUpdates()-values.get("u"));
			values.put("up", process.getUpdatesWithPrefixes()-values.get("up"));
			values.put("uw", process.getUpdatesWithWithdrawals()-values.get("uw"));
			values.put("ub", process.getUpdatesWithBoth()-values.get("ub"));
		
			String state = "<l t=\""+currentTime+"\"" +
					" p=\""+values.get("p")+"\"" +
					" w=\""+values.get("w")+"\"" +
					" u=\""+values.get("u")+"\"" +
					" up=\""+values.get("up")+"\"" +
					" uw=\""+values.get("uw")+"\"" +
					" ub=\""+values.get("ub")+"\">";
			state += "<ns>";

			PrefixStoreMapImpl store = (PrefixStoreMapImpl) process.getStore();
			Iterator<Neighbor> neighbors = process.getNeighbors().iterator();

			while (neighbors.hasNext()) {
				Neighbor neighbor = neighbors.next();
				state += "<n r=\""+(PeerRelation)neighbor.getAttachment()+"\" v=\""+neighbor.isValid()+"\">"+neighbor.getASIdentifier().getId()+"</n>";
			}

			state += "</ns>";
			
			/*Collection<RouteViewDataResponse> prefixDataList = store.getPrefixDataList();
			responseList.add(prefixDataList);*/
			
			//responseList.add(new LinkedHashMap<Prefix, PrefixInfo>(store.getCache().getTable()));
			state += getStats(store.getCache().getTable());
			
			/*OutputBufferImpl outputBuffer = (OutputBufferImpl) store.getOutputBuffer();
			OutputStateImpl outputState = (OutputStateImpl) outputBuffer.getOutputState();
			
			filtered.add(clone(outputState.getPrefixes()));*/

			logs.add(state);
	}

	public void close () {
		try {
			stream = getStream();
			
			StringBuffer result = new StringBuffer();
			
			try {
				int i = 0;
				
				result.append("<ls>");
				
				//stream.write("<ls>".getBytes());
				
				for (String log : logs) {
					result.append(log);
					//stream.write(log.getBytes());
					
					
					//stream.write("<f>".getBytes());
					/*result.append("<f>");
					Iterator<Entry<Neighbor, List<Prefix>>> iteratorPrefixes = this.filtered.get(i).entrySet().iterator();
					while (iteratorPrefixes.hasNext()) {
						Entry<Neighbor, List<Prefix>> entry = iteratorPrefixes.next();
						Neighbor neighbor = entry.getKey();
						
						String prefixString = "";
						for (Prefix prefix : entry.getValue()) {
							prefixString += prefix+"|";
						}
						
						String n = "<n as=\""+neighbor.getASIdentifier()+"\" p=\""+prefixString+"\" />";
						//stream.write(n.getBytes());
						result.append(n);
					}					
					
					result.append("</f>");*/
					
					//stream.write("</f>".getBytes());
					//stream.write("<rs>".getBytes());
					
					//result.append(getStats(responseList.get(i)));
					
					//result.append(getStats(responseList.get(i)));

					//stream.write("</rs></l>".getBytes());
					result.append("</l>");
					
					i++;
				}
				
				result.append("</ls>");
				//stream.write("</ls>".getBytes());
				stream.write(XML.toJSONObject(result.toString()).toString().getBytes());
			}
			finally {
				stream.close();
			 }
		} catch(IOException e) {
			throw new BGPSymException(e);
		}
	}
	
	//public String getStats (Collection<RouteViewDataResponse> responses) {
	public String getStats (Map<Prefix, PrefixInfo> prefixes) {
		StringBuffer result = new StringBuffer();
		
		float totalRouteLength = 0f;
		int totalRoutes = 0;
		int lostRoutes = 0;
		Set<ASIdentifier> reachableASes = new HashSet<ASIdentifier>();
		
		Iterator<PrefixInfo> iterator = prefixes.values().iterator();
		
		while (iterator.hasNext()) {
			PrefixInfo current = iterator.next();
			PrefixTableEntry pte = current.getCurrentEntry();
			
			if (pte != null) {
				Route route = pte.getRoute();
				
				if (route == null || route.getHops() == null) {
					lostRoutes++;
					continue;
				}
				
				totalRouteLength += route.getHops().length;
				totalRoutes++;
				
				reachableASes.add(route.getOrigin());
			}
		}
		
		/*Iterator<RouteViewDataResponse> iteratorResponses = responses.iterator();
		RouteViewDataResponse currentResponse;
		while (iteratorResponses.hasNext()) {
			currentResponse = iteratorResponses.next();
			
			Route route = currentResponse.route;
			
			if (route == null || route.getOrigin() == null) {
				lostRoutes++;
				continue;
			}
			
			totalRouteLength += route.getHops().length;
			totalRoutes++;
			
			reachableASes.add(route.getOrigin());
		}*/
		 
		float averageRouteLength = (totalRoutes> 0) ? totalRouteLength / totalRoutes : 0;
		
		result.append("<stats ").append("t='").append(totalRoutes).append("' ")
			.append("ra='").append(reachableASes.size()).append("' ")
			.append("ar='").append(averageRouteLength).append("' ")
			.append("lr='").append(lostRoutes).append("'")
			.append("/>");
		
		return result.toString();
	}
}
