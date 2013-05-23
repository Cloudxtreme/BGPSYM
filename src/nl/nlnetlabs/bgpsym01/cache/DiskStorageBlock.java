package nl.nlnetlabs.bgpsym01.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import nl.nlnetlabs.bgpsym01.callback.sym.SymCallback;
import nl.nlnetlabs.bgpsym01.callback.sym.SymCallbackFactory;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueue;
import nl.nlnetlabs.bgpsym01.primitives.types.FIFOQueueImpl;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public class DiskStorageBlock implements DiskStorage {

    RandomAccessFile file;

    private final short[] prefixesOnDisk;

    private static Logger log = Logger.getLogger(DiskStorageBlock.class);

    private final byte[] byteArray;

    private SymCallback callback = SymCallbackFactory.getInstance().getCallback();

    private final int blockSize;

    private String arrayFileName;

    private EDataInputStream eis = new EDataInputStream();

    private EDataOutputStream eos = new EDataOutputStream();

    int blocksCount = 0;

    public FIFOQueue<Integer> freeBlocks = new FIFOQueueImpl<Integer>(64);

    ByteArrayOutputStream baos1;
    ByteArrayOutputStream baos2;

    public DiskStorageBlock(int neighborsSize, String fileName, String arrayFileName) {
        blockSize = Tools.getInstance().getPrefixBlockSize(neighborsSize);

        this.arrayFileName = arrayFileName;

        byteArray = new byte[blockSize];

        baos1 = new ByteArrayOutputStream(blockSize);
        baos2 = new ByteArrayOutputStream(blockSize);

        File f = new File(fileName);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        try {
            file = new RandomAccessFile(fileName, "rw");
        } catch (FileNotFoundException e) {
            log.error("error", e);
            e.printStackTrace(System.err);
            throw new BGPSymException(e);
        }
        // get proper array :)
        f = new File(arrayFileName);
        prefixesOnDisk = new short[XProperties.getInstance().getPrefixArraySize()];
        Arrays.fill(prefixesOnDisk, (short) -1);
        loadPermanentData(f);

    }

    @SuppressWarnings("unchecked")
    private void loadPermanentData(File file) {
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                // prefixesOnDisk
                short[] ar = (short[]) ois.readObject();
                System.arraycopy(ar, 0, prefixesOnDisk, 0, Math.min(XProperties.getInstance().getPrefixArraySize(), ar.length));

                // freeBlocks
                freeBlocks = (FIFOQueue<Integer>) ois.readObject();
                blocksCount = ois.readInt();
                ois.close();
            } catch (IOException e) {
                throw new BGPSymException(e);
            } catch (ClassNotFoundException e) {
            }
        }
    }

    int getBlockNumber() {
        if (freeBlocks.size() > 0) {
            return freeBlocks.remove();
        }
        return blocksCount++;
        /*        if (log.isInfoEnabled()) {
                    log.info("out=" + out + ", blocksCount=" + blocksCount);
                }*/
    }

    void giveBlockBack(int num) {
        if (num > blocksCount) {
            throw new BGPSymException("num>blocksCount, num=" + num + ", blockCount=" + blocksCount);
        }
        freeBlocks.add(num);
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.cache.block.DiskStorage#writePrefixArrayPermanent(java.lang.String)
     */

    public void writePrefixArrayPermanent() throws IOException {
        ObjectOutputStream dos = new ObjectOutputStream(new FileOutputStream(new File(arrayFileName)));
        dos.writeObject(prefixesOnDisk);
        dos.writeObject(freeBlocks);
        dos.writeInt(blocksCount);
        dos.close();
        file.getFD().sync();
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.cache.block.DiskStorage#storePrefixes(java.util.Iterator)
     */

    public void storePrefixes(Iterator<PrefixInfo> iterator) throws IOException {

        int blockNumber = getBlockNumber();
        ByteArrayOutputStream baos = writePrefixesToStream(iterator, blockNumber);

        if (log.isDebugEnabled()) {
            log.debug("writing to blockNumber " + blockNumber);
        }
        long time = System.currentTimeMillis();
        try {
            file.seek(blockNumber * blockSize);
            file.write(baos.toByteArray());
        } catch (IOException e) {
            log.error("IOException, " + e.getMessage() + ", blockNumber=" + blockNumber);
            throw new BGPSymException(e);
        }
        time = System.currentTimeMillis() - time;
        callback.prefixStored(time);

    }

    ByteArrayOutputStream writePrefixesToStream(Iterator<PrefixInfo> iterator, int blockNumber) throws IOException {

        baos2.reset();
        baos1.reset();

        int count = 0;
        int tmpSize = 0;
        while (iterator.hasNext()) {
            PrefixInfo prefixInfo = iterator.next();

            writeEntry(baos2, prefixInfo);

            if (fits(baos1, baos2)) {
                baos2.writeTo(baos1);
                prefixesOnDisk[prefixInfo.getPrefix().getNum()] = (short) blockNumber;
            } else {

                tmpSize = baos2.size();

                /*
                 * this is a nice feature of LinkedHashMap iterator - it does not change element
                 * access time so if if we did touch the element its position in the cache has
                 * not changed :)
                 */
                if (log.isDebugEnabled()) {
                    log.debug("prefix " + prefixInfo.getPrefix() + " did not fit");
                }
                baos2.reset();
                break;
            }
            baos2.reset();
            count++;

            iterator.remove();

            // we can't write more than 1 byte!
            if (count >= 255) {
                break;
            }
        }

        if (count == 0) {
            throw new BGPSymException("not even 1 prefix fits into the block..., tmpSize=" + tmpSize);
        }
        if (log.isDebugEnabled()) {
            log.debug("written count=" + count);
        }
        // rewrite everything to baos2
        baos2.write(count);
        baos1.writeTo(baos2);
        int b2size = baos2.size();
        baos2.write(byteArray, 0, blockSize - b2size);

        callback.prefixStored(count, b2size, blockSize);

        return baos2;
    }

    /* (non-Javadoc)
     * @see nl.nlnetlabs.bgpsym01.cache.block.DiskStorage#readPrefix(nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix)
     */

    public Iterator<PrefixInfo> readPrefix(Prefix prefix) {
        short blockNumber = prefixesOnDisk[prefix.getNum()];
        if (blockNumber == -1) {
            return null;
        }
        try {
            file.seek(blockNumber * blockSize);
            file.readFully(byteArray);

            if (log.isDebugEnabled()) {
                log.debug("reading from block number " + blockNumber);
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            giveBlockBack(blockNumber);
            return getPrefixesFromStream(bais);

        } catch (IOException e) {
            log.error("error", e);
            e.printStackTrace(System.err);
            throw new BGPSymException(e);
        }
    }

    public Iterator<PrefixInfo> getPrefixesFromStream(ByteArrayInputStream bais) throws IOException {

        // how many prefixes there are
        int count = bais.read();
        if (log.isDebugEnabled()) {
            log.debug("reading " + count + " messages");
        }
        ArrayList<PrefixInfo> prefixes = new ArrayList<PrefixInfo>(count);
        for (int i = 0; i < count; i++) {
            PrefixInfo prefixInfo = readPrefixRecord(bais);
            prefixesOnDisk[prefixInfo.getPrefix().getNum()] = -1;
            prefixes.add(prefixInfo);
        }

        if (log.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();
            for (PrefixInfo pi : prefixes) {
                msg.append("| ").append(pi);
            }
            log.debug("loaded prefixes: " + prefixes.size() + ": " + msg.toString());
        }

        return prefixes.iterator();
    }

    /**
     * 
     * @param baos
     * @param baosTmp
     * @return true if baosTmp fits into baos
     */
    private boolean fits(ByteArrayOutputStream baos, ByteArrayOutputStream baosTmp) {
        return blockSize - baos.size() - 1 >= baosTmp.size();
    }

    void writeEntry(ByteArrayOutputStream baos, PrefixInfo prefixInfo) throws IOException {

        eos.init(baos);
        prefixInfo.writeExternal(eos);
        eos.close();

    }

    PrefixInfo readPrefixRecord(ByteArrayInputStream bais) throws IOException {

        eis.init(bais);
        PrefixInfo prefixInfo = new PrefixInfo();
        prefixInfo.readExternal(eis);
        return prefixInfo;

    }

    public void sync() {
        try {
            file.getFD().sync();
        } catch (IOException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
    }

	public int getBlockSize () {
		return this.blockSize;
	}

	public int getBlocksCount() {
		return this.blocksCount;
	}

	public RandomAccessFile getFile () {
		return this.file;
	}
}
