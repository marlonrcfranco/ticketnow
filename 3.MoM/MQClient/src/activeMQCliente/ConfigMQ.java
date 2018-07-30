
package activeMQCliente;

import javax.jms.Queue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.jms.core.JmsTemplate;

public class ConfigMQ {

	private ActiveMQQueue filaPendentes;
	private ActiveMQQueue filaConcluidos;
	private String brokerURL = "tcp://localhost:61616";

	public ConfigMQ(String brokerURL) {
		this.brokerURL = brokerURL;
		createFilaPendentes();
		createFilaConcluidos();
	}
	public Queue createFilaPendentes() {
		filaPendentes = new ActiveMQQueue("pendentes");
		return filaPendentes;
	}

	public Queue createFilaConcluidos() {
		filaConcluidos = new ActiveMQQueue("concluidos");
		return filaConcluidos;
	}

	public ActiveMQConnectionFactory activeMQConnectionFactory() {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
		factory.setBrokerURL(brokerURL);
		return factory;
	}

	public JmsTemplate jmsTemplate() {
		return new JmsTemplate(activeMQConnectionFactory());
	}
	
	public ActiveMQQueue getFilaPendentes() {
		return filaPendentes;
	}

	public void setFilaPendentes(ActiveMQQueue filaPendentes) {
		this.filaPendentes = filaPendentes;
	}

	public ActiveMQQueue getFilaConcluidos() {
		return filaConcluidos;
	}

	public void setFilaConcluidos(ActiveMQQueue filaConcluidos) {
		this.filaConcluidos = filaConcluidos;
	}
}
