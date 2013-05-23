package nl.nlnetlabs.bgpsym01.xstream;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("system")
public class XSystem {

    private ArrayList<ASIdentifier> ases;

    private ArrayList<XNode> nodes;

    public ArrayList<ASIdentifier> getAses() {
        return ases;
    }

    public void setAses(ArrayList<ASIdentifier> ases) {
        this.ases = ases;
    }

    public ArrayList<XNode> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<XNode> nodes) {
        this.nodes = nodes;
    }

}
