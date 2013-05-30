package nl.nlnetlabs.bgpsym01.primitives.bgp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.factories.ASFactory;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("u")
public class BGPUpdate implements Cloneable, Update {

    private static final int PREFIXES_LENGTH_BITS = 18;
    private static final int WITHDRAWAL_LENGTH_BITS = 18;

    /* 
     * though having an array instead of prefixes here would be better in some aspects,
     * we really like the fact that we can add stuff for example using addPrefix method.
     */
    private List<Prefix> prefixes;

    transient private ASIdentifier sender;

    private Collection<Prefix> withdrawals;

    private Route route;

    private long readyTime;

    public BGPUpdate() {

    }

    public BGPUpdate(ASIdentifier sender) {
        super();
        this.sender = sender;
    }

    public BGPUpdate(ASIdentifier sender, Route route) {
        super();
        this.sender = sender;
        this.route = route;
    }

    public UpdateType getType() {
        return UpdateType.BGPUPDATE;
    }

    public BGPUpdate(int id) {
        addPrefix(Prefix.getInstance(id));
    }

    @Override
    public String toString() {
        return "UPDATE pr=" + prefixes + ", route=" + route + ", withdrawals=" + withdrawals;
    }

    public Route getRoute() {
        return route;
    }

    public ASIdentifier getSender() {
        return sender;
    }

    public void setSender(ASIdentifier sender) {
        this.sender = sender;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Collection<Prefix> getWithdrawals() {
        return withdrawals;
    }

    public void setWithdrawals(Collection<Prefix> withdrawals) {
        this.withdrawals = withdrawals;
    }

    public void readExternal(EDataInputStream in) throws IOException {
        // prefix
        int size = in.readBits(PREFIXES_LENGTH_BITS);

        if (size > 0) {
            prefixes = new ArrayList<Prefix>(size);
            for (int i = 0; i < size; i++) {
                addPrefix(Prefix.getInstance(in.readBits(SystemConstants.PREFIX_SIZE_BITS)));
            }
        }

        // as
        sender = ASFactory.getInstance(in.readBits(SystemConstants.AS_SIZE_BITS));

        int tmp = in.readBits(WITHDRAWAL_LENGTH_BITS);
        if (tmp > 0) {
            withdrawals = new ArrayList<Prefix>(tmp);
            for (int i = 0; i < tmp; i++) {
                withdrawals.add(Prefix.getInstance(in.readBits(SystemConstants.PREFIX_SIZE_BITS)));
            }
        }
        // route
        if (in.readBoolean()) {
            route = new Route();
            route.readExternal(in);
        }
    }

    public void writeExternal(EDataOutputStream out) throws IOException {

        if (prefixes != null) {
            out.writeBits(prefixes.size(), PREFIXES_LENGTH_BITS);

            for (Prefix prefix : prefixes) {
                out.writeBits(prefix.getNum(), SystemConstants.PREFIX_SIZE_BITS);
            }
        } else {
            out.writeBits(0, PREFIXES_LENGTH_BITS);
        }

        // as
        out.writeBits(sender.getInternalId(), SystemConstants.AS_SIZE_BITS);

        int tmp = withdrawals != null ? withdrawals.size() : 0;

        // withdrawals
        out.writeBits(tmp, WITHDRAWAL_LENGTH_BITS);
        if (tmp > 0) {
            for (Prefix w : withdrawals) {
                out.writeBits(w.getNum(), SystemConstants.PREFIX_SIZE_BITS);
            }
        }

        out.writeBoolean(route != null);
        if (route != null) {
            route.writeExternal(out);
        }
    }

    @Override
    public boolean equals(Object update) {
        if (update instanceof BGPUpdate) {
            BGPUpdate tmp = (BGPUpdate) update;
            return (prefixes == null ? tmp.prefixes == null : prefixes.equals(tmp.prefixes)) && tmp.route.equals(route) && tmp.sender.equals(sender)
                    && (tmp.withdrawals != null ? tmp.withdrawals.equals(withdrawals) : true);
        }
        return false;
    }

    public List<Prefix> getPrefixes() {
        return prefixes;
    }

    public void addPrefix(Prefix prefix) {
        if (prefixes == null) {
            prefixes = new ArrayList<Prefix>();
        }
        prefixes.add(prefix);
    }

    public void addWithdrawal(Prefix prefix) {
        if (withdrawals == null) {
            withdrawals = new ArrayList<Prefix>();
        }
        withdrawals.add(prefix);
    }

    public void setPrefixes(List<Prefix> prefixes) {
        this.prefixes = prefixes;
    }

    public long getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(long readyTime) {
        this.readyTime = readyTime;
    }

}
