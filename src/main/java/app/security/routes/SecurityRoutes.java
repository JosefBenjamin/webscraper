package app.security.routes;

import app.security.controllers.SecurityController;
import app.security.enums.SecurityRole;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;


public class SecurityRoutes {
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static SecurityController securityController = SecurityController.getInstance();
    public static EndpointGroup getSecurityRoutes() {
        return ()->{
            path("/auth", () -> {
                get("/test", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg",  "Hello from Open")), SecurityRole.ANYONE);
                post("/login", securityController.login(), SecurityRole.ANYONE);
                post("/register", securityController.register(), SecurityRole.ANYONE);
                post("/user/addrole", securityController.addRole(), SecurityRole.ADMIN);
            });
        };
    }
    public static EndpointGroup getSecuredRoutes(){
        return ()->{
            path("/protected", ()->{
                get("/user_demo", (ctx) -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from USER Protected")), SecurityRole.USER);
                get("/admin_demo", (ctx) -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN Protected")), SecurityRole.ADMIN);
            });
        };
    }
}
