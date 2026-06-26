package Models.User;

import java.util.ArrayList;

public class News {
    ArrayList<Message> messages;

    public News(){
        messages=new ArrayList<>();
    }

    public News(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
