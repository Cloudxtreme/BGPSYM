package nl.nlnetlabs.bgpsym01.primitives;

import java.io.IOException;
import java.io.InputStream;

public interface InputHandler {

    public void handleInput(InputStream inputStream) throws IOException;

}
