import java.rmi.*;

public interface HelloInterface extends Remote {
    public String sayHello() throws RemoteException;
}