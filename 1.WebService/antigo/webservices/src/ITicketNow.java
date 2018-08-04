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
  @WebMethod String consultarIngresso(Integer numeroAssento, String letraFileira);
  @WebMethod String comprarIngresso(Integer numeroAssento, String letraFileira, String codCartao, String dataVencimento, String digitoVerificador);
}
