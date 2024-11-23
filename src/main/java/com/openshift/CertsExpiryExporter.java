package com.openshift;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CertsExpiryExporter {

    // Create a Logger
    private static final Logger logger = Logger.getLogger(CertsExpiryExporter.class.getName());

    // Define Prometheus Gauges
    static final Gauge certIssuedAtGauge = Gauge.build()
            .name("certutils_certificate_issue_timet")
            .labelNames("name")
            .help("TLS Certificate issue time in seconds since epoch")
            .register();

    static final Gauge certExpiryGauge = Gauge.build()
            .name("certutils_certificate_expiry_time")
            .labelNames("name")
            .help("TLS Certificate expiry time in seconds since epoch")
            .register();

    public static X509Certificate fetchCertificate(String httpsUrl) throws Exception {
        try {
            URL url = new URL(httpsUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.connect();

            Certificate[] certs = conn.getServerCertificates();
            if (certs.length > 0 && certs[0] instanceof X509Certificate) {
                return (X509Certificate) certs[0];
            }

            throw new RuntimeException("No X509Certificate found on " + httpsUrl);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching certificate", e);
            System.exit(1);
        }
        return null;
    }

    public static void updateMetrics(String httpsUrl) throws Exception {
        X509Certificate cert = fetchCertificate(httpsUrl);

        // Get issue and expiry time
        Date notBefore = cert.getNotBefore();
        Date notAfter = cert.getNotAfter();
        logger.info("notBefore: " + notBefore);
        logger.info("notAfter: " + notAfter);

        String appName = System.getenv("APP_NAME");

        // Conrt to seconds since epoch and set the metrics
        certIssuedAtGauge.labels(appName).set(notBefore.toInstant().getEpochSecond());
        certExpiryGauge.labels(appName).set(notAfter.toInstant().getEpochSecond());
    }

    public static void main(String[] args) throws Exception {

        // Create a ConsoleHandler with a SimpleFormatter
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter());

        // Add the handler to the logger
        logger.addHandler(consoleHandler);

        // Start Prometheus HTTP server to expose metrics
        HTTPServer server = new HTTPServer(8080);
        logger.info("Prometheus metrics exposed at http://localhost:8080/metrics");

        // URL of the server to check
        String httpsUrl = System.getenv("CHECK_HTTPS_URL");
        if (httpsUrl != null) {
            httpsUrl = httpsUrl.trim();
        } else {
            httpsUrl = "https://baidu.com";
        }

        logger.info("Checking certificate for " + httpsUrl);
        // Periodically update the metrics, here just called once for simplicity
        updateMetrics(httpsUrl);

        // Prevent the main thread from exiting
        while (true) {
            Thread.sleep(60000); // Sleep for 1 minute before refreshing metrics
            logger.info("Checking certificate for " + httpsUrl);
            updateMetrics(httpsUrl);
        }
    }
}

