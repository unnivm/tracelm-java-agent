package org.usbtechno.collector.auth.dto;

import org.usbtechno.collector.domain.UserAccount;

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
