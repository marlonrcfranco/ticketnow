import java.rmi.*;
class HelloServer {
    public static void main (String[] argv) {
        try {
            Naming.rebind("rmi://localhost/HelloServer", new Hello("Hello, world!"));
        }
        catch (Exception e) { 

        }
    }
}