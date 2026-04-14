package Helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConversorMoeda {

    public static String getMoeda() {
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL("https://api.exchangerate-api.com/v4/latest/BRL");
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestMethod("GET");

            BufferedReader recebePerguntaDoServidor = new BufferedReader(
                    new InputStreamReader(conexao.getInputStream()));

            String pergunta;
            StringBuilder resposta = new StringBuilder();

            while ((pergunta = recebePerguntaDoServidor.readLine()) != null) {
                resposta.append(pergunta);
            }
            recebePerguntaDoServidor.close();

            String json = resposta.toString();

            String usd = json.split("\"USD\":")[1].split(",")[0];
            String eur = json.split("\"EUR\":")[1].split(",")[0];
            String inr = json.split("\"INR\":")[1].split(",")[0];

            return "BRL -> USD: " + usd + " | EUR: " + eur + " | INR: " + inr;

        } catch (Exception exception) {
            return "Erro ao obter cotação.";
        }
    }
}
