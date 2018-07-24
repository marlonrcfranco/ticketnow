package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Validador extends UnicastRemoteObject implements ValidadorInterface {

	private String message = "Serviço disponível!";

	public Validador() throws RemoteException {
		super();
	}

	public String sayHello() throws RemoteException {
		return message;
	}

}