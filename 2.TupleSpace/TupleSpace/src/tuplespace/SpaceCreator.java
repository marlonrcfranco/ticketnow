/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tuplespace;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import static org.mozartspaces.core.MzsConstants.Container.UNBOUNDED;
import org.mozartspaces.core.MzsCoreException;

/**
 *
 * @author viniciuslucena
 */
public class SpaceCreator {
    private static DefaultMzsCore core;
    private static Capi capi;
    private static ContainerReference cref;
    private static String nomeContainer;
    private static int portaServidor;
   
    public static void main(String [] args) throws MzsCoreException {
        System.out.println("Inicializando Servidor TS");
        init();
        System.out.println("Populando o Espaço");
        popularEspaco();
    }
    
    
    public static void init() throws MzsCoreException {
        System.out.println("Carregando configuracao");
        
        carregarConfiguracaoServidor();
        
        System.out.println("Nome do Container: " + SpaceCreator.nomeContainer);
        System.out.println("Porta: " + SpaceCreator.portaServidor);
        
        SpaceCreator.core = DefaultMzsCore.newInstance(SpaceCreator.portaServidor);
        SpaceCreator.capi = new Capi(SpaceCreator.core);
        
        SpaceCreator.cref = SpaceCreator.capi.createContainer(SpaceCreator.nomeContainer, null, UNBOUNDED, null, new LindaCoordinator(false) );
    }

    public static void popularEspaco() throws MzsCoreException {
        for(int numeroAssento = 1; numeroAssento <= 10; numeroAssento++) {
            for(char letraFileira = 'A'; letraFileira <= 'E'; letraFileira++) {
                ClienteTupleSpace.Assento oAssento = new ClienteTupleSpace.Assento(numeroAssento, String.valueOf(letraFileira));
                SpaceCreator.capi.write(SpaceCreator.cref, new Entry((Serializable) oAssento));
            }
        }
    }
    
    public SpaceCreator(String nomeContainer, int portaServidor) throws MzsCoreException {
        System.out.println("Criando espaco [" + nomeContainer + ":" + portaServidor + "]");
        this.core = DefaultMzsCore.newInstance(portaServidor);
        this.capi = new Capi(this.core);
        
        this.cref = capi.createContainer(nomeContainer, null, UNBOUNDED, null, new LindaCoordinator(false) );
    }

    public SpaceCreator() throws MzsCoreException {
        int portaServidor = 55000;
        String nomeContainer = "admin";  
        
        this.core = DefaultMzsCore.newInstance(portaServidor);
        this.capi = new Capi(core);
        
        this.cref = capi.createContainer(nomeContainer, null, UNBOUNDED, null, new LindaCoordinator(false) );
        
    }
    
    private static void carregarConfiguracaoServidor() {
        String fileName = "config.txt";
        String line = null;

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(":");
 
                SpaceCreator.portaServidor = Integer.parseInt(split[1]);
                SpaceCreator.nomeContainer = split[0];
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
    
    
}
