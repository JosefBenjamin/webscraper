package app.security.entities;

public interface ISecurityUser {
    boolean verifyPass(String password);
    void addRole(Role role);

}
