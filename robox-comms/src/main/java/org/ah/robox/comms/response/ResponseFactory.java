/*******************************************************************************
 * Copyright (c) 2014 Creative Sphere Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Creative Sphere - initial API and implementation
 *
 *
 *******************************************************************************/
package org.ah.robox.comms.response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import org.ah.robox.comms.utils.PrintHex;

/**
 *
 * @author Daniel Sendula
 */
public class ResponseFactory {

    public static final int GET_PRINT_JOBS_RESPONSE = 0xe0;
    public static final int PRINTER_STATUS_RESPONSE = 0xe1;
    public static final int STANDARD_RESPONSE = 0xe3;
    public static final int PRINTER_DETAILS_RESPONSE = 0xe5;
    public static final int GCODE_RESPONSE = 0xe7;

    public static boolean DEBUG = false;

    private InputStream in;
    private byte[] buffer;
    private int ptr;
    private Response response;

    public ResponseFactory(InputStream in) {
        this.in = in;
    }

    public Response readResponse() throws IOException {

        long zeroByteRetry = 2500; // 2.5 seconds
        long now = System.currentTimeMillis();

        int r = 0;
        while (r == 0 && (System.currentTimeMillis() - now < zeroByteRetry)) {
            r = in.read();
            if (r < 0) { r = 256 + r; }
            if (r == 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignore) {}
            }
        }

        if (r == PRINTER_STATUS_RESPONSE) {
            response = new PrinterStatusResponse();
            buffer = new byte[210];
            ptr = 0;
            readBuffer(in, buffer);

            extractString("printJob", 16, TRIM_STRING_CONVERTER);
            extractString("lineNumber", 8, HEX_STRING_TO_INTEGER_CONVERTER);
            extractString("pause", 1, STRING_TO_PRINTER_PAUSE_CONVERTER);
            extractByte("busy", BYTE_TO_BOOLEAN_CONVERTER);

            extractByte("xLimit", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("yLimit", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("zLimit", BYTE_TO_BOOLEAN_CONVERTER);

            extractByte("filament1", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("filament2", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("nozzleSwitch", BYTE_TO_BOOLEAN_CONVERTER);

            extractByte("doorOpen", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("reelButton", BYTE_TO_BOOLEAN_CONVERTER);
            skip(5);
            skip(1);

            extractString("nozzleTemperature", 8, TRIM_STRING_CONVERTER);
            extractString("nozzleSetTemperature", 8, TRIM_STRING_CONVERTER);

            skip(9);

            skip(25);

            extractString("bedTemperature", 8, TRIM_STRING_CONVERTER);
            extractString("bedSetTemperature", 8, TRIM_STRING_CONVERTER);

            skip(8);
            extractByte("fan", BYTE_TO_BOOLEAN_CONVERTER);

            extractString("ambientTemperature", 8, TRIM_STRING_CONVERTER);
            extractString("ambientSetTemperature", 8, TRIM_STRING_CONVERTER);

            extractByte("headFan", BYTE_TO_BOOLEAN_CONVERTER);

            extractString("temperatureState", 1, STRING_TO_TEMPERATURE_STATE_CONVERTER);

            skip(5);

            extractString("xPosition", 8, TRIM_STRING_CONVERTER);
            extractString("yPosition", 8, TRIM_STRING_CONVERTER);
            extractString("zPosition", 8, TRIM_STRING_CONVERTER);

            skip(8);

            skip(8);
            extractString("filamentMultiplier", 8, TRIM_STRING_CONVERTER);
            skip(8);
            skip(8);
            extractString("feedRateMultiplier", 8, TRIM_STRING_CONVERTER);


        } else if (r == STANDARD_RESPONSE) {
            StandardResponse response = new StandardResponse();
            this.response = response;
            buffer = new byte[32];
            ptr = 0;
            readBuffer(in, buffer);

            extractByte("error1", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("sequenceError", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("fileTooLargeError", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("tooLongLine", BYTE_TO_BOOLEAN_CONVERTER);

            extractByte("error5", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error6", BYTE_TO_BOOLEAN_CONVERTER);

            extractByte("unknownCommand", BYTE_TO_BOOLEAN_CONVERTER);

            extractByte("error8", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error9", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error10", BYTE_TO_BOOLEAN_CONVERTER);

            extractByte("bufferOverFlow", BYTE_TO_BOOLEAN_CONVERTER);

            extractByte("error12", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error13", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error14", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error15", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error16", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error17", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error18", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error19", BYTE_TO_BOOLEAN_CONVERTER);
            extractByte("error20", BYTE_TO_BOOLEAN_CONVERTER);

        } else if (r == PRINTER_DETAILS_RESPONSE) {
            response = new PrinterDetailsResponse();
            buffer = new byte[256];
            ptr = 0;
            readBuffer(in, buffer);

            extractString("model", 5, TRIM_STRING_CONVERTER);
            extractString("serialNumber", 17, TRIM_STRING_CONVERTER);
            skip(42);
            extractString("printerId", 100, TRIM_STRING_CONVERTER);
            skip(86);
            extractString("colour", 6, TRIM_STRING_CONVERTER);
        } else if (r == GCODE_RESPONSE) {
            response = new GCodeResponse();
            buffer = new byte[4];
            ptr = 0;
            readBuffer(in, buffer);

            int packetSize = Integer.valueOf(new String(buffer, "US-ASCII"), 16);

            buffer = new byte[packetSize];
            readBuffer(in, buffer);

            ((GCodeResponse)response).setResponse(new String(buffer, "US-ASCII"));
        } else if (r == GET_PRINT_JOBS_RESPONSE) {
            PrintJobsResponse printJobsResponse = new PrintJobsResponse();
            response = printJobsResponse;
            buffer = new byte[2];
            readBuffer(in, buffer);

            int printJobs = Integer.valueOf(new String(buffer, "US-ASCII"), 16);

            buffer = new byte[printJobs * 16];
            readBuffer(in, buffer);

            for (int i = 0; i < printJobs; i++) {
                String printJob = new String(buffer, i * 16, 16, "US-ASCII");
                printJob = TRIM_STRING_CONVERTER.convert(printJob);
                printJobsResponse.getPrintJobs().add(printJob);
            }

        } else {
            return new UnknownResponse(r);
        }
        return response;
    }

    protected void readBuffer(InputStream is, byte[] buffer) throws IOException {
        int r = is.read(buffer);
        int read = r;
        while (r > 0 && read < buffer.length) {
            r = is.read(buffer, read, buffer.length - read);
            read = read + r;
        }

        if (DEBUG) {
            System.out.println("Received packet:");
            PrintHex.printHex(buffer);
        }
    }

    protected void skip(int size) {
        ptr = ptr + size;
    }

    protected void extractString(String propertyName, int size) {
        extractString(propertyName, size, NULL_STRING_CONVERTER);
    }

    protected void extractString(String propertyName, int size, Converter<String, ?> converter) {
        try {
            String str = new String(buffer, ptr, size, "US-ASCII");
            str = str.replace(Character.valueOf((char)127).toString(), "");
            ptr = ptr + size;

            Object value = converter.convert(str);

            set(value, propertyName, value.getClass());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected void extractByte(String propertyName, Converter<Byte, ?> converter) {
        byte b = buffer[ptr];
        ptr = ptr + 1;

        Object value = converter.convert(b);

        set(value, propertyName, value.getClass());
    }

    protected void extractBytes(String propertyName, int size, Converter<byte[], ?> converter) {
        byte[] data = new byte[size];
        System.arraycopy(buffer, ptr, data, 0, size);
        ptr = ptr + size;

        Object value = converter.convert(data);

        set(value, propertyName, value.getClass());
    }

    protected void set(Object value, String name, Class<?> cls) {
        Method setter = null;
        try {
            String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            try {
                setter = response.getClass().getDeclaredMethod(setterName, cls);
            } catch (NoSuchMethodException e) {
                if (cls == Integer.class) {
                    setter = response.getClass().getDeclaredMethod(setterName, Integer.TYPE);
                } else if (cls == Byte.class) {
                    setter = response.getClass().getDeclaredMethod(setterName, Byte.TYPE);
                } else if (cls == Short.class) {
                    setter = response.getClass().getDeclaredMethod(setterName, Short.TYPE);
                } else if (cls == Character.class) {
                    setter = response.getClass().getDeclaredMethod(setterName, Character.TYPE);
                } else if (cls == Float.class) {
                    setter = response.getClass().getDeclaredMethod(setterName, Float.TYPE);
                } else if (cls == Double.class) {
                    setter = response.getClass().getDeclaredMethod(setterName, Double.TYPE);
                } else if (cls == Boolean.class) {
                    setter = response.getClass().getDeclaredMethod(setterName, Boolean.TYPE);
                } else {
                    throw e;
                }
            }
            setter.invoke(response, value);
        } catch (IllegalArgumentException e) {
            if (value != null) {
                System.err.println("Got " + value + " of " + value.getClass() + " and tried to set through method " + setter);
            } else {
                System.err.println("Got null and tried to set through method " + setter);
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static NullStringConverter NULL_STRING_CONVERTER = new NullStringConverter();
    public static TrimStringConverter TRIM_STRING_CONVERTER = new TrimStringConverter();
    public static HexStringToInteger HEX_STRING_TO_INTEGER_CONVERTER = new HexStringToInteger();
    public static HexStringToInteger STRING_TO_INTEGER_CONVERTER = new HexStringToInteger();
    public static StringToPrinterPause STRING_TO_PRINTER_PAUSE_CONVERTER = new StringToPrinterPause();
    public static StringToTemperatureState STRING_TO_TEMPERATURE_STATE_CONVERTER = new StringToTemperatureState();

    public static ByteToBooleanConverter BYTE_TO_BOOLEAN_CONVERTER = new ByteToBooleanConverter();

    public static interface Converter<S, D> {
        D convert(S source0);
    }

    public static class NullStringConverter implements Converter<String, String> {
        public String convert(String source) { return source; }
    }

    public static class TrimStringConverter implements Converter<String, String> {
        public String convert(String source) { return source != null ? source.trim() : source; }
    }

    public static class HexStringToInteger implements Converter<String, Integer> {
        public Integer convert(String source) {
            int i = 0;
            try {
                i = Integer.parseInt(source, 16);
            } catch (NumberFormatException ignore) {
            }
            return i;
        }
    }

    public static class StringToInteger implements Converter<String, Integer> {
        public Integer convert(String source) {
            int i = 0;
            try {
                i = Integer.parseInt(source);
            } catch (NumberFormatException ignore) {
            }
            return i;
        }
    }

    public static class StringToPrinterPause implements Converter<String, PrinterPause> {
        public PrinterPause convert(String source) {
            int i = -1;
            try {
                i = Integer.parseInt(source, 16);
            } catch (NumberFormatException ignore) {
            }

            for (PrinterPause value : PrinterPause.values()) {
                if (value.getValue() == i) {
                    return value;
                }
            }

            throw new IllegalStateException("Unknown printer pause number: " + i);
        }
    }

    public static class StringToTemperatureState implements Converter<String, TemperatureState> {
        public TemperatureState convert(String source) {
            int i = -1;
            try {
                i = Integer.parseInt(source, 16);
            } catch (NumberFormatException ignore) {
            }

            for (TemperatureState value : TemperatureState.values()) {
                if (value.getState() == i) {
                    return value;
                }
            }

            throw new IllegalStateException("Unknown temperature state number: " + i);
        }
    }

    public static class ByteToBooleanConverter implements Converter<Byte, Boolean> {
        public Boolean convert(Byte source) { return (source & 1) != 0; }
    }

}
