
/*
 *  SimpleSocketProtocol.java
 *
 *  This class may be used by developer to implement a simple text-based protocol
 *  using from socket. This class has a reference to ListenerInfo so that the
 *  protocol developer may check whether the service must continue or be stopped.
 *
 *  (C) 2022 Ali Jannatpour <ali.jannatpour@concordia.ca>
 *
 *  This code is licensed under GPL.
 *
 */

package socklib;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public abstract class SimpleSocketProtocol implements SocketProtocol {
    private Socket s;
    private ListenerInfo li;
    private DataInputStream input;
    private DataOutputStream output;
    private Scanner scanner;
    private PrintWriter writer;

    public abstract void run() throws IOException;

    public Socket getSocket() { return s; }
    public ListenerInfo getListenerInfo() { return li; }
    protected DataInputStream getInputStream() {
        return input;
    }
    protected DataOutputStream getOutputStream() {
        return output;
    }
    protected Scanner getScanner() {
        return scanner;
    }
    protected PrintWriter getWriter() {
        return writer;
    }

    public SimpleSocketProtocol(Socket s, ListenerInfo info) {
        this.s = s;
        this.li = info;
        try {
            this.input = new DataInputStream(s.getInputStream());
            this.output = new DataOutputStream(s.getOutputStream());
            this.scanner = new Scanner(s.getInputStream());
            this.writer = new PrintWriter(s.getOutputStream());
        }
        catch (Exception ex) {
            throw new RuntimeException("Socket I/O Error", ex);
        }
    }

    protected void sendln(String data) {
        writer.println(data); writer.flush();
    }

    protected String recvln() {
        return getScanner().nextLine();
    }
    protected void close() throws IOException { s.close(); }
    protected boolean isClosed() { return s.isClosed(); }
    protected boolean isConnected() { return s.isConnected(); }
    protected boolean isRunning() { return li.isRunning(); }
}
