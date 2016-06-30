package com.unity.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Server implements Runnable {

	private AtomicLong clientId = new AtomicLong(0);
	private Map<Long, Socket> clientSocketMap = new ConcurrentHashMap<Long, Socket>();
	private int port;


	public Server(int portNumber) {
		this.port = Integer.valueOf(portNumber);
	}

	public static void main(String[] args) throws IOException {

		String portNumber;

		if (args.length == 0) {
			portNumber = "9090";// default port
		} else {
			portNumber = args[0];
		}

		new Thread(new Server(Integer.valueOf(portNumber))).start();

	}

	public void run() {
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port);
			e.printStackTrace();
			System.exit(-1);
		}
		while (true) {
			Worker worker;
			try {
				System.out.println("Waiting to accept connections ...");

				// server.accept returns a client connection
				Socket socket = server.accept();

				long id = clientId.incrementAndGet();


				System.out.println("Received connection from " + socket);
				clientSocketMap.put(id, socket);

				worker = new Worker(this, socket, id);

				Thread t = new Thread(worker);
				t.start();
			} catch (IOException e) {
				System.err.println("Accept failed: " + port);
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public Map<Long, Socket> getClientSocketMap(){
		return clientSocketMap;
	}
}