package nl.nlnetlabs.bgpsym01.command;

public class WaitForEmptyCommand extends MasterCommand {

    @Override
    public CommandType getCommandType() {
        return CommandType.WAITFOREMPTY;
    }

    @Override
    public void process() {
        Thread thread = new Thread() {

            @Override
            public void run() {
                // wait for empty queues
                waitForEmptyQueues();
                sendAckToCoordinator();
            }
        };
        thread.start();
    }

}
