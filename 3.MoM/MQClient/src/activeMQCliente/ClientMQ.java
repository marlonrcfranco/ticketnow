package activeMQCliente;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Topic;
import javax.jms.TopicSession;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author marlonrcfranco
 *
 */
public class ClientMQ {

	private String sEndServidor;
	private JmsTemplate jmsTemplate;

	public ClientMQ() throws IOException {
		CarregaConfiguracoes("MQconfig.txt");
		ConfigMQ cfg = new ConfigMQ("tcp://" + sEndServidor);
		jmsTemplate = cfg.jmsTemplate();
	}

	private boolean CarregaConfiguracoes(String pathMQconfigtxt) throws IOException {
		// Busca dados do arquivo MQconfig.txt
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
	 * @return true|false Retorna false se a insercao foi realizada com sucesso,
	 *         retorna true caso ocorra algum erro
	 */
	public boolean InserirPedido(String Cadeira, String CodCartao, String DataValidade, String DigitoVerificador) {
		String mensagem = Cadeira + ":" + CodCartao + ":" + DataValidade + ":" + DigitoVerificador;
		if (publish("pendentes", mensagem).equalsIgnoreCase("Mensagem publicada com sucesso!")) {
			return false;
		}
		return true;
	}

	private String publish(String fila, String message) {
		jmsTemplate.convertAndSend(fila, message);
		return "Mensagem publicada com sucesso!";
	}

	private String consume(String fila) {
		String message = "";

		return message;
	}

	public String getEndServidor() {
		return sEndServidor;
	}

	public void setEndServidor(String sEndServidor) {
		this.sEndServidor = sEndServidor;
	}
}
