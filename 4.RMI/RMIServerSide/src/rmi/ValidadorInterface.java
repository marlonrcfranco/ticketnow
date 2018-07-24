package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ValidadorInterface extends Remote {
	public String sayHello() throws RemoteException;
}