package nl.nlnetlabs.bgpsym01.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.NotImplementedException;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

/**
 * This class stores prefixes in memory compressing them using GZIP
 * 
 * @see GZIPStorageTest
 */
public class DiskStorageGZIP implements DiskStorage {

    private static Logger log = Logger.getLogger(DiskStorageGZIP.class);

    private final byte[][] prefixStreams;

    private final int blockSize;

    public DiskStorageGZIP(int size) {
        blockSize = Tools.getInstance().getPrefixBlockSize(size);
        prefixStreams = new byte[XProperties.getInstance().getPrefixArraySize()][];
    }

    public Iterator<PrefixInfo> readPrefix(Prefix prefix) {
        byte ar[] = prefixStreams[prefix.getNum()];
        if (ar == null) {
            return null;
        }

        int size = 0;
        size += ((ar[0] + 256) % 256) << 8;
        size += ((ar[1] + 256) % 256);

        LinkedList<PrefixInfo> list = new LinkedList<PrefixInfo>();
        ByteArrayInputStream bais = new ByteArrayInputStream(ar, 2, ar.length - 2);
        try {
            GZIPInputStream gzIn;
            EDataInputStream eis;
            gzIn = new GZIPInputStream(bais);
            eis = new EDataInputStream(gzIn);
            for (int i = 0; i < size; i++) {
                PrefixInfo pi = new PrefixInfo();
                pi.readExternal(eis);
                list.add(pi);
                int num = pi.getPrefix().getNum();
                prefixStreams[num] = null;
            }
        } catch (IOException e) {
            log.error(e);
            throw new BGPSymException(e);
        }

        return list.iterator();
    }

    public void storePrefixes(Iterator<PrefixInfo> iterator) throws IOException {
        long time = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // leave the space for lenght
        baos.write(0);
        baos.write(0);

        EDataOutputStream eos = new EDataOutputStream(new GZIPOutputStream(baos));
        ArrayList<Integer> prefixes = new ArrayList<Integer>();

        int counter = 0;
        while (iterator.hasNext()) {
            PrefixInfo prefixInfo = iterator.next();
            prefixInfo.writeExternal(eos);

            prefixes.add(prefixInfo.getPrefix().getNum());

            iterator.remove();
            counter++;

            if (eos.getSize() > blockSize) {
                break;
            }
        }
        eos.close();

        byte[] ar = baos.toByteArray();
        ar[0] = (byte) ((counter >>> 8) & 0xFF);
        ar[1] = (byte) ((counter >>> 0) & 0xFF);
        for (int x : prefixes) {
            prefixStreams[x] = ar;
        }

        time = System.currentTimeMillis() - time;
        // callback.prefixStored(time);

        if (log.isInfoEnabled()) {
            // log.info("written " + counter + ", size=" + ar.length + ", time="
            // + time + ", blockSize=" + blockSize);
        }
    }

    public void writePrefixArrayPermanent() throws IOException {
        throw new NotImplementedException();
    }

    public void sync() {
    }

}
