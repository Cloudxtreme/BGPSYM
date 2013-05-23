package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

import org.apache.log4j.Logger;

/**
 * Commands sent from Coordinator to JVM's. Upon receiving the message each JVM
 * has to find appropriate process (if cannot be found then throwing an
 * Exception is a good policy) and run this message along with the process.
 * 
 */
public abstract class CoordinationCommand implements EExternalizable {

    private static Logger log = Logger.getLogger(CoordinationCommand.class);

    public final static CoordinationCommand readCommand(EDataInputStream in) throws IOException {
        int num = in.readBits(CommandType.BIT_SIZE);
        CoordinationCommand command;
        try {
            command = CommandType.getClassByNumber(num).getClazz().newInstance();
        } catch (Exception e) {
            log.error("", e);
            throw new BGPSymException(e);
        }
        command.readExternal(in);
        return command;
    }

    public void readExternal(EDataInputStream in) throws IOException {
        readInternalData(in);
    }

    public final void writeExternal(EDataOutputStream out) throws IOException {
        int num = getCommandType().getNum();
        out.writeBits(num, CommandType.BIT_SIZE);
        writeInternalData(out);
    }

    abstract public void process();

    abstract public CommandType getCommandType();

    abstract protected void readInternalData(EDataInputStream in) throws IOException;

    abstract protected void writeInternalData(EDataOutputStream out) throws IOException;

}
