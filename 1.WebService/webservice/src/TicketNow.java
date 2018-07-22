package webservice;
 
import java.util.Date;
import javax.jws.WebService;
 
@WebService(endpointInterface = "webservice.ITicketNow")
public class TicketNow implements ITicketNow {
  public int consultar(int numero_fileira, char letra_corredor) {
    return 1;
  }

  public int comprar(int numero_fileira, char letra_corredor) {
    return 2;
  }
 
}