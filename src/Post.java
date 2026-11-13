import java.io.Serializable;

public class Post implements Serializable {
    int userId;
    int id;
    String title;
    String body;

    public Post() {
    }

    @Override
    public String toString() {
        return "id: " + id + "\nuser: " + userId + "\ntitle: " + title + "\nbody: " + body;
    }
}
