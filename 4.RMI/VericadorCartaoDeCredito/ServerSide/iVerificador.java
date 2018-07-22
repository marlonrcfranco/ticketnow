import java.rmi.*;

public interface iVerificador extends Remote {
    public String ValidaCartao(String CartaoCodigoCompleto) throws RemoteException;
}