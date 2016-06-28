package com.unity.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

class Worker implements Runnable {

	private Server server;
	private Long id;
	private DataInputStream in;

	Worker(Server server, Socket client, Long id) {
		this.server = server;
		this.id = id;

		try {
			in = new DataInputStream(client.getInputStream());
		} catch (IOException e) {
			System.err.println("ERROR : Unable to read from inputstream");
			e.printStackTrace();

		}
	}

	public void run() {
		String message;

		while (true) {
			try {
				message = in.readUTF();
				System.out.println("INFO : Message from client " + id + ": " + message);
				// Send data back to client
				// out.println(response);
				server.respond(message, id);
				// Append data to text area

			} catch (Exception e) {
				System.err.println("Read failed");
				e.printStackTrace();

			}
		}
	}
}
