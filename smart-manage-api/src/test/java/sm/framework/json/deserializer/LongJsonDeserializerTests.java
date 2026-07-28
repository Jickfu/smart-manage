package sm.framework.json.deserializer;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LongJsonDeserializerTests {

    @Test
    void validNumericStringIsDeserialized() {
        JsonParser jsonParser = parserWithText("1987654321098765432");

        assertEquals(1987654321098765432L, LongJsonDeserializer.INSTANCE.deserialize(jsonParser, null));
    }

    @Test
    void blankValueRemainsAbsent() {
        JsonParser jsonParser = parserWithText(" ");

        assertNull(LongJsonDeserializer.INSTANCE.deserialize(jsonParser, null));
    }

    @Test
    void invalidLongMustNotBeSilentlyConvertedToNull() {
        JsonParser jsonParser = parserWithText("not-a-long");

        assertThrows(
                DatabindException.class,
                () -> LongJsonDeserializer.INSTANCE.deserialize(jsonParser, null)
        );
    }

    private JsonParser parserWithText(String value) {
        JsonParser jsonParser = mock(JsonParser.class);
        when(jsonParser.getText()).thenReturn(value);
        return jsonParser;
    }
}

