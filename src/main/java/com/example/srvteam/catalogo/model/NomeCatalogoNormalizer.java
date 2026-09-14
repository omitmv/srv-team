package com.example.srvteam.catalogo.model;

import java.text.Normalizer;
import java.util.Locale;

public final class NomeCatalogoNormalizer {

    private NomeCatalogoNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }
}
