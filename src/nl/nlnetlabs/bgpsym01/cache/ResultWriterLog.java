package nl.nlnetlabs.bgpsym01.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.command.RouteViewDataResponse;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.factories.XStreamFactory;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.neighbor.Neighbor;
import nl.nlnetlabs.bgpsym01.route.PeerRelation;
import nl.nlnetlabs.bgpsym01.route.PrefixStoreMapImpl;

import org.apache.log4j.Logger;
import com.thoughtworks.xstream.XStream;

public class ResultWriterLog {
	private static final String OUTPUT_FILENAME_PREFIX = "log_";

	private static Logger log = Logger.getLogger(ResultWriterLog.class);

	private ASIdentifier asId;

	private OutputStream stream;
	
	private List<String> logs;

	private XStream xStream; 
	
	public ResultWriterLog(ASIdentifier asId) {
		this.asId = asId;
		this.logs = new ArrayList<String>();
		xStream = XStreamFactory.getXStream();
	}

    OutputStream getStream() {
        try {
            boolean append = true;
            return new FileOutputStream(new File(getFilename(this.asId)), append);
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

	public void writeLog (BGPProcess process, long currentTime) {
			if (stream == null) {
				stream = getStream();
			}
			
			log.info("ResultWriterLog: retrieving info");
			String state = "<log time=\""+currentTime+"\">\n";
			state += "\t<neighbors>\n";

			PrefixStoreMapImpl store = (PrefixStoreMapImpl) process.getStore();
			//PrefixCacheImplBlock cache = (PrefixCacheImplBlock) store.getCache();
			Iterator<Neighbor> neighbors = process.getNeighbors().iterator();
			
			log.info(neighbors);

			while (neighbors.hasNext()) {
				Neighbor neighbor = neighbors.next();
				state += "\t\t<neighbor rel=\""+(PeerRelation)neighbor.getAttachment()+"\" valid=\""+neighbor.isValid()+"\">"+neighbor.getASIdentifier().getId()+"</neighbor>\n";
			}

			state += "\t</neighbors>\n";
			/*state += "\n\t<responses>\n";

			Collection<RouteViewDataResponse> prefixDataList = store.getPrefixDataList();
			Iterator<RouteViewDataResponse> iterator = prefixDataList.iterator();
			
			log.info("responses: "+prefixDataList.size()+"");

			RouteViewDataResponse currentResponse;
			while (iterator.hasNext()) {
				currentResponse = iterator.next();
				state += xStream.toXML(currentResponse);
			}

			state += "\n\t</responses>\n</log>\n";*/
			logs.add(state);
			
			log.info("ResultWriterLog: written to list");
	}

	public void close () {
		try {
			if (stream != null) {
				stream.write("<logs>\n".getBytes());
				for (String log : logs) {
					stream.write(log.getBytes());
				}
				stream.write("</logs>".getBytes());
				stream.close();
			}
		} catch(IOException e) {
			throw new BGPSymException(e);
		}
	}
}
