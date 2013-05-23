package nl.nlnetlabs.bgpsym01.xstream;

import java.util.ArrayList;

import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("node")
public class XNode {

    private ASIdentifier asIdentifier;

    private ArrayList<XNeighbor> neighbors;

    public XNode() {
        neighbors = new ArrayList<XNeighbor>();
    }

    @Override
    public String toString() {
        return asIdentifier.toString();
    }

    public ArrayList<XNeighbor> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<XNeighbor> neighbors) {
        this.neighbors = neighbors;
    }

    public void addNeighbor(XNeighbor n) {
        neighbors.add(n);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XNode) {
            XNode tmp = (XNode) obj;
            return asIdentifier.equals(tmp.asIdentifier);
        }
        return false;
    }

    public ASIdentifier getAsIdentifier() {
        return asIdentifier;
    }

    public void setAsIdentifier(ASIdentifier asIdentifier) {
        this.asIdentifier = asIdentifier;
    }

}
