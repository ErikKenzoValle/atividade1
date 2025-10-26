import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Receptor {
    public static final int PORTA = 12345;

    public static void main(String[] args) {
        System.out.println("[Receptor] Servidor Receptor iniciado. Aguardando conexões na porta " + PORTA + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket clienteSocket = null;
                ObjectInputStream receptor = null;
                ObjectOutputStream transmissor = null;

                try {
                    clienteSocket = serverSocket.accept();
                    System.out.println("[Receptor] Cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());

                    transmissor = new ObjectOutputStream(clienteSocket.getOutputStream());
                    receptor = new ObjectInputStream(clienteSocket.getInputStream());

                    while (true) {
                        Object objRecebido = receptor.readObject();

                        if (objRecebido instanceof Pedido) {
                            Pedido pedido = (Pedido) objRecebido;
                            System.out.println("[Receptor] Pedido recebido para contar o número: " + pedido.getProcurado());

                            int contagem = processarPedidoParalelo(pedido);

                            Resposta resposta = new Resposta(contagem);
                            transmissor.writeObject(resposta);
                            transmissor.flush();
                            System.out.println("[Receptor] Resposta enviada: " + contagem);

                        } else if (objRecebido instanceof ComunicadoEncerramento) {
                            System.out.println("[Receptor] Cliente " + clienteSocket.getInetAddress().getHostAddress() + " encerrou a conexão.");
                            break;
                        }
                    }

                } catch (EOFException e) {
                    System.out.println("[Receptor] Cliente desconectado inesperadamente.");
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("[Receptor] Erro durante a comunicação: " + e.getMessage());
                } finally {
                    try {
                        if (receptor != null) receptor.close();
                        if (transmissor != null) transmissor.close();
                        if (clienteSocket != null) clienteSocket.close();
                    } catch (IOException e) {
                        System.err.println("[Receptor] Erro ao fechar recursos: " + e.getMessage());
                    }
                    System.out.println("\n[Receptor] Aguardando nova conexão...");
                }
            }
        } catch (IOException e) {
            System.err.println("[Receptor] Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private static int processarPedidoParalelo(Pedido pedido) {
        byte[] numeros = pedido.getNumeros();
        byte procurado = pedido.getProcurado();
        int contagemTotal = 0;

        int quantidade = Runtime.getRuntime().availableProcessors();

        if (quantidade > numeros.length) {
            quantidade = numeros.length;
        }

        if (quantidade == 0) {
            quantidade = 1; // Garantir ao menos uma thread
        }

        List<ContagemThread> threads = new ArrayList<>();
        int tamanhoChunk = (int) Math.ceil((double) numeros.length / quantidade);

        for (int i = 0; i < quantidade; i++) {
            int inicio = i * tamanhoChunk;
            int fim = Math.min(inicio + tamanhoChunk, numeros.length);

            if (inicio >= fim) break; // Evitar chunks vazios se a divisão for estranha

            byte[] chunk = Arrays.copyOfRange(numeros, inicio, fim);
            ContagemThread t = new ContagemThread(chunk, procurado);
            threads.add(t);
            t.start();
        }

        try {
            for (ContagemThread t : threads) {
                t.join();
                contagemTotal += t.getResultadoParcial();
            }
        } catch (InterruptedException e) {
            System.err.println("[Receptor] Thread de contagem interrompida: " + e.getMessage());
        }

        return contagemTotal;
    }
}