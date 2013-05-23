package nl.nlnetlabs.bgpsym01.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("registry")
public class XRegistry {

    private String host;

    private int port;

    @XStreamOmitField
    private Object attachment;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

	public XRegistry() {
		super();
	}

    public XRegistry(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object registry) {
        this.attachment = registry;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

}
