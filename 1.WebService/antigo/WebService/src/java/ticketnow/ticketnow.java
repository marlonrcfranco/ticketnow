/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ticketnow;

import activeMQCliente.ClientMQ;
import tuplespace.ClienteTupleSpace;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.mozartspaces.core.MzsCoreException;
import rmi.ClientRMI;

/**
 *
 * @author viniciuslucena
 */
@WebService(serviceName = "ticketnow")
public class ticketnow {

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        return "Hello " + txt + " !";
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getTime")
    public String getTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd MM-mm-ss"); 
        return sdf.format(cal.getTime().toString());
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "comprarIngresso")
    public String comprarIngresso(@WebParam(name = "numeroAssento") int numeroAssento, @WebParam(name = "letraAssento") String letraAssento, @WebParam(name = "codCartao") String codCartao, @WebParam(name = "dataVencimento") String dataVencimento, @WebParam(name = "digitoVerificador") String digitoVerificador) throws MzsCoreException, IOException {
        ClienteTupleSpace oClienteTupleSpace = new ClienteTupleSpace(); 
        ClientRMI oClientRMI = new ClientRMI();
        ClientMQ oClienMQ = new ClientMQ();
        
        String cadeira = numeroAssento + letraAssento;
        
        oClienteTupleSpace.take(numeroAssento, letraAssento);
        oClientRMI.ValidaCC(cadeira, codCartao, dataVencimento, digitoVerificador);
        oClienMQ.InserirPedidoNaFilaPedidos(cadeira, codCartao, dataVencimento, digitoVerificador);
        
        return null;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "consultarIngresso")
    public String consultarIngresso(@WebParam(name = "numeroAssento") int numeroAssento, @WebParam(name = "letraFileira") String letraFileira) throws MzsCoreException {
        ClienteTupleSpace oClienteTupleSpace = new ClienteTupleSpace(); 
        
        ArrayList<ClienteTupleSpace.Assento> resultadoRead = oClienteTupleSpace.read(numeroAssento, letraFileira);
        
        if(resultadoRead.isEmpty())
            return "assento (" + numeroAssento + "," + letraFileira + ") não está disponivel";
        else
            return "assento (" + numeroAssento + "," + letraFileira + ") está disponivel";
    }
}
