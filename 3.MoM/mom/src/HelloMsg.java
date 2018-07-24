import javax.jms.*;

public class HelloMsg {
    public static void main(String argv[]) throws Exception {
        // The producer and consumer need to get a connection factory and use it to set
        // up
        // a connection and a session
        QueueConnectionFactory connFactory = new com.sun.messaging.QueueConnectionFactory();
        QueueConnection conn = connFactory.createQueueConnection();
        // This session is not transacted, and it uses automatic message acknowledgement
        QueueSession session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue q = new com.sun.messaging.Queue("world");
        // Sender
        QueueSender sender = session.createSender(q);
        // Text message
        TextMessage msg = session.createTextMessage();
        msg.setText("Hello there!");
        System.out.println("Sending the message: " + msg.getText());
        sender.send(msg);
        // Receiver
        QueueReceiver receiver = session.createReceiver(q);
        conn.start();
        Message m = receiver.receive();
        if (m instanceof TextMessage) {
            TextMessage txt = (TextMessage) m;
            System.out.println("Message Received: " + txt.getText());
        }
        session.close();
        conn.close();
    }
}