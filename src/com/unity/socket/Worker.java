package com.unity.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

class Worker implements Runnable {
	private Socket client;
	private Server server;
	private Long id;
	

	// Constructor
	Worker(Server server, Socket client, Long id) {
		this.server = server;
		this.client = client;
		this.id = id;
	}

	public void run() {
		String line;
		DataInputStream in = null;
		try {
			in = new DataInputStream(client.getInputStream());
		} catch (IOException e) {
			System.out.println("in or out failed");
			System.exit(-1);
		}

		while (true) {
			try {
				line = in.readUTF();
				System.out.println("Line : " + line);
				// Send data back to client
//				out.println(response);
				server.respond(line,id);
				// Append data to text area

			} catch (IOException e) {
				System.out.println("Read failed");
				System.exit(-1);
			}
		}
	}
}
