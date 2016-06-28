package com.unity.socket;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

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

			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		new Thread(this).start();
	}

	public static void main(String[] args) throws IOException {

		String hostName = "localhost";
		int portNumber = Integer.parseInt("4444");
		Client client = new Client(hostName, portNumber);
		client.getInput();

	}

	public void getInput() {
		try {

			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

			String userInput;
			while ((userInput = stdIn.readLine()) != null) {
				out.writeUTF(userInput);
//				System.out.println("echo: " + in.readUTF());
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			System.exit(1);
		}
	}

	public void run() {
		while (true) {
			String message;
			try {
				message = in.readUTF();
				System.out.println(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}