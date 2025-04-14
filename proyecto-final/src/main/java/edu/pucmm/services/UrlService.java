package edu.pucmm.services;

import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UrlService {
    // Ahora se obtiene la colección a través de DatabaseUtil.
    private static final MongoCollection<Document> urlCollection = DatabaseUtil.getDatabase().getCollection("urls");

    // Crea un nuevo enlace acortado
    public static Document createUrlRecord(String originalUrl) {
        String shortCode = generateShortCode();
        Document doc = new Document("originalUrl", originalUrl)
                .append("shortCode", shortCode)
                .append("creationDate", System.currentTimeMillis())
                .append("accessCount", 0)
                .append("accessLogs", new ArrayList<Document>());
        urlCollection.insertOne(doc);
        return doc;
    }

    // Busca un enlace por su shortCode
    public static Document findByShortCode(String shortCode) {
        return urlCollection.find(eq("shortCode", shortCode)).first();
    }

    // Registra un acceso con 6 parámetros: shortCode, ip, user, platform, browser, clientDomain
    public static void logAccess(String shortCode, String ip, String user, String platform, String browser, String clientDomain) {
        Document accessLog = new Document("timestamp", System.currentTimeMillis())
                .append("ip", ip)
                .append("user", user)
                .append("platform", platform)
                .append("browser", browser)
                .append("clientDomain", clientDomain);
        urlCollection.updateOne(eq("shortCode", shortCode),
                combine(
                        inc("accessCount", 1),
                        push("accessLogs", accessLog)
                )
        );
    }

    // Actualiza la URL original de un shortCode existente
    public static void updateOriginalUrl(String shortCode, String newUrl) {
        urlCollection.updateOne(eq("shortCode", shortCode),
                set("originalUrl", newUrl));
    }

    // Lista todos los enlaces
    public static List<Document> findAllUrls() {
        return urlCollection.find().into(new ArrayList<>());
    }

    // Elimina un enlace
    public static void deleteByShortCode(String shortCode) {
        urlCollection.deleteOne(eq("shortCode", shortCode));
    }

    // Genera un shortCode de 6 caracteres aleatorios
    private static String generateShortCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // Actualiza el documento agregando el campo "createdBy"
    public static void updateDocumentWithCreatedBy(String shortCode, String username) {
        urlCollection.updateOne(eq("shortCode", shortCode), com.mongodb.client.model.Updates.set("createdBy", username));
    }

    // Retorna las URL que tengan el campo "createdBy" igual al username dado
    public static List<Document> findUrlsByUser(String username) {
        return urlCollection.find(eq("createdBy", username)).into(new ArrayList<>());
    }
}
