import tcdIO.Terminal;

import java.io.IOException;
import java.net.*;

public class Sensor extends Node {


    DatagramSocket socket;
    private String dstHost;
    private DatagramPacket packet;
    private Terminal terminal;
    private boolean ackReceived = true;
    private boolean transmissionDone = false;
    String[] sensorData;
    int seqNum = 0;
    private int gatewayPort;

    Sensor (String name, String dstHost, int dstPort, int srcPort, int gatewayPort) {
        this.name = name;
        this.srcPort = srcPort;
        this.dstHost = dstHost;
        this.dstPort = dstPort;
        this.gatewayPort = gatewayPort;
        listener = new Listener();
        try {
            socket= new DatagramSocket(srcPort);
        }
        catch(java.lang.Exception e) {
            e.printStackTrace();
        }

        In data = new In("sensorData");
        sensorData = data.readAllLines();
        terminal = new Terminal(name);

        try {
            socket.setSoTimeout(TIMEOUT_TIME);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public synchronized void toReceive(){
        if (!ackReceived ) {
            packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
            try {
                socket.receive(packet);
                seqNum = getSeqNum(packet);
                ackReceived = true;

                if (seqNum >= sensorData.length) {
                    transmissionDone = true;
                }
                terminal.println("Packet received from " + dstHost + ": " + getDstPort(packet) +
                        "\nPacket reads: " + getAck(packet));
            } catch (IOException e) {
                ackReceived = false;
                terminal.println("Socket timed out... Resending packet number " + seqNum);
            }
        }
    }

    public synchronized void toSend() {
        if (!transmissionDone && ackReceived) {
            ackReceived = false;
            dstAddress = new InetSocketAddress(dstHost, gatewayPort);
            DatagramPacket packet = null;
            byte[] payload = null;
            byte[] header = null;
            byte[] buffer = null;
            String s = sensorData[seqNum] + "_";
            payload = s.getBytes();
            header = assembleHeader(header, srcPort, dstPort, seqNum);
            buffer = new byte[header.length + payload.length];
            System.arraycopy(header, 0, buffer, 0, header.length);
            System.arraycopy(payload, 0, buffer, header.length, payload.length);
            packet = new DatagramPacket(buffer, buffer.length, dstAddress);
            try {
                socket.send(packet);
                terminal.println("SENT PACKET "  + seqNum);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
