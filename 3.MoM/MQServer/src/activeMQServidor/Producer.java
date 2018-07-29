/**
 * 
 */
package activeMQServidor;

import javax.jms.Queue;
import org.springframework.jms.core.JmsTemplate;

public class Producer {

	private JmsTemplate jmsTemplate;
	private Queue filaPendentes;
	private Queue filaConcluidos;

	public Producer() {
		ConfigMQ cfg = new ConfigMQ();
		jmsTemplate = cfg.jmsTemplate();
		filaPendentes = cfg.getFilaPendentes();
		filaConcluidos = cfg.getFilaConcluidos();
	}

	public void CarregaCongiguracoes() {
		
	}

	public String publish(String message) {
		jmsTemplate.convertAndSend(filaPendentes, message);
		return "Mensagem publicada com sucesso!";
	}
}
