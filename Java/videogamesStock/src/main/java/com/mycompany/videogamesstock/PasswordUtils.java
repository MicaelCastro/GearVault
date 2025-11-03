package com.mycompany.videogamesstock;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    /**
     * Gera o hash SHA-256 da password junta com o salt.
     * 
     * @param password A password em texto simples
     * @param salt O salt associado ao utilizador
     * @return O hash hexadecimal da password + salt
     */
    
    public static String hashPassword(String password, String salt) {
        try {
            // Cria um objeto MessageDigest configurado para SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Junta a password com o salt
            String text = password + salt;

            // Converte a string para bytes UTF-8 e calcula o hash
            byte[] hashBytes = md.digest(text.getBytes("UTF-8"));

            // Converte o array de bytes do hash para uma string hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b)); // cada byte para 2 dígitos hex
            }

            // Retorna o hash final como string
            return sb.toString();

        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // Se ocorrer algum erro, lança runtime exception
            throw new RuntimeException("Erro ao gerar hash da password", e);
        }
    }
}