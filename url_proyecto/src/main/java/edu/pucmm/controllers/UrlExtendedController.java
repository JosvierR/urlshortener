package edu.pucmm.controllers;

import io.javalin.http.Handler;
import org.bson.Document;
import edu.pucmm.services.UrlExtendedService;
import edu.pucmm.services.UrlService;

public class UrlExtendedController {

    // Endpoint REST: Listado de URL publicadas por un usuario (con estadísticas)
    public static Handler listUserUrlsExtended = ctx -> {
        String username = ctx.pathParam("username");
        if (username == null || username.isEmpty()) {
            ctx.status(400).result("Username is required");
            return;
        }
        var urls = UrlExtendedService.listUrlsByUser(username);
        ctx.json(urls);
    };

    // Endpoint REST: Creación de registro de URL para un usuario
    // Retorna: { fullUrl, shortUrl, creationDate, statistics:{accessCount, accessLogs}, previewImageBase64 }
    public static Handler createUrlRecordExtended = ctx -> {
        String username = ctx.formParam("username");
        String originalUrl = ctx.formParam("url");
        if (username == null || username.isEmpty() || originalUrl == null || originalUrl.isEmpty()) {
            ctx.status(400).result("Username and URL are required");
            return;
        }
        Document doc = UrlExtendedService.createUrlRecordForUser(username, originalUrl);

        // Construir la respuesta con la estructura solicitada
        Document response = new Document();
        response.append("fullUrl", doc.getString("originalUrl"));
        response.append("shortUrl", ctx.scheme() + "://" + ctx.host() + "/go/" + doc.getString("shortCode"));
        response.append("creationDate", doc.get("creationDate"));
        // Objeto de estadísticas (accesos y logs)
        Document statistics = new Document();
        statistics.append("accessCount", doc.get("accessCount"));
        statistics.append("accessLogs", doc.get("accessLogs"));
        response.append("statistics", statistics);
        // Vista previa en base64 (stub)
        response.append("previewImageBase64", getPreviewImageBase64(doc.getString("originalUrl")));

        ctx.json(response);
    };

    // Función stub para obtener la imagen de vista previa en base64
    private static String getPreviewImageBase64(String url) {
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA";
    }
}
