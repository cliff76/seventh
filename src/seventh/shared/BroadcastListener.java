/*
 * see license.txt
 */
package seventh.shared;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A very simple multicast server socket, that listens for messages broadcast to a particular group address and port.
 * 
 * @author Tony
 *
 */
public class BroadcastListener implements AutoCloseable {
    
    public static void main(String [] args) throws Exception {
        try(BroadcastListener listener = new BroadcastListener(1500, "224.0.0.44", 4888)) {
            listener.addOnMessageReceivedListener(new OnMessageReceivedListener() {
                
                @Override
                public void onMessage(DatagramPacket packet) {
                    String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    System.out.println(packet.getAddress() +" says: " + message);
                }
            });
            
            listener.start();
        }
    }
    
    /**
     * Message was received from a broadcast message
     * 
     * @author Tony
     *
     */
    public static interface OnMessageReceivedListener {
        public void onMessage(DatagramPacket packet);
    }
    
    private MulticastSocket socket;
    private InetAddress groupAddress;
    private AtomicBoolean active;
    private final int MTU;
    
    private List<OnMessageReceivedListener> listeners;


    
    /**
     * @param mtu
     * @param groupAddress
     * @param port
     * @throws Exception
     */
    public BroadcastListener(int mtu, String groupAddress, int port) throws Exception {
        this.MTU = mtu;
        this.socket = new MulticastSocket(port);
        this.groupAddress = InetAddress.getByName(groupAddress);
        this.socket.joinGroup(this.groupAddress);
        
        this.listeners = new ArrayList<BroadcastListener.OnMessageReceivedListener>();
        this.active = new AtomicBoolean();
    }

    /**
     * Adds a {@link OnMessageReceivedListener}
     * 
     * @param l
     */
    public void addOnMessageReceivedListener(OnMessageReceivedListener l) {
        this.listeners.add(l);
    }
    
    /**
     * Starts listening on a port for broadcast messages.
     * 
     * @param groupAddress
     * @param port
     */
    public void start() throws Exception {
        if(!this.active.get()) {
            this.active.set(true);
            
            byte[] buffer = new byte[this.MTU];
            while(this.active.get()) {
                
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);               
               	this.socket.receive(packet);               
                
                for(OnMessageReceivedListener l : this.listeners) {
                    l.onMessage(packet);
                }
            }
        }
    }
    
    @Override
    public void close() throws Exception {
        this.active.set(false);
        
        if(this.socket != null) {
            this.socket.leaveGroup(this.groupAddress);
            this.socket.close();
        }        
    }
}
