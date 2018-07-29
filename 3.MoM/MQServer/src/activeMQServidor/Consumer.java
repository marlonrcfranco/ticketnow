package activeMQServidor;

import javax.jms.Queue;

import org.springframework.jms.core.JmsTemplate;

public class Consumer {
	
	private JmsTemplate jmsTemplate;
	private Queue filaPendentes;
	private Queue filaConcluidos;

	public Consumer() {
		ConfigMQ cfg = new ConfigMQ();
		jmsTemplate = cfg.jmsTemplate();
		filaPendentes = cfg.getFilaPendentes();
		filaConcluidos = cfg.getFilaConcluidos();
	}

	public void consume() {
		String message;
		message = jmsTemplate.receiveAndConvert("pendentes").toString();
		System.out.println("Mensagem recebida: " + message);
	}
}
