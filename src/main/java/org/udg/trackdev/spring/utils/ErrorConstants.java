package org.udg.trackdev.spring.utils;

public final class ErrorConstants {

    public static final String USER_MAIL_NOT_FOUND = "No existeix cap usuari amb aquest correu electrònic";
    public static final String USER_NOT_FOUND = "L'usuari no existeix <%s>";
    public static final String USER_ALREADY_EXIST = "L'usuari ja existeix";
    public static final String LOGIN_KO = "El correu electrònic o la contrassenya no són correctes";
    public static final String USER_DISABLED = "L'usuari no està habilitat, contacta amb l'administrador";
    public static final String REGISTER_KO = "Error al registrar l'usuari";
    public static final String RECOVERY_CODE_NOT_MATCH = "El codi de recuperació no és correcte";
    public static final String USER_SHOULD_NOT_LOGEDIN = "El usuari no hauria d'estar autenticat";
    public static final String USER_NOT_LOGGED_IN = "El usuari ha d'estar autenticat";
    public static final String UNKNOWN_ROLE = "Rol desconegut";
    public static final String INVALID_PRJ_NAME_LENGTH = "El nom del projecte ha de tindre entre 1 i 50 caràcters";
    public static final String INVALID_COURSE_START_YEAR = "L'any seleccionat no és vàlid";

    public static final String INVALID_PRJ_QUALIFICATION = "La qualifiació ha de ser un valor entre 0 i 10";
    public static final String INVALID_SPRINT_NAME_LENGTH = "El nom del sprint ha de tindre entre 1 i 50 caràcters";
    public static final String INVALID_SUBJECT_NAME_LENGTH = "El nom de l'assignatura ha de tindre entre 1 i 50 caràcters";
    public static final String INVALID_SUBJECT_ACRONYM_LENGTH = "El acronim de l'assignatura ha de tindre entre 3 i 5 caràcters";
    public static final String INVALID_TASK_NAME_LENGTH = "El nom de la tasca ha de tindre entre 1 i 100 caràcters";
    public static final String PASSWORD_MINIUM_LENGTH = "La contrassenya ha de tindre almenys 8 caracters";
    public static final String INVALID_MAIL_FORMAT = "El correu electrònic no té un format vàlid";
    public static final String INVALID_MAIL_SIZE = "La longitud del correu electrònic no és vàlida";
    public static final String INVALID_USERNAME_SIZE = "La longitud del nom d'usuari ha de tindre entre 1 i 50 caràcters";
    public static final String INVALID_USERNAME_FORMAT = "El nom d'usuari conte caràcters no vàlids";
    public static final String INVALID_PASSWORD_FORMAT = "La contrassenya ha de contenir almenys una lletra minuscula, una majuscula i un numero";
    public static final String UNAUTHORIZED = "L'usuari no esta autoritzat a realitzar aquesta acció o veure aquest recurs";
    public static final String TASK_ALREADY_REVIEWED = "Aquest usuari ja ha revisat els punts d'aquesta tasca";
    public static final String PRJ_WITHOUT_MEMBERS = "El projecte ha de tindre almenys un membre";
    public static final String PRJ_WITHOUT_QUALIFICATION = "No es pot calcular la nota d'un projecte sense qualificació";
    public static final String SUBJECT_NOT_EXIST = "L'assignatura no existeix";
    public static final String USER_NOT_PRJ_MEMBER = "L'usuari no és membre del projecte";
    public static final String CAN_NOT_BE_NULL = "Hi ha un camp a NULL que no pot ser-ho";
    public static final String ENTITY_NOT_EXIST = "L'entitat no existeix";
    public static final String GITHUB_TOKEN_INVALID = "ERROR: Token invalid";
    public static final String API_GITHUB_KO = "ERROR: L'API de Github no funciona";


}
