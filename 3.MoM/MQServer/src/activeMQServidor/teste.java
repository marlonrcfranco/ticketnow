package activeMQServidor;

public class teste {

	public static void main(String[] args) {
//		Producer oProducer = new Producer();
		Consumer oConsumer = new Consumer();
		oConsumer.consume();
		
//		System.out.println(oProducer.publish("Olar"));

	}

}
