package webservice;
 
import java.util.Date;
import javax.jws.WebService;
 
@WebService(endpointInterface = "webservice.ITicketNow")
public class TicketNow implements ITicketNow {
  public String consultarAssento(Integer numeroAssento, String letraFileira) {
    return "Consultando Ingresso: (" + numeroAssento + "," + letraFileira + ")";
  }

  public String comprarAssento(Integer numeroAssento, String letraFileira, String codCartao, String dataVencimento, String digitoVerificador) {
    return "Comprando Ingresso: (" + numeroAssento + "," + letraFileira + ")";

  }
 
}