package org.usbtechno.deve;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;

import java.util.Optional;

public class TokenUtil {

    public static final String OPENAI_MODEL = "cl100k_base";

    private static final Optional<Encoding> encoding = Encodings.newDefaultEncodingRegistry()
            .getEncoding(OPENAI_MODEL); // OpenAI models

    public static int countTokens(String text) {
        if (text == null) return 0;
        return encoding.get().countTokens(text);
    }

}