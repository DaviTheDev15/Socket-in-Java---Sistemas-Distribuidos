package JServer;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import Helpers.ClientHandler;
import Helpers.ConversorMoeda;
import Helpers.TratamentoDeDados;
import Helpers.GetMyIp;
import JClient.Cliente;

public class Servidor {

    static final int MAX_CLIENTES = 1;
    static List<ClientHandler> clientesConectadosAoServidor = Collections.synchronizedList(new ArrayList<>());
    static Queue<Socket> filaDeEspera = new LinkedList<>();
    static final InetAddress meuIp = GetMyIp.retornaMeuIpReal();
    static final int porta = 3001;

    public static void main(String[] args) throws IOException {
        GetMyIp.debugIps();
        ServerSocket server = new ServerSocket(porta);
        System.out.println("Servidor rodando no IP: " + meuIp + " na porta " + porta);

        while (true) {
            Socket cliente = aceitarClientes(server);

            if (cliente != null) {
                validacaoClientesAtivos(cliente);
            }
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
                if (!verificaIpCliente(cliente)){
                    PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                    out.println("IP já está conectado. Conexão recusada");
                    cliente.close();
                    return;
                }
                if (clientesConectadosAoServidor.size() < MAX_CLIENTES) {
                    ativarCliente(cliente);
                } else {
                    filaDeEspera.add(cliente);
                    PrintWriter retornaSaidaProCliente = new PrintWriter(cliente.getOutputStream(), true);
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
            for (ClientHandler cliente : clientesConectadosAoServidor) {
                String ipExistente = cliente.getSocket().getInetAddress().getHostAddress();

                if (ipExistente.equals(IpDoNovoCliente)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void ativarCliente(Socket cliente) throws IOException {
        ClientHandler handler = new ClientHandler(cliente);
        clientesConectadosAoServidor.add(handler);

        handler.start();

        System.out.println("Cliente ATIVO: " + cliente.getInetAddress());
    }

    public static void removerCliente(Socket cliente) {
        synchronized (Servidor.class) {
            clientesConectadosAoServidor.removeIf(h -> h.getSocket().equals(cliente));

            System.out.println("Cliente removido: " + cliente.getInetAddress());
            try {
                cliente.close();
            } catch (IOException ignorar) {}

            if (!filaDeEspera.isEmpty()) {
                Socket proximoCliente = filaDeEspera.poll();
                try {
                    PrintWriter retornaSaidaProCliente = new PrintWriter(proximoCliente.getOutputStream(), true);
                    retornaSaidaProCliente.println("Você saiu da fila! Agora está conectado.");
                    System.out.println("Ativando cliente da fila: " + proximoCliente.getInetAddress());
                    ativarCliente(proximoCliente);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
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
            for (ClientHandler handler : clientesConectadosAoServidor) {
                listaDeIps.append(handler.getSocket().getInetAddress().getHostAddress()).append("\n");
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
        return TratamentoDeDados.getPiada();
    }

    public static String getNoticia() {
        return TratamentoDeDados.getNoticia();
    }

    public static String getConversor() throws InterruptedException {
        return ConversorMoeda.getMoeda();
    }
}
