package com.banfftech.reactodata.edmconfig;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;

import java.util.HashMap;
import java.util.Map;

public class DataMapper {
    public final static Map<String, EdmPrimitiveTypeKind> FIELDMAP = new HashMap<String, EdmPrimitiveTypeKind>() {
        {
            put("id", EdmPrimitiveTypeKind.String);
            put("id-ne", EdmPrimitiveTypeKind.String);
            put("id-long", EdmPrimitiveTypeKind.String);
            put("id-long-ne", EdmPrimitiveTypeKind.String);
            put("id-vlong", EdmPrimitiveTypeKind.String);
            put("id-vlong-ne", EdmPrimitiveTypeKind.String);
            put("comment", EdmPrimitiveTypeKind.String);
            put("description", EdmPrimitiveTypeKind.String);
            put("currency-precise", EdmPrimitiveTypeKind.Decimal);
            put("currency-amount", EdmPrimitiveTypeKind.Decimal);
            put("fixed-point", EdmPrimitiveTypeKind.Decimal);
            put("date-time", EdmPrimitiveTypeKind.DateTimeOffset);
            put("date", EdmPrimitiveTypeKind.Date);
            put("indicator", EdmPrimitiveTypeKind.String);
            // put("indicatorStr", EdmPrimitiveTypeKind.String);
            put("floating-point", EdmPrimitiveTypeKind.Double);
            put("long-varchar", EdmPrimitiveTypeKind.String);
            put("short-varchar", EdmPrimitiveTypeKind.String);
            put("very-long", EdmPrimitiveTypeKind.String);
            put("very-short", EdmPrimitiveTypeKind.String);
            put("value", EdmPrimitiveTypeKind.String);
            put("name", EdmPrimitiveTypeKind.String);
            put("url", EdmPrimitiveTypeKind.String);
            put("numeric", EdmPrimitiveTypeKind.Int64);
            put("blob", EdmPrimitiveTypeKind.Binary);
            put("byte-array", EdmPrimitiveTypeKind.Stream);
            put("object", EdmPrimitiveTypeKind.Binary);
            put("time", EdmPrimitiveTypeKind.TimeOfDay);
            put("email", EdmPrimitiveTypeKind.String);
            put("credit-card-number", EdmPrimitiveTypeKind.String);
            put("credit-card-date", EdmPrimitiveTypeKind.String);
            put("tel-number", EdmPrimitiveTypeKind.String);
        }
    };
}
