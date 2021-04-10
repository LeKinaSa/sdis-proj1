package peer;

import java.util.Arrays;

import peer.clientCommands.ClientCommand;

// $ java TestApp <peer_access_point> <sub_protocol> <operand_1> <operand_2>

public class TestApp {

    public static void main(String[] args) {
        // Obtain the Client Command
        if ((args.length < 2) || (args.length > 4)) {
            System.out.println("java peer.TestApp <peer_access_point> <sub_protocol> <operand_1> <operand_2>");
            return;
        }
        ClientCommand command;
        try {
            command = ClientCommand.parseArgs(args);
            System.out.println("Args successfully parsed");
        }
        catch (IllegalArgumentException ex) {
            System.out.println("Args couldn't be parsed");
            return;
        }
        if (command == null) {
            System.out.println("java peer.TestApp <peer_ap> <sub_protocol> <operand_1> <operand_2>");
            System.out.println("sub_protocol: STATE | BACKUP | DELETE | RESTORE | RECLAIM");
            return;
        }
        command.execute();
        System.out.println(Arrays.toString(args));
    }    
}