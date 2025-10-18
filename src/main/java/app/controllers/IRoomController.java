package app.controllers;

import io.javalin.http.Context;


public interface IRoomController {

    void addRoom(Context ctx);

    void removeRoom(Context ctx);

    void getRoomsForHotel(Context ctx);

    void deleteRoom(Context ctx);

}
