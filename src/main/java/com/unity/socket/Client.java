package com.unity.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unity.message.MessageDelivery;
import com.unity.message.MessageDelivery.Message;
import com.unity.message.MessageDelivery.Message.MessageType;
import com.unity.message.RequestMessage;

public class Client implements Runnable {

	private String hostName;
	private int portNumber;
	private Socket socket;
	private ObjectMapper mapper;

	public Client(String hostName, int portNumber) {
		this.hostName = hostName;
		this.portNumber = portNumber;
	}

	private void initialize() {
		try {
			mapper = new ObjectMapper();
			socket = new Socket(hostName, portNumber);
			System.out.println("Connection accepted by server " + socket);

			new Thread(this).start();

		} catch (Exception e) {
			System.err.println("Initialization Exception on port : " + portNumber);
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public static void main(String[] args) throws IOException {

		String portNumber;

		if (args.length == 0) {
			portNumber = "9090";// default port
		} else {
			portNumber = args[0];
		}

		String hostName = "localhost";

		Client client = new Client(hostName, Integer.valueOf(portNumber));
		client.initialize();

		client.getInput();

	}

	public void getInput() throws IOException {
		try {

			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

			String userInput;
			while ((userInput = stdIn.readLine()) != null && !socket.isClosed()) {
				if (userInput != null && !userInput.trim().isEmpty()) {
					Message messageProto = getUserInput(userInput);
					if (messageProto != null && messageProto.getMessageBytes().size() < (1024 * 1000)) {
						messageProto.writeDelimitedTo(socket.getOutputStream());
						socket.getOutputStream().flush();
					}
				}
			}
		} catch (IOException ioe) {
			System.err.println("unable to read from standard input ");
			ioe.printStackTrace();
			throw ioe;
		}
	}

	private Message getUserInput(String userInput) {

		RequestMessage requestMessage;
		try {
			requestMessage = mapper.readValue(userInput, RequestMessage.class);
			Message.Builder message = Message.newBuilder();
			if (requestMessage.getMessage() != null) {
				message.setMessage(requestMessage.getMessage());
			}

			message.setMessageType(MessageType.valueOf(requestMessage.getType().ordinal()));

			if (requestMessage.getTo() != null) {
				message.setTo(requestMessage.getTo());
			}
			return message.build();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void run() {
		try {
			while (!socket.isClosed()) {
				MessageDelivery.Message message;
				try {
					message = Message.parseDelimitedFrom(socket.getInputStream());

					if (message != null) {
						printMessageToTerminal(message);
					}
				} catch (IOException e) {
					System.err.println("unable to read from standard input ");
					e.printStackTrace();
				}
			}
		} finally {
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (socket.isClosed()) {
			System.out.println("Connection terminated from Server");
		}
	}

	private void printMessageToTerminal(Message message) {
		if (message == null) {
			System.out.println("INFO :: _INVALID_MESSAGE_");
			return;
		}
		System.out.println(message);
	}
}