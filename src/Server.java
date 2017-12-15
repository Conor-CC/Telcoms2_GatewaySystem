import tcdIO.Terminal;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Server extends Node {

    private int sequenceNum = 0;
    private static final int PACKET_SIZE = 65536;
    DatagramSocket socket;
    DatagramPacket packet;
    String dstHost;
    private Terminal terminal;
    String[][] recievedDataWindow;
    int[] sensorPorts;


    Server (String name, String dstHost, int srcPort, int dstPort, String[][] recievedDataWindow, int[] sensorPorts) {
        listener = new Listener();
        this.name = name;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.dstHost = dstHost;
        try {
            socket = new DatagramSocket(srcPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        terminal = new Terminal(name);
        this.recievedDataWindow = recievedDataWindow;
        this.sensorPorts = sensorPorts;
    }

    public synchronized void toReceive() {
        packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        try {
            socket.receive(packet);
            terminal.println("Packet received from " + dstHost + ": " + getSrcPort(packet));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void toSend() {
        String ack = "ACK";
        String response = "";
        flushDataToMem();
        //printData();
        //closeIfFull();
        response = ack + sequenceNum;
        dstAddress = new InetSocketAddress(dstHost, dstPort);
        byte[] payload = null;
        byte[] header = null;
        byte[] buffer = null;
        payload = response.getBytes();
        header = assembleHeader(header, getSrcPort(packet), getDstPort(packet), sequenceNum);
        buffer = new byte[header.length + payload.length];
        System.arraycopy(header, 0, buffer, 0, header.length);
        System.arraycopy(payload, 0, buffer, header.length, payload.length);
        packet = new DatagramPacket(buffer, buffer.length, dstAddress);
        try {
            socket.send(packet);
            terminal.println("Packet sent to " + packet.getAddress() + ": " + getSrcPort(packet));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void flushDataToMem() {
        for (int i = 0; i < sensorPorts.length; i++) {
            if (getSrcPort(packet) == sensorPorts[i]) {
                if (sequenceNum >= 8) {
                    sequenceNum = getSeqNum(packet) + 1;
                    i = sensorPorts.length;
                }
                else if (recievedDataWindow[i][sequenceNum] == null) {
                    String d = new String(packet.getData());
                    recievedDataWindow[i][sequenceNum] = d.substring(12, 30);
                    sequenceNum = getSeqNum(packet) + 1;
                    i = sensorPorts.length;
                }
            }
        }
    }

    public void closeIfFull() {                             //Optional for debugging purposes mostly
        boolean full = true;
        for (int i = 0; i < sensorPorts.length; i++) {
            for (int j = 0; j < 8; j++) {
                if (recievedDataWindow[i][j] == null) {
                    full = false;
                }
            }
        }
        if (full) {
            terminal.println("All has been received. Woop!");
            System.exit(1);
        }
    }

    public void printData() {                              //Also useful for debugging
        for (int i = 0; i < sensorPorts.length; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.println(recievedDataWindow[i][j]);
            }
        }
        System.out.println("_________________________________\n");
    }
}
