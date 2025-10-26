public class ContagemThread extends Thread {
    private byte[] numerosChunk;
    private byte procurado;
    private int resultadoParcial;

    public ContagemThread(byte[] numerosChunk, byte procurado) {
        this.numerosChunk = numerosChunk;
        this.procurado = procurado;
        this.resultadoParcial = 0;
    }

    @Override
    public void run() {
        for (byte b : numerosChunk) {
            if (b == procurado) {
                resultadoParcial++;
            }
        }
    }

    public int getResultadoParcial() {
        return this.resultadoParcial;
    }
}