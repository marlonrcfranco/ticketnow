package ticketnowws;
 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.ws.Endpoint;
import tuplespace.SpaceCreator;
 
public class TicketNowPublisher {
    private static int portaServidor;
    
    public static void main(String[] args){
        carregarConfiguracaoWebService();
        
        String endereco = "http://127.0.0.1:" + TicketNowPublisher.portaServidor + "/ticketnowws";
        System.out.println("WebService rodando em " + endereco);
        
        Endpoint.publish(endereco, new TicketNowWS());
    }
    
    private static void carregarConfiguracaoWebService() {
        String fileName = "config_WebService.txt";
        String line = null;
        int erro = 0;
        
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                TicketNowPublisher.portaServidor = Integer.parseInt(line);
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
            TicketNowPublisher.portaServidor = 56000;
        }
            
        
    }
}