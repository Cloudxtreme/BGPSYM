package nl.nlnetlabs.bgpsym01.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("prefix")
public class XPrefix {

    private String prefix;

    @XStreamAlias("num")
    @XStreamAsAttribute
    private int prefixNum;

    @XStreamAlias("as")
    private String asName;

    @XStreamAlias("asNum")
    private int asInternalId;

    public XPrefix(String prefix, int prefixNum, String asName, int asInternalId) {
        this.prefix = prefix;
        this.prefixNum = prefixNum;
        this.asName = asName;
        this.asInternalId = asInternalId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getPrefixNum() {
        return prefixNum;
    }

    public void setPrefixNum(int prefixNum) {
        this.prefixNum = prefixNum;
    }

    public String getAsName() {
        return asName;
    }

    public void setAsName(String asName) {
        this.asName = asName;
    }

    @Override
    public String toString() {
        return "XPR[" + prefix + "(" + prefixNum + ")@" + asName + "(" + asInternalId + ")]";
    }

    public int getAsInternalId() {
        return asInternalId;
    }

    public void setAsInternalId(int asInternal) {
        this.asInternalId = asInternal;
    }
}
