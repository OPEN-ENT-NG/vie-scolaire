package fr.openent.viescolaire.core.enums;

public enum TrombinoscopeError {
    // An error has occurred while fetching structure info
    STRUCTURE_FAILURE("viescolaire.trombinoscope.failure.structure.failure"),
    // An error has occurred during failure clear history
    TROMBINOSCOPE_CLEAR_FAILURES_HISTORY_FAIL("viescolaire.trombinoscope.failure.clear.failures.fail"),
    // An error has occured during upload file in targeted path in vertx
    UPLOAD_FILE_FAILURE("viescolaire.trombinoscope.failure.upload.file.fail"),
    // An error has occured during unzipping file
    UNZIP_FILE_FAILURE("viescolaire.trombinoscope.failure.unzip.file.fail"),
    // An error has occurred while attempting to create trombinoscope
    TROMBINOSCOPE_CREATE_FAILURE("viescolaire.trombinoscope.failure.create"),
    // An error has occurred while reading unzipped file
    DIRECTORY_READ_FAILURE("viescolaire.trombinoscope.failure.rootPath"),
    // Error while trying to fetch students from their audience's name
    FETCH_STUDENT_BY_NAME_FAILURE("viescolaire.trombinoscope.failure.retrieve.request.students"),
    // Impossible to retrieve students linked by its audience
    RETRIEVE_LINKED_STUDENT_AUDIENCE_FAILURE("viescolaire.trombinoscope.failure.retrieve.directory.students"),
    // Extension is not available
    EXTENSION_FILE_FAILURE("viescolaire.trombinoscope.failure.extension"),
    // An error has occurred while writing provided file into vertx
    WRITING_FILE_FAILURE("viescolaire.trombinoscope.failure.picture"),
    // Wrong file format
    FILE_FORMAT_FAILURE("viescolaire.trombinoscope.failure.format.picture.name"),
    // No matching with students
    MATCH_STUDENT_FAILURE("viescolaire.trombinoscope.failure.no.match.student"),
    // Failure : found too many students
    TOO_MANY_STUDENTS_FOUND_FAILURE( "viescolaire.trombinoscope.failure.to.many.matches.student");

    private final String key;

    TrombinoscopeError(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }
}
