
/*
 *  MessageHandler.java
 *
 *  This interface is part of socklib core.
 *
 *  (C) 2022 Ali Jannatpour <ali.jannatpour@concordia.ca>
 *
 *  This code is licensed under GPL.
 *
 */

package socklib;

public interface MessageHandler {
    void onMessage(String msg);
}
