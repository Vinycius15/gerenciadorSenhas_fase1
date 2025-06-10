// Local: src/main/java/util/CriptoUtil.java

package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CriptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String ALGORITHM_MODE = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int KEY_LENGTH_BIT = 256;
    private static final int ITERATION_COUNT = 65536; // Para fortalecer a chave
    private static final int SALT_LENGTH = 32;

    /**
     * Cria um salt aleatório criptograficamente seguro.
     * O salt é armazenado no banco (tabela USUARIO).
     *
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
     *
     * @param senha A senha em texto plano.
     * @param salt  O salt único para o usuário.
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
    /**
     * Gera uma chave AES de 256 bits a partir da senha mestra e do salt.
     */
    private static SecretKeySpec getChaveSecreta(String senhaMestra, String salt) throws Exception {
        // Usaremos PBKDF2 para derivar a chave, que é mais seguro
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        // O salt usado aqui deve ser o HASH MESTRE do usuário, não o salt simples!
        // Mas para simplificar o requisito, usaremos o salt simples aqui.
        PBEKeySpec spec = new PBEKeySpec(senhaMestra.toCharArray(), salt.getBytes(), ITERATION_COUNT, KEY_LENGTH_BIT);

        // A chave final derivada
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM);
    }

    /**
     * CRIPTOGRAFIA (CREATE/UPDATE) - Criptografa o dado sensível (senha de serviço, cartão, nota).
     * @param dadosTextoPuro O dado a ser criptografado.
     * @param senhaMestra A senha mestra do usuário (em texto P U R O durante a sessão).
     * @param salt O salt único do usuário (o mesmo usado para o Hash da Senha Mestra).
     * @return O dado criptografado e codificado em Base64.
     */
    public static String criptografarItem(String dadosTextoPuro, String senhaMestra, String salt) {
        try {
            SecretKeySpec secretKey = getChaveSecreta(senhaMestra, salt);
            Cipher cipher = Cipher.getInstance(ALGORITHM_MODE);

            // Vetor de Inicialização (IV) aleatório
            byte[] iv = new byte[IV_LENGTH_BYTE];
            (new SecureRandom()).nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(dadosTextoPuro.getBytes());

            // O resultado final é o IV + o texto criptografado. Codificamos em Base64 para salvar no TEXT do DB.
            byte[] finalCipherText = new byte[IV_LENGTH_BYTE + cipherText.length];
            System.arraycopy(iv, 0, finalCipherText, 0, IV_LENGTH_BYTE);
            System.arraycopy(cipherText, 0, finalCipherText, IV_LENGTH_BYTE, cipherText.length);

            return Base64.getEncoder().encodeToString(finalCipherText);

        } catch (Exception e) {
            throw new RuntimeException("Falha na criptografia AES/GCM: " + e.getMessage(), e);
        }
    }

    /**
     * DESCRIPTOGRAFIA (READ) - Descriptografa o dado sensível.
     * @param dadosCriptografados O dado lido do banco (em Base64).
     * @param senhaMestra A senha mestra do usuário (em texto P U R O durante a sessão).
     * @param salt O salt único do usuário.
     * @return O dado em texto puro.
     */
    public static String descriptografarItem(String dadosCriptografados, String senhaMestra, String salt) {
        try {
            SecretKeySpec secretKey = getChaveSecreta(senhaMestra, salt);
            Cipher cipher = Cipher.getInstance(ALGORITHM_MODE);

            byte[] decodedBytes = Base64.getDecoder().decode(dadosCriptografados);

            // Separa o IV do texto criptografado
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(decodedBytes, 0, iv, 0, IV_LENGTH_BYTE);

            byte[] cipherText = new byte[decodedBytes.length - IV_LENGTH_BYTE];
            System.arraycopy(decodedBytes, IV_LENGTH_BYTE, cipherText, 0, cipherText.length);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            return new String(cipher.doFinal(cipherText));

        } catch (Exception e) {
            // Este erro é grave e pode indicar que a senha mestra ou o salt estão incorretos (ou os dados corrompidos)
            throw new RuntimeException("Falha na descriptografia. Senha Mestra incorreta ou dados corrompidos.", e);
        }
    }
}