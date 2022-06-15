package com.cybavo;

import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybavo.listeners.QRCodeCatch;
import com.cybavo.listeners.WebhookCatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws LoginException, IOException {
        // Get Discord Token
        String token;

        if (System.getenv("JAVA_DISCORD_BOT_TOKEN") == null) {
            if (!new File("./token").exists()) {
                LOGGER.error("Not Found Token");
                return;
            } else
                token = Files.readAllLines(new File("./token").toPath()).get(0);
        } else {
            token = System.getenv("JAVA_DISCORD_BOT_TOKEN");
        }

        // Discord Builder
        JDABuilder.createDefault(token)
                .setAutoReconnect(true)
                .setMaxReconnectDelay(60)
                .addEventListeners(new WebhookCatch(), new QRCodeCatch())
                .build();
    }
}