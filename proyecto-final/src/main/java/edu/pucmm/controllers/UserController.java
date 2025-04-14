package edu.pucmm.controllers;

import io.javalin.http.Handler;
import org.bson.Document;
import edu.pucmm.services.UserService;
import edu.pucmm.models.User;
import edu.pucmm.services.JwtUtil;

import java.util.HashMap;
import java.util.Map;

public class UserController {

    public static Handler register = ctx -> {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        if (username == null || password == null) {
            ctx.status(400).result("Missing username or password");
            return;
        }
        // Creamos usuario con rol "user" y se registra en MongoDB
        User user = new User(username, password, "user");
        boolean success = UserService.registerUser(user);
        if (success) {
            ctx.status(201).result("User registered successfully!");
        } else {
            ctx.status(400).result("User already exists.");
        }
    };

    public static Handler login = ctx -> {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        if (username == null || password == null) {
            ctx.status(400).result("Missing username or password");
            return;
        }
        Document userDoc = UserService.loginUser(username, password);
        if (userDoc != null) {
            // Generar JWT para autenticación, usando MongoDB para validar el usuario
            String token = JwtUtil.generateToken(username);

            // Forma compatible con Java 8 (en lugar de Map.of(...))
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", token);

            ctx.json(tokenMap);
        } else {
            ctx.status(401).result("Invalid credentials");
        }
    };

    // Nuevo handler para promover a un usuario a administrador
    public static Handler promoteUser = ctx -> {
        String token = ctx.header("Authorization");
        if (token == null || !JwtUtil.validateToken(token)) {
            ctx.status(401).result("Unauthorized");
            return;
        }
        String currentUser = JwtUtil.getUsernameFromToken(token);
        if (!UserService.isAdmin(currentUser)) {
            ctx.status(403).result("Forbidden: Only admin can promote users");
            return;
        }
        String usernameToPromote = ctx.formParam("username");
        if (usernameToPromote == null || usernameToPromote.isEmpty()) {
            ctx.status(400).result("Username is required");
            return;
        }
        boolean success = UserService.promoteUserToAdmin(usernameToPromote);
        if (success) {
            ctx.result("User promoted to admin successfully.");
        } else {
            ctx.status(404).result("User not found");
        }
    };

    // Nuevo handler para eliminar un usuario (no se permite eliminar a un administrador)
    public static Handler deleteUser = ctx -> {
        String token = ctx.header("Authorization");
        if (token == null || !JwtUtil.validateToken(token)) {
            ctx.status(401).result("Unauthorized");
            return;
        }
        String currentUser = JwtUtil.getUsernameFromToken(token);
        if (!UserService.isAdmin(currentUser)) {
            ctx.status(403).result("Forbidden: Only admin can delete users");
            return;
        }
        String usernameToDelete = ctx.pathParam("username");
        if (usernameToDelete == null || usernameToDelete.isEmpty()) {
            ctx.status(400).result("Username is required");
            return;
        }
        boolean success = UserService.deleteUser(usernameToDelete);
        if (success) {
            ctx.result("User deleted successfully.");
        } else {
            ctx.status(400).result("Cannot delete admin user or user not found.");
        }
    };
}
