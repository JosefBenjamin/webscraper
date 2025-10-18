package app.controllers;


import io.javalin.http.Context;


public interface IHotelController {

    void getAllHotels(Context ctx);

    void getHotelById(Context ctx);

    void createHotel(Context ctx);

    void updateHotel(Context ctx);

    void deleteHotel(Context ctx);

}
