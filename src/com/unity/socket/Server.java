package com.unity.socket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unity.message.MessageType;

public class Server implements Runnable {

	private AtomicLong clientId = new AtomicLong(0);
	private Map<Long, Socket> clientSocketMap = new ConcurrentHashMap<Long, Socket>();
	private Map<Socket, DataOutputStream> socketOutputStreamMap = new ConcurrentHashMap<Socket, DataOutputStream>();
	private ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) throws IOException {

		new Thread(new Server()).start();

	}

	public void run() {
		ServerSocket server = null;
		try {
			server = new ServerSocket(4444);
		} catch (IOException e) {
			System.out.println("Could not listen on port 4444");
			System.exit(-1);
		}
		while (true) {
			Worker w;
			try {

				long id = clientId.incrementAndGet();

				// server.accept returns a client connection
				Socket socket = server.accept();
				System.out.println("Received connection from " + socket);
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());

				socketOutputStreamMap.put(socket, output);
				clientSocketMap.put(id, socket);

				w = new Worker(this, socket, id);

				Thread t = new Thread(w);
				t.start();
			} catch (IOException e) {
				System.out.println("Accept failed: 4444");
				System.exit(-1);
			}
		}
	}

	void broadcastToOthers(String message, Long requestorClientID) {

		BroadCastMessage msg = parseMessage(message);
		if (msg != null) {
			String messagePart = msg.getMessage();
			String[] clients = msg.getClientIDs().split(",");

			System.out.println("MessagePart :" + messagePart);

			for (String clientId : clients) {

				System.out.println("In broadcast" + Long.valueOf(clientId));
				send(messagePart, Long.valueOf(clientId));
			}
		}
		System.out.println("Message is empty");
	}

	public void send(String line, Long requestorClientID) {
		synchronized (socketOutputStreamMap) {

			Socket clientSocket = clientSocketMap.get(requestorClientID);

			DataOutputStream outputStream = (DataOutputStream) socketOutputStreamMap.get(clientSocket);
			try {
				outputStream.writeUTF(line);
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	public void respond(String line, Long requestorClientID) {

		if (MessageType.WHO_AM_I.name().equals(line)) {
			send(requestorClientID.toString(), requestorClientID);
		} else if (MessageType.WHO_IS_HERE.name().equals(line)) {

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
			send(builder.toString(), requestorClientID);
		} else {
			broadcastToOthers(line, requestorClientID);
		}
	}

	public BroadCastMessage parseMessage(String message) {

		try {
			BroadCastMessage msg = mapper.readValue(message, BroadCastMessage.class);
			return msg;
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
}