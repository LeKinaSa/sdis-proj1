package peer.messages;

import peer.Channel;
import peer.ChannelName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Message {
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("(?<version>[0-9]\\.[0-9]) +(?<type>PUTCHUNK|STORED|GETCHUNK|CHUNK|DELETE|REMOVED) +(?<senderId>[0-9]+) +((?<fileId>.*\\..*?) +)?((?<chunkNo>[0-9]+) +)?((?<replication>[0-9]+) +)?\r\n\r\n(?<body>.*)");
    private static final String CRLF = "\r\n"; // 0xD 0xA

    private final ChannelName channel;
    protected final Channel mc, mdb, mdr;
    protected final String version;
    protected final int peerId;

    public Message(ChannelName channelName, Channel mc, Channel mdb, Channel mdr, String version, int peerId) {
        this.channel = channelName;
        this.mc = mc;
        this.mdb = mdb;
        this.mdr = mdr;
        this.version = version;
        this.peerId = peerId;
    }

    protected byte[] generateMessageWithBody(String header, byte[] body) {
        ByteArrayOutputStream aux = new ByteArrayOutputStream();
        try {
            aux.write(header.getBytes());
            aux.write(CRLF.getBytes());
            aux.write(CRLF.getBytes());
            aux.write(body);
        }
        catch (IOException exception) {
            return null;
        }
        return aux.toByteArray();
    }

    protected byte[] generateMessageWithoutBody(String header) {
        ByteArrayOutputStream aux = new ByteArrayOutputStream();
        try {
            aux.write(header.getBytes());
            aux.write(CRLF.getBytes());
            aux.write(CRLF.getBytes());
        }
        catch (IOException exception) {
            return null;
        }
        return aux.toByteArray();
    }

    public static Message parse(Channel mc, Channel mdb, Channel mdr, DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        Matcher matcher = MESSAGE_PATTERN.matcher(message);
        if (matcher.matches()) {
            // Matches: version type senderId fileId chunkNo replication body
            String version = matcher.group("version");
            int senderId;
            try {
                senderId = Integer.parseInt(matcher.group("senderId"));
            }
            catch (NumberFormatException exception) {
                return null;
            }
            String messageType = matcher.group("type");
            // Message Types: PUTCHUNK STORED GETCHUNK CHUNK DELETE REMOVED
            switch (messageType) {
                case "PUTCHUNK": {
                    String fileId = matcher.group("fileId");
                    int chunkNo, replication;
                    try {
                        chunkNo = Integer.parseInt(matcher.group("chunkNo"));
                        replication = Integer.parseInt(matcher.group("replication"));
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                    byte[] body = matcher.group("body").getBytes();
                    return new BackupSenderMessage(mc, mdb, mdr, version, senderId, fileId, chunkNo, replication, body);
                }
                case "STORED": {
                    String fileId = matcher.group("fileId");
                    int chunkNo;
                    try {
                        chunkNo = Integer.parseInt(matcher.group("chunkNo"));
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                    return new BackupReceiverMessage(mc, mdb, mdr, version, senderId, fileId, chunkNo);
                }
                case "GETCHUNK": {
                    String fileId = matcher.group("fileId");
                    int chunkNo;
                    try {
                        chunkNo = Integer.parseInt(matcher.group("chunkNo"));
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                    return new RestoreSenderMessage(mc, mdb, mdr, version, senderId, fileId, chunkNo);
                }
                case "CHUNK": {
                    String fileId = matcher.group("fileId");
                    int chunkNo;
                    try {
                        chunkNo = Integer.parseInt(matcher.group("chunkNo"));
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                    byte[] body = matcher.group("body").getBytes();
                    return new RestoreReceiverMessage(mc, mdb, mdr, version, senderId, fileId, chunkNo, body);
                }
                case "DELETE": {
                    String fileId = matcher.group("fileId");
                    return new DeleteSenderMessage(mc, mdb, mdr, version, senderId, fileId);
                }
                case "REMOVED": {
                    String fileId = matcher.group("fileId");
                    int chunkNo;
                    try {
                        chunkNo = Integer.parseInt(matcher.group("chunkNo"));
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                    return new ReclaimReceiverMessage(mc, mdb, mdr, version, senderId, fileId, chunkNo);
                }
                default:
                    return null;
            }
        }
        return null;
    }

    public InetAddress getIp() {
        switch (this.channel) {
            case MC:
                return mc.ip;
            case MDB:
                return mdb.ip;
            case MDR:
                return mdr.ip;
            default:
                return null;
        }
    }

    public int getPort() {
        switch (this.channel) {
            case MC:
                return mc.port;
            case MDB:
                return mdb.port;
            case MDR:
                return mdr.port;
            default:
                return 0;
        }
    }

    public abstract byte[] assemble();

    public abstract Message answer(int id);
}
