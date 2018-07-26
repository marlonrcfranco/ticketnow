package rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
	private String sEndServidor;
	private String sServico;
	private String sCodCartao = "5105105105105100";

	/**
	 * Exemplo de cartoes aceitos: 
	 * American Express: 378282246310005 
	 * American Express: 371449635398431 
	 * American Express Corporate:378734493671000
	 * Australian BankCard: 5610591081018250 
	 * Diners Club: 30569309025904 
	 * Diners Club: 38520000023237 
	 * Discover: 6011111111111117 
	 * Discover: 6011000990139424
	 * JCB: 3530111333300000 
	 * JCB: 3566002020360505 
	 * MasterCard: 5555555555554444
	 * MasterCard: 5105105105105100 
	 * Visa: 4111111111111111 
	 * Visa: 40128888888a81881
	 * Visa: 4222222222222 
	 * Dankort (PBS): 76009244561 
	 * Dankort (PBS): 5019717010103742 
	 * Switch/Solo (Paymentech): 6331101999990016
	 * 
	 */

	public String Conecta() {
		ValidadorInterface oValidador;
		try {
			oValidador = (ValidadorInterface) Naming.lookup("rmi://" + sEndServidor + "/" + sServico);
			if (sServico.equalsIgnoreCase("teste"))
				return oValidador.teste();
			else
				return oValidador.ValidaCC(sCodCartao);
		} catch (MalformedURLException | RemoteException | NotBoundException e1) {
			e1.printStackTrace();
			return "ERRO";
		}
	}

	public void setEndServidor(String endServidor) {
		this.sEndServidor = endServidor;
	}

	public void setsServico(String sServico) {
		this.sServico = sServico;
	}

	public String getsCodCartao() {
		return sCodCartao;
	}

	public void setsCodCartao(String sCodCartao) {
		this.sCodCartao = sCodCartao;
	}

}
