package ticketnowws;
 
import java.util.Date;
import javax.jws.WebService;
import java.util.ArrayList;
import java.io.IOException;

import activeMQCliente.ClientMQ;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import tuplespace.ClienteTupleSpace;
import rmi.ClientRMI;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.capi3.CountNotMetException;

 
@WebService(endpointInterface = "ticketnowws.iTicketNow")

public class TicketNow implements iTicketNow {
    private String nomeContainerTS;
    private String ipServidorTS;
    private int portaServidorTS;

    public TicketNow() { }
    
    
    
    @Override
    public String comprarIngresso(Integer numeroAssento, String letraAssento, String codCartao, String dataVencimento, String digitoVerificador) throws MzsCoreException, IOException {
        carregarConfiguracoesTupleSpace();

        ClienteTupleSpace oClienteTupleSpace = new ClienteTupleSpace(this.nomeContainerTS, this.ipServidorTS, this.portaServidorTS); 
        ClientRMI oClientRMI = new ClientRMI();
        ClientMQ oClienMQ = new ClientMQ();
        
        String cadeira = numeroAssento + letraAssento;
        ArrayList<ClienteTupleSpace.Assento> resultadoRead = oClienteTupleSpace.readAll(numeroAssento, letraAssento);
        
        if(resultadoRead.isEmpty()) {
            oClienteTupleSpace.encerrar();
            return "WebService: Ingresso não está disponível";
        }
        
        ArrayList<ClienteTupleSpace.Assento> resultadoTake = oClienteTupleSpace.take(numeroAssento, letraAssento);

        oClienMQ.InserirPedidoNaFilaPedidos(cadeira, codCartao, dataVencimento, digitoVerificador);

        oClientRMI.ValidaCC(cadeira, codCartao, dataVencimento, digitoVerificador);
       
        oClienteTupleSpace.encerrar();
        return "WebService: Comprando ingresso";
    }

    @Override
    public String consultarAssento(Integer numeroAssento, String letraFileira) throws MzsCoreException {
        carregarConfiguracoesTupleSpace();

        ClienteTupleSpace oClienteTupleSpace = new ClienteTupleSpace(this.nomeContainerTS, this.ipServidorTS, this.portaServidorTS); 

        ArrayList<ClienteTupleSpace.Assento> resultadoRead = oClienteTupleSpace.readAll(numeroAssento, letraFileira);
        
        oClienteTupleSpace.encerrar();
        
        if(resultadoRead.isEmpty())
            return "assento (" + numeroAssento + "," + letraFileira + ") não está disponivel";
        else
            return "assento (" + numeroAssento + "," + letraFileira + ") está disponivel";

    }
    
    @Override
    public String consultarTudosAssentos() throws MzsCoreException {
        carregarConfiguracoesTupleSpace();

        ClienteTupleSpace oClienteTupleSpace = new ClienteTupleSpace(this.nomeContainerTS, this.ipServidorTS, this.portaServidorTS); 

        ArrayList<ClienteTupleSpace.Assento> resultadoRead = oClienteTupleSpace.readAll(null, null);


        String retorno = "";

        for(ClienteTupleSpace.Assento assento : resultadoRead) {
            retorno += assento.getNumeroAssento() + assento.getLetraFileira() + ":";
        }
        
        oClienteTupleSpace.encerrar();
        return retorno;
    }
    
    private void carregarConfiguracoesTupleSpace() {
      String fileName = "config_TupleSpace.txt";
      String line = null;
      int erro = 0;

      try {
          FileReader fileReader = new FileReader(fileName);
          BufferedReader bufferedReader = new BufferedReader(fileReader);

          while((line = bufferedReader.readLine()) != null) {
              String[] split = line.split(":");

              this.nomeContainerTS = split[0];
              this.ipServidorTS = split[1];
              this.portaServidorTS = Integer.parseInt(split[2]);

          }   

          bufferedReader.close();         
      }
      catch(FileNotFoundException ex) {
          System.out.println("Erro na hora de abir o arquivo '" + fileName + "'");  
          erro = 1;
      }
      catch(IOException ex) {
          System.out.println("Erro na leitura do arquivo '" + fileName + "'");            
          erro = 1;
      } 

      if(erro == 1) { 
          System.out.println("Carregando configuração padrão");
          this.nomeContainerTS = "admin";
          this.ipServidorTS = "localhost";
          this.portaServidorTS = 55000;
      }

    }
}