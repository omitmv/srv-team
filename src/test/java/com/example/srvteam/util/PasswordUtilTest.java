package com.example.srvteam.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    void testCriptografarSenha() {
        String senha = "123456";
        String senhaCriptografada = PasswordUtil.criptografarSenha(senha);
        
        assertNotNull(senhaCriptografada);
        assertNotEquals(senha, senhaCriptografada);
        assertEquals(32, senhaCriptografada.length()); // MD5 sempre gera hash de 32 caracteres
    }

    @Test
    void testCriptografarSenhaConsistente() {
        String senha = "123456";
        String hash1 = PasswordUtil.criptografarSenha(senha);
        String hash2 = PasswordUtil.criptografarSenha(senha);
        
        assertEquals(hash1, hash2); // Mesmo input deve gerar mesmo hash
    }

    @Test
    void testVerificarSenhaCorreta() {
        String senha = "123456";
        String senhaCriptografada = PasswordUtil.criptografarSenha(senha);
        
        assertTrue(PasswordUtil.verificarSenha(senha, senhaCriptografada));
    }

    @Test
    void testVerificarSenhaIncorreta() {
        String senha = "123456";
        String senhaErrada = "654321";
        String senhaCriptografada = PasswordUtil.criptografarSenha(senha);
        
        assertFalse(PasswordUtil.verificarSenha(senhaErrada, senhaCriptografada));
    }

    @Test
    void testSenhasDiferentesGeramHashsDiferentes() {
        String senha1 = "123456";
        String senha2 = "654321";
        
        String hash1 = PasswordUtil.criptografarSenha(senha1);
        String hash2 = PasswordUtil.criptografarSenha(senha2);
        
        assertNotEquals(hash1, hash2);
    }
}
