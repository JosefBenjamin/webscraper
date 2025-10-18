package app.controllers;

import app.config.HibernateConfig;
import app.converters.DTOConverter;
import app.converters.EntityConverter;
import app.dao.HotelDAO;
import app.dao.IDAO;
import app.dto.HotelDTO;
import app.exceptions.ApiException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HotelController implements IHotelController {

    //TODO: Dependencies
    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final IDAO dao = HotelDAO.getInstance(emf);
    private final DTOConverter dtoConverter = new DTOConverter();
    private final EntityConverter entityConverter = new EntityConverter();

    //TODO: Logging of class
    private static final Logger logger = LoggerFactory.getLogger(HotelController.class);
    private static final Logger debugLogger = LoggerFactory.getLogger("app");


    @Override
    public void getAllHotels(Context ctx) {
        List<HotelDTO> hotelDTOS = dao.getAllHotels().stream()
                        .map((h) -> dtoConverter.fromHotel(h)).toList();
        ctx.status(HttpStatus.OK);
        ctx.json(hotelDTOS);
        logger.info("Fetched all hotels, count: " + hotelDTOS.size());
    }

    @Override
    public void getHotelById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Hotel hotel = dao.getHotelById(id);
        if (hotel != null) {
            HotelDTO hotelDTO = dtoConverter.fromHotel(hotel);
            ctx.status(HttpStatus.OK);
            ctx.json(hotelDTO);
            logger.info("Fetched hotel with id: " + id);
        } else {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("hotel not found");
            logger.warn("Hotel with id " + id + " not found");
        }
    }

    @Override
    public void createHotel(Context ctx) {
        try {
            HotelDTO incoming = ctx.bodyAsClass(HotelDTO.class);
            Hotel toPersist = entityConverter.fromHotel(incoming); // assumes DTOConverter has toHotel
            if (toPersist == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.result("Missing hotel payload");
                return;
            }
            Hotel created = dao.createHotel(toPersist);
            HotelDTO out = dtoConverter.fromHotel(created);
            ctx.status(HttpStatus.CREATED);
            ctx.json(out);
            logger.info("Created hotel id={} name={}", created.getId(), created.getName());
        } catch(ApiException e) {
            ctx.status(e.getCode());
            ctx.result(e.getMessage());
        }
        catch (Exception  e) {
            debugLogger.error("Failed to create hotel", e);
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Could not create hotel");
        }
    }

    @Override
    public void updateHotel(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Hotel hotel = dao.getHotelById(id);
        if (hotel == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("hotel not found");
            return;
        }

        HotelDTO incoming;
        try {
            incoming = ctx.bodyAsClass(HotelDTO.class);
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Invalid hotel payload");
            return;
        }
        if (incoming.name() != null) {
            hotel.setName(incoming.name());
        }
        if (incoming.address() != null) {
            hotel.setAddress(incoming.address());
        }

        Hotel updated = dao.updateHotel(hotel);
        ctx.status(HttpStatus.OK);
        ctx.json(dtoConverter.fromHotel(updated));
    }

    @Override
    public void deleteHotel(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Hotel hotel = dao.getHotelById(id);
        if (hotel == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("hotel not found");
            return;
        }

        boolean deleted = dao.deleteHotel(hotel);
        if (deleted) {
            ctx.status(HttpStatus.NO_CONTENT);
        } else {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("Could not delete hotel");
        }
    }


}
