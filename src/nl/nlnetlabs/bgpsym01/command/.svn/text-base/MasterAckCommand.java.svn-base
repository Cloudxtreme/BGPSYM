package nl.nlnetlabs.bgpsym01.command;

public class MasterAckCommand extends MasterCommand {

    @Override
    public CommandType getCommandType() {
        return CommandType.MASTERACK;
    }

    @Override
    public void process() {
        jvm.gotAck();
    }

}
