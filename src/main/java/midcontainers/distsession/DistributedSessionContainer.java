/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package midcontainers.distsession;

import java.net.*;
import java.io.*;

import midcontainers.Binding;
import midcontainers.Container;
import midcontainers.ContainerException;
import midcontainers.Named;
import midcontainers.local.LocalContainer;


/**
 *
 * @author alexis
 */

public class DistributedSessionContainer extends LocalContainer {
    
    private final MulticastSocket socket;
    private final InetAddress group;     
    private final String groupAddress;   
    private final int port;

    
    public DistributedSessionContainer(String groupAddress, int port) {
        try {       
            
            this.groupAddress = groupAddress;                          
            this.port = port;                                          
            this.group = InetAddress.getByName(groupAddress);          
            socket = new MulticastSocket(port);                        
            socket.setSoTimeout(10000);       
            
        } catch (UnknownHostException e) {                             
            throw new ContainerException(e);                           
        } catch (IOException e) {                                      
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

    
    
}
