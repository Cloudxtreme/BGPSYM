package nl.nlnetlabs.bgpsym01.callback.sym;

import java.util.HashMap;

public class SymCallbackFactory {

    private static SymCallbackFactory instance = new SymCallbackFactory();

    private HashMap<String, SymCallback> callbacks = new HashMap<String, SymCallback>();

    private SymCallbackFactory() {

    }

    public static SymCallbackFactory getInstance() {
        return instance;
    }

    public SymCallback getCallback() {
        return getCallback(Thread.currentThread().getName());
    }

    public SymCallback getCallback(String name) {

        SymCallback callback;
        synchronized (callbacks) {
            callback = callbacks.get(name);
            if (callback == null) {
                callback = new SymCallbackImpl();
                callbacks.put(name, callback);
            }
        }

        return callback;
    }

}
