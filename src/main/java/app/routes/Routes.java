package app.routes;

import app.controllers.HotelController;
import app.controllers.RoomController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    //TODO: Dependencies
    private final HotelController hotelController = new HotelController();
    private final RoomController roomController = new RoomController();

    public EndpointGroup getRoutes() {
        return () -> {
            path("/hotel", () -> {
                get(ctx -> hotelController.getAllHotels(ctx));               // GET /hotel
                post(ctx -> hotelController.createHotel(ctx));                // POST /hotel

                    path("/{id}", () -> {
                        get(ctx -> hotelController.getHotelById(ctx));           // GET /hotel/{id}
                        put(ctx ->hotelController.updateHotel(ctx));             // PUT /hotel/{id}
                        delete(ctx -> hotelController.deleteHotel(ctx));         // DELETE /hotel/{id}

                        path("/rooms", () -> {
                            get(ctx -> roomController.getRoomsForHotel(ctx));    // GET /hotel/{id}/rooms
                            post(ctx -> roomController.addRoom(ctx));            // POST /hotel/{id}/rooms
                            path("/{roomId}", () -> {
                                delete(ctx -> roomController.removeRoom(ctx));   // DELETE /hotel/{id}/rooms/{roomId}
                            });
                        });
                    });
            });
            path("/room", () -> {
                path("/{roomId}", () -> {
                    delete(ctx -> roomController.deleteRoom(ctx));             // DELETE /room/{roomId}
                });
            });
        };
    }
}
