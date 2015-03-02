package net.labhackercd.edemocracia.data.db.model;

import android.net.Uri;

import net.labhackercd.edemocracia.data.api.model.Message;

import java.io.Serializable;
import java.util.UUID;

public class LocalMessage implements Serializable {
    public Long _id;
    public Long insertedMessageId;
    public Long rootMessageId;
    public Message parentMessage;
    public String body;
    public String subject;
    public Uri videoAttachment;
    public UUID uuid;
    public Status status;

    public enum Status {
        /**
         * Job was cancelled. Probably due to an error.
         */
        CANCELLED
    }
}
