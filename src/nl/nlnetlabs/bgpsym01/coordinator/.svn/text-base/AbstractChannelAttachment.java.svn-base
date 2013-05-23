package nl.nlnetlabs.bgpsym01.coordinator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.callback.sym.SymCallback;
import nl.nlnetlabs.bgpsym01.callback.sym.SymCallbackFactory;
import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.main.tcp.OverloadMonitor;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.InputHandler;
import nl.nlnetlabs.bgpsym01.primitives.types.ByteFIFO;
import nl.nlnetlabs.bgpsym01.primitives.types.ByteFIFOImpl;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

abstract public class AbstractChannelAttachment {

    private static Logger log = Logger.getLogger(AbstractChannelAttachment.class);

    // how long can a message be...
    protected static final int BITS_SIZE = 30;

    private static final int BITS_NEEDED_TO_DECODE_COMMAND = (int) Math.ceil((double) BITS_SIZE / (double) Byte.SIZE);

    private static final boolean useInputHanlder = XProperties.getInstance().isUseInputHandlerThread();

    private ByteFIFO inputByteFIFO = new ByteFIFOImpl(SystemConstants.DEFAULT_BYTE_FIFO_SIZE, true);
    protected ByteFIFO outputByteFIFO = new ByteFIFOImpl(SystemConstants.DEFAULT_BYTE_FIFO_SIZE, false);

    private static ArrayList<ByteFIFO> outputByteFIFOs = new ArrayList<ByteFIFO>();

    protected SymCallback callback = SymCallbackFactory.getInstance().getCallback();

    protected byte[] tmpArray;
    private boolean isCurrent;
    private int size;

    protected OverloadMonitor monitor;

    private boolean overloaded;

    private int maxCut;

    // protected ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // protected EDataOutputStream eos = new EDataOutputStream();

    protected EDataInputStream eis = new EDataInputStream();

    public static void registerByteFIFO(ByteFIFOImpl fifo) {
        synchronized (outputByteFIFOs) {
            outputByteFIFOs.add(fifo);
        }
    }

    public static Iterator<ByteFIFO> getFIFOsIterator() {
        return outputByteFIFOs.iterator();
    }

    public AbstractChannelAttachment() {
        // outputByteFIFO.setCut(false);
        tmpArray = new byte[SystemConstants.BYTE_ARRAY_SIZE];
    }

    // protected void write(ByteBuffer buf) throws IOException {
    // buf.flip();
    // inputByteFIFO.getOutputStream().write(buf.array(), 0, buf.limit());
    // buf.clear();
    // }

    public boolean wantsToWrite() {
        return outputByteFIFO.available() > 0;
    }

    public boolean wantsToRead() {
        // most attachments always want to read
        return true;
    }

    public void fillBuffer(ByteBuffer buffer) throws IOException {
        final int a = outputByteFIFO.available();
        final int b = buffer.capacity();
        final int toWrite = a <= b ? a : b;

        InputStream inputStream = outputByteFIFO.getInputStream();
        inputStream.read(tmpArray, 0, toWrite);
        try {
            buffer.put(tmpArray, 0, toWrite);
            inputStream.close();
        } catch (BufferOverflowException e) {
            // TODO: make this thing silent only when we are shutting down...
        }
    }

    public void cutOutputBuffer(int toCut) {
        if (toCut > maxCut) {
            maxCut = toCut;
        }
        callback.bytesSent(toCut);
        int av;
        outputByteFIFO.removeBytes(toCut);
        av = outputByteFIFO.available();
        if (monitor != null && av < monitor.getFifoLowValue()) {
            if (overloaded) {
                /*                    if (log.isInfoEnabled()) {
                                        log.info("fifo is back ok");
                                    }
                 */monitor.backOK();
                 /*                    if (log.isInfoEnabled()) {
                                        log.info("called monitor for back OK");
                                    }
                  */overloaded = false;
            }
        }
    }

    public void read(ByteBuffer buffer, InputHandlerThread inputHandlerThread) throws IOException {
        inputByteFIFO.getOutputStream().write(buffer.array(), 0, buffer.limit());

        while (inputByteFIFO.available() > 0) {
            InputStream inputStream = inputByteFIFO.getInputStream();
            if (!isCurrent) {
                if (inputByteFIFO.available() < BITS_NEEDED_TO_DECODE_COMMAND) {
                    // we are not even able to read the size
                    break;
                }

                eis.init(inputStream);
                size = eis.readBits(BITS_SIZE);
                isCurrent = true;
                eis.close();
                inputStream = inputByteFIFO.getInputStream();
            }

            // if we are here then for sure isCurrent == true
            if (inputStream.available() >= size) {
                isCurrent = false;
                long start = System.currentTimeMillis();
                if (useInputHanlder) {
                    byte[] array = new byte[size];
                    inputStream.read(array, 0, size);

                    ByteArrayInputStream bais = new ByteArrayInputStream(array);
                    inputHandlerThread.add(bais, getInputHandler());
                } else {
                    getInputHandler().handleInput(inputStream);
                }
                inputStream.close();
                long end = System.currentTimeMillis();
                if (end - start > 200) {
                    log.info("timeX=" + (end - start));
                }
            } else {
                break;
            }
        }
    }

    abstract protected InputHandler getInputHandler() throws IOException;

    public void sendObject(EExternalizable toSend) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EDataOutputStream eos = new EDataOutputStream(baos);
        // eos.init(baos);
        try {
            toSend.writeExternal(eos);
            eos.close();

            int av;
            synchronized (this) {
                OutputStream outputStream = outputByteFIFO.getOutputStream();
                eos.init(outputStream);
                eos.writeBits(baos.size(), BITS_SIZE);
                eos.close();

                baos.writeTo(outputStream);
                if (log.isDebugEnabled()) {
                    log.debug("to be sent: " + outputByteFIFO.available() + ", baos.size()=" + baos.size());
                }
                av = outputByteFIFO.available();
                if (monitor != null && av > monitor.getFifoBigValue()) {
                    if (!overloaded) {
                        /*                        if (log.isInfoEnabled()) {
                                                    log.info("fifo overload...");
                                                }*/
                        overloaded = true;
                        monitor.overload(av);
                    }
                }
            }
        } catch (IOException e) {
            log.error("", e);
            throw new BGPSymException(e);
        }
    }

    public void setMonitor(OverloadMonitor monitor) {
        this.monitor = monitor;
    }

}