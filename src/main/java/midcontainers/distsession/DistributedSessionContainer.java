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
import java.util.concurrent.atomic.AtomicBoolean;
/**
 *
 * @author alexis
 */

public class DistributedSessionContainer extends LocalContainer {
    
    public class SessionContainer implements Session {
        
        private final Map<String, Serializable> session = new HashMap<String, Serializable>();

        public Serializable get(String key) {
        
         return this.session.get(key);
       
        }

        public void delete(String key) {
            this.session.remove(key);
            DistributedSessionContainer.this.send(DistributedSessionContainer.SessionCommand.DELETE, key, null);
        }


        public void set(String key, Serializable value) {
            this.session.put(key, value);
            send(DistributedSessionContainer.SessionCommand.SET, key, value);
          
        }
        
        public void deleteMe(String key) {
            this.session.remove(key);
           
        }


        public void setMe (String key, Serializable value) {
            this.session.put(key, value);

        }
        
        public void sync(){
            for (Map.Entry<String, Serializable> entry : session.entrySet()){
                send(SessionCommand.SET,entry.getKey(), entry.getValue());
            }
        }
        
    }
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ListenThread thread ;
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
        DatagramPacket inPacket = new DatagramPacket(incomingBuffer, incomingBuffer.length);
        SessionContainer session = (SessionContainer) DistributedSessionContainer.this.obtainReference(Session.class);
        public void run() {
        
            while(running.get()) {
                try {
                System.out.println("1");

                socket.receive(inPacket);

                Serializable[] command = decode(inPacket.getData(), 3);

                switch ((SessionCommand) command[0]) {
                    case SET:
                        System.out.println("SET");
                        session.setMe((String)command[1],command[2]);

                        break;

                    case DELETE:
                        session.deleteMe((String)command[1]);
                        break;

                    case SYNC:
                       System.out.println("SYNC"); 
                       session.sync();
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

    private void send(SessionCommand command, String key, Serializable value) {      
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
     this.running.set(true);
   
     thread.start();
     send(SessionCommand.SYNC,null,null);
        } catch (IOException e) {            
     throw new ContainerException(e); 
        }                                    
    }      

    public void stop() {                                 
        try {                 
        socket.leaveGroup(group);   
        this.running.set(true);
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
            Binding b = new Binding(Session.class, SessionContainer.class, null, SINGLETON);
            this.declare(b);
            this.singletons.put(b.getKey(), new SessionContainer() );
            this.thread = new ListenThread();
           
        } catch (UnknownHostException e) {                             
            throw new ContainerException(e);                           
        } catch (IOException e) {                                      
            throw new ContainerException(e);                           
        }                                                              
    }
    

    
        
}
