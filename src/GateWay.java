import tcdIO.Terminal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;


public class GateWay extends Node {

    DatagramSocket socketOne;
    DatagramSocket socketTwo;
    private String gateWayAddress;
    private String serverAddress;
    private DatagramPacket packet;
    int[] sensorPorts;
    boolean serverComms;
    boolean sensorComms;
    private Terminal terminal;
    GateWay(String name, int[] sensorPorts, int srcSensPort, int srcDstPort, String gateWayAddress, String serverAddress) {
        this.name = name;
        this.gateWayAddress = gateWayAddress;
        this.serverAddress = serverAddress;
        this.sensorPorts = sensorPorts;
        listener = new Listener();
        try {
            socketOne = new DatagramSocket(srcSensPort);
            socketTwo = new DatagramSocket(srcDstPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        terminal = new Terminal(name);
        serverComms = false;
        sensorComms = true;
    }

    public synchronized void toReceive() {
        packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        if (sensorComms) {
            try {
                socketOne.receive(packet);
                sensorComms = false;
                serverComms = true;
                terminal.println("Socket One: Packet received from " + packet.getAddress() + ": " + getSrcPort(packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (serverComms) {
            try {
                socketTwo.receive(packet);
                serverComms = false;
                sensorComms = true;
                terminal.println("Socket two: Packet received from " + packet.getAddress() + ": " + getSrcPort(packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static boolean listContainsPort(int portNum, int[] sensorPorts) {
        for (int i = 0; i < sensorPorts.length; i++) {
            if (portNum == sensorPorts[i]) {
                return true;
            }
        }
        return false;
    }

    public synchronized void toSend(){
        int portNum = packet.getPort();
        if (listContainsPort(portNum, sensorPorts)) { //For Sending packets on to sensors
            dstAddress = new InetSocketAddress(serverAddress, getDstPort(packet));
        }
        else {                                        //For Sending packets onto the server
            dstAddress = new InetSocketAddress(gateWayAddress, getSrcPort(packet));
        }
        packet.setSocketAddress(dstAddress);

        if (serverComms) {
            try {
                socketTwo.send(packet);
                terminal.println("Packet sent to " + packet.getAddress() + ": " + getSrcPort(packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (sensorComms) {
            try {
                socketOne.send(packet);
                terminal.println("Packet sent to " + packet.getAddress() + ": " + getSrcPort(packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
