package com.huertos.parcelmongo.exception;

public final class ErrorMessages {

    public static final String PARCEL_NOT_FOUND = "No se encontró la parcela con id '%s'";
    public static final String PARCEL_DUPLICATE = "Ya existe una parcela registrada con el nombre '%s'";
    public static final String PARCEL_DUPLICATE_GENERIC = "Ya existe una parcela registrada con ese nombre";
    public static final String PARCEL_ID_INVALID = "El identificador de la parcela no tiene un formato válido";
    public static final String PARCEL_DATA_INVALID = "La parcela contiene datos inválidos";
    public static final String PARCEL_INTEGRITY = "No se pudo registrar la parcela porque los datos violan una restricción de integridad";
    public static final String SOIL_TYPE_INVALID = "El tipo de suelo debe ser ARCILLOSO, LIMOSO o ARENOSO";
    public static final String BODY_INVALID = "El cuerpo de la solicitud es inválido o tiene un formato incorrecto";
    public static final String RESOURCE_NOT_FOUND = "El recurso solicitado no existe";
    public static final String INTERNAL_ERROR = "Ocurrió un error interno al procesar la solicitud. Intente nuevamente más tarde.";
    public static final String MALICIOUS_CONTENT = "El texto contiene caracteres o contenido no permitido";
    public static final String TEXT_PATTERN = "Solo se permiten letras, números, espacios, apóstrofos, comas, puntos y guiones";

    private ErrorMessages() {
    }

    public static String sanitizedRequired(String label) {
        return "El " + label + " es obligatorio después de sanitizar el texto";
    }

    public static String sanitizedSize(String label, int min, int max) {
        return "El " + label + " debe tener entre " + min + " y " + max + " caracteres";
    }
}
