/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientetuplespace;

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
public class SpaceCreator {
    
   
    public static void main(String [] args) throws MzsCoreException {
        init();
    }
    
    
    public static void init() throws MzsCoreException {
        int serverPort = 18000;
        String nomeContainer = "admin";
        
        DefaultMzsCore core = DefaultMzsCore.newInstance(serverPort);
        Capi capi = new Capi(core);
        
        ContainerReference cref = capi.createContainer(nomeContainer, null, UNBOUNDED, null, new LindaCoordinator(false) );
    }

  
        
    public SpaceCreator(String nomeContainer, int portaServidor) throws MzsCoreException {
                
        DefaultMzsCore core = DefaultMzsCore.newInstance(portaServidor);
        Capi capi = new Capi(core);
        
        ContainerReference cref = capi.createContainer(nomeContainer, null, UNBOUNDED, null, new LindaCoordinator(false) );
        
    }

    
    
    
    
}
