package nl.nlnetlabs.bgpsym01.callback;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;

import org.apache.log4j.Logger;

public abstract class FileCallbackAbstract extends CallbackMock {

    private BufferedWriter writer;
    private static Logger log = Logger.getLogger(FileCallbackAbstract.class);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private FileWriter fileWriter;

    public FileCallbackAbstract(String fileName) throws IOException {
        fileWriter = new FileWriter(fileName);
        writer = new BufferedWriter(fileWriter);
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            log.error(e);
            throw new BGPSymException(e);
        }
    }

    protected void write(String string) {
        try {
            writer.write(dateFormat.format(new Date()));
            writer.write(" ");
            writer.write(string);
            writer.write("\n");
            // writer.flush();
        } catch (IOException e) {
            log.error("write error", e);
        }
    }

    @Override
    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new BGPSymException(e);
        }
    }

}
