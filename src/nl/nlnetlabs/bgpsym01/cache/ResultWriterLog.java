package nl.nlnetlabs.bgpsym01.cache;

import java.io.BufferedOutputStream;
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

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.log4j.Logger;
import com.thoughtworks.xstream.XStream;

public class ResultWriterLog {
	private static final String OUTPUT_FILENAME_PREFIX = "log_";

	private static Logger log = Logger.getLogger(ResultWriterLog.class);

	private ASIdentifier asId;
	
	private List<String> logs;

	private XStream xStream;

	private List<Collection<RouteViewDataResponse>> responseList;
	
	public ResultWriterLog(ASIdentifier asId) {
		this.asId = asId;
		this.logs = new ArrayList<String>();
		xStream = XStreamFactory.getXStream();
		responseList = new ArrayList<Collection<RouteViewDataResponse>>();
	}

    /*OutputStream getStream() {
        try {
            return new TFileOutputStream(new TFile(getFilename(this.asId)), true);
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
    }*/

	File getDirectory () {
		return new File(XProperties.getInstance().getResultDirectory() + File.separator + Tools.getInstance().getStartAsString());
	}

	String getFilename(ASIdentifier asId) {
		return getDirectory().getAbsolutePath() + File.separator + OUTPUT_FILENAME_PREFIX + asId.toString()+".tar.gz/logs";
	}

	public void writeLog (BGPProcess process, long currentTime) {			
			String state = "<l t=\""+currentTime+"\">";
			state += "<p>"+process.getReceivedPrefixes()+"</p>";
			state += "<w>"+process.getReceivedWithdrawals()+"</w>";
			state += "<ns>\n";

			PrefixStoreMapImpl store = (PrefixStoreMapImpl) process.getStore();
			//PrefixCacheImplBlock cache = (PrefixCacheImplBlock) store.getCache();
			Iterator<Neighbor> neighbors = process.getNeighbors().iterator();

			while (neighbors.hasNext()) {
				Neighbor neighbor = neighbors.next();
				state += "<n r=\""+(PeerRelation)neighbor.getAttachment()+"\">"+neighbor.getASIdentifier().getId()+"</n>";
			}

			state += "</ns>";
			state += "<rs>";

			Collection<RouteViewDataResponse> prefixDataList = store.getPrefixDataList();
			responseList.add(prefixDataList);
			//Iterator<RouteViewDataResponse> iterator = prefixDataList.iterator();

			/*
			RouteViewDataResponse currentResponse;
			while (iterator.hasNext()) {
				currentResponse = iterator.next();
				state += xStream.toXML(currentResponse);
			}

			state += "\n\t</responses>\n</log>\n";*/
			logs.add(state);
	}

	public void close () {
		try {
			FileOutputStream fileOut = new FileOutputStream (new File (getFilename(asId)));
			BufferedOutputStream bufferOut = new BufferedOutputStream (fileOut);
			GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream (bufferOut);
			TarArchiveOutputStream tarOut = new TarArchiveOutputStream (gzipOut);
			
			try {
				int i = 0;
				
				tarOut.write("<ls>".getBytes());
				
				for (String log : logs) {
					tarOut.write(log.getBytes());
					Iterator<RouteViewDataResponse> iterator = responseList.get(i).iterator();

					
					RouteViewDataResponse currentResponse;
					while (iterator.hasNext()) {
						currentResponse = iterator.next();
						String response = xStream.toXML(currentResponse)
							.replaceAll("\t", "")
							.replaceAll("\n", "");
						tarOut.write(response.getBytes());
					}

					tarOut.write("</rs></l>".getBytes());
					
					i++;
				}
				
				tarOut.write("</ls>".getBytes());
			}
			finally {
				tarOut.close();
				gzipOut.close();
				bufferOut.close();
				fileOut.close();
			 }
		} catch(IOException e) {
			throw new BGPSymException(e);
		}
	}
}
