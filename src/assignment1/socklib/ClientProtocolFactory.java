
/*
 *  ClientProtocolFactory.java
 *
 *  This class is created for compatibility with ProtocolFactory implementation.
 *  There is no need to use this class on the client side.
 *  See also: ProtocolFactory.java
 *
 *  (C) 2022 Ali Jannatpour <ali.jannatpour@concordia.ca>
 *
 *  This code is licensed under GPL.
 *
 */

package socklib;

import java.io.IOException;
import java.net.Socket;

public class ClientProtocolFactory {
    public static SocketProtocol connectTo(String server, int port, ProtocolFactory pf) throws IOException {
        Socket s = new Socket(server, port);
        return pf.create(s, () -> !s.isClosed()); // no server, therefore check  for socket only
    }
}
