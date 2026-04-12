package Helpers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.*;

public class Dados {

    private static List<String> listaDePiadas = new ArrayList<>();
    private static List<String> listaDeNoticias = new ArrayList<>();
    private static Random numeroAleatorio = new Random();

    static {
        try {
            String arquivoJson = new String(Files.readAllBytes(Paths.get("Dados.json")));

            String blocoPiadas = arquivoJson.split("\"piadas\"\\s*:\\s*\\[")[1].split("]")[0];
            for (String piada : blocoPiadas.split(",")) {
                listaDePiadas.add(piada.replace("\"", "").trim());
            }

            String blocoNoticias = arquivoJson.split("\"noticias\"\\s*:\\s*\\[")[1].split("]")[0];
            for (String noticia : blocoNoticias.split(",")) {
                listaDeNoticias.add(noticia.replace("\"", "").trim());
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static String getPiada() {
        return listaDePiadas.get(numeroAleatorio.nextInt(listaDePiadas.size()));
    }

    public static String getNoticia() {
        return listaDeNoticias.get(numeroAleatorio.nextInt(listaDeNoticias.size()));
    }
}