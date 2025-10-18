package app.dto;


//TODO: RoomDTO: id, hotelId, number, price

public record RoomDTO(
        Integer id,
        Integer hotelId,
        int number,
        double price
) { }
