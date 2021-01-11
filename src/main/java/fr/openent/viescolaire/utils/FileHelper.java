package fr.openent.viescolaire.utils;

import fr.wseduc.webutils.DefaultAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.storage.Storage;
import org.entcore.common.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;

import static org.entcore.common.utils.FileUtils.deleteImportPath;

public class FileHelper {
    private static final Logger log = LoggerFactory.getLogger(FileHelper.class);

    private final Vertx vertx;
    private final String path;

    public FileHelper(Vertx vertx, String path) {
        this.vertx = vertx;
        this.path = path;
    }

    /**
     * Upload any file
     *
     * @param request Request http
     * @param handler Result return path to the directory unzipped created
     */
    public void upload(final HttpServerRequest request, final Handler<AsyncResult<String>> handler) {
        request.pause();
        request.setExpectMultipart(true);
        request.exceptionHandler(getExceptionHandler(path, handler));

        request.uploadHandler(upload -> {
            String filename = Normalizer
                    .normalize(upload.filename(), Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "")
                    .replaceAll("'", "")
                    .replaceAll(" ", "_");

            final String filePath = path + File.separator + filename;

            upload.endHandler(event -> handler.handle(Future.succeededFuture(filePath)));
            upload.streamToFileSystem(filePath);
        });

        this.vertx.fileSystem().mkdir(path, directory -> {
            if (directory.failed()) {
                String message = "[Viescolaire@FileHelper::upload] Failed to create directory at path: " + path + ".";
                log.error(message, directory.cause());
                handler.handle(Future.failedFuture(message));
                return;
            }
            request.resume();
        });
    }

    /**
     * Get exception handler. It return a handler that catch error while the request upload the file.
     * In case of exception, the handler delete the directory.
     *
     * @param path    Temp directory path
     * @param handler Function handler
     * @return Handler<Throwable>
     */
    private Handler<Throwable> getExceptionHandler(final String path, final Handler<AsyncResult<String>> handler) {
        return event -> {
            log.error("[Viescolaire@FileHelper::getExceptionHandler] Due to error " + event.getMessage() + ", will delete path");
            handler.handle(new DefaultAsyncResult<>(event));
            deleteImportPath(vertx, path);
        };
    }

    public static void removeFiles(Storage storage, List<String> fileIds, Handler<AsyncResult<JsonObject>> handler) {
        if (fileIds.isEmpty()) {
            handler.handle(Future.succeededFuture(new JsonObject().put("remove file status", "ok")));
            return;
        }

        storage.removeFiles(new JsonArray(fileIds), result -> {
            if (!"ok".equals(result.getString("status"))) {
                String message = "[Viescolaire@FileHelper::removeFile] Failed to upload picture.";
                log.error(message, result.getString("message"));
                handler.handle(Future.failedFuture(message));
                return;
            }
            handler.handle(Future.succeededFuture(result));
        });
    }

    public static void exist(Storage storage, String fileId, Handler<AsyncResult<Boolean>> handler) {
        storage.readFile(fileId, result -> {
            if (result == null) {
                handler.handle(Future.succeededFuture(false));
                return;
            }
            handler.handle(Future.succeededFuture(true));
        });
    }


    public static List<String> readDirectoryHandler(String path, AsyncResult<List<String>> dirResult, Handler<AsyncResult<Void>> handler) {
        if (dirResult.failed()) {
            String message = "[Viescolaire@FileHelper::readDirectory] Failed to read directory " +
                    "at path: " + path + ". ";
            log.error(message, dirResult.cause());
            handler.handle(Future.failedFuture(message));
            return null;
        }
        return dirResult.result();
    }

    public static String getAbsolutePath(String path) {
        String[] pathArray = path.split("/");
        String absolutePath;
        if (pathArray.length >= 2) {
            absolutePath = pathArray[pathArray.length - 2] + "/" + pathArray[pathArray.length - 1];
        } else if (pathArray.length == 1) {
            absolutePath = pathArray[pathArray.length - 1];
        } else {
            absolutePath = path;
        }
        return absolutePath;
    }

    /**
     * This method is based on the origin FileUtils unzip from entcore but is rewritten in order to handle thrown exception such as
     * NullPointerException and IllegalArgumentException (for cases like uncommun character/encode)
     *
     * @param zipFilename   zip path file name
     * @param destDirname   dest path dir name
     * @param handler       async handler
     */
    public static void unzip(String zipFilename, String destDirname, Handler<AsyncResult<Void>> handler) {
        Thread t = new Thread(new Runnable() {
            private final Handler<AsyncResult<Void>> callback = handler;
            public void run() {
                try {
                    FileUtils.unzip(zipFilename, destDirname);
                    this.callback.handle(Future.succeededFuture());
                } catch (IOException | NullPointerException | IllegalArgumentException e) {
                    this.callback.handle(Future.failedFuture(e.getMessage()));
                }
            }
        });
        t.start();
    }
}
