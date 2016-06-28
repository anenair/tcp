package com.unity.socket;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client implements Runnable {

	private DataInputStream in;
	private DataOutputStream out;
	private String hostName;
	private int portNumber;
	private Socket socket;

	public Client(String hostName, int portNumber) {
		this.hostName = hostName;
		this.portNumber = portNumber;
		initialize();
	}

	private void initialize() {
		try {
			socket = new Socket(hostName, portNumber);
			System.out.println("Connection accepted by server " + socket);

			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
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
		client.getInput();

	}

	public void getInput() throws IOException {
		try {

			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

			String userInput;
			while ((userInput = stdIn.readLine()) != null) {
				out.writeUTF(userInput);
			}
		} catch (IOException ioe) {
			System.err.println("unable to read from standard input ");
			ioe.printStackTrace();
			throw ioe;
		}
	}

	public void run() {
		while (true) {
			String message;
			try {
				message = in.readUTF();
				printMessageToTerminal(message);
			} catch (IOException e) {
				System.err.println("unable to read from standard input ");
				e.printStackTrace();
			}
		}
	}

	private void printMessageToTerminal(String message) {
		if (message == null || "".equals(message.trim())) {
			System.out.println("INFO :: _INVALID_MESSAGE_");
			return;
		}
		System.out.println(message);
	}
}