import socket
import threading
import time

RESET = "\033[0m"
VERDE = "\033[32m"
AZUL = "\033[34m"
AMARELO = "\033[33m"
VERMELHO = "\033[31m"

esperando_resposta = False

def receber(servidor):
    global esperando_resposta
    try:
        while True:
            resposta = servidor.recv(1024).decode()

            if not resposta:
                break

            linhas = resposta.split("\n")
            corpo = False

            for linha in linhas:
                linha = linha.strip()

                if "IP já está conectado. Conexão recusada" in linha:
                    print(VERMELHO + linha + RESET)
                    servidor.close()
                    exit()

                if linha == "":
                    corpo = True
                    print()
                    continue

                if corpo:
                    print(VERDE + linha + RESET)
                    esperando_resposta = False
                    corpo = False
                else:
                    print(AZUL + linha + RESET)

    except:
        print(VERMELHO + "Conexão encerrada." + RESET)

def enviar(servidor):
    global esperando_resposta

    try:
        while True:
            if esperando_resposta:
                time.sleep(0.1)
                continue

            print("\n========================")
            print("Escolha uma opção:")
            print("1 - Piada")
            print("2 - Notícia")
            print("3 - Senha")
            print("4 - Moeda")
            print("0 - Sair")
            print(">> ", end="")

            pergunta = input()

            if pergunta == "1":
                comando = "GET /PIADA"
            elif pergunta == "2":
                comando = "GET /NOTICIA"
            elif pergunta == "3":
                comando = "GET /SENHA"
            elif pergunta == "4":
                comando = "GET /MOEDA"
            elif pergunta == "5":
                comando = "GET /LISTA"
            elif pergunta == "0":
                print("Encerrando...")
                servidor.close()
                return
            else:
                print(VERMELHO + "Opção inválida!" + RESET)
                continue
            esperando_resposta = True
            servidor.sendall((comando + "\n").encode())

    except:
        print(VERMELHO + "Erro ao enviar." + RESET)

def main():
    try:
        servidor = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        servidor.connect(("localhost", 12345))

        print(AMARELO + "Conectado ao servidor!" + RESET)

        thread_receber = threading.Thread(target=receber, args=(servidor,))
        thread_receber.start()

        enviar(servidor)

    except:
        print(VERMELHO + "Erro ao conectar." + RESET)
        
if __name__ == "__main__":
    main()