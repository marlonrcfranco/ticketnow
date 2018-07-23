package tuplespace;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

public class HandleThread implements Runnable{
    protected Socket clntSocket = null;
    protected String txtFrmSrvr   = null;

    public HandleThread(Socket clntSocket, String txtFrmSrvr) {
        this.clntSocket = clntSocket;
        this.txtFrmSrvr   = txtFrmSrvr;
        System.out.println("criei um handle para o cliente");
    }
    public void run() {
        try {
            System.out.println("executando o handle do cliente");

            espaco.add(new Entry(20,'C'));
            /*
            InputStream inputstrm  = clntSocket.getInputStream();
            OutputStream outputstrm = clntSocket.getOutputStream();
            long timetaken = System.currentTimeMillis();
            outputstrm.write(("OK\n\nWrkrRunnable: " + this.txtFrmSrvr + " - " +timetaken +"").getBytes());
            outputstrm.close();
            inputstrm.close();
            System.out.println("Your request has processed in time : " + timetaken);
            */
        } catch (IOException e) {           
            e.printStackTrace();
        }
    }

    private void write(Entry e) {

    }
}