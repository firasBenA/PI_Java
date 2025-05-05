package tn.esprit.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BadWordsFilter {
    // Liste statique des mots interdits (à personnaliser selon vos besoins)
    private static final Set<String> BAD_WORDS = new HashSet<>(Arrays.asList(
            "idiot", "stupide", "insulte", "grossier", "mauvais", "merde",
            "fuck", "shit", "damn"
    ));

    /**
     * Vérifie si le texte contient un mot interdit.
     * @param text Texte à vérifier
     * @return Le mot interdit trouvé, ou null si aucun
     */
    public static String containsBadWord(String text) {
        if (text == null) return null;
        String lowerText = text.toLowerCase();
        for (String badWord : BAD_WORDS) {
            if (lowerText.contains(badWord.toLowerCase())) {
                return badWord;
            }
        }
        return null;
    }

    /**
     * Remplace les mots interdits par des astérisques.
     * @param text Texte à filtrer
     * @return Texte avec mots interdits remplacés
     */
    public static String replaceBadWords(String text) {
        if (text == null) return null;
        String result = text;
        for (String badWord : BAD_WORDS) {
            String replacement = "*".repeat(badWord.length());
            result = result.replaceAll("(?i)" + badWord, replacement);
        }
        return result;
    }
}