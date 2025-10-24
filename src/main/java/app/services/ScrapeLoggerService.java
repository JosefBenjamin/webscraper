package app.services;

import app.dao.CrawlLoggerDAO;
import app.dao.SourceDAO;
import app.dao.UserDAO;
import app.entities.CrawlLogger;
import app.entities.ScrapedData;
import app.entities.Source;
import app.enums.CrawlStatus;
import app.security.daos.SecurityDAO;
import app.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScrapeLoggerService {
    private EntityManagerFactory emf;
    private CrawlLoggerDAO clDAO;
    private SourceDAO sourceDAO;
    private SecurityDAO securityDAO;
    private UserDAO userDAO;
    private CrawlLoggerDAO crawlLoggerDAO;
    private static final Logger logger = LoggerFactory.getLogger(ScrapeLoggerService.class);


    public ScrapeLoggerService(EntityManagerFactory emf){
        this.emf = emf;
        this.clDAO  = CrawlLoggerDAO.getInstance(emf);
        this.sourceDAO = SourceDAO.getInstance(emf);
        this.securityDAO = SecurityDAO.getInstance(emf);
        this.userDAO = UserDAO.getInstance(emf);
        this.crawlLoggerDAO = CrawlLoggerDAO.getInstance(emf);
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

    /**

    //TODO status: <-----UPDATED STATUS----->
    private void updateStatus(Long logId, CrawlStatus status, String errorLog){
        try(EntityManager em = emf.createEntityManager()){
            if(logId == null || status == null || errorLog == null){
                throw new IllegalArgumentException("Neither logId, status nor errorLog can be null");
            }
            try{
                em.getTransaction().begin();
                CrawlLogger foundLog = crawlLoggerDAO.findById(logId);
                if(foundLog == null){
                    em.getTransaction().rollback();
                    throw new EntityNotFoundException("Could not find a crawl log object");
                }
                foundLog.setStatus(status);
                foundLog.setError(errorLog);
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                em.getTransaction().rollback();
                throw new RuntimeException(e);
            }
        }
    }



    //TODO status: <-----SUCCESS----->
    public void successLog(Long logId){
        updateStatus(logId, CrawlStatus.SUCCESS, null);
    }


    //TODO status: <-----FAILED----->
    public void failLog(Long logId, String errorLog){
        String standardizedErrMsg = errorLog.toUpperCase().trim();
        updateStatus(logId, CrawlStatus.FAILED, standardizedErrMsg);
    }

     */

    /**

    //TODO: <------- Attach Scraped Data (Items) ------->
    public void attachItems(Long logId, List<ScrapedData> items) {
        try (EntityManager em = emf.createEntityManager()) {
            if (logId == null || items.isEmpty()) {
                throw new IllegalArgumentException("logId can't be null and the list must contain one or more ites (ScrapedData)");
            }
            try {
                em.getTransaction().begin();
                CrawlLogger foundLog = em.find(CrawlLogger.class, logId);
                if (foundLog == null) {
                    return;
                }
                Set<ScrapedData> standardizedItems = new HashSet<>(items);
                for (ScrapedData item : standardizedItems) {
                    item.setCrawlLogger(foundLog);
                    foundLog.addItem(item);
                }
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                em.getTransaction().rollback();
                throw new RuntimeException(e);
            }
        }
    }

     */

}
