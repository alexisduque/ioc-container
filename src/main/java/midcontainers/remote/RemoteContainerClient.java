/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package midcontainers.remote;

/**
 *
 * @author Alex
 */

import java.net.*;
import java.io.*;
import java.lang.Object;
import java.util.concurrent.atomic.*;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import midcontainers.Container;
import midcontainers.local.LocalContainer;
import midcontainers.ContainerException;
import midcontainers.Binding;

public class RemoteContainerClient implements Container {
    
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    public static enum RemoteCommand {
        CHECK_REFERENCE,
        CHECK_DEFINITION,
        GET_REFERENCE,
        GET_DEFINITION,
        INVOKE
    }
    
    public RemoteContainerClient(String host, int port) {           
        try {                                                       
            Socket socket = new Socket(host, port);                 
            // /!\ ORDER MATTERS OR YOU DEADLOCK /!\                
            out = new ObjectOutputStream(socket.getOutputStream()); 
            in = new ObjectInputStream(socket.getInputStream());    
        } catch (IOException e) {                                   
            throw new ContainerException(e);                        
        }                                                           
    }
    
    public Container delegateTo(Container container) throws UnsupportedOperationException {                                
        throw new UnsupportedOperationException("A remote container client does not support delegation");                  
    }                                                                                                                      

    public Container declare(Binding binding) throws UnsupportedOperationException {                                       
        throw new UnsupportedOperationException("A remote container client can only obtain references and defined values");
    }                                                                                                                      

    public Container define(String name, Object value) throws UnsupportedOperationException {                              
        throw new UnsupportedOperationException("A remote container client can only obtain references and defined values");
    }
    
    
    
    public boolean hasReferenceDeclaredFor(Class<?> interfaceClass, String qualifier) {
        try {                                                                          
            out.writeObject(RemoteCommand.CHECK_REFERENCE);                                          
            out.writeObject(interfaceClass.getName());                                 
            out.writeObject(qualifier);                                                
            out.flush();                                                               
            return (Boolean) in.readObject();                                          
        } catch (IOException e) {                                                      
            throw new ContainerException(e);                                           
        } catch (ClassNotFoundException e) {                                           
            throw new ContainerException(e);                                           
        }                                                                              
    }
    
    public boolean hasReferenceDeclaredFor(Class<?> interfaceClass) {
            
            return this.hasReferenceDeclaredFor(interfaceClass, null);
            
    }

    public Object definitionValue(String name) {
        try {                                   
            out.writeObject(RemoteCommand.GET_DEFINITION);    
            out.writeObject(name);              
            out.flush();                        
            return in.readObject();             
        } catch (IOException e) {               
            throw new ContainerException(e);    
        } catch (ClassNotFoundException e) {    
            throw new ContainerException(e);    
        }                                       
    }
    
    public <T> T obtainReference(Class<T> interfaceClass) {
        
        return this.obtainReference(interfaceClass, null);
    }
    
    public <T> T obtainReference(Class<T> interfaceClass, String qualifier) {
        try {

            out.writeObject(RemoteCommand.GET_REFERENCE);
            out.writeObject(interfaceClass.getName());
            out.writeObject(qualifier);
            out.flush();
            final int objectId = (Integer) in.readObject();

            InvocationHandler handler = new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
                    // TODO: la suite du protocole pour un INVOKE, en particulier lui passer objectId et le nom de méthode
                    out.writeObject(RemoteCommand.INVOKE);
                    (...)                    

                    if (parameters == null) {
                        out.writeInt(0);
                    } else {
                        out.writeInt(parameters.length);
                        for (Class<?> type : method.getParameterTypes()) {
                            out.writeObject(type.getName());
                        }
                        // TODO: ecrire les valeurs des parametres
                        (...)
                    }
                    out.flush();
                    return in.readObject();
                }
            };

            // Fabrique un proxy sur l'interface interfaceClass
            return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{interfaceClass}, handler);

        } catch (IOException e) {
            throw new ContainerException(e);
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }
    
     public boolean hasValueDefinedFor(String name) {
         
       return true;
       
     }
}