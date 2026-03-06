package store.purchase.domain;

import java.util.Base64;

public  class PubsubMessage {
    private String data;
    private String messageId;
    private String publishTime;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String decodeData() {
        if (data == null || data.isEmpty()) {
            return null;
        }
       return new String(Base64.getDecoder().decode(data));
    }
}
