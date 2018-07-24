import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HelloServant extends UnicastRemoteObject implements HelloService {

	protected HelloServant() throws RemoteException {
		super();
	}

	@Override
	public String echo(String input) throws RemoteException {
		return "From server: " + input;
	}

}
