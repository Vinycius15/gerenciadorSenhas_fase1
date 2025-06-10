// Local: src/main/java/util/CriptoUtil.java

package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class CriptoUtil {

    // Comprimento do Salt que será armazenado no DB (CHAR(32))
    private static final int SALT_LENGTH = 32;

    /**
     * Cria um salt aleatório criptograficamente seguro.
     * O salt é armazenado no banco (tabela USUARIO).
     * @return Uma String de salt (32 caracteres).
     */
    public static String gerarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[24]; // 24 bytes para garantir 32 caracteres em Base64
        random.nextBytes(salt);

        // Retorna o salt codificado em Base64 e limita ao tamanho CHAR(32) do DB
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        return encodedSalt.substring(0, SALT_LENGTH);
    }

    /**
     * Gera o hash da senha mestra usando SHA-256 e o salt.
     * Na prática, usaria BCrypt ou Argon2, mas SHA-256 atende ao requisito acadêmico.
     * @param senha A senha em texto plano.
     * @param salt O salt único para o usuário.
     * @return O hash final da senha em formato hexadecimal (CHAR(64) no DB).
     */
    public static String gerarHashMestre(String senha, String salt) {
        try {
            // Concatena senha e salt para aumentar a segurança do hash
            String input = senha + salt;
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Gera o hash
            byte[] hashBytes = md.digest(input.getBytes());

            // Converte o array de bytes para uma String hexadecimal de 64 caracteres
            StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString(); // Exatamente 64 caracteres

        } catch (NoSuchAlgorithmException e) {
            // Lança exceção em tempo de execução se o algoritmo não for suportado
            throw new RuntimeException("Algoritmo de Hash SHA-256 não encontrado.", e);
        }
    }

}