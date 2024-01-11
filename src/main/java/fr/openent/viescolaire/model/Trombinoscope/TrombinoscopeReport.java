package fr.openent.viescolaire.model.Trombinoscope;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.utils.DateHelper;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.template.FileTemplateProcessor;
import fr.wseduc.webutils.template.lambdas.I18nLambda;
import fr.wseduc.webutils.template.lambdas.LocaleDateLambda;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.mongodb.MongoDbResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TrombinoscopeReport {

    private String content;

    private final FileTemplateProcessor templateProcessor;
    private Long startDate;
    private Long endDate;
    private String uai;
    private String structureId;
    private String fileRecordMessage;
    private List<ReportException> reports;
    private Boolean isCorrectFile;

    public TrombinoscopeReport(Vertx vertx, String locale) {
        this.reports = new ArrayList<>();
        templateProcessor = new FileTemplateProcessor(vertx, "template");
        templateProcessor.escapeHTML(false);
        templateProcessor.setLambda("i18n", new I18nLambda(locale));
        templateProcessor.setLambda("datetime", new LocaleDateLambda(locale));
    }

    public TrombinoscopeReport start() {
        this.startDate = System.currentTimeMillis();
        return this;
    }

    public TrombinoscopeReport end() {
        this.endDate = System.currentTimeMillis();
        return this;
    }

    public JsonObject duration() {
        if (startDate == null || endDate == null) return new JsonObject();

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm:ss", Locale.FRANCE);
        Long duration = endDate - startDate;
        return new JsonObject()
                .put("start", sdf.format(startDate))
                .put("end", sdf.format(endDate))
                .put("duration", duration);
    }

    private String runTime() {
        long elapsed = this.endDate - this.startDate ;
        String milliseconds = Long.toString(elapsed);
        String seconds = Long.toString((elapsed / 1000) % 60);
        String minutes = Long.toString(((elapsed / 1000) / 60) % 60);
        String hours = Long.toString((elapsed / 1000) / 3600);

        return hours + "h" + minutes + "m" + seconds + "s" + milliseconds + "ms";
    }

    public void generate(Handler<AsyncResult<String>> handler) {
        if (areNullParameters()) {
            handler.handle(Future.failedFuture("[Viescolaire@TrombinoscopeReport::generate] can't generate report " +
                    "with null parameters"));
            return;
        }

        JsonObject params = new JsonObject()
                .put("UAI", this.getUai())
                .put("structureId", this.getStructureId())
                .put("date", this.startDate)
                .put("startTime", this.startDate)
                .put("endTime", this.endDate)
                .put("runTime", this.runTime())
                .put("correctFile", this.isCorrectFile)
                .put("recordFile", this.getFileRecordMessage())
                .put("reports", new JsonArray(this.reports.stream().map(ReportException::toJSON).collect(Collectors.toList())));

        this.templateProcessor.processTemplate("trombinoscope/report/trombinoscope-report.txt", params, report -> {
            if (report == null) {
                this.content = "[Viescolaire@TrombinoscopeReport::generate] failed to generate report.";
                handler.handle(Future.failedFuture(this.content));
            } else {
                this.content = report;
                handler.handle(Future.succeededFuture(this.content));
            }
        });
    }

    private boolean areNullParameters() {
        return this.getUai() == null || this.getStructureId() == null || this.startDate == null || this.endDate == null;
    }

    public void save(Handler<AsyncResult<Void>> handler) {
        JsonObject document = new JsonObject()
                .put("createdAt", DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT))
                .put("UAI", this.getUai())
                .put("structureId", this.getStructureId())
                .put("content", this.content);

        String collection = Viescolaire.VSCO_SCHEMA + ".trombinoscopeReport";
        MongoDb.getInstance().save(collection, document, MongoDbResult.validResultHandler(res -> {
            if (res.isLeft()) handler.handle(Future.failedFuture(res.left().getValue()));
            else handler.handle(Future.succeededFuture());
        }));
    }

    public String getUai() {
        return uai;
    }

    public void setUai(String uai) {
        this.uai = uai;
    }

    public String getStructureId() {
        return structureId;
    }

    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    public List<ReportException> getReports() {
        return reports;
    }

    public void setReports(List<ReportException> reports) {
        this.reports = reports;
    }

    public void addReport(ReportException report) {
        this.reports.add(report);
    }

    public String getFileRecordMessage() {
        return fileRecordMessage;
    }

    public void setFileRecordMessage(String fileRecordMessage, Boolean correctFile) {
        this.fileRecordMessage = fileRecordMessage;
        this.isCorrectFile = correctFile;
        this.end();
    }

    public void setFileRecordMessage(Boolean correctFile) {
        this.fileRecordMessage = "";
        this.isCorrectFile = correctFile;
        this.end();
    }

    public Boolean isCorrectFile() {
        return isCorrectFile;
    }
}
