package com.nhnacademy.system;

public enum ModbusFunctionCode {
    READ_COILS(1),
    READ_DISCRETE_INPUTS(2),
    READ_HOLDING_REGISTERS(3),
    READ_INPUT_REGISTERS(4),
    WRITE_SINGLE_COIL(5),
    WRITE_SINGLE_REGISTER(6),
    WRITE_MULTIPLE_COILS(15),
    WRITE_MULTIPLE_REGISTERS(16);

    private final int code;

    ModbusFunctionCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static ModbusFunctionCode fromCode(int code) {
        for (ModbusFunctionCode functionCode : ModbusFunctionCode.values()) {
            if (functionCode.getCode() == code) {
                return functionCode;
            }
        }
        throw new IllegalArgumentException("Invalid Function code : " + code);
    }
}
