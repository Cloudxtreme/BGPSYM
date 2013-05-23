package nl.nlnetlabs.bgpsym01.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

public class DiskTest {

    private RandomAccessFile file;
    private static Logger log = Logger.getLogger(DiskTest.class);

    public DiskTest(String name) throws FileNotFoundException {
        file = new RandomAccessFile(name, "rwd");
    }

    public DiskTest() throws FileNotFoundException {
        this("/home/wojciech/work/test/file");
    }

    public static void main(String[] args) throws IOException {
        new DiskTest().run();
    }

    private void run() throws IOException {
        long writeStart = System.currentTimeMillis();
        file.seek(1012);
        file.write("krótki tekst".getBytes());
        long writeEnd = System.currentTimeMillis();

        byte[] tab = new byte[10];

        long readStart = System.currentTimeMillis();
        file.seek(1012);
        file.readFully(tab);
        long readEnd = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("write time: " + (writeEnd - writeStart) + ", read time: " + (readEnd - readStart));
        }
    }

}
