import java.io.*;
import java.net.*;

/**
 * Thread auxiliar para o ProgramaD.
 * Cada instância desta classe se conecta a um servidor R,
 * envia um Pedido, recebe uma Resposta e, em seguida,
 * envia um ComunicadoDeEncerramento.
 */
public class ClienteThread extends Thread {
    private String ipServidor;
    private int porta;
    private Pedido pedido;
    private int resultadoContagem;

    public ClienteThread(String ipServidor, int porta, Pedido pedido) {
        this.ipServidor = ipServidor;
        this.porta = porta;
        this.pedido = pedido;
        this.resultadoContagem = 0; // Inicia com 0
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(this.ipServidor, this.porta);
             ObjectOutputStream transmissor = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream receptor = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("[D] Conectado ao servidor " + ipServidor + ". Enviando pedido...");

            // 4. Envia o Pedido [cite: 54]
            transmissor.writeObject(this.pedido);
            transmissor.flush();

            // 5. Recebe a Resposta [cite: 55]
            Object objRecebido = receptor.readObject();
            if (objRecebido instanceof Resposta) {
                this.resultadoContagem = ((Resposta) objRecebido).getContagem();
                System.out.println("[D] Resposta recebida de " + ipServidor + ": " + this.resultadoContagem);
            } else {
                System.err.println("[D] Resposta inesperada de " + ipServidor + ": " + objRecebido.getClass().getName());
            }

            // 6. Envia Comunicado de Encerramento [cite: 58]
            transmissor.writeObject(new ComunicadoEncerramento());
            transmissor.flush();
            System.out.println("[D] Conexão com " + ipServidor + " encerrada.");

        } catch (UnknownHostException e) {
            System.err.println("[D] Servidor não encontrado: " + ipServidor);
        } catch (IOException e) {
            System.err.println("[D] Erro de I/O com " + ipServidor + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("[D] Erro: Classe da resposta não encontrada.");
        } catch (Exception e) { // [cite: 60]
            System.err.println("[D] Erro inesperado na thread para " + ipServidor + ": " + e.getMessage());
        }
    }

    public int getResultadoContagem() {
        return this.resultadoContagem;
    }
}