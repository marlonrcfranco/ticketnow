package tuplespace;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.IOException;

public class TupleSpace implements Runnable {
    private static ArrayList<Entry> espaco = new ArrayList<Entry>();
    protected int          serverPortVal;
    protected ServerSocket serverSocketVal = null;
    protected boolean      hasStopped    = false;
    protected Thread       movingThread = null;

    public TupleSpace(int port) {
        this.serverPortVal = port;
        this.run();
    }

    public void run(){
        synchronized(this){
            this.movingThread = Thread.currentThread();
        }
        openSeverSocket();
        while(!hasStopped()){
            Socket clntSocket = null;
            try {
                System.out.println("Socket do Servidor: " + serverSocketVal.toString());
                clntSocket = this.serverSocketVal.accept();
                System.out.println("Novo cliente conectado");
            } catch (IOException e) {
                if(hasStopped()) {
                    System.out.println("Server has Stopped...Please check") ;
                    return;
                }
                throw new RuntimeException(
                    "Client cannot be connected - Error", e);
            }
            new Thread(new HandleThread(clntSocket, "This is a multithreaded Server")).start();
        }
        System.out.println("Server has Stopped...Please check") ;
    }
    private synchronized boolean hasStopped() {
        return this.hasStopped;
    }
    public synchronized void stop(){
        this.hasStopped = true;
        try {
            this.serverSocketVal.close();
        } catch (IOException e) {
            throw new RuntimeException("Server can not be closed - Please check error", e);
        }
    }
    private void openSeverSocket() {
        try {
            this.serverSocketVal = new ServerSocket(this.serverPortVal);
        } catch (IOException e) {
            throw new RuntimeException("Nao foi possivel abrir o servidor na porta " + this.serverPortVal, e);
        }
    }

}