package store.purchase.domain;

public class PushRequest {
    private PubsubMessage message;
    private String subscription;

    public PubsubMessage getMessage() {
        return message;
    }

    public void setMessage(PubsubMessage message) {
        this.message = message;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }
}