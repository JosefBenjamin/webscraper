package app.routes;

import app.config.HibernateConfig;
import app.controllers.CrawlLogController;
import app.controllers.HealthController;
import app.controllers.SourceController;
import app.services.ScrapeLoggerService;
import app.services.SourceService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    //TODO: Dependencies
    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    ScrapeLoggerService scrapeLoggerService = new ScrapeLoggerService(emf);
    SourceService sourceService = new SourceService(emf);
    SourceController sourceController = new SourceController(sourceService);
    CrawlLogController crawlLogController = new CrawlLogController(scrapeLoggerService);
    HealthController healthController = new HealthController();

    public EndpointGroup getRoutes() {
        return () -> {
            //TODO: Top-level path
            path("/scrape_source", () -> {
                post(ctx -> sourceController.createSource(ctx)); //Create a source
                    path("/{id}", () -> {
                        get(ctx -> sourceController.getASource(ctx));           // fetch a source config
                        put(ctx ->sourceController.updateASource(ctx));             // update a source
                        delete(ctx -> sourceController.deleteASource(ctx));         // delete a source
                    });
                    path("/public_sources", () -> {
                    get(ctx -> sourceController.listPublic(ctx));
                });

                    path("/my_sources", () -> {
                        get(ctx -> sourceController.listUserSources(ctx));
                    });
            });

            //TODO: Top-level path
            path("/scraplog", () -> {
                path("/{sourceId}", () -> {
                    path("/run", () -> {
                        post(ctx -> crawlLogController.startCrawl(ctx));
                    });
                });

                path("/{logId}", () -> {
                    path("/success", () -> {
                        get(ctx -> crawlLogController.markSuccess(ctx));
                    });

                    path("/fail", () -> {
                        get(ctx -> crawlLogController.markFailed(ctx));
                    });

                    path("/items", () -> {
                        get(ctx -> crawlLogController.attachItems(ctx));
                    });
                });

            });

            //TODO: Top-level path
            path("/healthcheck", () -> {
                   get(ctx -> healthController.apiHealthCheck(ctx));
            });

        };
    }

}
