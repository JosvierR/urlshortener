package edu.pucmm.services;

import org.bson.Document;
import java.util.List;

public class UrlExtendedService {

    // Crea un registro de URL para un usuario, agregando el campo "createdBy"
    public static Document createUrlRecordForUser(String username, String originalUrl) {
        Document doc = UrlService.createUrlRecord(originalUrl);
        doc.append("createdBy", username);
        UrlService.updateDocumentWithCreatedBy(doc.getString("shortCode"), username);
        return doc;
    }

    // Lista las URL publicadas por un usuario (filtradas por "createdBy")
    public static List<Document> listUrlsByUser(String username) {
        return UrlService.findUrlsByUser(username);
    }
}
