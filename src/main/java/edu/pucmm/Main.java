package edu.pucmm;

import io.javalin.Javalin;
import edu.pucmm.services.UserService;
import edu.pucmm.controllers.UserController;
import edu.pucmm.controllers.UrlController;
import edu.pucmm.services.JwtUtil;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static MongoDatabase database;

    public static void main(String[] args) {
        // 1) Conexión a MongoDB mediante variable de ambiente
        String mongoUrl = System.getenv("MONGODB_URL");
        if (mongoUrl == null || mongoUrl.isEmpty()) {
            System.err.println("ERROR: MONGODB_URL is not set.");
            System.exit(1);
        }
        MongoClient mongoClient = MongoClients.create(mongoUrl);
        database = mongoClient.getDatabase("url_shortener_db");

        // 2) Crear usuario administrador por defecto si no existe
        UserService.createDefaultAdmin();

        // 3) Iniciar Javalin y servir archivos estáticos de /public
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(port);

        // 4) Redirigir /summary/{shortCode} -> summary.html?code=...
        app.get("/summary/{shortCode}", ctx -> {
            ctx.redirect("/summary.html?code=" + ctx.pathParam("shortCode"));
        });

        // 5) Ruta API para el dashboard: /api/summary/{shortCode}
        app.get("/api/summary/{shortCode}", UrlController.summaryApi);

        // 6) Rutas de registro, login, acortar URL y redirección
        app.post("/register", UserController.register);
        app.post("/login", UserController.login);
        app.post("/shorten", UrlController.shortenUrl);
        app.get("/go/{shortCode}", UrlController.redirect);

        // 7) Proteger /api/urls con JWT (solo usuarios registrados)
        app.before("/api/urls", ctx -> {
            if (!JwtUtil.validateToken(ctx.header("Authorization"))) {
                ctx.status(401).result("Unauthorized");
            }
        });
        // Este endpoint retorna TODAS las URLs (para que el admin las vea)
        // o si deseas filtrar por usuario, ajusta la lógica en UrlController
        app.get("/api/urls", UrlController.listUserUrls);

        // 8) Ruta para eliminar enlaces (solo admin)
        app.before("/delete/{shortCode}", ctx -> {
            if (!JwtUtil.validateToken(ctx.header("Authorization"))) {
                ctx.status(401).result("Unauthorized");
                return;
            }
            String username = JwtUtil.getUsernameFromToken(ctx.header("Authorization"));
            if (!UserService.isAdmin(username)) {
                ctx.status(403).result("Forbidden: Only admin can delete links.");
                return;
            }
        });
        app.delete("/delete/{shortCode}", UrlController.deleteUrl);

        // NUEVO ENDPOINT /api/me para obtener username y rol del usuario logueado
        app.get("/api/me", ctx -> {
            String authHeader = ctx.header("Authorization");
            if (!JwtUtil.validateToken(authHeader)) {
                ctx.status(401).result("Unauthorized");
                return;
            }
            String username = JwtUtil.getUsernameFromToken(authHeader);
            var userDoc = UserService.findByUsername(username);
            if (userDoc == null) {
                ctx.status(404).result("User not found");
                return;
            }
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("username", userDoc.getString("username"));
            userInfo.put("role", userDoc.getString("role"));
            ctx.json(userInfo);
        });

        System.out.println("Javalin running on http://localhost:" + port + "/");
    }
}
