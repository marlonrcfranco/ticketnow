package rmi;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import gui.apPrincipalClient;

public class ClientRMI {
	private static String sEndServidor;
	private static ClientRMI instanciaUnica = null;
	private Object lock;

	private ClientRMI() throws InvocationTargetException, InterruptedException {
		inicializaTela();
	}

	public static ClientRMI getInstancia() throws InvocationTargetException, InterruptedException {
		if (instanciaUnica == null) {
			instanciaUnica = new ClientRMI();
		}
		return instanciaUnica;
	}

	private void inicializaTela() throws InvocationTargetException, InterruptedException {
		apPrincipalClient tela = new apPrincipalClient();
		
		apPrincipalClient.frmConfiguraoRmi.setVisible(true);
		sEndServidor = tela.getsEndServidor();
	}

	public String getsEndServidor() {
		return sEndServidor;
	}

	public void setsEndServidor(String sEndServidor) {
		ClientRMI.sEndServidor = sEndServidor;
	}

	public String ValidaCC(String CodCartao) throws RemoteException, MalformedURLException, NotBoundException {
		Client oClient = new Client();
		oClient.setEndServidor(sEndServidor);
		oClient.setsServico("Validador");
		return oClient.Conecta();
	}

}
