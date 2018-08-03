/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ticketnowws;
import java.io.IOException;
import java.util.ArrayList;
import org.mozartspaces.core.MzsCoreException;
import tuplespace.ClienteTupleSpace;
import tuplespace.SpaceCreator;

/**
 *
 * @author viniciuslucena
 */
public class WSFake {
    
    public static void main(String[] args) throws MzsCoreException, IOException {
        TicketNowWS oTicketNowWS = new TicketNowWS();
        
        //System.out.println("RESPOSTA: " + oTicketNowWS.comprarIngresso(10, "E", "70000000000000000", "2020", "144"));
        //System.out.println("RESPOSTA: " + oTicketNowWS.consultarAssento(10, "B"));
        System.out.println("RESPOSTA: " + oTicketNowWS.consultarTudosAssentos());
    }
    
}
