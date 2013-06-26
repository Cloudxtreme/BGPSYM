package nl.nlnetlabs.bgpsym01.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

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
	
	private List<String> logs;

	private XStream xStream;

	private List<Collection<RouteViewDataResponse>> responseList;
	
	public ResultWriterLog(ASIdentifier asId) {
		this.asId = asId;
		this.logs = new ArrayList<String>();
		xStream = XStreamFactory.getXStream();
		responseList = new ArrayList<Collection<RouteViewDataResponse>>();
	}

	String getFilename(ASIdentifier asId) {
		//return getDirectory().getAbsolutePath() + File.separator + OUTPUT_FILENAME_PREFIX + "logs.tar.gz/"+ asId.toString();
		return XProperties.getInstance().getResultDirectory() + File.separator + Tools.getInstance().getStartAsString() + File.separator + asId.toString() + ".tar.gz/logs";
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
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(getFilename(this.asId));
			
			Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
				
			try {
				int i = 0;
				
				writer.write("<ls>");
				
				for (String log : logs) {
					writer.write(log);
					Iterator<RouteViewDataResponse> iterator = responseList.get(i).iterator();

					
					RouteViewDataResponse currentResponse;
					while (iterator.hasNext()) {
						currentResponse = iterator.next();
						String response = xStream.toXML(currentResponse)
							.replaceAll("\t", "")
							.replaceAll("\n", "");
						writer.write(response);
					}

					writer.write("</rs></l>");
					
					i++;
				}
				
				writer.write("</ls>");
			}
			finally {
				writer.close();
			 }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new BGPSymException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
