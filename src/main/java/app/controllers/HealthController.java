package app.controllers;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

public class HealthController {
        private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);


    public void apiHealthCheck(Context ctx){
        //Logger is configured to only write in console --> logback.xml
        LOGGER.info("WebScraper API Health check initiated at date: " + Instant.now());
        /**
         * {
         *   "status": "OK",
         *   "timestamp": "2025-10-23T13:45:56Z",
         *   "service": "WebScraper API"
         * }
         */
        ctx.status(200).json(Map.of("status", "OK", "timestamp",
                Instant.now().toString(), "service", "WebScaper API"));
    }

}
