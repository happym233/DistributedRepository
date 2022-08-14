package socklib;

import server.Directory;
import server.PeerConnections;

import java.net.Socket;
import java.util.Map;

public interface ERAPFactory {
    SocketProtocol create(Socket s, Directory directory, PeerConnections connections, String id, ListenerInfo li);
}
