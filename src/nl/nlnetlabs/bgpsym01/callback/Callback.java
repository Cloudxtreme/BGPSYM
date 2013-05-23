package nl.nlnetlabs.bgpsym01.callback;

import java.io.IOException;
import java.util.NoSuchElementException;

import nl.nlnetlabs.bgpsym01.primitives.OutputEntity;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.BGPUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

/**
 * This is the generic callback interface. Every interesting event has (should
 * has) a method connected to it.
 * 
 */
public interface Callback {

    enum CallbackType {
        FILE(0), LOG4J(1), MOCK(2);

        private int num;

        private static int BITS = 2;

        private CallbackType(int num) {
            this.num = num;
        }

        public static CallbackType readExternal(EDataInputStream in) throws IOException {
            int num = in.readBits(BITS);
            for (CallbackType type : CallbackType.values()) {
                if (type.num == num) {
                    return type;
                }
            }
            throw new NoSuchElementException();
        }

        public void writeExternal(EDataOutputStream eos) throws IOException {
            eos.writeBits(num, BITS);
        }
    }

    public void prefixRegistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute);

    public void prefixReceived(ASIdentifier asIdentifier, Prefix prefix, Route route);

    public void withdrawalReceived(ASIdentifier asIdentifier, Prefix prefix);

    public void prefixUnregistered(ASIdentifier asIdentifier, Prefix prefix, Route oldRoute, Route newRoute);

    public void prefixAdvertised(ASIdentifier asIdentifier, Prefix prefix, Route route);

    public void withdrawalSent(ASIdentifier asIdentifier, Prefix prefix, Route newRoute);

    public void updateReceived(ASIdentifier asIdentifier, BGPUpdate update);

    public void updateSend(ASIdentifier asIdentifier, BGPUpdate update);

    public void addEntity(OutputEntity entity);

    public void arbitrary(String msg);

    public void close();

    public void flush();

    /**
     * @param asId
     * @param timerStart
     *            (differ in real-world ms)
     * @param storeStart
     *            (differ in real-world ms)
     */
    public void mraiRegister(ASIdentifier asId, long timerStart, long storeStart);

    public void mraiTrigger(ASIdentifier asId);

    /**
     * @param asId
     * @param prefix
     * @param unflap
     *            (in real-world ms)
     * @param readyTime
     *            (in real-world ms)
     */
    public void flapRegister(ASIdentifier asId, Prefix prefix, long unflap, long readyTime);

    /**
     * @param asId
     * @param prefix
     * @param readyTime
     *            (in real-world ms)
     */
    public void flapTrigger(ASIdentifier asId, Prefix prefix, long readyTime);

}
