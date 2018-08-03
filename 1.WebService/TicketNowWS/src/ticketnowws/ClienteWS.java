package ticketnowws;
 
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import org.mozartspaces.core.MzsCoreException;

class ClienteWS {
 
  public static void main(String args[]) throws Exception {
    String portaServidor = "56000";
    
    URL url = new URL("http://127.0.0.1:" + portaServidor + "/ticketnowws?wsdl");
    QName qname = new QName("http://ticketnowws/","TicketNowWSService");
    Service ws = Service.create(url, qname);
    iTicketNow ticketnow = ws.getPort(iTicketNow.class);
    
    System.out.println("Comunicação com Web Service estabelecida");

    //System.out.println("Resposta: " + ticketnow.comprarIngresso(1, "A", "7000000000000000", "2020", "133"));
    //System.out.println("Resposta: " + ticketnow.consultarAssento(10, "A"));
    System.out.println("Tickets disponiveis: " + ticketnow.consultarTudosAssentos());
  }
}