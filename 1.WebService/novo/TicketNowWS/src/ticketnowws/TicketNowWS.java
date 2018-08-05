package ticketnowws;
 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.xml.ws.Endpoint;
 
public class TicketNowWS {
    private static int portaServidor;
    
    public static void main(String[] args) throws UnknownHostException{
        carregarConfiguracaoWebService();
        
        String endereco = "http://" + getMyIP() + ":" + TicketNowWS.portaServidor + "/ticketnowws";
        System.out.println("WebService rodando em " + endereco);
        
        Endpoint.publish(endereco, new TicketNow());
    }
    
    private static void carregarConfiguracaoWebService() {
        String fileName = "config_WebService.txt";
        String line = null;
        int erro = 0;
        
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                TicketNowWS.portaServidor = Integer.parseInt(line);
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
        
        if( erro == 1 ) {
            System.out.println("Carregando configuração padrão");
            System.out.println("Porta: 56000");
            TicketNowWS.portaServidor = 56000;
        }
            
        
    }
    
    private static String getMyIP() throws UnknownHostException {
        InetAddress IP = InetAddress.getLocalHost();
        return IP.getHostAddress();
    }
}