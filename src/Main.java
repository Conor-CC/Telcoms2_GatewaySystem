import tcdIO.Terminal;

public class Main {

    public static void main(String[] args) {
        Terminal terminal = new Terminal("Control Window");
        terminal.println("Welcome. This window is used to initialise sensors, a gateway and a server.\n" +
                "Enter in the amount of sensors");
        int sensorCount = terminal.readInt();
        String[][]  receivedDataWindow = new String[sensorCount][8];

        Sensor[] sensors = new Sensor[sensorCount];
        int[] ports = new int[sensorCount];

        for (int i = 0; i < sensorCount; i++) {
            sensors[i] = new Sensor(("Sensor " + i), "localhost", Node.SERVER_SRC_PORT, (Node.SENSOR_START_PORT + i), Node.GATEWAYSENSOR_SRC_PORT);
            ports[i] = (Node.SENSOR_START_PORT + i);
        }
        Server server = new Server("Server One", "localhost", Node.SERVER_SRC_PORT, Node.GATEWAYSERVER_SRC_PORT, receivedDataWindow, ports);
        GateWay gateWay = new GateWay("Gateway One", ports, Node.GATEWAYSENSOR_SRC_PORT, Node.GATEWAYSERVER_SRC_PORT, "localhost", "localhost");
        server.startListener();
        gateWay.startListener();
        for (int i = 0; i < sensorCount; i++) {
            sensors[i].startListener();
        }

    }
}
