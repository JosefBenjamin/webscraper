package app.services;

import app.dao.CrawlLoggerDAO;
import app.dao.ScrapedDataDAO;
import app.dao.SourceDAO;
import app.dao.UserDAO;
import app.entities.CrawlLogger;
import app.entities.ScrapedData;
import app.entities.Source;
import app.enums.CrawlStatus;
import app.integration.CrawlClientSidecar;
import app.security.daos.SecurityDAO;
import app.security.entities.User;
import app.utils.Hashing;
import app.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class ScrapeLoggerService {
    private EntityManagerFactory emf;
    private CrawlLoggerDAO clDAO;
    private SourceDAO sourceDAO;
    private SecurityDAO securityDAO;
    private UserDAO userDAO;
    private CrawlLoggerDAO crawlLoggerDAO;
    private ScrapedDataDAO scrapedDataDAO;
    private static final Logger logger = LoggerFactory.getLogger(ScrapeLoggerService.class);


    public ScrapeLoggerService(EntityManagerFactory emf){
        this.emf = emf;
        this.clDAO  = CrawlLoggerDAO.getInstance(emf);
        this.sourceDAO = SourceDAO.getInstance(emf);
        this.securityDAO = SecurityDAO.getInstance(emf);
        this.userDAO = UserDAO.getInstance(emf);
        this.crawlLoggerDAO = CrawlLoggerDAO.getInstance(emf);
        this.scrapedDataDAO = ScrapedDataDAO.getInstance(emf);
    }



    //TODO status: <-----RUNNING----->
    public Long startRunning(Long sourceId, String username){
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                Source src = em.find(Source.class, sourceId);
                if(src == null){
                    throw new EntityNotFoundException("Source not found; " + sourceId);
                }

                User requestedByUser = em.find(User.class, username);
                if(requestedByUser == null){
                    throw new EntityNotFoundException("User not found with: " + username);
                }

                String srcOwner = src.getUser().getUsername().toLowerCase().trim();
                String usersUsername = username.toLowerCase().trim();
                boolean isOwner = srcOwner.equals(usersUsername);
                boolean isAdmin = requestedByUser.getRoles().stream()
                        .anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));
                if (!isOwner && !isAdmin) {
                    throw new PersistenceException("Forbidden: user cannot run this source");
                }

                CrawlLogger crawlLogger = CrawlLogger.builder()
                        .source(src)
                        .user(requestedByUser)
                        .status(CrawlStatus.RUNNING)
                        .error(null)
                        .build();

                em.persist(crawlLogger);
                em.getTransaction().commit();
                return crawlLogger.getId();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }


    //TODO:  Runs the crawl end-to-end OUTSIDE the original transaction
    public void executeCrawl(Long crawlLogId) {
        Source source;
        // 1) Load source + selectors (read-only)
        try (EntityManager em = emf.createEntityManager()) {
            CrawlLogger crawlLogger = em.find(CrawlLogger.class, crawlLogId);
            if (crawlLogger == null){
                throw new EntityNotFoundException("Log not found: " + crawlLogId);
            }
            source = crawlLogger.getSource();
        }

        try {
            // 2) Call Python sidecar
            Map<String, Object> selectors = Utils.readMap(source.getSelectorsJson());
            CrawlClientSidecar client = new CrawlClientSidecar();
            if(source.getBaseUrl() == null || selectors == null){
                throw new NullPointerException("These can't be null pal, Map.of in CrawlClientSidecar rejects null right away");
            }
            String rawJson = client.crawl(source.getBaseUrl(), selectors);

            // 3) Parse and persist items in a NEW TX
            ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode root = om.readTree(rawJson);
            JsonNode itemsNode = root.path("extracted").path("items");

            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();
                CrawlLogger log = em.find(CrawlLogger.class, crawlLogId);
                if (itemsNode.isArray()) {
                    for (JsonNode itemNode : itemsNode) {
                        var map = om.convertValue(itemNode, Map.class);
                        ScrapedData item = new ScrapedData();
                        item.setSource(log.getSource());
                        item.setCrawlLogger(log);
                        item.setDataJson(Utils.writeToJsonString(map));
                        if (itemNode.hasNonNull("link")){
                            item.setUrl(itemNode.get("link").asText());
                        }
                        String hash = Hashing.sha256(item.getDataJson());
                        item.setHash(hash); // implement sha256 helper
                        if(scrapedDataDAO.hashExists(log.getSource().getId(), hash)){
                            continue;
                        }
                        em.persist(item);
                    }
                }
                // 4) Mark success
                log.setStatus(CrawlStatus.SUCCESS);
                log.setError(null);
                em.getTransaction().commit();
            }
        } catch (Exception ex) {
            // 5) On any error, mark FAILED in its own TX
            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();
                CrawlLogger log = em.find(app.entities.CrawlLogger.class, crawlLogId);
                if (log != null) {
                    log.setStatus(CrawlStatus.FAILED);
                    log.setError(ex.getMessage());
                }
                em.getTransaction().commit();
            }
            throw new RuntimeException(ex);
        }
    }

}
