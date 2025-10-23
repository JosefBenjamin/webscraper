package app.controllers;

import app.dto.SourceDTOs.SourceCreateDTO;
import app.dto.SourceDTOs.SourceDTO;
import app.dto.SourceDTOs.SourceUpdateDTO;
import app.services.SourceService;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceController.class);
    private SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    //TODO: REQUEST TYPE: POST --> ROLE: USER
    public void createSource(Context ctx){
        UserDTO userDTO = ctx.attribute("user");
        LOGGER.info("User authenticated");
        SourceCreateDTO sourceCreateDTO = ctx.bodyAsClass(SourceCreateDTO.class);
        SourceDTO sourceDTO = sourceService.create(userDTO.getUsername(), sourceCreateDTO);
        LOGGER.info("You have successfully created and persisted a Scrape configuration, congrats");
        ctx.status(201).json(sourceDTO);
    }

    //TODO: REQUEST TYPE: GET --> ROLE: USER
    public void listUserSources(Context ctx){
        UserDTO userDTO = ctx.attribute("user");
        ctx.json(sourceService.listMine(userDTO.getUsername()));
        LOGGER.info("User has retrieved all of their own scrape configs");
    }

    //TODO: REQUEST TYPE: GET --> ROLE: USER/ADMIN
    public void listPublic(Context ctx){
        ctx.json(sourceService.listPublic());
            LOGGER.info("User has retrieved all public scrape configs");
    }

    //TODO: REQUEST TYPE: GET --> ROLE: USER/ADMIN
    public void getASource(Context ctx){
        /**
         *Long id extracts the :id part of the URL path
         *Example: GET /api/sources/{id}
         */
        Long id = Long.valueOf(ctx.pathParam("id"));
        UserDTO user = ctx.attribute("user");
        SourceDTO outGoing = sourceService.get(id, user.getUsername(), true);
        ctx.json(outGoing);
    }

    //TODO: REQUEST TYPE: PUT --> ROLE USER/ADMIN
    public void updateASource(Context ctx){
        Long id = Long.valueOf(ctx.pathParam("id"));
        UserDTO user = ctx.attribute("user");
        SourceUpdateDTO updateDTO = ctx.bodyAsClass(SourceUpdateDTO.class);
        SourceDTO sourceDTO = sourceService.update(id, user.getUsername(), updateDTO);
        ctx.json(sourceDTO);
    }



    //TODO: REQUEST TYPE: DELETE --> ROLE USER/ADMIN
    public void deleteASource(Context ctx){
        Long id = Long.valueOf(ctx.pathParam("id"));
        UserDTO user = ctx.attribute("user");
        sourceService.delete(id, user.getUsername());
        ctx.status(204);
    }

}
