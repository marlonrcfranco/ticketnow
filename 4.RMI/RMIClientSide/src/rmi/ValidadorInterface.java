package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ValidadorInterface extends Remote {
	public String teste() throws RemoteException;
	public String ValidaCC(String codCartao) throws RemoteException;
}