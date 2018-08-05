/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */
package tuplespace;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.LindaCoordinator.LindaSelector;
import org.mozartspaces.capi3.Queryable;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import static org.mozartspaces.core.MzsConstants.Container.UNBOUNDED;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsConstants.Selecting;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;


public class ClienteTupleSpace {
    
    private static URI SPACE;
    private static MzsCore core;
    private static Capi capi;
    private static ContainerReference cref;
    
    private static String nomeContainer;
    private static String ipServidor;
    private static int portaServidor;
    
    /**
     * 
     * @param nomeContainer
     *          Especifica o nome do container que o cliente usará.
     * @param ipServidor
     * 
     * @param portaServidor
     *          
    */
    public ClienteTupleSpace(String nomeContainer, String ipServidor, int portaServidor) throws MzsCoreException { 
        ClienteTupleSpace.nomeContainer = nomeContainer;
        ClienteTupleSpace.ipServidor = ipServidor;
        ClienteTupleSpace.portaServidor = portaServidor;
        
        System.out.println("Procurando Servidor");
        procurarServidor();
    }
    
    public ClienteTupleSpace() {
        // Configuracao Padrao
        ClienteTupleSpace.nomeContainer = "admin";
        ClienteTupleSpace.ipServidor = "localhost";
        ClienteTupleSpace.portaServidor = 55000;
    }
    
    public static void encerrar () throws MzsCoreException {
        capi.shutdown(null);
    }
    
    private static void procurarServidor() throws MzsCoreException {
        SPACE = URI.create("xvsm://" + ipServidor + ":" + portaServidor);
        core = DefaultMzsCore.newInstance(0);
        capi = new Capi(core);
        
        cref = capi.lookupContainer(nomeContainer, SPACE, RequestTimeout.ZERO, null);
    }
    
    public void write(Integer numeroAssento, String letraFileira) throws MzsCoreException {
        System.out.println("\n\n!!!! Escrevendo: (" + numeroAssento + "," + letraFileira + ")\n\n");
        
        Assento oAssento = new Assento(numeroAssento, letraFileira);
        capi.write(cref, new Entry((Serializable) oAssento));
    }
    
    public ArrayList<Assento> read(Integer numeroAssento, String letraFileira) throws MzsCoreException {
        System.out.println("\n\n!!!! Lendo: (" + numeroAssento + "," + letraFileira + ")\n\n");
        
        ArrayList<Assento> resultadoPesquisa;
        Assento template = new Assento(numeroAssento, letraFileira);
        
        LindaCoordinator.LindaSelector newSelector = LindaCoordinator.newSelector(template, 1);
        
        resultadoPesquisa = capi.read(cref, newSelector, 0, null);
        
        System.out.println("Resultado da pesquisa: " + resultadoPesquisa);
        
        return resultadoPesquisa;
    }
    
    public ArrayList<Assento> readAll(Integer numeroAssento, String letraFileira) throws MzsCoreException {
        System.out.println("\n\n!!!! Lendo: (" + numeroAssento + "," + letraFileira + ")\n\n");
        
        ArrayList<Assento> resultadoPesquisa;
        Assento template = new Assento(numeroAssento, letraFileira);
        
        LindaCoordinator.LindaSelector newSelector = LindaCoordinator.newSelector(template, Selecting.COUNT_ALL);
        
        resultadoPesquisa = capi.read(cref, newSelector, 0, null);
        
        System.out.println("Resultado da pesquisa: " + resultadoPesquisa);
        
        return resultadoPesquisa;
    }
    
    public ArrayList<Assento> take(Integer numeroAssento, String letraFileira) throws MzsCoreException {
        System.out.println("Taking");
        
        ArrayList<Assento> resultadoPesquisa;
        Assento template = new Assento(numeroAssento, letraFileira);
        
        LindaCoordinator.LindaSelector newSelector = LindaCoordinator.newSelector(template);
        
        resultadoPesquisa = capi.take(cref, newSelector, 0, null);
        return resultadoPesquisa;
    }
    
    public ArrayList<Assento> takeAll(Integer numeroAssento, String letraFileira) throws MzsCoreException {
        ArrayList<Assento> resultadoPesquisa;
        Assento template = new Assento(numeroAssento, letraFileira);
        
        LindaCoordinator.LindaSelector newSelector = LindaCoordinator.newSelector(template, Selecting.COUNT_ALL);
        
        resultadoPesquisa = capi.take(cref, newSelector, 0, null);
        return resultadoPesquisa;
    }

    public static class Assento implements Serializable {

        private final Integer numeroAssento;
        private final String letraFileira;

        public Assento(final Integer numeroAssento, final String letraFileira) {
            this.numeroAssento = numeroAssento;
            this.letraFileira = letraFileira;
        }
        
        public Integer getNumeroAssento() {
            return this.numeroAssento;
        }

        public String getLetraFileira() {
            return this.letraFileira;
        }
        
        @Override
        public String toString() {
            return "Assento: (" + numeroAssento + "," + letraFileira + ")\n\n";
        }
 
        
    }

}
