package rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
	private String sEndServidor;
	private String sServico;

	public String Conecta() throws RemoteException, MalformedURLException, NotBoundException {
		ValidadorInterface oValidador = (ValidadorInterface) Naming.lookup("rmi://"+ sEndServidor +"/"+ sServico);
		return oValidador.sayHello();
	}

	public void setEndServidor(String endServidor) {
		this.sEndServidor = endServidor;
	}

	public void setsServico(String sServico) {
		this.sServico = sServico;
	}
}
