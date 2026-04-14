package Helpers;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import Threads.ThreadEnviar;
import Threads.ThreadReceber;

public class ClientHandler {
    private final Socket socket;
    private final BlockingQueue<String> fila;
    private Thread receberThread;
    private Thread enviarThread;

    public ClientHandler(Socket socket){
        this.socket = socket;
        this.fila = new LinkedBlockingDeque<>();
    }

    public void start(){
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Bem-vindo! Use: GET /PIADA | /NOTICIA | /SENHA | /MOEDA");

        } catch (Exception e) {
            e.printStackTrace();
        }

        receberThread = new Thread(new ThreadReceber(socket, fila));
        enviarThread = new Thread(new ThreadEnviar(socket, fila));

        receberThread.start();
        enviarThread.start();

    }

    public Socket getSocket(){
        return this.socket;
    }
}
