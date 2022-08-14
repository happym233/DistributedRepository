package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerConnections {
    private Map<String, PeerConnector> connections;
    private List<Socket> socketList;
    private String id;

    public PeerConnections(String id) {
        this.id = id;
        connections = new HashMap<>();
        socketList = new ArrayList<>();
    }

    public synchronized void add(String id, String address, Integer port) throws IOException {
        System.out.println("Trying to create peer connector to " + id+ " from address: " + address + " port: " + port);
        if (this.id == id || connections.get(id) != null) return;
        Socket socket = new Socket(address, port);
        PeerConnector peerConnector = new PeerConnector(socket);
        socketList.add(socket);
        connections.put(id, peerConnector);
        System.out.println("Created peer connector to " + id+ " from address: " + address + " port: " + port);
    }

    public Map<String, PeerConnector> getConnections() {
        return connections;
    }
    public void close() {
        while(true) {
            Socket s;
            synchronized (socketList) {
                if (socketList.stream().count() <= 0)
                    return;
                s = socketList.remove(0);
            }
            try {
                s.close();
            }
            catch (Exception ex) {
                System.out.println(ex.toString());
            }
        }
    }

}
