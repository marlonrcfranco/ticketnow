package ticketnow;
 
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import org.mozartspaces.core.MzsCoreException;

class ClienteWS {
 
  public static void main(String args[]) throws Exception {
    URL url = new URL("http://127.0.0.1:9997/ticketnow?wsdl");
    QName qname = new QName("http://ticketnow/","TicketNowService");
    Service ws = Service.create(url, qname);
    iTicketNow ticketnow = ws.getPort(iTicketNow.class);
    
    System.out.println("Cheguei!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

    /*
    System.out.println("Consultando: " + ticketnow.consultarAssento(10,"A"));
    System.out.println("Consultando: " + ticketnow.consultarAssento(10,"A"));
    */
    System.out.println("Resposta: " + ticketnow.comprarIngresso(10, "A", "7000000000000000", "2020", "133"));
 
  }
}