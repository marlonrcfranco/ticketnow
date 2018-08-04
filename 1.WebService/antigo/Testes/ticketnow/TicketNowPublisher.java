package ticketnow;
 
import javax.xml.ws.Endpoint;
 
public class TicketNowPublisher {
 
  public static void main(String[] args)
  {
    String endereco = "http://127.0.0.1:9997/ticketnow";
	  Endpoint.publish(endereco, new TicketNow());
  }
}