package com.unity.socket;

import java.io.IOException;
import java.net.Socket;

import com.unity.message.MessageDelivery;
import com.unity.message.MessageDelivery.Message;
import com.unity.message.MessageDelivery.Message.MessageType;

class Worker implements Runnable {

	private Server server;
	private Long id;
	private Socket socket;

	Worker(Server server, Socket client, Long id) {
		this.server = server;
		this.id = id;
		this.socket = client;
	}

	public void run() {
		MessageDelivery.Message requestMessage;

		while (!socket.isClosed()) {
			try {
				System.out.println("waiting for input");
				requestMessage = Message.parseDelimitedFrom(socket.getInputStream());
				this.respond(requestMessage, id);

			} catch (Exception e) {
				System.out.println("Read failed");
				e.printStackTrace();
			} finally {
				if (socket != null && !socket.isConnected()) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		if (socket.isClosed()) {
			System.out.println("Client id :" + id + " disconnected.");
		}
	}

	private void respond(Message requestMessage, Long requestorClientID) {

		if (requestMessage == null) {
			System.out.println("WARNING : Unsupported message type.");
			return;
		}

		switch (requestMessage.getMessageType()) {
		case ME:
			send(getMe(), id);
			break;
		case OTHERS:
			send(getOthers(), id);
			break;
		case RELAY:
			broadcastToOthers(requestMessage, requestorClientID);
			break;
		case SIGNOUT:
			signout();
			break;
		}
	}

	private void signout() {
		try {
			server.getClientSocketMap().remove(id);
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private Message getMe() {
		Message message = Message.newBuilder().setMessageType(MessageType.ME).setMessage(id.toString()).build();
		return message;
	}

	private void broadcastToOthers(Message requestMessage, Long requestorClientID) {

		if (requestMessage != null) {

			String[] clients = requestMessage.getTo().split(",");

			for (int i = 0; i < clients.length && i < 255; i++) {
				send(getRelay(requestMessage.getMessage()), Long.valueOf(clients[i].trim()));
			}
		}
	}

	private Message getRelay(String broadcastMessage) {
		Message.Builder message = Message.newBuilder().setMessageType(MessageType.RELAY);
		message.setMessage(broadcastMessage);
		message.setFrom(id.toString());

		return message.build();
	}

	public void send(Message responseMessage, Long requestorClientID) {

		if (responseMessage == null) {
			System.out.println("INFO : No message to be sent");
			return;
		}

		Socket clientSocket = server.getClientSocketMap().get(requestorClientID);

		try {
			responseMessage.writeDelimitedTo(clientSocket.getOutputStream());
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	private Message getOthers() {

		Message.Builder message = Message.newBuilder().setMessageType(MessageType.OTHERS);
		StringBuilder builder = null;

		for (Long clientID : server.getClientSocketMap().keySet()) {
			if (!clientID.equals(id)) {

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
		message.setMessage(builder.toString());
		return message.build();
	}
}
