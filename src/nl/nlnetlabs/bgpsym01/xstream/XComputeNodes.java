package nl.nlnetlabs.bgpsym01.xstream;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dasNodes")
public class XComputeNodes {

    private ArrayList<XRegistry> registries;

	@XStreamAlias("coordinator")
    private XRegistry coordinator;

    public ArrayList<XRegistry> getRegistries() {
        return registries;
    }

    public void setRegistries(ArrayList<XRegistry> registries) {
        this.registries = registries;
    }

    public XRegistry getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(XRegistry coordinator) {
        this.coordinator = coordinator;
    }

    public void addXRegistry(XRegistry registry) {
        if (registries == null) {
            registries = new ArrayList<XRegistry>();
        }
        registries.add(registry);
    }
}
