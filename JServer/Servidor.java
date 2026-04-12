package JServer;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import Helpers.ConversorMoeda;
import Helpers.Dados;

public class Servidor {

    static final int MAX_CLIENTES = 3;
    static List<Socket> clientesConectadosAoServidor = Collections.synchronizedList(new ArrayList<>());
    static Queue<Socket> filaDeEspera = new LinkedList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(12345);
        System.out.println("Servidor rodando...");

        while (true) {
            Socket cliente = aceitarClientes(server);
            validacaoClientesAtivos(cliente);
        }
    }

    public static Socket aceitarClientes(ServerSocket server){
        try{
            Socket cliente = server.accept();
            System.out.println("Nova conexão: " + cliente.getInetAddress());
            return cliente;
        } catch (IOException exception){
            System.out.println("Erro ao aceitar cliente");
            exception.printStackTrace();
            return null;
        }
    }

    public static void validacaoClientesAtivos(Socket cliente){
        try{
            synchronized (Servidor.class) {
                if (clientesConectadosAoServidor.size() < MAX_CLIENTES && verificaIpCliente(cliente)) {
                    ativarCliente(cliente);
                } else {
                    filaDeEspera.add(cliente);
                    PrintWriter retornaSaidaProCliente = new PrintWriter(cliente.getOutputStream(), true);
                    retornaSaidaProCliente.println("Servidor cheio ou IP já ativo!");
                    retornaSaidaProCliente.println("Você está na fila de espera.");
                    System.out.println("Cliente " + cliente.getInetAddress() + " foi para fila.");
                }
            }
        }
        catch (IOException exception){
            System.out.println("Erro ao validar cliente");
            exception.printStackTrace();
        }
    }

    public static boolean verificaIpCliente(Socket novoCliente){
        String IpDoNovoCliente = novoCliente.getInetAddress().getHostAddress();

        synchronized (clientesConectadosAoServidor) {
            for (Socket cliente : clientesConectadosAoServidor) {
                String ipExistente = cliente.getInetAddress().getHostAddress();
                if (ipExistente.equals(IpDoNovoCliente)) {
                    return false;
                }
            }
        return true;
        }
    }

    public static void ativarCliente(Socket cliente) throws IOException {
        clientesConectadosAoServidor.add(cliente);

        PrintWriter retornaSaidaProCliente = new PrintWriter(cliente.getOutputStream(), true);
        retornaSaidaProCliente.println("Bem-vindo! Use: GET /PIADA | /NOTICIA | /SENHA | /MOEDA");

        System.out.println("Cliente ATIVO: " + cliente.getInetAddress());

        BlockingQueue<String> fila = new LinkedBlockingQueue<>();

        new Thread(() -> receber(cliente, fila)).start();
        new Thread(() -> enviar(cliente, fila)).start();
    }

    public static void removerCliente(Socket cliente) {
        synchronized (Servidor.class) {
            clientesConectadosAoServidor.remove(cliente);
            System.out.println("Cliente removido: " + cliente.getInetAddress());

            try {
                cliente.close();
            } catch (IOException ignorar) {}

            if (!filaDeEspera.isEmpty()) {
                Socket proximoCliente = filaDeEspera.poll();
                try {
                    PrintWriter retornaSaidaProCliente = new PrintWriter(proximoCliente.getOutputStream(), true);
                    retornaSaidaProCliente.println("Você saiu da fila! Agora está conectado.");

                    ativarCliente(proximoCliente);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public static void receber(Socket cliente, BlockingQueue<String> fila) {
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
            System.out.println("Cliente desconectado.");
        } finally {
            removerCliente(cliente);
        }
    }

    public static void enviar(Socket cliente, BlockingQueue<String> fila) {
        try {
            PrintWriter retornaSaidaProCliente = new PrintWriter(cliente.getOutputStream(), true);

            while (!cliente.isClosed()) {
                String resposta = fila.take();
                System.out.println("Enviando resposta para " + cliente.getInetAddress());
                System.out.println(resposta);
                retornaSaidaProCliente.println(resposta);
            }

        } catch (Exception exception) {
            System.out.println("Erro ao enviar.");
        }
    }

    public static String processarRequisicao(String requisicao, Socket cliente) {
        String IpDoCliente = cliente.getInetAddress().getHostAddress();

        try {
            String[] partes = requisicao.split(" ");
            if (partes.length < 2) {
                return montarResposta(IpDoCliente, "400 BAD REQUEST", "Requisição inválida");
            }

            String rota = partes[1].toUpperCase();

            switch (rota) {
                case "/PIADA":
                    return montarResposta(IpDoCliente, "200 OK", getPiada());

                case "/NOTICIA":
                    return montarResposta(IpDoCliente, "200 OK", getNoticia());

                case "/SENHA":
                    return montarResposta(IpDoCliente, "200 OK", gerarSenha());

                case "/MOEDA":
                    return montarResposta(IpDoCliente, "200 OK", getConversor());
                
                case "/LISTA":
                    return montarResposta(IpDoCliente, "200 OK", getListaAtivos());

                default:
                    return montarResposta(IpDoCliente, "400 BAD REQUEST", "Comando inválido");
            }

        } catch (Exception exception) {
            return montarResposta(IpDoCliente, "500 INTERNAL ERROR", "Erro no servidor");
        }
    }

    public static String getListaAtivos(){
        StringBuilder listaDeIps = new StringBuilder();

        synchronized (clientesConectadosAoServidor) {
            for (Socket cliente : clientesConectadosAoServidor) {
                listaDeIps.append(cliente.getInetAddress()).append("\n");
            }
        }
        return listaDeIps.toString();
    }

    public static String montarResposta(String ip, String status, String body) {
        return "HTTP/1.1 " + status + "\n" +
                "Client-IP: " + ip + "\n" +
                "Content-Type: text/plain\n\n" +
                body;
    }

    public static String gerarSenha() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int tamanho = ThreadLocalRandom.current().nextInt(8, 12);

        StringBuilder senha = new StringBuilder();
        for (int i = 0; i < tamanho; i++) {
            senha.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return "Senha: " + senha;
    }

    public static String getPiada() {
        return Dados.getPiada();
    }

    public static String getNoticia() {
        return Dados.getNoticia();
    }

    public static String getConversor() throws InterruptedException {
        return ConversorMoeda.getMoeda();
    }
}
