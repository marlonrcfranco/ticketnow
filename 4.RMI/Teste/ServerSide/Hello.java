import java.rmi.*;
import java.rmi.server.*;
public class Hello extends UnicastRemoteObject implements HelloInterface {
    private String message; // Strings are serializable
    public String sayHello() throws RemoteException {
        return message;
    }
    public Hello (String msg) throws RemoteException {
        message = msg;
    }
}