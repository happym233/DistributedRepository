
/*
 *  UnsafeRunnable.java
 *
 *  This interface is part of socklib core.
 *
 *  (C) 2022 Ali Jannatpour <ali.jannatpour@concordia.ca>
 *
 *  This code is licensed under GPL.
 *
 */

package socklib;

import java.io.IOException;

public interface UnsafeRunnable {
    void run() throws IOException; // TODO arguments?
}
