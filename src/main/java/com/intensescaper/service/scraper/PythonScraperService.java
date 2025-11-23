package com.intensescaper.service.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intensescaper.entity.Ilan;
import com.intensescaper.entity.Kullanici;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Python-based scraper using undetected-chromedriver
 * Better Cloudflare bypass than Java Selenium
 */
@Service("pythonScraper")
@Primary // Use this scraper by default
@Slf4j
public class PythonScraperService implements ScraperService {

    private static final String PYTHON_SCRIPT = "scripts/sahibinden_scraper.py";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String url) {
        return url != null && url.contains("sahibinden.com");
    }

    @Override
    public String getSiteName() {
        return "sahibinden.com";
    }

    @Override
    public List<Ilan> scrape(String url, Kullanici kullanici) {
        log.info("🐍 Python scraper starting for: {}", url);

        List<Ilan> ilanlar = new ArrayList<>();

        try {
            // Get absolute path to script
            String scriptPath = Paths.get(PYTHON_SCRIPT).toAbsolutePath().toString();

            // Build command
            ProcessBuilder pb = new ProcessBuilder("python3", scriptPath, url);
            pb.redirectErrorStream(false); // Separate stderr for logging

            log.info("Executing: python3 {} {}", scriptPath, url);

            Process process = pb.start();

            // Read stdout (JSON data)
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Read stderr (logs)
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    log.info("[Python] {}", line);
                }
            }

            // Wait for process to complete
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("Python script exited with code: {}", exitCode);
                throw new RuntimeException("Python scraper failed with exit code: " + exitCode);
            }

            // Parse JSON output
            String jsonOutput = output.toString().trim();
            log.info("Python output: {}", jsonOutput);

            if (jsonOutput.isEmpty()) {
                log.warn("Python script returned empty output");
                return ilanlar;
            }

            JsonNode data = objectMapper.readTree(jsonOutput);

            // Check for error
            if (data.has("error")) {
                log.error("Python scraper error: {}", data.get("error").asText());
                throw new RuntimeException("Python scraper error: " + data.get("error").asText());
            }

            // Convert to Ilan entity
            Ilan ilan = new Ilan();
            ilan.setSite("sahibinden.com");
            ilan.setIlanUrl(url);
            ilan.setBaslik(getTextValue(data, "baslik"));

            // Parse price (remove " TL" and convert to Double)
            String fiyatStr = getTextValue(data, "fiyat");
            if (fiyatStr != null) {
                try {
                    fiyatStr = fiyatStr.replace(" TL", "").replace(".", "").replace(",", ".");
                    ilan.setFiyat(Double.parseDouble(fiyatStr));
                } catch (NumberFormatException e) {
                    log.warn("Could not parse price: {}", fiyatStr);
                }
            }

            ilan.setTelefonNumarasi(getTextValue(data, "telefonNumarasi"));
            ilan.setKonum(getTextValue(data, "konum"));
            ilan.setIlanTarihi(getTextValue(data, "ilanTarihi"));
            ilan.setAciklama(getTextValue(data, "aciklama"));
            ilan.setKullanici(kullanici);

            ilanlar.add(ilan);

            log.info("✅ Python scraper extracted: Title={}, Phone={}",
                    ilan.getBaslik(), ilan.getTelefonNumarasi());

        } catch (Exception e) {
            log.error("Python scraper failed", e);
            throw new RuntimeException("Python scraper failed: " + e.getMessage(), e);
        }

        return ilanlar;
    }

    private String getTextValue(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
    }
}
