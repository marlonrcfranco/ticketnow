package rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import activeMQCliente.ClientMQ;

public class Validador extends UnicastRemoteObject implements ValidadorInterface {
	private ClientMQ oClientMQ;

	public Validador() throws RemoteException {
		super();
	}

	public String teste() throws RemoteException {
		return "Servidor online.";
	}

	public String ValidaCC(String Cadeira, String CodCartao, String DataValidade, String DigitoVerificador) {
		String retorno;
		String message;
		try {
			oClientMQ = new ClientMQ();
			oClientMQ.CarregaConfiguracoes("MQconfig.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		message = oClientMQ.consume("pedidos");
		if (CodCartao.length() != 16 || !CodCartao.startsWith("7")) {
			retorno = "Cartão INVÁLIDO.";
			message = message + ":INVALIDO";
		} else {
			retorno = "Cartão VÁLIDO.";
			message = message + ":VALIDO";
		}
		oClientMQ.publish("concluidos", message);
		return retorno;
	}
}