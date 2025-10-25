package app.services;

import app.dao.SourceDAO;
import app.dao.UserDAO;
import app.dto.SourceDTOs.SourceCreateDTO;
import app.dto.SourceDTOs.SourceDTO;
import app.dto.SourceDTOs.SourceUpdateDTO;
import app.entities.Source;
import app.security.entities.User;
import app.utils.Utils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class SourceService {
    private final SourceDAO sourceDAO;
    private final UserDAO userDAO;

    public SourceService(EntityManagerFactory emf) {
        this.sourceDAO = SourceDAO.getInstance(emf);
        this.userDAO   = UserDAO.getInstance(emf);
    }

    // CREATE
    public SourceDTO create(String ownerUsername, SourceCreateDTO dto) {
        validateCreate(dto);
        User owner = userDAO.findByUsername(ownerUsername);
        if (owner == null) {
            throw new EntityNotFoundException("Owner not found: " + ownerUsername);
        }

        if (sourceDAO.existsByOwnerAndName(ownerUsername, dto.name())) {
            throw new EntityExistsException("You already have a source named '" + dto.name() + "'");
        }

        Source s = new Source();
        s.setUser(owner);
        s.setName(dto.name());
        s.setBaseUrl(dto.baseUrl());
        s.setAllowedPathPattern(dto.allowedPathPattern());
        s.setSelectorsJson(Utils.writeToJsonString(dto.selectors()));
        s.setPublicReadable(dto.publicReadable());
        s.setEnabled(dto.enabled());

        sourceDAO.persist(s);
        return toDTO(s);
    }

    // READ
    public SourceDTO get(Long sourceId, String requesterUsername, boolean allowPublic) {
        Source s = sourceDAO.findById(sourceId);
        if (s == null) {
            throw new EntityNotFoundException("Source not found: " + sourceId);
        }
        if (!canRead(s, requesterUsername, allowPublic)) {
            throw new PersistenceException("Forbidden");
        }
        return toDTO(s);
    }


    public List<SourceDTO> listMine(String ownerUsername) {
        return sourceDAO.findAllSourcesByOwner(ownerUsername).stream().map(this::toDTO).toList();
    }


    public List<SourceDTO> listPublic() {
        return sourceDAO.findAllPublicSources().stream().map(this::toDTO).toList();
    }

    // UPDATE
    public SourceDTO update(Long sourceId, String requesterUsername, SourceUpdateDTO dto) {
        validateUpdate(dto);
        Source s = sourceDAO.findById(sourceId);
        if (s == null) {
            throw new EntityNotFoundException("Source not found: " + sourceId);
        }
        requireOwnerOrAdmin(s, requesterUsername);

        if (dto.name() != null && !dto.name().isBlank()) {
            if (!s.getName().equals(dto.name()) &&
                    sourceDAO.existsByOwnerAndName(s.getUser().getUsername(), dto.name())) {
                throw new EntityExistsException("You already have a source named '" + dto.name() + "'");
            }
            s.setName(dto.name());
        }
        if (dto.baseUrl() != null) {
            requireValidUrl(dto.baseUrl());
            s.setBaseUrl(dto.baseUrl());
        }
        if (dto.allowedPathPattern() != null) {
            s.setAllowedPathPattern(dto.allowedPathPattern());
        }
        if (dto.selectors() != null) {
            requireNonEmptySelectors(dto.selectors());
            s.setSelectorsJson(Utils.writeToJsonString(dto.selectors()));
        }
        if (dto.publicReadable() != null) {
            s.setPublicReadable(dto.publicReadable());
        }
        if (dto.enabled() != null) {
            s.setEnabled(dto.enabled());
        }

        sourceDAO.update(s);
        return toDTO(s);
    }

    // DELETE
    public void delete(Long sourceId, String requesterUsername) {
        Source s = sourceDAO.findById(sourceId);
        if (s == null) {
            throw new EntityNotFoundException("Source not found: " + sourceId);
        }
        requireOwnerOrAdmin(s, requesterUsername);
        sourceDAO.delete(sourceId);
    }


    // TOGGLES
    public SourceDTO setEnabled(Long sourceId, String requesterUsername, boolean enabled) {
        Source s = sourceDAO.findById(sourceId);
        if (s == null) {
            throw new EntityNotFoundException("Source not found");
        }
        requireOwnerOrAdmin(s, requesterUsername);
        s.setEnabled(enabled);
        sourceDAO.update(s);
        return toDTO(s);
    }


    public SourceDTO setPublicReadable(Long sourceId, String requesterUsername, boolean isPublic) {
        Source s = sourceDAO.findById(sourceId);
        if (s == null) {
            throw new EntityNotFoundException("Source not found");
        }
        requireOwnerOrAdmin(s, requesterUsername);
        s.setPublicReadable(isPublic);
        sourceDAO.update(s);
        return toDTO(s);
    }

    // Helpers
    private void requireOwnerOrAdmin(Source s, String requesterUsername) {
        if (requesterUsername == null) {
            throw new PersistenceException("Unauthorized");
        }
        User user = userDAO.findByUsername(requesterUsername);
        if (user == null) {
            throw new PersistenceException("Unauthorized");
        }
        boolean isOwner = s.getUser().getUsername().equals(requesterUsername);
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> "admin".equalsIgnoreCase(r.getName()));
        if (!isOwner && !isAdmin) {
            throw new PersistenceException("Forbidden");
        }
    }


    private boolean canRead(Source s, String requesterUsername, boolean allowPublic) {
        if (s.isPublicReadable() && allowPublic) {
            return true;
        }
        if (requesterUsername == null) {
            return false;
        }
        return s.getUser().getUsername().equals(requesterUsername);
    }

    private void validateCreate(SourceCreateDTO d) {
        if (d == null) {
            throw new IllegalArgumentException("Body required");
        }
        if (isBlank(d.name())) {
            throw new IllegalArgumentException("Name is required");
        }
        requireValidUrl(d.baseUrl());
        requireNonEmptySelectors(d.selectors());
    }


    private void validateUpdate(SourceUpdateDTO d) {
        if (d == null) {
            throw new IllegalArgumentException("Body required");
        }
        if (d.baseUrl() != null) {
            requireValidUrl(d.baseUrl());
        }
        if (d.selectors() != null) {
            requireNonEmptySelectors(d.selectors());
        }
    }


    private void requireValidUrl(String url) {
        if (isBlank(url)) {
            throw new IllegalArgumentException("baseUrl is required");
        }
        try {
            URI.create(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid baseUrl");
        }
    }


    private void requireNonEmptySelectors(Map<String,Object> sel) {
        if (sel == null || sel.isEmpty()) {
            throw new IllegalArgumentException("selectors must be a non-empty JSON object");
        }
    }
    private boolean isBlank(String s){
        return s == null || s.isBlank();
    }

    private SourceDTO toDTO(Source s) {
        return new SourceDTO(
                s.getName(),
                s.getBaseUrl(),
                s.getAllowedPathPattern(),
                Utils.readMap(s.getSelectorsJson()),
                s.isPublicReadable(),
                s.isEnabled(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
