/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package midcontainers.distsession;

import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
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

public class DistributedSessionContainer extends LocalContainer {
    public ListenThread thread ;
    private final SessionContainer mySession;
    private final MulticastSocket socket;
    private final InetAddress group;     
    private final String groupAddress;   
    private final int port;
    private static final int BUFFER_SIZE = 8192;
    private final byte[] incomingBuffer = new byte[BUFFER_SIZE];
    private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(BUFFER_SIZE);
    public static enum SessionCommand {
    SYNC, SET, DELETE
    }
    
    public class ListenThread extends Thread {

        
    public void run() {
        
        while(true) {
            try {
            System.out.println("1");
            DatagramPacket inPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);
            socket.receive(inPacket);
 
            Serializable[] command = decode(inPacket.getData(), 3);

            switch ((SessionCommand) command[0]) {
                case SET:
                    System.out.println("SET");
                    mySession.set((String)command[1],command[2]);
                    
                    break;

                case DELETE:
                    mySession.delete((String)command[1]);
                    break;

                case SYNC:
                   System.out.println("SYNC"); 
                   for (String key : new HashSet<String>(mySession.session.keySet())) {
                        System.out.println("2");
                        DistributedSessionContainer.this.send(SessionCommand.SET, key, mySession.get(key));
                    }
                    break;
            }
            
            } catch (IOException e) {
            }
            
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

    public void send(SessionCommand command, String key, Serializable value) {      
        byte[] bytes = encode(command, key, value);                                  
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, this.group, this.port);
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
     this.thread.start();
     send(SessionCommand.SYNC,null,null);
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
            this.thread = new ListenThread();
            this.groupAddress = groupAddress;                          
            this.port = port;                                          
            this.group = InetAddress.getByName(groupAddress);          
            socket = new MulticastSocket(port);                        
            socket.setSoTimeout(10000);       
            this.declare(new Binding(Session.class, SessionContainer.class, null, SINGLETON));
            
           
        } catch (UnknownHostException e) {                             
            throw new ContainerException(e);                           
        } catch (IOException e) {                                      
            throw new ContainerException(e);                           
        }                                                              
    }
    
    
    public final class SessionContainer implements Session {
        
        private final Map<String, Serializable> session = new HashMap<String, Serializable>();
        
        public SessionContainer() {

           }

        public Serializable get(String key) {
        
         return this.session.get(key);
       
        }

        public void delete(String key) {
            this.session.remove(key);
            DistributedSessionContainer.this.send(SessionCommand.DELETE, key, null);
        }


        public void set(String key, Serializable value) {
            this.session.put(key, value);
                DistributedSessionContainer.this.send(SessionCommand.SET, key, value);

        }
    }
        
}
