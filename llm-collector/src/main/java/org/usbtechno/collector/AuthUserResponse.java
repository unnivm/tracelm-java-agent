package org.usbtechno.collector;

public class AuthUserResponse {

    public Long id;
    public String name;
    public String email;

    public AuthUserResponse() {
    }

    public AuthUserResponse(UserAccount user) {
        this.id = user.id;
        this.name = user.name;
        this.email = user.email;
    }
}
