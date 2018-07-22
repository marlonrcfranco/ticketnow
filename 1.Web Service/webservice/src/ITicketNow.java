/**
 * Interface do Serviço
 * 
  */
package webservice;
 
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
 
@WebService
@SOAPBinding(style = Style.RPC)
public interface ITicketNow {
  @WebMethod int consultar(int numero_fileira, char letra_corredor);
  @WebMethod int comprar(int numero_fileira, char letra_corredor);
}