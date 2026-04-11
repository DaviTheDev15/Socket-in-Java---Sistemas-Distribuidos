package JServer;

import java.net.*;
import java.io.*;
import java.util.*;

public class Servidor {

    static final int MAX_CLIENTES = 3;

    static List<Socket> clientesAtivos = Collections.synchronizedList(new ArrayList<>());
    static Queue<Socket> filaEspera = new LinkedList<>();

    // Scanner único (evita conflito)
    static Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(12345);
        System.out.println("Servidor rodando...");

        while (true) {
            Socket cliente = server.accept();
            System.out.println("Nova conexão: " + cliente.getInetAddress());

            synchronized (Servidor.class) {
                if (clientesAtivos.size() < MAX_CLIENTES) {
                    ativarCliente(cliente);
                } else {
                    filaEspera.add(cliente);
                    PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                    out.println("Servidor cheio. Você está na fila de espera.");
                    System.out.println("Cliente IP: " + cliente.getInetAddress() + " foi colocado na fila.");
                }
            }
        }
    }

    // Ativa cliente (entra nos 3 ativos)
    public static void ativarCliente(Socket cliente) throws IOException {
        clientesAtivos.add(cliente);

        PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
        out.println("Bem-vindo! Você está conectado ao servidor.");

        System.out.println("Cliente ATIVO: " + cliente.getInetAddress());

        new Thread(() -> enviar(cliente)).start();
        new Thread(() -> receber(cliente)).start();
    }
    
    public static void removerCliente(Socket socket) {
        synchronized (Servidor.class) {
            clientesAtivos.remove(socket);
            System.out.println("Removido. Ativos agora: " + clientesAtivos.size());

            try {
                socket.close();
            } catch (IOException ignored) {
            }

            // Se tiver alguém na fila, ativa
            if (!filaEspera.isEmpty()) {
                Socket proximo = filaEspera.poll();
                try {
                    ativarCliente(proximo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void receber(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Cliente " + socket.getInetAddress() + ": " + msg);
            }

        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + socket.getInetAddress());
        } finally {
            removerCliente(socket);
        }
    }

    public static void enviar(Socket socket) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String msg = teclado.nextLine();
                out.println(msg);
            }

        } catch (Exception e) {
            System.out.println("Erro ao enviar para cliente.");
        }
    }
}