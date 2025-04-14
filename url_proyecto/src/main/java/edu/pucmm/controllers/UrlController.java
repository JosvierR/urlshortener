package edu.pucmm.controllers;

import io.javalin.http.Handler;
import org.bson.Document;
import edu.pucmm.services.UrlService;
import edu.pucmm.services.JwtUtil;

public class UrlController {

    // Acortar URL: ahora incluye previewImageBase64
    public static Handler shortenUrl = ctx -> {
        String originalUrl = ctx.formParam("url");
        if (originalUrl == null || originalUrl.isEmpty()) {
            ctx.status(400).result("URL is required");
            return;
        }
        var record = UrlService.createUrlRecord(originalUrl);
        String shortUrl = windowOrigin() + "/go/" + record.getString("shortCode");
        String previewImage = record.getString("previewImageBase64") != null
                ? record.getString("previewImageBase64")
                : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA";
        ctx.json(java.util.Map.of(
                "shortUrl", shortUrl,
                "previewImageBase64", previewImage
        ));
    };

    // El resto de tus handlers se mantienen igual
    public static Handler redirect = ctx -> {
        String shortCode = ctx.pathParam("shortCode");
        Document doc = UrlService.findByShortCode(shortCode);
        if (doc == null) {
            ctx.status(404).result("URL not found");
            return;
        }
        String token = ctx.header("Authorization");
        String user = "invitado";
        if (token != null && JwtUtil.validateToken(token)) {
            user = JwtUtil.getUsernameFromToken(token);
        }
        String ip = ctx.ip();
        String ua = ctx.userAgent() != null ? ctx.userAgent().toLowerCase() : "";
        String platform = "Desconocido";
        if (ua.contains("windows")) platform = "Windows";
        else if (ua.contains("mac")) platform = "Mac OS";
        else if (ua.contains("android")) platform = "Android";
        else if (ua.contains("linux")) platform = "Linux";
        String browser = "Desconocido";
        if (ua.contains("chrome")) browser = "Chrome";
        else if (ua.contains("firefox")) browser = "Firefox";
        else if (ua.contains("safari") && !ua.contains("chrome")) browser = "Safari";
        else if (ua.contains("edge")) browser = "Edge";
        String clientDomain = ctx.header("Referer");
        if (clientDomain == null || clientDomain.isEmpty()) {
            clientDomain = "Desconocido";
        }
        UrlService.logAccess(shortCode, ip, user, platform, browser, clientDomain);
        ctx.redirect(doc.getString("originalUrl"));
    };

    public static Handler summaryApi = ctx -> {
        String shortCode = ctx.pathParam("shortCode");
        Document doc = UrlService.findByShortCode(shortCode);
        if (doc == null) {
            ctx.status(404).result("URL not found");
            return;
        }
        ctx.json(doc);
    };

    public static Handler listUserUrls = ctx -> {
        ctx.json(UrlService.findAllUrls());
    };

    public static Handler editUrl = ctx -> {
        String shortCode = ctx.pathParam("shortCode");
        Document doc = UrlService.findByShortCode(shortCode);
        if (doc == null) {
            ctx.status(404).result("URL not found");
            return;
        }
        String newUrl = ctx.formParam("newUrl");
        if (newUrl == null || newUrl.isEmpty()) {
            ctx.status(400).result("Missing newUrl param");
            return;
        }
        UrlService.updateOriginalUrl(shortCode, newUrl);
        ctx.result("URL updated successfully.");
    };

    public static Handler deleteUrl = ctx -> {
        String shortCode = ctx.pathParam("shortCode");
        Document doc = UrlService.findByShortCode(shortCode);
        if (doc == null) {
            ctx.status(404).result("URL not found");
            return;
        }
        UrlService.deleteByShortCode(shortCode);
        ctx.result("URL deleted successfully.");
    };

    private static String windowOrigin() {
        return "http://localhost:7070"; // Ajusta según corresponda
    }
}
