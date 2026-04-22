package Helpers;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class GetMyIp {

    public static InetAddress retornaMeuIpReal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            InetAddress fallback = null;

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;

                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ip = ips.nextElement();

                    if (ip instanceof Inet4Address && !ip.isLoopbackAddress()) {

                        String endereco = ip.getHostAddress();

                        // ✅ prioridade: redes locais válidas
                        if (endereco.startsWith("192.168.") ||
                            endereco.startsWith("10.") ||
                            (endereco.startsWith("172.") && is172Valido(endereco))) {
                            return ip;
                        }

                        // guarda como fallback (caso não ache nenhum melhor)
                        fallback = ip;
                    }
                }
            }

            if (fallback != null) return fallback;

            throw new RuntimeException("Nenhum IP válido encontrado");

        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter IP", e);
        }
    }

    // valida faixa 172.16.x.x até 172.31.x.x
    private static boolean is172Valido(String ip) {
        try {
            int segundo = Integer.parseInt(ip.split("\\.")[1]);
            return segundo >= 16 && segundo <= 31;
        } catch (Exception e) {
            return false;
        }
    }

    // 🔍 Debug simples
    public static void debugIps() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                if (!ni.isUp() || ni.isLoopback()) continue;

                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ip = ips.nextElement();

                    if (ip instanceof Inet4Address && !ip.isLoopbackAddress()) {
                        System.out.println(ip.getHostAddress());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}