import java.io.*;
import java.net.*;

public class ClienteThread extends Thread {
    private String ipServidor;
    private int porta;
    private Pedido pedido;
    private int resultadoContagem;

    public ClienteThread(String ipServidor, int porta, Pedido pedido) {
        this.ipServidor = ipServidor;
        this.porta = porta;
        this.pedido = pedido;
        this.resultadoContagem = 0;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(this.ipServidor, this.porta);
             ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream receptor = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("[D] Conectado ao servidor " + ipServidor + ". Enviando pedido...");

            transmissor.writeObject(this.pedido);
            transmissor.flush();

            Object objRecebido = receptor.readObject();
            if (objRecebido instanceof Resposta) {
                this.resultadoContagem = ((Resposta) objRecebido).getContagem();
                System.out.println("[D] Resposta recebida de " + ipServidor + ": " + this.resultadoContagem);
            } else {
                System.err.println("[D] Resposta inesperada de " + ipServidor + ": " + objRecebido.getClass().getName());
            }

            transmissor.writeObject(new ComunicadoEncerramento());
            transmissor.flush();
            System.out.println("[D] Conexão com " + ipServidor + " encerrada.");

        } catch (UnknownHostException e) {
            System.err.println("[D] Servidor não encontrado: " + ipServidor);
        } catch (IOException e) {
            System.err.println("[D] Erro de I/O com " + ipServidor + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("[D] Erro: Classe da resposta não encontrada.");
        } catch (Exception e) {
            System.err.println("[D] Erro inesperado na thread para " + ipServidor + ": " + e.getMessage());
        }
    }

    public int getResultadoContagem() {
        return this.resultadoContagem;
    }
}