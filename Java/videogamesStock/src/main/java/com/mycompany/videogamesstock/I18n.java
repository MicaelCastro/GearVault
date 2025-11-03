package com.mycompany.videogamesstock;

import java.util.*;     // Importa classes de internacionalização (Locale, ResourceBundle)                                                         

// Classe para gerir a internacionalização (traduções da aplicação)
public class I18n {
    
    // Define o idioma padrão como português de Portugal
    private static Locale currentLocale = new Locale("pt", "PT");
    
    // Carrega o ficheiro de mensagens correspondente ao idioma atual
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);

    // Devolve a tradução associada a uma chave
    public static String t(String key) {
        return bundle.getString(key);
    }

    // Altera o idioma e recarrega o ficheiro de mensagens
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    // Retorna o idioma atualmente definido
    public static Locale getLocale() {
        return currentLocale;
    }
}