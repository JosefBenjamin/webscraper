package app.services;


import app.dao.CrawlLoggerDAO;
import app.dao.ScrapedDataDAO;
import app.dao.SourceDAO;
import app.entities.CrawlLogger;
import app.entities.ScrapedData;
import app.entities.Source;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public class ScrapingService {

    private final ScrapedDataDAO scrapedDataDAO;
    private final CrawlLoggerDAO crawlLoggerDAO;
    private final SourceDAO sourceDAO;


    public ScrapingService(EntityManagerFactory emf) {
        this.scrapedDataDAO = ScrapedDataDAO.getInstance(emf);
        this.crawlLoggerDAO = CrawlLoggerDAO.getInstance(emf);
        this.sourceDAO = SourceDAO.getInstance(emf);
    }

// ---------- CREATE ----------
    //TODO: Save a batch of scraped items for a given crawl log and source.
    public void saveBatch(Long crawlLogId, Long sourceId, List<ScrapedData> items) {
        if (items == null || items.isEmpty()){
            return;
        }

        CrawlLogger log = crawlLoggerDAO.findById(crawlLogId);
        if (log == null){
            throw new EntityNotFoundException("CrawlLogger not found: " + crawlLogId);
        }

        Source src = sourceDAO.findById(sourceId);
        if (src == null){
            throw new EntityNotFoundException("Source not found: " + sourceId);
        }

        for (ScrapedData item : items) {
            item.setCrawlLogger(log);
            item.setSource(src);
            item.setCrawledAt(Instant.now());
            scrapedDataDAO.persist(item);
        }
    }

    // ---------- READ ----------
    public Set<ScrapedData> listAll() {
        return scrapedDataDAO.retrieveAll();
    }

    public ScrapedData get(Long id) {
        ScrapedData data = scrapedDataDAO.findById(id);
        if (data == null) {
            throw new EntityNotFoundException("Scraped data not found: " + id);
        }
        return data;
    }

    public Set<ScrapedData> listBySource(Long sourceId) {
        Source src = sourceDAO.findById(sourceId);
        if (src == null){
            throw new EntityNotFoundException("Source not found: " + sourceId);
        }
        return src.getItems();   // relies on JPA relationship; use DAO query if you prefer eager fetch
    }

    // ---------- UPDATE ----------
    public ScrapedData update(ScrapedData updated) {
        if (updated == null){
            throw new IllegalArgumentException("ScrapedData required");
        }
        return scrapedDataDAO.update(updated);
    }

    // ---------- DELETE ----------
    public boolean delete(Long id) {
        return scrapedDataDAO.delete(id);
    }

}
