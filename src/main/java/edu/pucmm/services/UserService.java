package edu.pucmm.services;

import edu.pucmm.Main;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import edu.pucmm.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private static MongoCollection<Document> userCollection = Main.database.getCollection("users");

    public static boolean registerUser(User user) {
        Document existing = userCollection.find(Filters.eq("username", user.getUsername())).first();
        if (existing != null) return false;
        Document doc = new Document("username", user.getUsername())
                .append("password", user.getPassword())
                .append("role", user.getRole());
        userCollection.insertOne(doc);
        return true;
    }

    public static Document loginUser(String username, String password) {
        return userCollection.find(
                Filters.and(
                        Filters.eq("username", username),
                        Filters.eq("password", password)
                )
        ).first();
    }

    public static void createDefaultAdmin() {
        Document admin = userCollection.find(Filters.eq("username", "admin")).first();
        if (admin == null) {
            User adminUser = new User("admin", "admin", "admin");
            registerUser(adminUser);
            LOGGER.info("Default admin created -> username: admin");
        }
    }

    public static boolean isAdmin(String username) {
        Document doc = userCollection.find(Filters.eq("username", username)).first();
        if (doc == null) return false;
        return "admin".equals(doc.getString("role"));
    }

    public static boolean promoteUserToAdmin(String username) {
        Document doc = userCollection.find(Filters.eq("username", username)).first();
        if (doc == null) return false;
        userCollection.updateOne(Filters.eq("username", username), Updates.set("role", "admin"));
        return true;
    }

    public static boolean deleteUser(String username) {
        Document doc = userCollection.find(Filters.eq("username", username)).first();
        if (doc == null) return false;
        if ("admin".equals(doc.getString("role"))) {
            return false;
        }
        userCollection.deleteOne(Filters.eq("username", username));
        return true;
    }

    // NUEVO
    public static Document findByUsername(String username) {
        return userCollection.find(Filters.eq("username", username)).first();
    }
}
