package com.cybavo.listeners;

import java.io.File;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class QRCodeCatch extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(QRCodeCatch.class);

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        for (Attachment attachment : event.getMessage().getAttachments()) {
            try {
                File file = attachment.downloadToFile().get();
                QRCodeReader qrcodeReader = new QRCodeReader();
                BufferedImage image = ImageIO.read(file);
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                Binarizer binarizer = new HybridBinarizer(source);
                BinaryBitmap imageBinaryBitmap = new BinaryBitmap(binarizer);
                Result result = qrcodeReader.decode(imageBinaryBitmap);
                
                String msg = String.format("User:%s ,QRcode Content:%s", event.getAuthor().getName(), result.getText());

                LOGGER.info(msg);
                LOGGER.info("QRcode Result Raw: {}", result.getRawBytes());
                event.getChannel().sendMessage(msg).queue();;
            } catch (Exception e) {
                LOGGER.debug("Not QRCode: {}", e.getLocalizedMessage());
            }
        }
    }
}
