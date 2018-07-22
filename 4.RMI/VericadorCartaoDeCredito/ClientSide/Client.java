import java.rmi.*;
class Client {
    public static void main (String[] args) {
        iVerificador oVerificador;
        String name = "rmi://localhost/Verificador";
        String retorno;
        String CodCartao = (args.length < 1 ? "": args[0]);
        try {
            oVerificador = (iVerificador)Naming.lookup(name);
            retorno = oVerificador.ValidaCartao(CodCartao);
            System.out.println(retorno);
        } catch (Exception e) {
            System.out.println("Verificador exception:"+e);
        }
    }
}