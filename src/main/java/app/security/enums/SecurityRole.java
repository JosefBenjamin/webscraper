package app.security.enums;

import io.javalin.security.RouteRole;

public enum SecurityRole implements RouteRole {

    ADMIN,
    USER,
    ANYONE;

}
