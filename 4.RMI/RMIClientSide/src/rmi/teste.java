package rmi;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import gui.apPrincipalClient;

public class teste {
	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		ClientRMI oClientRMI;
		System.out.println("Primeira instancia:");
		oClientRMI = ClientRMI.getInstancia();
		System.out.println("Segunda instancia:");
		oClientRMI = ClientRMI.getInstancia();

		System.out.println("Terceiro passo.");

	}

}
