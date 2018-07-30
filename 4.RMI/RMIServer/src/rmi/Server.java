package rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
	private int iPorta;

	public void Inicializa() throws RemoteException {
		Registry r = LocateRegistry.createRegistry(iPorta);
		r.rebind("Validador", new Validador());
		System.out.println("Server running...");
	}

	public int getiPorta() {
		return iPorta;
	}

	public void setiPorta(int iPorta) {
		this.iPorta = iPorta;
	}

}
