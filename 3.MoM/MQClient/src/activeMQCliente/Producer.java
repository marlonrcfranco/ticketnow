/**
 * 
 */
package activeMQCliente;

import org.springframework.jms.core.JmsTemplate;

public class Producer {

	private JmsTemplate jmsTemplate;

	public Producer(String brokerURL) {
		ConfigMQ cfg = new ConfigMQ(brokerURL);
		jmsTemplate = cfg.jmsTemplate();
	}

	public String publish(String fila, String message) {
		jmsTemplate.convertAndSend(fila, message);
		return "Mensagem publicada com sucesso!";
	}
}
