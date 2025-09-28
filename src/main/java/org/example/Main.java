package org.example;

// Importação Correta: Usaremos 'out' para a saída
import static java.lang.System.out;

// A importação 'import java.io.IOException;' é mantida, mas não está sendo usada no corpo.

public class Main {
    // 1. O método main DEVE ser público, estático e receber String[] args.
    public static void main(String[] args) {

        // 2. CORREÇÃO: Usa String.format para criar a string, e passa para o método out.println()
        out.println(String.format("Hello and welcome!"));

        for (int i = 1; i <= 5; i++) {
            // 3. CORREÇÃO: Usa o objeto 'out' importado estaticamente para chamar o método println()
            out.println("i = " + i);
        }
    }
}