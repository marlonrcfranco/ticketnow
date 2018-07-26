/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientetuplespace;

import java.io.Serializable;

/**
 *
 * @author viniciuslucena
 */
public class Assento implements Serializable {
    private final int numeroAssento;
    private final char letraFileira;

    public Assento(int numeroAssento, char letraFileira) {
        this.numeroAssento = numeroAssento;
        this.letraFileira = letraFileira;
    }
    
    public int getNumeroAssento() {
        return this.numeroAssento;
    }
    
    public char getLetraFileira() {
        return this.letraFileira;
    }
    
    
}
