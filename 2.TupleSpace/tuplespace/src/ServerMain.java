package tuplespace;

class ServerMain {
    public static void main(String args[]) {
        int port = 9002;
        System.out.println("Servidor executando na porta " + port);
        TupleSpace ts = new TupleSpace(port);
        
    }
}