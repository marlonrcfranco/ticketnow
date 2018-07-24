import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		Registry r = LocateRegistry.createRegistry(7777);
		r.rebind("hello", new HelloServant());
	}
}
