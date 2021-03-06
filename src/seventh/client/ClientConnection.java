/*
 * see license.txt 
 */
package seventh.client;

import harenet.api.Client;
import harenet.api.impl.HareNetClient;

import java.io.IOException;
import java.net.InetSocketAddress;

import seventh.client.ClientProtocol.GameCreationListener;
import seventh.shared.Command;
import seventh.shared.Console;
import seventh.shared.TimeStep;

/**
 * Handles the client connection to the Seventh server.
 * 
 * @author Tony
 *
 */
public class ClientConnection {

	private Client client;
	private ClientNetworkProtocol protocol;
	
	
	/**
	 * @param config
	 * @param console
	 */
	public ClientConnection(SeventhGame app, ClientSeventhConfig config, Console console) {
		this.client = new HareNetClient(config.getNetConfig());
		
		this.protocol = new ClientNetworkProtocol(this, app);
		this.client.addConnectionListener(this.protocol);
//      this.client.addConnectionListener(new LagConnectionListener(50, 100, listener));
		
		console.addCommand(new Command("netstat") {
			
			@Override
			public void execute(Console console, String... args) {
				if(client.isConnected()) {
					console.println("");
					console.println("Bandwidth Usage:");
					console.println("\tTotal Incoming Bytes: " + (client.getNumberOfBytesReceived() / 1024) + "KiB");
					console.println("\tTotal Outgoing Bytes: " + (client.getNumberOfBytesSent() / 1024) + "KiB");
					console.println("\tIncoming bit/s: " + client.getAvgBitsPerSecRecv());
					console.println("\tOutgoing bit/s: " + client.getAvgBitsPerSecSent());
					console.println("");
				}
				else {
					console.println("Not connected.");
				}
				
			}
		});
	}
	
	/**
	 * Pump network updates
	 * 
	 * @param timeStep
	 */
	public void updateNetwork(TimeStep timeStep) {
	    protocol.updateNetwork(timeStep);
		protocol.postQueuedMessages();
	}

	/**
	 * Disconnect from the server
	 */
	public void disconnect() {
	    protocol.close();
	}
	
	/**
	 * @return true if connected to a server
	 */
	public boolean isConnected() {
		return client.isConnected();
	}
	
	/**
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}
	
	/**
	 * @return the {@link ClientProtocol}
	 */
	public ClientProtocol getClientProtocol() {
	    return protocol;
	}
	
	/**
	 * Connects to a Seventh server
	 * 
	 * @param host
	 * @param port
	 * @throws IOException
	 */
	public void connect(String host, int port) throws IOException {
		
		disconnect();
				
		InetSocketAddress address = new InetSocketAddress(host, port);
		if(client.connect(12000, address)) {
			client.start();
		}
		else {
			throw new IOException("Unable to connect to: " + host + ":" + port);
		}
	}
	
	/**
	 * Sets the {@link GameCreationListener}
	 * 
	 * @param listener
	 */
	public void setGameCreationListener(GameCreationListener listener) {
	    this.protocol.setGameCreationListener(listener);
	}
	
	
}
