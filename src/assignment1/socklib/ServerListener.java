package socklib;

import server.Directory;
import server.ERAP;
import server.PeerConnections;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class ServerListener implements ListenerInfo {
    String appname;
    PeerConnections peerConnections;
    String id;
    Directory directory;
    ServerSocket sc;
    MainThreadHandler mainproc;
    SocketList socklist;
    // TODO thread list
    Thread mainThread;
    List<Thread> threads;
    ERAPFactory prot;
    NotificationHandler nhandler;
    boolean dontThrowError;

    public ServerListener(String name, String id, PeerConnections connections, Directory directory, int port, ERAPFactory pf) {
        this(name, id, connections, directory, port, pf, null);
    }

    public ServerListener(String name, String id, PeerConnections connections, Directory directory, int port, ERAPFactory pf, NotificationHandler handler) {
        this(name, id, connections, directory, port, pf, handler, true);
    }
    public ServerListener(String name, String id, PeerConnections connections, Directory directory, int port, ERAPFactory pf, NotificationHandler handler, boolean dontThrowError) {
        try {
            this.appname = name;
            this.id = id;
            this.peerConnections = connections;
            this.directory = directory;
            this.sc = new ServerSocket(port);
            this.prot = pf;
            this.nhandler = handler == null? new SimpleNotificationHandler() : handler;
            this.dontThrowError = dontThrowError;
            this.threads = new ArrayList<>();
            sc.setSoTimeout(30000);
        }
        catch(Exception ex)
        {
            safeThrow(new RuntimeException("Server Error: Cannot create a listener", ex));
            return;
        }
    }

    public void start() {
        log(String.format("Starting server %s...", quote(appname)));
        if(sc == null) {
            safeThrow(new RuntimeException("Server Error: No listener"));
            return;
        }
        if(mainproc != null) {
            safeThrow(new RuntimeException("Server Error: Service already running"));
            return;
        }
        if(socklist != null) {
            // shouldn't happen
            socklist.close();
            socklist = null;
        }
        mainproc = new MainThreadHandler();
        socklist = new SocketList();
        try {
            mainThread = new Thread(mainproc);
            mainThread.start();
        }
        catch(RuntimeException rx) {
            safeThrow(rx);
            return;
        }
        log(String.format("Server %s started.", quote(appname)));
    }

    public void stop() throws InterruptedException {
        if(mainproc == null) {
            safeThrow(new RuntimeException("Server Error: Service not running"));
            return;
        }
        mainproc.stop = true; // deferred stop
        socklist.close();
        // TODO kill main thread
        mainThread.interrupt();
        // TODO schedule  children kill, after certain timeout
        Thread.sleep(2000);
        for (Thread thread: threads) {
            thread.interrupt();
        }
        socklist = null;
        log(String.format("Stopping server %s...", quote(appname)));
    }

    @Override
    public boolean isRunning() {
        return !mainproc.stop;
    }

    private class MainThreadHandler implements Runnable {
        public boolean stop = false;
        public void run() {
            try {
                while(!stop) {
                    try {
                        Socket s = sc.accept(); // for non blocking see: java.nio.SocketChannel
                        if(stop) {
                            s.close();
                            return;
                        }
                        try {
                            socklist.add(s);
                            Thread thread = new Thread(new SafeRunnable(prot.create(s, directory, peerConnections, id, ServerListener.this)));
                            threads.add(thread);
                            thread.start();
                            //TODO add thread to the threads list
                        }
                        catch(Exception ex) {
                            reportError(ex);
                        }
                    }
                    catch (SocketTimeoutException e) {

                    }
                    catch(Exception ex) {
                        reportError(ex);
                    }
                }

            }
            finally {
                log(String.format("Server %s stopped.", quote(appname)));
            }
        }
    }

    private class SafeRunnable implements Runnable {
        private SocketProtocol o;
        private SafeRunnable(SocketProtocol o) {
            this.o = o;
        }
        public void run() {
            try {
                o.run();
            }
            catch(Exception ex) {
                reportError(ex);
            }
            finally {
                try {
                    if (!o.getSocket().isClosed())
                        o.getSocket().close();
                }
                catch(Exception foo) {}
            }
        }
    }

    private class SocketList
    {
        private List<Socket> list = new ArrayList<Socket>();

        private synchronized void add(Socket s) {
            list.add(s);
        }

        private void close() {
            while(true) {
                Socket s;
                synchronized (list) {
                    if (list.stream().count() <= 0)
                        return;
                    s = list.remove(0);
                }
                try {
                    s.close();
                }
                catch (Exception ex) {
                    reportError(ex);
                }
            }
        }
    }

    private void reportError(Exception ex) {
        try {
            nhandler.onError(ex);
        }
        catch(Exception foo) {
            String s = foo.getMessage();
            System.out.println(s);
        }
    }

    private void log(String msg) {
        try {
            nhandler.onMessage(msg);
        }
        catch(Exception foo) {
            String s = foo.getMessage();
            System.out.println(s);
        }
    }

    private void safeThrow(RuntimeException rx) {
        if(dontThrowError)
            reportError(rx);
        else
            throw rx;
    }

    private static String quote(String s) {
        return String.format("\"%s\"", (s + "").replace("\"", "\"\""));
    }
}
