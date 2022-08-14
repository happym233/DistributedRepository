package server;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;

public class PeerConnector {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String lastResult;

    public PeerConnector(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(reader.readLine());
    }

    public int[] fetch(String query) throws IOException {
        writer.println(query);
        writer.flush();
        String line = reader.readLine();
        this.lastResult = line;
        System.out.println(line);
        String[] res = line.split(" ");
        int res_len = res.length;
        if (res[0].equals("OK")) {
            try {
                if (res_len == 1) {
                    return new int[0];
                }
                String[] res_split = res[1].split(",");
                int[] int_res = new int[res_split.length];
                for (int i = 0; i < res_split.length; i++) {
                    int_res[i] = Integer.parseInt(res_split[i]);
                }
                return int_res;
            } catch (NumberFormatException e) {
                return new int[0];
            }
        } else {
            return null;
        }
    }

    public String getLastResult() {
        return this.lastResult;
    }

    public void stop() throws IOException {
        socket.close();
    }
}
