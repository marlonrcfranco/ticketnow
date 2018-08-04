package webservice;

import javax.xml.ws.Endpoint;

public class TicketNowPublisher {

  public static void main(String[] args)
  {
    // publicando o serviço
    Endpoint.publish("http://127.0.0.1:9876/ticketnow", new TicketNow());
  }
}
