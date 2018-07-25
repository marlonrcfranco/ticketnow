/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientetuplespace;

import java.util.ArrayList;

import org.mozartspaces.core.MzsCoreException;
/**
 *
 * @author viniciuslucena
 */
public class Main {
    public static void main (String[] args) throws MzsCoreException {
        System.out.println("Iniciando a main");
        ClienteTupleSpace clienteTS;
        clienteTS = new ClienteTupleSpace();
        
        clienteTS.write(10,'A');
        
        ArrayList<Assento> resultado = clienteTS.read(10,'A');
        System.out.println("Assento: " + resultado.get(0).getNumeroAssento() + resultado.get(0).getLetraFileira());
    }
}
