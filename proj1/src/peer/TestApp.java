package peer;

import java.io.Console;
import java.util.Arrays;

import peer.clientCommands.ClientCommand;

// $ java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>

public class TestApp {

    public static void main(String[] args) {
        // Obtain the Client Command
        if ((args.length < 2) || (args.length > 4)) {
            System.out.println("java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }
        ClientCommand command = null;
        try {
            command = ClientCommand.parseArgs(args);
            System.out.println("Args successfully parsed");
            command.execute();
        }
        catch (IllegalArgumentException ex) {
            return;
        }
        if (command == null) {
            System.out.println("java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            System.out.println("sub_protocol: STATE | BACKUP | DELETE | RESTORE | RECLAIM");
            return;
        }

        // 
        System.out.println(Arrays.toString(args));
    }    
}