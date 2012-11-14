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

import midcontainers.local.LocalContainer;
import midcontainers.ContainerException;

public class RemoteContainerServer extends LocalContainer {

    private final ServerSocket serverSocket;
    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    public static enum RemoteCommand {
        CHECK_REFERENCE,
        CHECK_DEFINITION,
        GET_REFERENCE,
        GET_DEFINITION,
        INVOKE
    }

    public RemoteContainerServer(int port) {
        super();
        this.port = port;
        try {
            this.serverSocket = new ServerSocket();
        } catch (IOException e) {
            throw new ContainerException(e);
        }
    }
    
    public void start() {                                  
        try {                                              
            serverSocket.setSoTimeout(10000);              
            serverSocket.bind(new InetSocketAddress(port));
        } catch (SocketException e) {                      
            throw new ContainerException(e);               
        } catch (IOException e) {                          
            throw new ContainerException(e);               
        }                                                  

        running.set(true);                                 
        acceptingThread.start();                           
    }
    
    private final Thread acceptingThread = new Thread() {
        public void run() {
            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Worker(clientSocket).start();
                } catch (SocketTimeoutException ignored) {
                } catch (IOException e) {
                    throw new ContainerException(e);
                }
            }
        }
    };
    
 private class Worker extends Thread {
    private final Socket socket;
    private int clientObjectsCounter = 0;
    private final Map<Integer, Object> clientObjects = new HashMap<Integer, Object>();

    public Worker(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            
            while (running.get()) {
                command = (RemoteCommand) in.readObject();
                switch (command) {

                    case CHECK_DEFINITION:
                        name = (String) in.readObject();
                        out.writeObject(hasValueDefinedFor(name));
                        out.flush();
                        break;

                    case CHECK_REFERENCE:
                        // (...)
                        break;

                    case RemoteContainerClient.GET_DEFINITION:
                        // (...)
                        break;

                    case GET_REFERENCE:
                        name = (String) in.readObject();
                        qualifier = (String) in.readObject();
                        instance = obtainReference(Class.forName(name), qualifier);
                        clientObjects.put(clientObjectsCounter, instance);
                        out.writeObject(clientObjectsCounter);
                        out.flush();
                        clientObjectsCounter = clientObjectsCounter + 1;
                        break;

                    case INVOKE:
                        objectId = in.readInt();
                        name = (String) in.readObject();
                        parametersCount = in.readInt();
                        parameters = new Object[parametersCount];
                        parameterTypes = new Class<?>[parametersCount];
                        for (int i = 0; i < parametersCount; i++) {
                            parameterTypes[i] = Class.forName((String) in.readObject());
                        }
                        for (int i = 0; i < parametersCount; i++) {
                            parameters[i] = in.readObject();
                        }

                        instance = clientObjects.get(objectId);
                        Method method = instance.getClass().getMethod(name, parameterTypes);
                        out.writeObject(method.invoke(instance, parameters));
                        out.flush();
                        break;
                }
            }
        } catch (IOException e) {
            throw new ContainerException(e);
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        } catch (NoSuchMethodException e) {
            throw new ContainerException(e);
        } catch (InvocationTargetException e) {
            throw new ContainerException(e);
        } catch (IllegalAccessException e) {
            throw new ContainerException(e);
        }
    }
}
}