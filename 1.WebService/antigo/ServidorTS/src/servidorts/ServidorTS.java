/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorts;

import java.io.*;
import java.net.URI;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import static org.mozartspaces.core.MzsConstants.Container.UNBOUNDED;
import org.mozartspaces.core.MzsCoreException;

/**
 *
 * @author viniciuslucena
 */
public class ServidorTS {
    private DefaultMzsCore core;
    private Capi capi;
    private ContainerReference cref;
    private static String nomeContainer;
    private static int portaServidor;
    
   
    public static void main(String [] args) throws MzsCoreException {
        
        System.out.println("!!!!!!!!\nVou iniciar\n!!!!!!!");
        init();
        /*
        System.out.println("Iniciei");
        System.out.println("Vou popular");
        popularEspaco();
        System.out.println("Populei");
        */
        
    }
    
    public static void init() throws MzsCoreException {
        carregarConfiguracaoServidor();
        System.out.println("Configuracao carregada");
        System.out.println(ServidorTS.nomeContainer + ":" + ServidorTS.portaServidor);
        
        DefaultMzsCore core = DefaultMzsCore.newInstance(ServidorTS.portaServidor);
        Capi capi = new Capi(core);
        
        ContainerReference cref = capi.createContainer(ServidorTS.nomeContainer, null, UNBOUNDED, null, new LindaCoordinator(false) );
    }

    private static void carregarConfiguracaoServidor() {
        String fileName = "config.txt";
        String line = null;

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(":");
 
                ServidorTS.portaServidor = Integer.parseInt(split[1]);
                ServidorTS.nomeContainer = split[0];
            }   

            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println("Erro na hora de abir o arquivo '" + fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Erro na leitura do arquivo '" + fileName + "'");                  
        } 
        
    }

    private static void popularEspaco() throws MzsCoreException {
        URI SPACECliente = URI.create("xvsm://localhost:" + ServidorTS.portaServidor);
        DefaultMzsCore coreCliente = DefaultMzsCore.newInstance(0);
        Capi capiCliente = new Capi(coreCliente);
        
        ContainerReference crefCliente = capiCliente.lookupContainer(ServidorTS.nomeContainer, SPACECliente, MzsConstants.RequestTimeout.ZERO, null);
        
        int numeroAssento = 1;
        char letraFileira = 'A';
        System.out.println("Escrevendo..");
        
        Assento oAssento = new Assento(10, "A");
        capiCliente.write(crefCliente, new Entry((Serializable) oAssento));
        
        System.out.println("Escrito");

    }
    
    public static class Assento implements Serializable {

        private final Integer numeroAssento;
        private final String letraFileira;

        Assento(final Integer numeroAssento, final String letraFileira) {
            this.numeroAssento = numeroAssento;
            this.letraFileira = letraFileira;
        }

        @Override
        public String toString() {
            return "Assento: (" + numeroAssento + "," + letraFileira + ")\n\n";
        }
 
        
    }

}
