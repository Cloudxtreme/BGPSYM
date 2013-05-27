package nl.nlnetlabs.bgpsym01.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import nl.nlnetlabs.bgpsym01.main.SystemConstants;
import nl.nlnetlabs.bgpsym01.main.Tools;
import nl.nlnetlabs.bgpsym01.neighbor.impl.NeighborImplTCP;
import nl.nlnetlabs.bgpsym01.neighbor.impl.TCPConnection;
import nl.nlnetlabs.bgpsym01.primitives.bgp.ASIdentifier;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Prefix;
import nl.nlnetlabs.bgpsym01.primitives.bgp.RunnableUpdate;
import nl.nlnetlabs.bgpsym01.primitives.bgp.Update;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;
import nl.nlnetlabs.bgpsym01.process.BGPProcess;
import nl.nlnetlabs.bgpsym01.route.MRAITimer;
import nl.nlnetlabs.bgpsym01.route.MRAITimerImpl;
import nl.nlnetlabs.bgpsym01.route.PeerRelation;
import nl.nlnetlabs.bgpsym01.primitives.mocks.MRAITimerMock;
import nl.nlnetlabs.bgpsym01.xstream.XProperties;
import nl.nlnetlabs.bgpsym01.xstream.XRegistry;

import org.apache.log4j.Logger;

/**
 * This command connect neighbors to queue of given asIdentifier
 */
public class ConnectCommand extends MasterCommand {

	private static Logger log = Logger.getLogger(ConnectCommand.class);

    private ASIdentifier asIdentifier;

    private List<ASIdentifier> asIds;

	private static Random random = new Random(System.currentTimeMillis() * 19 + Tools.getInstance().getProcNum());

    @Override
    public CommandType getCommandType() {
        return CommandType.CONNECT_COMMAND;
    }

    @Override
    public void process() {
		final boolean hasRealMRAITimer = random.nextInt(100) <= XProperties.getInstance().mraiProc;

        Update update = new RunnableUpdate() {

            @Override
            public void run(BGPProcess process) {
				//log.info("received connect command with as: "+asIdentifier+" ases: "+asIds);
                
				for (ASIdentifier asId : asIds) {
					XRegistry xRegistry = process.getRegistries().get(asId.getProcessId());
					TCPConnection connection = (TCPConnection) xRegistry.getAttachment();
					if (connection == null) {
						//log.info("no connection yet with "+asId);
					}

					NeighborImplTCP n = new NeighborImplTCP(asId, connection);
					n.setTimer(getMRAITimer(n, hasRealMRAITimer));

					// @TODO accomodate transfer of peer relation
					n.setAttachment(PeerRelation.PEER);

                    process.getNeighbors().addNeighbor(n);
                }
            }
        };
        asIdentifier.getProcess().getQueue().addMessage(update);
    }

	@SuppressWarnings("unused")
	private MRAITimer getMRAITimer (NeighborImplTCP n, boolean hasRealMRAITimer) {
		if (1 < 2) {
			MRAITimerImpl timer = new MRAITimerImpl();
			int threshhold = hasRealMRAITimer ? 30000 : 0;
			timer.setThreshold(threshhold);
			timer.setAsIdentifier(n.getASIdentifier());
			return timer;
		}
		else {
			return new MRAITimerMock();
		}
	}

    public ASIdentifier getAsIdentifier() {
        return asIdentifier;
    }

    public void setAsIdentifier(ASIdentifier asIdentifier) {
        this.asIdentifier = asIdentifier;
    }

    @Override
    protected void readInternalData(EDataInputStream in) throws IOException {
        asIdentifier = ASIdentifier.staticReadExternal(in);
        int size = in.readInt();
        asIds = new ArrayList<ASIdentifier>(size);
        for (int i = 0; i < size; i++) {
            asIds.add(ASIdentifier.staticReadExternal(in));
        }
        for (int j = 0; j < size; j++) {
        	int value = in.readInt();
        	asIds.get(j).setAttachment(PeerRelation.getByValue(value));
        }
    }

    @Override
    protected void writeInternalData(EDataOutputStream out) throws IOException {
        asIdentifier.writeExternal(out);
        out.writeList(asIds);
		for (ASIdentifier as : asIds) {
			PeerRelation pr = (PeerRelation) as.getAttachment();
			out.writeInt(pr.getValue());
		}
    }

    public List<ASIdentifier> getAsIds() {
        return asIds;
    }

    public void setAsIds(List<ASIdentifier> ids) {
        this.asIds = ids;
    }

}
