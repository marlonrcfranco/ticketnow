/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tuplespace;

import org.mozartspaces.core.MzsCoreException;

/**
 *
 * @author viniciuslucena
 */
public class WSFake {
    
    public static void main(String [] args) throws MzsCoreException {
        ClienteTupleSpace clienteTS = new ClienteTupleSpace("admin", "localhost", 56002);
        
        clienteTS.readAll(null, null);
        clienteTS.readAll(10, null);
        clienteTS.readAll(20, null);
        clienteTS.readAll(null, "A");

 
        ClienteTupleSpace.encerrar();
        
    }
    
}
