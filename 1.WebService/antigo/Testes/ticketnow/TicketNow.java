package ticketnow;
 
import java.util.Date;
import javax.jws.WebService;
import java.util.ArrayList;
import java.io.IOException;

import activeMQCliente.ClientMQ;
import clientetuplespace.ClienteTupleSpace;
import rmi.ClientRMI;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.capi3.CountNotMetException;

 
@WebService(endpointInterface = "ticketnow.iTicketNow")

public class TicketNow implements iTicketNow {
     
  public float soma(float num1, float num2) {
    return num1 + num2;
  }
   
  public float subtracao(float num1, float num2) {
    return num1 - num2;
  }
 
  public float multiplicacao(float num1, float num2) {
    return num1 * num2;
  }
 
  public float divisao(float num1, float num2) {
    return num1 / num2;
  }

  public String comprarIngresso(Integer numeroAssento, String letraAssento, String codCartao, String dataVencimento, String digitoVerificador) throws MzsCoreException, IOException {
    ClienteTupleSpace oClienteTupleSpace = new ClienteTupleSpace("admin", "localhost", 55000); 
    ClientRMI oClientRMI = new ClientRMI();
    ClientMQ oClienMQ = new ClientMQ();
    
    String cadeira = numeroAssento + letraAssento;
    
    oClienteTupleSpace.take(numeroAssento, letraAssento);
    oClientRMI.ValidaCC(cadeira, codCartao, dataVencimento, digitoVerificador);
    oClienMQ.InserirPedidoNaFilaPedidos(cadeira, codCartao, dataVencimento, digitoVerificador);

    return "WebService: Comprando ingresso";
  }

  public String consultarAssento(Integer numeroAssento, String letraFileira) throws MzsCoreException {
    System.out.println("Criando Cliente do Espaco de Tuplas");
    ClienteTupleSpace oClienteTupleSpace = new ClienteTupleSpace("admin", "localhost", 55000); 
        
    ArrayList<ClienteTupleSpace.Assento> resultadoRead = oClienteTupleSpace.read(numeroAssento, letraFileira);
    
    if(resultadoRead.isEmpty())
        return "assento (" + numeroAssento + "," + letraFileira + ") não está disponivel";
    else
        return "assento (" + numeroAssento + "," + letraFileira + ") está disponivel";

  }
 
}