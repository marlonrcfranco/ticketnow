package rmi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientRMI {
	private String sEndServidor;
	private String sServico = "Validador";
	private ValidadorInterface oValidador;

	public ClientRMI() throws IOException {
		CarregaConfiguracoes("RMIconfig.txt");
	}

	public boolean CarregaConfiguracoes(String pathRMIconfigtxt) throws IOException {
		// busca dados do arquivo RMIconfig.txt
		File file = new File(pathRMIconfigtxt);
		BufferedReader reader = null;
		String text = null;
		reader = new BufferedReader(new FileReader(file));
		text = reader.readLine();
		reader.close();
		if (text == null || text.isEmpty() || text.equals("")) {
			return false;
		}
		sEndServidor = text;
		return true;
	}

	public String testaConexao() {
		try {
			oValidador = (ValidadorInterface) Naming.lookup("rmi://" + sEndServidor + "/" + sServico);
			return oValidador.teste();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
			return "ERRO";
		}
	}

	/**
	 * Método de invocacao do metodo remoto ValidaCC.
	 * 
	 * @param Cadeira
	 *            Codigo da cadeira, com uma letra e um numero (ex: "A1")
	 * @param CodCartao
	 *            Codigo do cartao com 16 digitos numericos não negativos (ex:
	 *            7777777777777777)
	 * @param DataValidade
	 *            Data de validade mês e ano, com 4 digitos (ex: "0120", que
	 *            equivale a 01/2020)
	 * @param DigitoVerificador
	 *            Digito verificador atrás do cartão - 3 numeros não negativos (ex:
	 *            999)
	 * @return "Cartão VÁLIDO." ou "Cartão INVÁLIDO". Retorna "Cartão VÁLIDO." se o
	 *         cartão foi aceito, Retorna "Cartão INVÁLIDO." se o cartão não foi
	 *         aceito. Retorna uma mensagem de erro caso ocorra algum erro.
	 */
	public String ValidaCC(String Cadeira, String codCartao, String DataVencimento, String DigitoVerificador) {
		String retorno;
		try {
			oValidador = (ValidadorInterface) Naming.lookup("rmi://" + sEndServidor + "/" + sServico);
			retorno = oValidador.ValidaCC(Cadeira, codCartao, DataVencimento, DigitoVerificador);
			System.out.println(retorno);
			return retorno;
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
			return "ERRO na invocacao do metodo remoto ValidaCC()";
		}
	}

	public void setEndServidor(String endServidor) {
		this.sEndServidor = endServidor;
	}

	public void setsServico(String sServico) {
		this.sServico = sServico;
	}
}
