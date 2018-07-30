package activeMQCliente;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.jms.core.JmsTemplate;

/**
 * @author marlonrcfranco
 *
 */
public class ClientMQ {

	private String sEndServidor;
	private ConfigMQ cfg;
	private JmsTemplate jmsTemplate;

	public ClientMQ() throws IOException {
		CarregaConfiguracoes("MQconfig.txt");
	}

	public boolean CarregaConfiguracoes(String pathMQconfigtxt) throws IOException {
		File file = new File(pathMQconfigtxt);
		BufferedReader reader = null;
		String text = null;
		reader = new BufferedReader(new FileReader(file));
		text = reader.readLine();
		reader.close();
		if (text == null || text.isEmpty() || text.equals("")) {
			return false;
		}
		sEndServidor = text;
		cfg = new ConfigMQ("tcp://" + sEndServidor);
		jmsTemplate = cfg.jmsTemplate();
		return true;
	}

	/**
	 * Método de Insercao do Pedido de Verificação de Cartão na fila de Pendentes.
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
	 * @return true|false Retorna true se a insercao foi realizada com sucesso,
	 *         retorna false caso ocorra algum erro
	 */
	public boolean InserirPedidoNaFilaPedidos(String Cadeira, String CodCartao, String DataValidade,
			String DigitoVerificador) {
		String mensagem = Cadeira + ":" + CodCartao + ":" + DataValidade + ":" + DigitoVerificador;
		if (publish("pedidos", mensagem)) {
			return true;
		}
		return false;
	}

	public boolean publish(String fila, String message) {
		jmsTemplate.convertAndSend(fila, message);
		System.out.println("Mensagem '" + message + "' publicada com sucesso na fila '" + fila + "'.");
		return true;
	}

	public String consume(String fila) {
		String message = jmsTemplate.receiveAndConvert(fila).toString();
		System.out.println("Mensagem recebida da fila '" + fila + "': " + message);
		return message;
	}

	public String PegarDaFilaConcluidos() {
		String resposta = consume("concluidos");
		if (resposta.endsWith("VALIDO")) {
			return "Cartão VÁLIDO!";
		} else {
			return "Cartão INVÁLIDO!";
		}
	}

	public String getEndServidor() {
		return sEndServidor;
	}

	public void setEndServidor(String sEndServidor) {
		this.sEndServidor = sEndServidor;
	}
}
