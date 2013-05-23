package nl.nlnetlabs.bgpsym01.command;

import java.util.NoSuchElementException;

public enum CommandType {

    ANNOUNCE(AnnounceCommand.class, 1), ACK(AckCommand.class, 2), MASTERACK(MasterAckCommand.class, 3), OVERLOAD(OverloadCommand.class, 4), DIAGNOSTIC(
            DiagnosticCommand.class, 6), STORE_PREFIXES(StorePrefixesCommand.class, 7), WAITFOREMPTY(WaitForEmptyCommand.class, 9), SHUTDOWN(

                    ShutdownCommand.class, 10), CHANGE_CALLBACK(ChangeCallbackCommand.class, 11), SYNC_FILES(SyncFilesCommand.class, 12), DISCONNECT_COMMAND(
                            DisconnectCommand.class, 13), SET_REGISTRY(SetRegistryCommand.class, 14), LAST_SEEN(LastSeenRequestCommand.class, 15), LAST_SEEN_RESP(
                                    LastSeenResponseCommand.class, 16), SYNC_TIME(SyncTimeCommand.class, 17), RTT(RTTCommand.class, 18), RTT_RESPONSE(RTTCommandResponse.class, 19), PREFIX_RESET(
                                            PrefixDataResetCommand.class, 20), RIS_REQUEST(RISGetDataRequestCommand.class, 21), RIS_RESPONSE(RISGetDataResponseCommand.class, 22), INVALIDATE_COMMAND(
                                                    InvalidateCommand.class, 23), LOG_REQUEST(LogRequestCommand.class, 24), CONNECT_COMMAND(ConnectCommand.class, 25);

    /**
     * How many bits do we need to encode command type?
     */
    public static int BIT_SIZE = 5;

    private final Class<? extends CoordinationCommand> clazz;
    private final int num;

    CommandType(Class<? extends CoordinationCommand> name, int num) {
        this.num = num;
        this.clazz = name;
    }

    public Class<? extends CoordinationCommand> getClazz() {
        return clazz;
    }

    public int getNum() {
        return num;
    }

    public static CommandType getClassByNumber(int num) {
        for (CommandType element : CommandType.values()) {
            if (element.getNum() == num) {
                return element;
            }
        }
        throw new NoSuchElementException("num=" + num);
    }

}
