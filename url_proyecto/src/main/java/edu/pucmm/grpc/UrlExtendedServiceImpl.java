package edu.pucmm.grpc;

import edu.pucmm.services.UrlExtendedService;
import edu.pucmm.services.DatabaseUtil;

import io.grpc.stub.StreamObserver;
import org.bson.Document;

import edu.pucmm.grpc.AccessLog;
import edu.pucmm.grpc.UrlRecord;
import edu.pucmm.grpc.ListUserUrlsRequest;
import edu.pucmm.grpc.ListUserUrlsResponse;
import edu.pucmm.grpc.CreateUrlRecordRequest;
import edu.pucmm.grpc.CreateUrlRecordResponse;

public class UrlExtendedServiceImpl extends UrlExtendedServiceGrpc.UrlExtendedServiceImplBase {

    @Override
    public void listUserUrls(ListUserUrlsRequest request, StreamObserver<ListUserUrlsResponse> responseObserver) {
        // Inicializamos la conexión a MongoDB
        DatabaseUtil.initDatabase();
        String username = request.getUsername();

        try {
            // Lógica para buscar registros en la base de datos
            var docList = UrlExtendedService.listUrlsByUser(username);
            ListUserUrlsResponse.Builder responseBuilder = ListUserUrlsResponse.newBuilder();

            if (docList != null) {
                for (Document doc : docList) {
                    String fullUrl = doc.getString("originalUrl"); // en la BD se llama "originalUrl"
                    String shortCode = doc.getString("shortCode");
                    String shortUrl = "http://localhost:7070/go/" + shortCode;
                    long creationDate = doc.getLong("creationDate");
                    int accessCount = doc.getInteger("accessCount", 0);

                    @SuppressWarnings("unchecked")
                    var logDocs = (java.util.List<Document>) doc.get("accessLogs", java.util.List.class);
                    var accessLogsProto = new java.util.ArrayList<AccessLog>();

                    if (logDocs != null) {
                        for (Document logDoc : logDocs) {
                            AccessLog.Builder logBuilder = AccessLog.newBuilder();
                            if (logDoc.containsKey("timestamp")) {
                                logBuilder.setTimestamp(logDoc.getLong("timestamp"));
                            }
                            if (logDoc.containsKey("ip")) {
                                logBuilder.setIp(logDoc.getString("ip"));
                            }
                            if (logDoc.containsKey("user")) {
                                logBuilder.setUser(logDoc.getString("user"));
                            }
                            if (logDoc.containsKey("platform")) {
                                logBuilder.setPlatform(logDoc.getString("platform"));
                            }
                            if (logDoc.containsKey("browser")) {
                                logBuilder.setBrowser(logDoc.getString("browser"));
                            }
                            if (logDoc.containsKey("clientDomain")) {
                                logBuilder.setClientDomain(logDoc.getString("clientDomain"));
                            }
                            accessLogsProto.add(logBuilder.build());
                        }
                    }

                    String previewImageBase64 = doc.getString("previewImageBase64") != null
                            ? doc.getString("previewImageBase64")
                            : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA";

                    UrlRecord record = UrlRecord.newBuilder()
                            .setFullUrl(fullUrl != null ? fullUrl : "")
                            .setShortUrl(shortUrl)
                            .setCreationDate(creationDate)
                            .setAccessCount(accessCount)
                            .addAllAccessLogs(accessLogsProto)
                            .setPreviewImageBase64(previewImageBase64)
                            .build();

                    responseBuilder.addUrls(record);
                }
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void createUrlRecord(CreateUrlRecordRequest request, StreamObserver<CreateUrlRecordResponse> responseObserver) {
        // Inicializamos la conexión a MongoDB
        DatabaseUtil.initDatabase();
        String username = request.getUsername();
        String originalUrl = request.getOriginalUrl();

        try {
            Document doc = UrlExtendedService.createUrlRecordForUser(username, originalUrl);

            String fullUrl = doc.getString("originalUrl");
            String shortCode = doc.getString("shortCode");
            String shortUrl = "http://localhost:7070/go/" + shortCode;
            long creationDate = doc.getLong("creationDate");
            int accessCount = doc.getInteger("accessCount", 0);

            @SuppressWarnings("unchecked")
            var logDocs = (java.util.List<Document>) doc.get("accessLogs", java.util.List.class);
            var accessLogsProto = new java.util.ArrayList<AccessLog>();

            if (logDocs != null) {
                for (Document logDoc : logDocs) {
                    AccessLog.Builder logBuilder = AccessLog.newBuilder();
                    if (logDoc.containsKey("timestamp")) {
                        logBuilder.setTimestamp(logDoc.getLong("timestamp"));
                    }
                    if (logDoc.containsKey("ip")) {
                        logBuilder.setIp(logDoc.getString("ip"));
                    }
                    if (logDoc.containsKey("user")) {
                        logBuilder.setUser(logDoc.getString("user"));
                    }
                    if (logDoc.containsKey("platform")) {
                        logBuilder.setPlatform(logDoc.getString("platform"));
                    }
                    if (logDoc.containsKey("browser")) {
                        logBuilder.setBrowser(logDoc.getString("browser"));
                    }
                    if (logDoc.containsKey("clientDomain")) {
                        logBuilder.setClientDomain(logDoc.getString("clientDomain"));
                    }
                    accessLogsProto.add(logBuilder.build());
                }
            }

            String previewImageBase64 = doc.getString("previewImageBase64") != null
                    ? doc.getString("previewImageBase64")
                    : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA";

            UrlRecord urlRecord = UrlRecord.newBuilder()
                    .setFullUrl(fullUrl != null ? fullUrl : "")
                    .setShortUrl(shortUrl)
                    .setCreationDate(creationDate)
                    .setAccessCount(accessCount)
                    .addAllAccessLogs(accessLogsProto)
                    .setPreviewImageBase64(previewImageBase64)
                    .build();

            CreateUrlRecordResponse response = CreateUrlRecordResponse.newBuilder()
                    .setUrlRecord(urlRecord)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
