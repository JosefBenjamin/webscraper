package app.controllers;

import app.config.HibernateConfig;
import app.converters.DTOConverter;
import app.converters.EntityConverter;
import app.dao.HotelDAO;
import app.dao.IDAO;
import app.dto.RoomDTO;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class RoomController implements IRoomController{

    //TODO: Dependencies
    //TODO: Dependencies
    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final IDAO dao = HotelDAO.getInstance(emf);
    private final DTOConverter dtoConverter = new DTOConverter();
    private final EntityConverter entityConverter = new EntityConverter();

    //TODO: Logging of class
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);
    private static final Logger debugLogger = LoggerFactory.getLogger("app");


    @Override
    public void addRoom(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id")); // business key for hotel
        Hotel hotel = dao.getHotelById(id);
        if (hotel == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("hotel not found");
            return;
        }
        try {
            RoomDTO incoming = ctx.bodyAsClass(RoomDTO.class); // user provides room details
            Room room = entityConverter.fromRoom(incoming);
            if (room == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.result("Invalid room payload");
                return;
            }
            boolean added = dao.addRoom(hotel, room);
            if (added) {
                ctx.status(HttpStatus.CREATED);
                ctx.json(dtoConverter.fromRoom(room));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.result("Could not add room");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Invalid room payload");
        }
    }

    @Override
    public void removeRoom(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int roomId = Integer.parseInt(ctx.pathParam("roomId"));
        Hotel hotel = dao.getHotelById(id);
        if (hotel == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("hotel not found");
            return;
        }

        Room room = new Room();
        room.setId(roomId);
        boolean removed = dao.removeRoom(hotel, room);
        if (removed) {
            ctx.status(HttpStatus.NO_CONTENT);
        } else {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("room not found");
        }
    }

    @Override
    public void getRoomsForHotel(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Hotel hotel = dao.getHotelById(id);
        if (hotel == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("hotel not found");
            return;
        }
        List<RoomDTO> roomDTOS = dao.getRoomsForHotel(hotel).stream()
                        .map((r) -> dtoConverter.fromRoom(r)).toList();
        ctx.status(HttpStatus.OK);
        ctx.json(roomDTOS);
        logger.info("Fetched all rooms, count: " + roomDTOS.size());
    }

    @Override
    public void deleteRoom(Context ctx) {
        int roomId = Integer.parseInt(ctx.pathParam("roomId"));
        Room room = dao.getRoomById(roomId);
        if (room == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("room not found");
            return;
        }

        boolean removed = dao.removeRoom(room.getHotel(), room);
        if (removed) {
            ctx.status(HttpStatus.NO_CONTENT);
        } else {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("room not found");
        }
    }
}
