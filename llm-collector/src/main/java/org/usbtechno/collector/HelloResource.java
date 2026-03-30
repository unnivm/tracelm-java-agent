package org.usbtechno.collector;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/hello")
public class HelloResource {

    @GET
    public String hello() {
        return "Hello Quarkus rest welcome you";
    }
}