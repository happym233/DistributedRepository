/*
 *  GreeterServerExample.java
 *
 *  A sample client example using UDP and TCP sockets to 
 *  1. discover available server via UDP broadcast
 *  2. bind to the discovered TCP server 
 *  
 *  (C) 2022 Alexander Aric <alexander.aric@concordia.ca>
 *
 *
 */

package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Scanner;
import java.net.*;
import java.io.*;
import java.util.*;

public class ClientPDP {
	public static void main(String args[]) throws IOException {
        DatagramSocket dSocket;
        try {
		//Open a random port to send the package
		dSocket = new DatagramSocket();
		dSocket.setBroadcast(true);
		byte[] sendData = "PEER_REQUEST".getBytes();
		// Broadcast the message over all the network interfaces
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			if (networkInterface.isLoopback() || !networkInterface.isUp()) { continue; } // Omit loopbacks
			for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
				InetAddress broadcast = interfaceAddress.getBroadcast();
				if (broadcast == null) { continue; } //Don't send if no broadcast IP.
				try {
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8000);
					dSocket.send(sendPacket); // Send the broadcast package!
				} 
				catch (Exception e) {
				}
                    		System.out.println("\n> Request sent to IP: "
                            		+ broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
			}
		}

		System.out.println("\n> Done looping through all interfaces. Now waiting for a reply!");
            
		byte[] recvBuf = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
		dSocket.receive(receivePacket); //Wait for a response

		//We have a response
		System.out.println("\n> Received packet from " + receivePacket.getAddress().getHostAddress() + " : " + receivePacket.getPort());
 		String server_ip = receivePacket.getAddress().getHostAddress();
		String msg = new String(receivePacket.getData()).trim();
		String[] splited = msg.split(" ");
		if (splited[0].equals("PEER_RESPONSE")) {
				System.out.println("\n> Ready to connect! ");
				//TODO implement TCP binding
				System.out.println("\n> Connecting to ip: " + server_ip + "server id: " +splited[1] +  " tcp port: " + splited[2]);
				PrintWriter writer = null;
				BufferedReader reader = null;
				try {
					Socket socket = new Socket(receivePacket.getAddress().getHostAddress(), Integer.parseInt(splited[2]));
					writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					Scanner in = new Scanner(System.in);
					String message = "";
					String line = "";
					line = reader.readLine();
					System.out.println("\n>> " + line);
					while (socket.isConnected()) {
						System.out.print("message: > ");
						message = in.nextLine();
						writer.println(message);
						writer.flush();
						line = reader.readLine();
						System.out.println(">> " + line);
						if (line.startsWith("BYE")) {
							break;
						}
					}
					writer.close();
					reader.close();
				}catch(SocketException e) {
					e.printStackTrace();
					System.out.println("Server have close connect");
					writer.close();
					reader.close();
				}
		}

            dSocket.close();  //Close the port!
        } catch (
                IOException ex) {
            System.out.println("Hey, there is an error!!!");
        }
    }
}
