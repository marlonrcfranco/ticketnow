/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientetuplespace;

import java.util.Arrays;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import static org.mozartspaces.core.MzsConstants.Container.UNBOUNDED;
import org.mozartspaces.core.MzsCoreException;

/**
 *
 * @author viniciuslucena
 */
public class TupleSpaceCreator {
    private static DefaultMzsCore core;
    private static Capi capi;
    private static ContainerReference container;

    public static void main (String[] args) throws MzsCoreException {
        String nomeContainer = "TupleSpaceSD";
        int serverPort = 9999;
        
        init(nomeContainer, serverPort);
    }
    
    public static void init(String nomeContainer, int serverPort) throws MzsCoreException {
        System.out.println("Inicializando o Espaco");
        TupleSpaceCreator.core = DefaultMzsCore.newInstance(serverPort);
        TupleSpaceCreator.capi = new Capi(core);
        //this.container = capi.createContainer(null, Arrays.asList(new LindaCoordinator(false)), null);
        TupleSpaceCreator.container = capi.createContainer(nomeContainer, null, UNBOUNDED, null, new LindaCoordinator(false));
    }
}
