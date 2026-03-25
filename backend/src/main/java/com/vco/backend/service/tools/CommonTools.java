package com.vco.backend.service.tools;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class CommonTools {

    private static final DateTimeFormatter CURRENT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy h:mm a", Locale.ENGLISH);

    @Tool(description = "Returns the current date and time with timezone offset.")
    public String currentDateTime() {
        return ZonedDateTime.now().format(CURRENT_DATE_TIME_FORMATTER);
    }
}
