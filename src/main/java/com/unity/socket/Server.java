package com.unity.socket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unity.message.RequestMessage;

public class Server implements Runnable {

	private AtomicLong clientId = new AtomicLong(0);
	private Map<Long, Socket> clientSocketMap = new ConcurrentHashMap<Long, Socket>();
	private Map<Socket, DataOutputStream> socketOutputStreamMap = new ConcurrentHashMap<Socket, DataOutputStream>();
	private int port;

	private ObjectMapper mapper = new ObjectMapper();

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
			System.exit(-1);
		}
		while (true) {
			Worker worker;
			try {

				long id = clientId.incrementAndGet();

				// server.accept returns a client connection
				Socket socket = server.accept();
				System.out.println("Received connection from " + socket);
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());

				socketOutputStreamMap.put(socket, output);
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

	void broadcastToOthers(RequestMessage msg, Long requestorClientID) {

		if (msg != null) {
			String messagePart = msg.getMessage();

			if (messagePart != null && !"".equals(messagePart.trim())) {
				messagePart = "Message From " + requestorClientID + " : " + messagePart;
			}

			String[] clients = msg.getTo().split(",");

			for (String clientId : clients) {
				send(messagePart, Long.valueOf(clientId));
			}
		}
	}

	public void send(String message, Long requestorClientID) {

		if (message == null || "".equals(message.trim())) {
			System.err.println("INFO : No message to be sent");
			return;
		}

		Socket clientSocket = clientSocketMap.get(requestorClientID);

		DataOutputStream outputStream = (DataOutputStream) socketOutputStreamMap.get(clientSocket);
		try {

			outputStream.writeUTF(message);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public void respond(String message, Long requestorClientID) {

		RequestMessage msg = parseMessage(message);
		if (msg == null) {
			System.out.println("WARNING : Unsupported message type.");
			return;
		}

		switch (msg.getType()) {
		case ME:
			send(requestorClientID.toString(), requestorClientID);
			break;
		case OTHERS:
			send(getOthers(requestorClientID), requestorClientID);
			break;
		case RELAY:
			broadcastToOthers(msg, requestorClientID);
			break;
		}
	}

	private String getOthers(Long requestorClientID) {
		StringBuilder builder = null;

		for (Long clientID : clientSocketMap.keySet()) {
			if (!clientID.equals(requestorClientID)) {

				if (builder == null) {
					builder = new StringBuilder(clientID.toString());
				}

				else {
					builder.append(",").append(clientID);
				}
			}
		}
		if (builder == null) {
			builder = new StringBuilder("No one else");
		}
		return builder.toString();
	}

	public RequestMessage parseMessage(String message) {

		try {
			RequestMessage msg = mapper.readValue(message, RequestMessage.class);
			return msg;
		} catch (Exception e) {
			System.err.println("Parse Exception for message : " + message);
		}
		return null;

	}
}