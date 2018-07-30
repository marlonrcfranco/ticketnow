/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservicefake;

import clientetuplespace.ClienteTupleSpace;
import clientetuplespace.SpaceCreator;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.mozartspaces.core.MzsCoreException;

/**
 *
 * @author viniciuslucena
 */
public class WebServiceFake {


    public static void main(String[] args) throws URISyntaxException, MzsCoreException {
        ClienteTupleSpace clienteTS = new ClienteTupleSpace("aline", "localhost", 10000);
        
        clienteTS.write(10, "A");
        clienteTS.write(10, "B");
        clienteTS.write(10, "C");
        clienteTS.write(10, "D");
        
        
        ClienteTupleSpace.encerrar();
    }

    
    
}
