package JClient;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    public static final String RESET = "\u001B[0m";
    public static final String VERDE = "\u001B[32m";
    public static final String AZUL = "\u001B[34m";
    public static final String AMARELO = "\u001B[33m";
    public static final String VERMELHO = "\u001B[31m";

    private static volatile boolean esperandoResposta = false;

    public static void main(String[] args) {
        try {
            Socket servidorSocket = new Socket("localhost", 12345);

            System.out.println(AMARELO + "Conectado ao servidor!" + RESET);

            new Thread(() -> receber(servidorSocket)).start();
            enviar(servidorSocket);

        } catch (Exception e) {
            System.out.println(VERMELHO + "Erro ao conectar." + RESET);
        }
    }

    public static void enviar(Socket servidor) {
        try {
            PrintWriter retornaSaidaProServidor = new PrintWriter(servidor.getOutputStream(), true);
            try (Scanner teclado = new Scanner(System.in)) {
                while (true) {
                    if (esperandoResposta) {
                        Thread.sleep(100);
                        continue;
                    }
                    System.out.println("\n========================");
                    System.out.println("Escolha uma opção:");
                    System.out.println("1 - Piada");
                    System.out.println("2 - Notícia");
                    System.out.println("3 - Senha");
                    System.out.println("4 - Moeda");
                    System.out.println("0 - Sair");
                    System.out.print(">> ");

                    String pergunta = teclado.nextLine();
                    String comando;

                    switch (pergunta) {
                        case "1":
                            comando = "GET /PIADA";
                            break;
                        case "2":
                            comando = "GET /NOTICIA";
                            break;
                        case "3":
                            comando = "GET /SENHA";
                            break;
                        case "4":
                            comando = "GET /MOEDA";
                            break;
                        case "5":
                            comando = "GET /LISTA";
                            break;
                        case "0":
                            System.out.println("Encerrando...");
                            servidor.close();
                            return;
                        default:
                            System.out.println(VERMELHO + "Opção inválida!" + RESET);
                            continue;
                    }

                    esperandoResposta = true;
                    retornaSaidaProServidor.println(comando);
                }
            }

        } catch (Exception exception) {
            System.out.println(VERMELHO + "Erro ao enviar." + RESET);
        }
        
    }

    public static void receber(Socket servidor) {
        try {
            BufferedReader recebeRespostaDoServidor = new BufferedReader(
                    new InputStreamReader(servidor.getInputStream()));

            String resposta;
            boolean corpo = false;

            while ((resposta = recebeRespostaDoServidor.readLine()) != null) {

                if (resposta.contains("IP já está conectado. Conexão recusada")) {
                    System.out.println(VERMELHO + resposta + RESET);
                    servidor.close();
                    System.exit(0);
                }

                if (resposta.isEmpty()) {
                    corpo = true;
                    System.out.println();
                    continue;
                }

                if (corpo) {
                    System.out.println(VERDE + resposta + RESET);

                    esperandoResposta = false;
                    corpo = false;

                } else {
                    System.out.println(AZUL + resposta + RESET);
                }
            }

        } catch (Exception exception) {
            System.out.println(VERMELHO + "Conexão encerrada." + RESET);
        }
    }
}
