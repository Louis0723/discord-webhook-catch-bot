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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

public class WebhookCatch extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookCatch.class);

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getMessage().isWebhookMessage()) {
            event.getMessage().delete().queue();
            LOGGER.warn("Delete webhook message: {}", event.getMessage().toString());
            for (Webhook webhook : event.getGuild().retrieveWebhooks().complete()) {
                webhook.delete().queue();
                LOGGER.warn("Delete webhook: {}", webhook.getName());
                AuditLogPaginationAction auditLogs = event.getGuild().retrieveAuditLogs();
                auditLogs.stream().filter(auditLogEntry -> auditLogEntry.getType() == ActionType.WEBHOOK_CREATE)
                        .forEach(auditLogEntry -> {
                            if (auditLogEntry.getTargetIdLong() == event.getMessage().getAuthor().getIdLong()) {
                                // if(auditLogEntry.getUser()!=null){
                                if (auditLogEntry.getUser() != null && !auditLogEntry.getUser().isBot()) {
                                    final Member member = event.getGuild().getMember(auditLogEntry.getUser());
                                    if (member != null) {
                                        member.getRoles().forEach(
                                                role -> event.getGuild().removeRoleFromMember(member, role).queue());
                                        event.getGuild().getTextChannels().forEach(channel -> channel
                                                .upsertPermissionOverride(member).setDeny(Permission.values()).queue());
                                        LOGGER.warn("Remove member role and permission: {}", member.getNickname());

                                        // TODO: Record,Notify event for yourself
                                    }
                                }
                            }
                        });
            }
        }
    }

}
