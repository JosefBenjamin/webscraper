package app.routes;

import app.config.HibernateConfig;
import app.controllers.CrawlLogController;
import app.controllers.HealthController;
import app.controllers.SourceController;
import app.security.enums.SecurityRole;
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
                post(ctx -> sourceController.createSource(ctx), SecurityRole.USER ,SecurityRole.ADMIN ); //Create a source
                    path("/{id}", () -> {
                        get(ctx -> sourceController.getASource(ctx), SecurityRole.USER, SecurityRole.ADMIN);           // fetch a source config
                        patch(ctx ->sourceController.updateASource(ctx), SecurityRole.USER, SecurityRole.ADMIN);             // update a source
                        delete(ctx -> sourceController.deleteASource(ctx), SecurityRole.USER, SecurityRole.ADMIN);         // delete a source
                    });
                    path("/public_sources", () -> {
                    get(ctx -> sourceController.listPublic(ctx), SecurityRole.USER, SecurityRole.ADMIN);
                });

                    path("/my_sources", () -> {
                        get(ctx -> sourceController.listUserSources(ctx), SecurityRole.USER, SecurityRole.ADMIN);
                    });
            });

            //TODO: Top-level path
            path("/scrape", () -> {
                path("/{sourceId}", () -> {
                    path("/run", () -> {
                        post(ctx -> crawlLogController.startCrawl(ctx), SecurityRole.USER, SecurityRole.ADMIN);
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
