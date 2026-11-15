package co.assip.erp.ses.formato9027.dto;

import java.util.LinkedHashMap;

/** Los 62 campos oficiales conservan el orden de inserción para exportación. */
public class Formato9027DTO extends LinkedHashMap<String, String> {
    public Formato9027DTO() {
        super(64);
    }
}
