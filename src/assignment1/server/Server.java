package server;

import socklib.ServerListener;
import socklib.ServerPDP;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
    private int tcpPort;
    private static int udpPort=8000;
    private String id;
    private PeerConnections connections;
    private Directory directory;
    private ServerPDP spdp;
    private ServerListener listener;

    public Server(String id, Integer tcpPort) {
        this.tcpPort = tcpPort;
        this.id = id;
        connections = new PeerConnections(id);
        directory = new Directory();
        spdp = null;
        listener = null;
    }

    public void runPDP() throws InterruptedException {
        spdp = new ServerPDP();
        spdp.init(tcpPort, udpPort, id, connections);
        spdp.start();
    }

    public void runListener() {
        listener = new ServerListener("RepoServer", id, connections, directory, tcpPort, ERAP::new);
        listener.start();
    }


    public void run() throws InterruptedException {
        runPDP();
        runListener();
        System.out.println("\nStarted TCP server port (" + tcpPort + ")\nYou may open multiple simultaneous connections.");
        System.out.println("\nPress hit ENTER if you wish to stop the server. Note that the service may NOT stop immediately.");
        new Scanner(System.in).nextLine();
        stop();
    }

    public void stop() throws InterruptedException {
        spdp.stop();
        listener.stop();
        connections.close();
    }


    public static void main(String[] args) {
        String id = "";
        Integer tcpPort = -1;
        if (args.length != 2) {
            throw new IllegalArgumentException("Illegal input parameters: <repo id><TCP port>");
        } else {
            id = args[0];
            tcpPort = Integer.parseInt(args[1]);
        }
        try {
            Server server = new Server(id, tcpPort);
            server.run();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("FATAL ERROR: " + ex.getMessage());
        }
    }

}
