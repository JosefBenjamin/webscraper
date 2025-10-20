package app.services;

import app.dao.CrawlLoggerDAO;
import app.dao.SourceDAO;
import app.dao.UserDAO;
import app.entities.CrawlLogger;
import app.entities.Source;
import app.enums.CrawlStatus;
import app.security.daos.SecurityDAO;
import app.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            try{
                em.getTransaction().begin();

                Source src = sourceDAO.findById(sourceId);
                if(src == null){
                    throw new EntityNotFoundException("Source not found; " + sourceId);
                }

                User requestedByUser = userDAO.findByUsername(username);
                if(requestedByUser == null){
                    throw new EntityNotFoundException("User not found with: " + username);
                }

                CrawlLogger crawlLogger = CrawlLogger.builder()
                        .source(src)
                        .user(requestedByUser)
                        .status(CrawlStatus.RUNNING)
                        .error(null)
                        .build();

                crawlLoggerDAO.persist(crawlLogger);
                em.getTransaction().commit();
                return crawlLogger.getId();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new RuntimeException(e);
            }
        }
    }



    //TODO status: <-----QUEUED----->



    //TODO status: <-----SUCCESS----->




    //TODO status: <-----FAILED----->





}
