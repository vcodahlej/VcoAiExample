package com.vco.chuckmcp;

import com.github.javafaker.Faker;
import java.util.Locale;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ChuckNorrisFactsTool {

    private final Faker faker = new Faker(Locale.ENGLISH);

    @Tool(name = "randomChuckNorrisFact", description = "Returns a random Chuck Norris fact.")
    public String randomChuckNorrisFact() {
        return this.faker.chuckNorris().fact();
    }

}
