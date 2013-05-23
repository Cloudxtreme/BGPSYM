package nl.nlnetlabs.bgpsym01.main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;

import org.apache.log4j.Logger;

public class StorageCompressor {

    private static Logger log = Logger.getLogger(StorageCompressor.class);

    Process process;

    private boolean shutdown;

    public void compressStorage() {
        try {
            // this is linux only
            // Runtime.getRuntime().exec
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("/bin/tar", "zcf",
                    XProperties.getInstance().getStorageFileName(XProperties.getInstance().prefixCount, Tools.getInstance().getProcNum()), "-C", XProperties
                            .getInstance().getDiskCacheDir(), ".");
            process = pb.start();
            synchronized (this) {
                // we have to do that to avoid race condition - shutdown() might
                // be called just before start()
                if (shutdown) {
                    process.destroy();
                }
            }
            try {
                int exitValue = process.waitFor();
                if (log.isInfoEnabled()) {
                    log.info("exitValue=" + exitValue);
                }
            } catch (InterruptedException e) {
            }

        } catch (IOException e) {
            throw new BGPSymException(e);
        } finally {

        }
    }

    public synchronized void shutdown() {
        shutdown = true;
        if (process != null) {
            process.destroy();
        }
    }

    public void loadCompressedStorage(int myNum) {
        XProperties properties = XProperties.getInstance();
        String storageFileName = properties.getStorageFileName(properties.prefixStartingPoint, myNum);
        File gzFile = new File(storageFileName);
        if (!gzFile.exists()) {
            throw new BGPSymException(new FileNotFoundException("name=" + storageFileName));
        }

        // decompress the file
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("/bin/tar", "zxf", storageFileName, "-C", properties.getDiskCacheDir());
        Process process;
        int exitVal = -1;
        try {
            process = pb.start();
            process.getInputStream().close();
            process.getOutputStream().close();
            exitVal = process.waitFor();
        } catch (Exception e) {
            throw new BGPSymException(e);
        }

        if (exitVal != 0) {
            InputStream errorStream = process.getErrorStream();
            BufferedInputStream bis = new BufferedInputStream(errorStream);
            byte[] tmp = new byte[1024];
            try {
                bis.read(tmp);
            } catch (IOException e) {
            }
            throw new BGPSymException("unable to decompress, exitVal=" + exitVal + ", error=" + new String(tmp));
        }

        if (log.isInfoEnabled()) {
            log.info("decompressed...");
        }
    }

}
