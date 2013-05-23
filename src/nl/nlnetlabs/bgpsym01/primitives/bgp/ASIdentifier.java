package nl.nlnetlabs.bgpsym01.primitives.bgp;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.route.Policy;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("as")
public class ASIdentifier implements Comparable<ASIdentifier>, EExternalizable {

    private static final String AS_REMOVABLE_PREFIX = "AS";

    private static final int AS_SIZE_BITS = SystemConstants.AS_SIZE_BITS;

    /**
     * 
     */
    @XStreamOmitField
    private static Logger log = Logger.getLogger(ASIdentifier.class);

    @XStreamAlias("pId")
    @XStreamAsAttribute
    private byte processId;

    private ASType type = ASType.NORMAL;

    public ASType getType() {
        return type;
    }

    public void setType(ASType type) {
        this.type = type;
    }

    @XStreamAlias("name")
    private String id;

    @XStreamAlias("id")
    @XStreamAsAttribute
    private short internalId;

    @XStreamOmitField
    private BGPProcess process;

    private Object attachment;

    private Class<? extends Policy> policyClass;

    public ASIdentifier(String id, int internalId) {
        this.id = id;
        if (internalId >= 1 << AS_SIZE_BITS) {
            String msg = "asId=" + internalId + " is bigger than max value=" + (1 << AS_SIZE_BITS);
            log.error(msg);
            throw new RuntimeException(msg);
        }
        this.internalId = (short) internalId;
    }

    public String getASNum() {
        return id.replace(AS_REMOVABLE_PREFIX, "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int compareTo(ASIdentifier o) {
        if (internalId < o.internalId) {
            return -1;
        } else if (internalId == o.internalId) {
            return 0;
        }
        return 1;
    }

    @Override
    public String toString() {
        return id + "|" + internalId + "|" + processId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASIdentifier) {
            ASIdentifier as2 = (ASIdentifier) obj;
            return as2.internalId == internalId;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return internalId + 78;
    }

    public int getInternalId() {
        return internalId;
    }

    public byte getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = (byte) processId;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public Class<? extends Policy> getPolicyClass() {
        return policyClass;
    }

    public void setPolicyClass(Class<? extends Policy> policyClass) {
        this.policyClass = policyClass;
    }

    public BGPProcess getProcess() {
        return process;
    }

    public void setProcess(BGPProcess process) {
        this.process = process;
    }

    public static ASIdentifier staticReadExternal(EDataInputStream in) throws IOException {
        return ASFactory.getInstance(in.readBits(AS_SIZE_BITS));
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
        out.writeBits(internalId, AS_SIZE_BITS);
    }

    public void readExternal(EDataInputStream in) throws IOException {
        throw new NotImplementedException();
    }

}
