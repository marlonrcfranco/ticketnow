import java.rmi.*;
import java.rmi.server.*;

public class Verificador extends UnicastRemoteObject implements iVerificador {
    private String message; // Strings are serializable
    public Verificador() throws RemoteException {
    }
    public String ValidaCartao(String CartaoCodigoCompleto) throws RemoteException {
        if(CartaoCodigoCompleto.equals("1")){
            return "Validado com sucesso!";
        }
        else{
            return "Cartão inválido.";
        }
    }
}