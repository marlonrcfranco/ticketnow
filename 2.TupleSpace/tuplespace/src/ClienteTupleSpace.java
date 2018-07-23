package tuplespace;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class ClienteTupleSpace {
    private String ip_server;
    private int port_server;
    private Socket socket_cliente;

    public ClienteTupleSpace(String ip_server, int port_server) {
        init(ip_server, port_server);
    }
    
    int write(Entry e) {
        System.out.println("Escrevendo");
        return 1;
    }

    private void init(String ip_server, int port_server) {
       try {
            socket_cliente = new Socket(ip_server, port_server);
       } catch (IOException ex) {
            System.out.println("Erro na criação do Socket do Cliente");
            System.out.println(ex);
       }
    }

}
