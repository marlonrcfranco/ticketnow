import java.rmi.*;

class Server {
    public static void main (String[] argv) {
        try {
            iVerificador oVerificador = new Verificador();
            Naming.rebind("rmi://localhost/Verificador", oVerificador);
        }
        catch (Exception e) {
        }
    }
}