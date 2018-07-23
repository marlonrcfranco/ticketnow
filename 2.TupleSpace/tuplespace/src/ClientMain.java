package tuplespace;

class ClientMain {
    public static void main(String args[]) {

        Entry e = new Entry(10, 'B');
        e.mostrar();
     
        ClienteTupleSpace client = new ClienteTupleSpace("127.0.0.1", 9002);

    }
}