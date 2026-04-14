package Threads;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ThreadEnviar implements Runnable{
    private final Socket cliente;
    private final BlockingQueue<String> fila;

    public ThreadEnviar(Socket cliente, BlockingQueue<String> fila){
        this.cliente = cliente;
        this.fila = fila;
    }

    @Override
    public void run(){
        try {
            PrintWriter retornaSaidaProCliente = new PrintWriter(cliente.getOutputStream(), true);

            while (!cliente.isClosed()) {
                String resposta = fila.take();
                System.out.println("Enviando resposta para " + cliente.getInetAddress());
                System.out.println(resposta);
                retornaSaidaProCliente.println(resposta);
            }

        } catch (Exception exception) {
            System.out.println("Erro ao enviar para: " + cliente.getInetAddress());
        }
    }
}
