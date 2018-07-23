package tuplespace;

import java.io.*;

class Entry {
    private int numero_cadeira;
    private char letra_fileira;

    public Entry(){}

    public Entry(int num_cad, char letra_fil){
        numero_cadeira = num_cad;
        letra_fileira = letra_fil;
    }


    public void setNumeroCadeira(int num_cad) {
        numero_cadeira = num_cad;
    }

    public void setLetraFileira(char letra_fil) {
        letra_fileira = letra_fil;
    }

    public int getNumeroCadeira() {
        return numero_cadeira;
    }

    public char getLetraFileira() {
        return letra_fileira;
    }

    public void mostrar() {
        System.out.println("cadeira: " + numero_cadeira);
        System.out.println("fileira: " + letra_fileira);
    }
}