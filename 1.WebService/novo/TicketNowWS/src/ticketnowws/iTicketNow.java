package ticketnowws;
 
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import java.io.IOException;
import org.mozartspaces.core.MzsCoreException;
 
@WebService
@SOAPBinding(style = Style.RPC)
public interface iTicketNow {
    @WebMethod  String comprarIngresso(Integer numeroAssento, String letraAssento, String codCartao, String dataVencimento, String digitoVerificador) throws MzsCoreException, IOException;
    @WebMethod  String consultarAssento(Integer numeroAssento, String letraFileira) throws MzsCoreException;
    @WebMethod  String consultarTudosAssentos() throws MzsCoreException;
}