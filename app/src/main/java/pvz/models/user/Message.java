package pvz.models.user;

public class Message {
    private String message;
    private boolean unread;

    public Message(String message) {
        this.message = message;
        this.unread = true;
    }

    public Message(String message, boolean unread) {
        this.message = message;
        this.unread = unread;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    @Override
    public String toString() {
        return this.message;
    }
}