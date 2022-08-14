
/*
 *  SimpleNotificationHandler.java
 *
 *  This class reports errors and notificaiton messages to a given output
 *  stream, i.e. stdout or stderr.
 *
 *  (C) 2022 Ali Jannatpour <ali.jannatpour@concordia.ca>
 *
 *  This code is licensed under GPL.
 *
 */

package socklib;

import java.io.OutputStream;
import java.io.PrintWriter;

public class SimpleNotificationHandler implements NotificationHandler {
    PrintWriter out;
    PrintWriter err;

    public SimpleNotificationHandler() {
        this(System.out);
    }

    public SimpleNotificationHandler(OutputStream out) {
        this(out, out);
    }

    public SimpleNotificationHandler(OutputStream out, OutputStream err) {
        this.out = out == null? null: new PrintWriter(out);
        this.err = new PrintWriter(err == null? out: err);
    }

    @Override
    public void onError(Exception e) {
        if(err != null) {
            err.println(e.getMessage());
            err.flush();
        }
    }

    @Override
    public void onMessage(String msg) {
        if(out != null) {
            out.println(msg);
            out.flush();
        }
    }
}
