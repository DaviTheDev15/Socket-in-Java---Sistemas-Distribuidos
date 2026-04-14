package Threads;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import JServer.Servidor;

import static JServer.Servidor.processarRequisicao;

public class ThreadReceber  implements Runnable{
    private final Socket cliente;
    private final BlockingQueue<String> fila;

    public ThreadReceber(Socket cliente, BlockingQueue<String> fila) {
        this.cliente = cliente;
        this.fila = fila;
    }

    @Override
    public void run() {
        try {
            BufferedReader recebeRequisicaoDoCliente = new BufferedReader(
                    new InputStreamReader(cliente.getInputStream()));

            String requisicao;

            while ((requisicao = recebeRequisicaoDoCliente.readLine()) != null) {

                if (requisicao.isEmpty()) continue;

                String resposta = processarRequisicao(requisicao, cliente);
                fila.put(resposta);
            }

        } catch (Exception exception) {
            System.out.println("Cliente desconectado: " + cliente.getInetAddress());
        } finally {
            Servidor.removerCliente(cliente);
        }
    }
}
