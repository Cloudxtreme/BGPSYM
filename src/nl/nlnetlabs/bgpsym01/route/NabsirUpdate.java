package nl.nlnetlabs.bgpsym01.route;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import nl.nlnetlabs.bgpsym01.coordinator.events.PrefixData;
import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.primitives.BGPSymException;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Route;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EExternalizable;

/**
 * Represent one BGP updates as seen by RIS monitor in NABSIR format (see
 * http://__TODO__)
 * 
 */
public class NabsirUpdate implements EExternalizable {

    private static final String DEFAULT_STARTING_DATE = "01/01/08 00:00:00";
    private Prefix prefix;
    private Route route;

    private boolean isWithdrawal;
    private long time;

    private ASIdentifier from;
    private ASIdentifier to;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Prefix getPrefix() {
        return prefix;
    }

    public void setPrefix(Prefix prefix) {
        this.prefix = prefix;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public boolean isWithdrawal() {
        return isWithdrawal;
    }

    public void setWithdrawal(boolean isWithdrawal) {
        this.isWithdrawal = isWithdrawal;
    }

    public ASIdentifier getFrom() {
        return from;
    }

    public void setFrom(ASIdentifier from) {
        this.from = from;
    }

    public ASIdentifier getTo() {
        return to;
    }

    public void setTo(ASIdentifier to) {
        this.to = to;
    }

    private boolean eq(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return o1 == o2;
        }
        return o1.equals(o2);
    }


    String getTxtDate(long arg, long l) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        try {
            // only coordinator calls it, so having it as static is OK
            calendar.setTime(format.parse(DEFAULT_STARTING_DATE));
            calendar.setTimeInMillis(calendar.getTimeInMillis() + arg + l);
            return format.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace(System.err);
            throw new BGPSymException(e);
        }
    }


    public String toString(Map<String, PrefixData> map) {
        String path = "[";
        if (route != null) {
            int num = 0;
            for (ASIdentifier asId : route.getHops()) {
                if (num++ > 0) {
                    path += " ";
                }
                path += " " + asId.getASNum();
            }
        }
        path += "]";
        String with = isWithdrawal ? "True" : "False";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(from.getASNum());
        stringBuilder.append("  ;  ");
        stringBuilder.append(to.getASNum());
        stringBuilder.append("  ;  ");
        stringBuilder.append(getName(map, prefix));
        stringBuilder.append("  ;  ");
        stringBuilder.append(with);
        stringBuilder.append(" ; False ; ");
        stringBuilder.append(getTxtDate(time, getAdditional(map, prefix)));
        stringBuilder.append("  ;  ");
        stringBuilder.append(path);
        stringBuilder.append("  ;  None  ;  Update");
        return stringBuilder.toString();
    }

    public long getAdditional(Map<String, PrefixData> map, Prefix prefix) {
        PrefixData prefixData = map.get(prefix.toString());
        return prefixData == null ? 0 : prefixData.additional;
    }

    private String getName(Map<String, PrefixData> map, Prefix prefix) {
        PrefixData prefixData = map.get(prefix.toString());
        return prefixData == null ? null : prefixData.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NabsirUpdate) {
            NabsirUpdate update = (NabsirUpdate) obj;
            return eq(update.prefix, prefix) && eq(update.route, route) && isWithdrawal == update.isWithdrawal && eq(update.from, from) && eq(update.to, to)
            && eq(update.time, time);
        }
        return false;
    }

    public void readExternal(EDataInputStream in) throws IOException {
        from = ASIdentifier.staticReadExternal(in);
        to = ASIdentifier.staticReadExternal(in);
        time = in.readLong();
        if (in.readBoolean()) {
            route = new Route();
            route.readExternal(in);
        }
        isWithdrawal = in.readBoolean();
        prefix = Prefix.getInstance(in.readBits(SystemConstants.PREFIX_SIZE_BITS));
    }

    public void writeExternal(EDataOutputStream out) throws IOException {
        from.writeExternal(out);
        to.writeExternal(out);
        out.writeLong(time);
        out.writeBoolean(route != null);
        if (route != null) {
            route.writeExternal(out);
        }
        out.writeBoolean(isWithdrawal);
        out.writeBits(prefix.getNum(), SystemConstants.PREFIX_SIZE_BITS);
    }

}
