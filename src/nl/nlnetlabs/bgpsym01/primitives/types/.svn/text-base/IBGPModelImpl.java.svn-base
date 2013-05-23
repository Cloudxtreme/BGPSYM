package nl.nlnetlabs.bgpsym01.primitives.types;

import nl.nlnetlabs.bgpsym01.xstream.XProperties;

public class IBGPModelImpl implements IBGPModel {

    private int value;

    public IBGPModelImpl(int maxConvergenceTime, int maxAsSize, int asSize) {
        double d = XProperties.getInstance().iBGPLog ? getValueLog(maxConvergenceTime, maxAsSize, asSize) : getValueExp(maxConvergenceTime, maxAsSize, asSize);
        value = (int) d;
    }

    private double getValueLog(int maxConvergenceTime, int maxAsSize, int asSize) {
        return Math.log(asSize) / Math.log(Math.pow(maxAsSize, 1.0 / maxConvergenceTime));
    }

    private double getValueExp(int maxConvergenceTime, int maxAsSize, int asSize) {
        return (int) Math.pow(maxConvergenceTime, Math.min(asSize, maxAsSize) / maxAsSize);
    }

    // there is a test for it - everytime you change this behavior be sure to
    // change the test behavior
    public int getConvergenceTime() {
        return value;
        // return 0;
    }

}
