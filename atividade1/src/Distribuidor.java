import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Distribuidor {

    private static final String[] IPS_SERVIDORES = {"127.0.0.1"};
    private static final int PORTA_PADRAO = 12345;
    private static final Random random = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Digite o tamanho do vetor grande: ");
            int tamanhoVetor = scanner.nextInt();

            System.out.println("Gerando vetor com " + tamanhoVetor + " elementos...");
            byte[] vetorGrande = gerarVetorAleatorio(tamanhoVetor);

            System.out.print("Deseja exibir o vetor gerado? (s/n): ");
            if (scanner.next().equalsIgnoreCase("s")) {
                if (tamanhoVetor > 1000) {
                    System.out.println("(Exibindo apenas os primeiros 1000 elementos)");
                    System.out.println(Arrays.toString(Arrays.copyOfRange(vetorGrande, 0, Math.min(tamanhoVetor, 1000))));
                } else {
                    System.out.println(Arrays.toString(vetorGrande));
                }
            }

            byte numeroProcurado;
            System.out.println("Escolha o número a procurar:");
            System.out.println("1. Um número aleatório do próprio vetor");
            System.out.println("2. O número 111 (para testar 0 ocorrências) [cite: 66]");
            System.out.print("Opção: ");
            int opcao = scanner.nextInt();

            if (opcao == 1) {
                numeroProcurado = vetorGrande[random.nextInt(tamanhoVetor)];
            } else {
                numeroProcurado = 111;
            }
            System.out.println("Número a ser procurado: " + numeroProcurado);

            executarContagemSequencial(vetorGrande, numeroProcurado);

            executarContagemDistribuida(vetorGrande, numeroProcurado);

        } catch (Exception e) {
            System.err.println("[D] Erro fatal no Distribuidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    /**
     * Gera um vetor de bytes com valores aleatórios entre -100 e 100. [cite: 3]
     */
    private static byte[] gerarVetorAleatorio(int tamanho) {
        byte[] vetor = new byte[tamanho];
        for (int i = 0; i < tamanho; i++) {
            vetor[i] = (byte) (random.nextInt(201) - 100);
        }
        return vetor;
    }

    /**
     * Executa a contagem de forma sequencial (local) para fins de comparação. [cite: 68]
     */
    private static void executarContagemSequencial(byte[] vetor, byte procurado) {
        System.out.println("\n--- Iniciando Contagem Sequencial (Local) ---");
        long inicio = System.currentTimeMillis(); // [cite: 92]

        int contagem = 0;
        for (byte b : vetor) {
            if (b == procurado) {
                contagem++;
            }
        }

        long fim = System.currentTimeMillis();
        double tempo = (fim - inicio) / 1000.0;

        System.out.println("[SEQUENCIAL] Resultado: " + contagem);
        System.out.printf("[SEQUENCIAL] Tempo de execução: %.4f segundos%n", tempo);
    }

    private static void executarContagemDistribuida(byte[] vetor, byte procurado) {
        System.out.println("\n--- Iniciando Contagem Distribuída ---");
        long inicio = System.currentTimeMillis(); // [cite: 92]

        int numServidores = IPS_SERVIDORES.length;
        List<ClienteThread> threads = new ArrayList<>();

        // 3. Divida o vetor em partes de tamanhos semelhantes [cite: 51]
        int tamanhoChunk = (int) Math.ceil((double) vetor.length / numServidores);

        for (int i = 0; i < numServidores; i++) {
            int inicioChunk = i * tamanhoChunk;
            int fimChunk = Math.min(inicioChunk + tamanhoChunk, vetor.length);

            if (inicioChunk >= fimChunk) break; // Evita chunks vazios

            byte[] chunk = Arrays.copyOfRange(vetor, inicioChunk, fimChunk);
            Pedido pedido = new Pedido(chunk, procurado); // [cite: 27]

            // 4. Crie uma thread para cada servidor 
            ClienteThread t = new ClienteThread(IPS_SERVIDORES[i], PORTA_PADRAO, pedido);
            threads.add(t);
            t.start();
        }

        // 5. Após o término de todas as threads... [cite: 56]
        int contagemTotal = 0;
        try {
            for (ClienteThread t : threads) {
                t.join(); // [cite: 61]
                contagemTotal += t.getResultadoContagem();
            }
        } catch (InterruptedException e) {
            System.err.println("[D] Thread principal interrompida: " + e.getMessage());
        }

        long fim = System.currentTimeMillis(); // [cite: 113]
        double tempo = (fim - inicio) / 1000.0; // [cite: 118]

        // ...componha e exiba a resposta final. [cite: 56]
        System.out.println("[DISTRIBUÍDO] Resultado Final: " + contagemTotal);
        System.out.printf("[DISTRIBUÍDO] Tempo de execução: %.4f segundos%n", tempo);
    }
}