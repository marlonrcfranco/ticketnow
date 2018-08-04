package webservice;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

class TicketNowClient {

  public static void main(String args[]) throws Exception {
    URL url = new URL("http://127.0.0.1:9876/ticketnow?wsdl");
    QName qname = new QName("http://ticketnow/","TicketNowService");
    Service ws = Service.create(url, qname);
    ITicketNow ticketnow = ws.getPort(ITicketNow.class);

    int aux = ticketnow.comprar(1, 'A');
    
    if(aux == 1) 
      System.out.println("Cliente está consultando ingresso");
    else if (aux == 2)
      System.out.println("Cliente está comprando ingresso");

  }
}
