/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory
 * Copyright 2010 Space Based Computing Group. All rights reserved.
 * Use is subject to license terms.
 */

package clientetuplespace;

import java.io.Serializable;
import java.util.ArrayList;
import java.lang.String;
import java.util.Arrays;
import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.Selector;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;


/**
 * A simple "Hello, space!" example with the MozartSpaces core. First a core
 * instance with an embedded space is created and a container in that space,
 * then an entry is written into that container and read afterwards, before the
 * container is destroyed and the core is shut down.
 */
public class ClienteTupleSpacesource {
    private MzsCore core;
    private Capi capi;
    private ContainerReference container;
    static final int TIMEOUT = 86400000;

    public ClienteTupleSpacesource() throws MzsCoreException  {
        String nomeContainer = "TupleSpaceSD";
        procurarContainer(nomeContainer);
    }
    
    public void procurarContainer(String nomeContainer) throws MzsCoreException {
        int portaCliente = 9000;
        
        System.out.println("Tentado se conectar ao Espaco");
        this.core = DefaultMzsCore.newInstance(portaCliente);
        this.capi = new Capi(core);
        this.container = capi.lookupContainer(nomeContainer);
        System.out.println("Conectado ao Espaco");
        //this.container = capi.createContainer(null, Arrays.asList(new LindaCoordinator(false)), null);
    }
    
    public void write(int numeroAssento, char letraFileira) throws MzsCoreException {
        Assento oAssento = new Assento(numeroAssento, letraFileira);
        capi.write(container, new Entry((Serializable) oAssento));
    }
    
    public ArrayList<Assento> read(int numeroAssento, char letraFileira) throws MzsCoreException {
        ArrayList<Assento> resultadoPesquisa;
        Assento template = new Assento(numeroAssento, letraFileira);
        
        LindaCoordinator.LindaSelector newSelector = LindaCoordinator.newSelector(template);
        
        resultadoPesquisa = capi.read(container, newSelector, TIMEOUT, null);
        return resultadoPesquisa;
    }
    

    public ArrayList<Assento> take(int numeroAssento, char letraFileira) throws MzsCoreException {
        ArrayList<Assento> resultadoPesquisa;
        Assento template = new Assento(numeroAssento, letraFileira);
        
        LindaCoordinator.LindaSelector newSelector = LindaCoordinator.newSelector(template);
        
        resultadoPesquisa = capi.take(container, newSelector, TIMEOUT, null);
        return resultadoPesquisa;
    }
}
