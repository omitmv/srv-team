package com.example.srvteam.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtil {
    
    /**
     * Criptografa a senha aplicando Base64 e depois MD5
     * @param senha Senha em texto puro
     * @return Senha criptografada
     */
    public static String criptografarSenha(String senha) {
        try {
            // Passo 1: Aplicar Base64
            String senhaBase64 = Base64.getEncoder().encodeToString(senha.getBytes(StandardCharsets.UTF_8));
            
            // Passo 2: Aplicar MD5 no resultado do Base64
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(senhaBase64.getBytes(StandardCharsets.UTF_8));
            
            // Converter bytes para string hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao criptografar senha: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica se a senha informada corresponde à senha criptografada
     * @param senhaTexto Senha em texto puro
     * @param senhaCriptografada Senha criptografada armazenada no banco
     * @return true se as senhas correspondem
     */
    public static boolean verificarSenha(String senhaTexto, String senhaCriptografada) {
        String senhaTextoCriptografada = criptografarSenha(senhaTexto);
        return senhaTextoCriptografada.equals(senhaCriptografada);
    }
}
