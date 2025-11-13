package com.andinobus.backendsmartcode.planificacion;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class CsvPlanificacionParser {

    private static final List<Character> DELIMS = List.of(',', ';', '\t');

    public PlanificacionParseResult parse(InputStream inputStream, String sourceName) throws IOException {
        BufferedInputStream bis = (inputStream instanceof BufferedInputStream) ? (BufferedInputStream) inputStream : new BufferedInputStream(inputStream);
        bis.mark(4);
        Charset charset = detectCharset(bis);
        // detectCharset performed read+reset on the same stream; ensure position at start
        return parse(new InputStreamReader(bis, charset), sourceName);
    }

    // For tests and string inputs
    public PlanificacionParseResult parse(String content, String sourceName) {
        return parse(new StringReader(content), sourceName);
    }

    private PlanificacionParseResult parse(Reader reader, String sourceName) {
        PlanificacionParseResult result = new PlanificacionParseResult();
        result.setSourceName(sourceName);
        try (BufferedReader br = new BufferedReader(reader)) {
            // Buscar línea de encabezados real (algunos archivos tienen un título en la primera línea)
            String headerLine = null;
            Map<String, Integer> headerIndex = null;
            char delim = ',';
            String line;
            int currentRow = 0;
            while ((line = br.readLine()) != null) {
                currentRow++;
                if (line.trim().isEmpty()) continue;
                char candidateDelim = detectDelimiter(line);
                List<String> candidateHeaders = splitCsvLine(line, candidateDelim);
                Map<String, Integer> candidateIndex = mapHeaders(candidateHeaders);
                boolean hasCore = candidateIndex.containsKey("origen") || candidateIndex.containsKey("destino") || candidateIndex.containsKey("horadesalida");
                if (hasCore) {
                    // Aceptar esta línea como encabezado
                    headerLine = line;
                    headerIndex = candidateIndex;
                    delim = candidateDelim;
                    break;
                }
                // si no, seguir leyendo hasta encontrar encabezado
            }
            if (headerLine == null) {
                result.addWarning("No se encontraron encabezados válidos en el archivo");
                return result;
            }

            if (!headerIndex.containsKey("origen") || !headerIndex.containsKey("destino")) {
                result.addWarning("Encabezados requeridos no encontrados: ORIGEN y DESTINO");
            }
            if (!headerIndex.containsKey("horadesalida")) {
                result.addWarning("Encabezado requerido no encontrado: HORA DE SALIDA");
            }

            int row = currentRow; // número de fila del encabezado
            while ((line = br.readLine()) != null) {
                row++;
                if (line.trim().isEmpty()) continue;
                List<String> cols = splitCsvLine(line, delim);
                String origen = getCol(cols, headerIndex.get("origen"));
                String destino = getCol(cols, headerIndex.get("destino"));
                String horaSalidaRaw = getCol(cols, headerIndex.get("horadesalida"));
                String horaLlegadaRaw = getCol(cols, headerIndex.get("horadellegada"));

                if (isBlank(origen) && isBlank(destino) && isBlank(horaSalidaRaw) && isBlank(horaLlegadaRaw)) {
                    continue; // skip empty rows
                }

                LocalTime horaSalida = parseTime(horaSalidaRaw);
                LocalTime horaLlegada = parseTime(horaLlegadaRaw);

                String error = null;
                if (isBlank(origen) || isBlank(destino)) {
                    error = joinError(error, "Falta ORIGEN o DESTINO");
                }
                if (horaSalidaRaw != null && !horaSalidaRaw.isBlank() && horaSalida == null) {
                    error = joinError(error, "Formato inválido en HORA DE SALIDA: '" + horaSalidaRaw + "'");
                }
                if (horaLlegadaRaw != null && !horaLlegadaRaw.isBlank() && horaLlegada == null) {
                    error = joinError(error, "Formato inválido en HORA DE LLEGADA: '" + horaLlegadaRaw + "'");
                }

                PlanificacionItem item = new PlanificacionItem(origen, destino, horaSalida, horaLlegada, row, safe(horaSalidaRaw), safe(horaLlegadaRaw), error);
                result.addItem(item);
            }
        } catch (IOException e) {
            result.addWarning("Error leyendo archivo: " + e.getMessage());
        }
        return result;
    }

    private static Charset detectCharset(InputStream is) throws IOException {
        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }
        is.mark(3);
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        is.reset();
        // BOM checks
        if (b1 == 0xEF && b2 == 0xBB && b3 == 0xBF) {
            return StandardCharsets.UTF_8; // UTF-8 BOM
        }
        return StandardCharsets.UTF_8; // default
    }

    private static char detectDelimiter(String headerLine) {
        int bestCount = -1;
        char best = ',';
        for (char d : DELIMS) {
            int c = countOccurrences(headerLine, d);
            if (c > bestCount) { bestCount = c; best = d; }
        }
        return best;
    }

    private static int countOccurrences(String s, char ch) {
        int c = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == ch) c++;
        return c;
    }

    private static final Pattern SPLIT_PATTERN_CACHELESS = Pattern.compile(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");

    private static List<String> splitCsvLine(String line, char delim) {
        // Very simple CSV split with quote handling for commas; for other delimiters, split by delim and strip quotes
        List<String> out = new ArrayList<>();
        if (delim == ',') {
            // Split on commas not inside quotes
            String[] parts = SPLIT_PATTERN_CACHELESS.split(line, -1);
            for (String p : parts) out.add(unquote(p.trim()));
            return out;
        }
        // Generic split by delimiter, not handling embedded quotes across delimiters (good enough for our inputs)
        String[] parts = line.split(Pattern.quote(String.valueOf(delim)), -1);
        for (String p : parts) out.add(unquote(p.trim()));
        return out;
    }

    private static String unquote(String s) {
        if (s == null) return null;
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static Map<String, Integer> mapHeaders(List<String> headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String k = normalize(headers.get(i));
            // canonical keys
            if (matches(k, "origen") || matches(k, "ciudadorigen") || matches(k, "desde") ) {
                map.put("origen", i);
            } else if (matches(k, "destino") || matches(k, "ciudaddestino") || matches(k, "hasta")) {
                map.put("destino", i);
            } else if (matches(k, "horadesalida") || matches(k, "salida") || matches(k, "hora_salida") ) {
                map.put("horadesalida", i);
            } else if (matches(k, "horadellegada") || matches(k, "llegada") || matches(k, "hora_llegada") || matches(k, "horadellegadabus") ) {
                map.put("horadellegada", i);
            }
        }
        return map;
    }

    private static boolean matches(String normalized, String target) {
        return normalized.equals(target);
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        n = n.toLowerCase(Locale.ROOT).trim();
        // remove non alphanumeric
        n = n.replaceAll("[^a-z0-9]", "");
        return n;
    }

    private static String getCol(List<String> cols, Integer idx) {
        if (idx == null) return null;
        if (idx < 0 || idx >= cols.size()) return null;
        String v = cols.get(idx);
        return v != null ? v.trim() : null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String s) { return isBlank(s) ? null : s; }

    private static String joinError(String base, String add) {
        if (base == null || base.isBlank()) return add;
        return base + "; " + add;
    }

    private static final List<DateTimeFormatter> TIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H'h'mm")
    );

    private static LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String r = raw.trim();
        // Normalize variations like 7.30, 7-30
        r = r.replace('.', ':').replace('-', ':');
        for (DateTimeFormatter f : TIME_FORMATS) {
            try {
                return LocalTime.parse(r, f);
            } catch (DateTimeParseException ignored) {}
        }
        // Try to coerce single numbers like "7" → 07:00
        if (r.matches("^\\d{1,2}$")) {
            try { return LocalTime.of(Integer.parseInt(r), 0); } catch (Exception ignored) {}
        }
        // Try patterns like 0730 or 1230 → 07:30
        if (r.matches("^\\d{3,4}$")) {
            try {
                int val = Integer.parseInt(r);
                int hour = val / 100; int min = val % 100;
                if (hour >=0 && hour <= 23 && min >=0 && min <=59) return LocalTime.of(hour, min);
            } catch (Exception ignored) {}
        }
        return null;
    }

}