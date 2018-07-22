import java.rmi.*;
class HelloClient {
    public static void main (String[] args) {
        HelloInterface hello;
        String name = "rmi://localhost/HelloServer";
        String text;
        try {
            hello = (HelloInterface)Naming.lookup(name);
            text = hello.sayHello();
            System.out.println(text);
        } catch (Exception e) {
            System.out.println("HelloClient exception:"+e);
        }
    }
}