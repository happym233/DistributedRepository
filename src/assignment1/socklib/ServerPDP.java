
package socklib;

import server.PeerConnections;
import server.PeerConnector;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class ServerPDP {
	DatagramSocket receiveDSocket;
	DatagramSocket discoveryDSocket;
	private int udpPort;
	private int tcpPort;
	private String id;
	private Thread mainThread;
	private DiscoveryThreadHandler dh;
	private Thread discoveryThread;
	private PeerConnections connections;

	public void init(int tcpPort, int udpPort, String id, PeerConnections connections) {
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.id = id;
		this.connections = connections;
		this.receiveDSocket = null;
		this.discoveryDSocket = null;
	}

	public void start() {
		mainThread = new Thread(new Runnable() {
			@Override
			public void run() {
				runSdu();
				receiveDSocket.close();
			}
		});
		mainThread.start();
		dh = new DiscoveryThreadHandler();
		try {
			discoveryThread = new Thread(dh);
			discoveryThread.start();
		} catch (RuntimeException e) {
			System.out.println(e);
		}

	}

	public void stop() throws InterruptedException {
		mainThread.interrupt();
		dh.stop = true;
		Thread.sleep(1000);
		discoveryThread.interrupt();
		receiveDSocket.close();
		discoveryDSocket.close();
	}

	private void runSdu() {
		try {
			byte[] receiveBuff = new byte[1024]; //receiving buffer
			receiveDSocket = new DatagramSocket(null);
			receiveDSocket.setReuseAddress(true);
			receiveDSocket.setSoTimeout(30000);
			receiveDSocket.setBroadcast(true);
			receiveDSocket.bind(new InetSocketAddress(udpPort));
			DatagramPacket dPacket = new DatagramPacket(receiveBuff, receiveBuff.length);
			System.out.println("\nStarted UDP server, listening on Broadcast IP, port " + udpPort + "\n");
			while (true) {
				System.out.println("> Ready to receive b-cast packets...");
				try {
					receiveDSocket.receive(dPacket); //receiving data
					System.out.println("> Received packet from " + dPacket.getAddress().getHostAddress()
							+ ":" + dPacket.getPort());
					String msg = new String(dPacket.getData(), dPacket.getOffset(), dPacket.getLength());
					if (msg.equals("PEER_REQUEST")) {
						String srvResponse = "PEER_RESPONSE " + id + " " + tcpPort;
						System.out.println(srvResponse);
						byte[] sendBuff = srvResponse.getBytes();
						DatagramPacket dPacket2 = new DatagramPacket(sendBuff, sendBuff.length, dPacket.getAddress(), dPacket.getPort());
						receiveDSocket.send(dPacket2);    //Send a response
						System.out.println(getClass().getName() + "> Sent response to client IP: "
								+ dPacket.getAddress().getHostAddress() + ":" + dPacket.getPort());
					}
					Thread.sleep(2000);
				} catch (SocketTimeoutException e) {

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class DiscoveryThreadHandler implements Runnable{
		public boolean stop = false;

		public void run() {
			System.out.println("Started discovery thread");
			try {
				while(!stop) {
					//Open a random port to send the package
					discoveryDSocket = new DatagramSocket();
					discoveryDSocket.setSoTimeout(10000);
					discoveryDSocket.setReuseAddress(true);
					discoveryDSocket.setBroadcast(true);
					byte[] sendData = "PEER_REQUEST".getBytes();
					// Broadcast the message over all the network interfaces
					Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
					while (interfaces.hasMoreElements()) {
						NetworkInterface networkInterface = interfaces.nextElement();
						if (networkInterface.isLoopback() || !networkInterface.isUp()) {
							continue;
						} // Omit loopbacks
						for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
							InetAddress broadcast = interfaceAddress.getBroadcast();
							if (broadcast == null) {
								continue;
							} //Don't send if no broadcast IP.
							try {
									DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, udpPort);
									discoveryDSocket.send(sendPacket);
							} catch (Exception e) {
							}
							System.out.println("\n> Request sent to IP: "
									+ broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
						}
					}

					System.out.println("\n> Done looping through all interfaces. Now waiting for a reply!");

					byte[] recvBuf = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

					try {
						while (true) {
							discoveryDSocket.receive(receivePacket); //Wait for a response
							System.out.println("\n> Received packet from " +
									receivePacket.getAddress().getHostAddress() + " : " + receivePacket.getPort());
							String msg = new String(receivePacket.getData()).trim();
							String[] split = msg.split(" ");
							String server_ip = receivePacket.getAddress().getHostAddress();
							try {
								if (split[0].equals("PEER_RESPONSE")) {
									connections.add(split[1], server_ip, Integer.parseInt(split[2]));
								}
							} catch (Exception e) {
								System.out.println(e);
							}
						}
					} catch (SocketTimeoutException e) {

					}
					Thread.sleep(10000);
				}
			} catch (SocketException e) {
				System.out.println(e);
			} catch (IOException | InterruptedException e) {
				System.out.println(e);
			}

		}
	}
}