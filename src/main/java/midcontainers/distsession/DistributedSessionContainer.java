/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package midcontainers.distsession;

import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

import midcontainers.Binding;
import midcontainers.Container;
import midcontainers.ContainerException;
import midcontainers.Named;
import midcontainers.local.LocalContainer;
import static midcontainers.Binding.Policy.NEW;
import static midcontainers.Binding.Policy.SINGLETON;

/**
 *
 * @author alexis
 */

public class DistributedSessionContainer extends LocalContainer{
    
    private final Session mySession;
    private final MulticastSocket socket;
    private final InetAddress group;     
    private final String groupAddress;   
    private final int port;
    private static final int BUFFER_SIZE = 8192;
    private final byte[] incomingBuffer = new byte[BUFFER_SIZE];
    private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(BUFFER_SIZE);
    enum SessionCommand {
    SYNC, SET, DELETE
    }
    
    public class ListenThread extends Thread {

    public void run() {
        DatagramPacket inPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);
        Serializable[] command = decode(inPacket.getData(), 3);

        switch ((SessionCommand) command[0]) {
            case SET:
                (...)
                break;

            case DELETE:
                (...)
                break;

            case SYNC:
                (...)
                break;
        }
        }
    }


    private byte[] encode(Serializable... objects) {                                          
        outputBuffer.reset();                                                                 
        try {                                                                                 
            ObjectOutputStream out = new ObjectOutputStream(outputBuffer);                    
            for (Serializable object : objects) {                                             
                out.writeObject(object);                                                      
            }                                                                                 
            out.close();                                                                      
            return outputBuffer.toByteArray();                                                
        } catch (IOException e) {                                                             
            throw new ContainerException(e);                                                  
        }                                                                                     
    }

    private void send(SessionCommand command, String key, Serializable value) {      
        byte[] bytes = encode(command, key, value);                                  
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, port);
        try {                                                                        
            socket.send(packet);                                                     
        } catch (IOException e) {                                                    
            throw new ContainerException(e);                                         
        }                                                                            
    }

    private Serializable[] decode(byte[] buffer, int count) {                              
        try {                                                                              
            Serializable[] decoded = new Serializable[count];                              
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
            for (int i = 0; i < count; i++) {                                              
                decoded[i] = (Serializable) in.readObject();                               
            }                                                                              
            in.close();                                                                    
            return decoded;                                                                
        } catch (IOException e) {                                                          
            throw new ContainerException(e);                                               
        } catch (ClassNotFoundException e) {                                               
            throw new ContainerException(e);                                               
        }                                                                                  
    }    

    public void start() {                    
    try {                                
     socket.joinGroup(group);         
        } catch (IOException e) {            
     throw new ContainerException(e); 
        }                                    
    }      

    public void stop() {                                 
        try {                               
        socket.leaveGroup(group);       
        } catch (IOException e) {           
            throw new ContainerException(e);
        }
    }
        
    public DistributedSessionContainer(String groupAddress, int port) {
        try {       
            this.mySession = new SessionContainer();
            this.groupAddress = groupAddress;                          
            this.port = port;                                          
            this.group = InetAddress.getByName(groupAddress);          
            socket = new MulticastSocket(port);                        
            socket.setSoTimeout(10000);       
            this.declare(new Binding(Session.class, SessionContainer.class, null, SINGLETON));
            new ListenThread().start();
            
            
        } catch (UnknownHostException e) {                             
            throw new ContainerException(e);                           
        } catch (IOException e) {                                      
            throw new ContainerException(e);                           
        }                                                              
    }
    
    
    public class SessionContainer implements Session {
        
        private final Map<String, Serializable> session = new HashMap<String, Serializable>();
        
        public SessionContainer() {
        }
        
      /**
     * Fetch a session value.
     *
     * @param key key of the value
     * @return the value, or <code>null</code> if none was found
     */
    
        public Serializable get(String key) {
        
        return this.session.get(key);
       
        }

    /**
     * Remove a value
     *
     * @param key the key of the value to be removed
     */
        public void delete(String key) {
            this.session.remove(key);
        }

    /**
     * Define or update a session value
     *
     * @param key   the value key
     * @param value the value
     */
        public void set(String key, Serializable value) {
            this.session.put(key, value);
            }
        }
        
}
