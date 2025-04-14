package edu.pucmm.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseUtil {
    private static MongoDatabase database;

    public static void initDatabase() {
        if (database == null) {
            // Primero intenta obtener la variable de entorno
            String mongoUrl = System.getenv("MONGODB_URL");
            // Si no está definida, intenta obtenerla de las propiedades del sistema
            if (mongoUrl == null || mongoUrl.isEmpty()) {
                mongoUrl = System.getProperty("MONGODB_URL");
            }
            // Si aun así no se define, puedes optar por usar un valor por defecto
            if (mongoUrl == null || mongoUrl.isEmpty()) {
                mongoUrl = "mongodb+srv://josvierp:88IWveNxopWiL6Yh@proyectofinalcluster.okz4kdn.mongodb.net/?retryWrites=true&w=majority&appName=ProyectoFinalCluster";
                System.out.println("DEBUG: MONGODB_URL no estaba definida; usando valor por defecto: " + mongoUrl);
            } else {
                System.out.println("DEBUG: MONGODB_URL = " + mongoUrl);
            }
            MongoClient mongoClient = MongoClients.create(mongoUrl);
            // Asegúrate de que el nombre de la base de datos es el correcto
            database = mongoClient.getDatabase("url_shortener_db");
            System.out.println("Conexión a MongoDB inicializada correctamente.");
        }
    }

    public static MongoDatabase getDatabase() {
        if (database == null) {
            initDatabase();
        }
        return database;
    }
}
