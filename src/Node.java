
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public abstract class Node {

    public static final int SERVER_SRC_PORT = 5005;
    public static final int SENSOR_START_PORT = 3000;
    public static final int PACKET_SIZE = 65536;
    public static final int GATEWAYSENSOR_SRC_PORT = 5003;
    public static final int GATEWAYSERVER_SRC_PORT = 5004;
    public static final int TIMEOUT_TIME = 50000; //10000 = 1s, 50000 = 5s etc.  Set to 100 or lower for fun results.



    public abstract void toSend();
    public abstract void toReceive();

    Listener listener;
    String name;
    InetSocketAddress dstAddress;
    int srcPort;
    int dstPort;


    public void startListener() {
        listener.start();
    }

    public synchronized int getSrcPort(DatagramPacket packet) {
        byte[] toReturn = new byte[4];
        byte[] packetToCheck = packet.getData();
        for (int i = 0; i < 4; i++) {
            toReturn[i] = packetToCheck[i];
        }
        String s = new String(toReturn);
        int num = Integer.parseInt(s);
        return num;
    }

    public synchronized int getDstPort(DatagramPacket packet) {
        byte[] toReturn = new byte[4];
        byte[] packetToCheck = packet.getData();
        for (int i = 0; i < 4; i++) {
            toReturn[i] = packetToCheck[i + 5];
        }
        String s = new String(toReturn);
        int num = Integer.parseInt(s);
        return num;
    }

    public synchronized int getSeqNum(DatagramPacket packet) {
        byte[] toReturn = new byte[1];
        byte[] packetToCheck = packet.getData();
        toReturn[0] = packetToCheck[10];
        String s = new String(toReturn);
        //System.out.println(s);
        int num = Integer.parseInt(s);
        return num;
    }

    public synchronized byte[] assembleHeader(byte[] header, int sourcePort, int destPort, int sequenceNumber) {
        byte[] headerToReturn = null;
        if (sourcePort < 65536 && destPort < 65536) {
            String srcAndDstPort = Integer.toString(sourcePort) + "_" + Integer.toString(destPort)
                    + "_" + Integer.toString(sequenceNumber) + "_";
            headerToReturn = srcAndDstPort.getBytes();
        }
        return headerToReturn;
    }

    public synchronized String getAck(DatagramPacket packet) {
        byte[] payload = packet.getData();
        byte[] s = new byte[5];
        String toReturn;
        for (int i = 12; i < payload.length; i++) {
            if (payload[i] != 0) {
                s[i-11] = payload[i];
            }
        }
        toReturn = new String(s);
        return toReturn;
    }

    public class Listener extends Thread implements Runnable {

        Listener () {}

        public synchronized void run() {
            while (true) {
                toReceive();
                toSend();
            }
        }

        public void start() {
            new Thread(this).start();
       }
    }
}
