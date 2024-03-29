package nl.nlnetlabs.bgpsym01.route;

import java.util.NoSuchElementException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("rel")
public enum PeerRelation {

    // he is my peer
    PEER(0, 2),

    // he is my provider
    PROVIDER(-1, 3),

    // he is my customer
    CUSTOMER(1, 1),

    // it's me
    SIBLING(2, 1),

    // doesn't really matter - will never be used
    BOGUS(3, 0),

    // route view - preference does not matter - they never originate
    ROUTEVIEWMONITOR(4, 0),

    RISMONITOR(5, 0);

    private int value;

    private int preference;

    private PeerRelation(int value, int preference) {
        this.value = value;
        this.preference = preference;
    }

    public static PeerRelation getByValue(int value) {
        for (PeerRelation tmp : PeerRelation.values()) {
            if (tmp.value == value) {
                return tmp;
            }
        }
        throw new NoSuchElementException();
    }

    public int getPreference() {
        return preference;
    }

	public int getValue() {
		return value;
	}

}
