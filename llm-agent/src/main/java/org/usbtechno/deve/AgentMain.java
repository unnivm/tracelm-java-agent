package org.usbtechno.deve;


import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class AgentMain {

    public static void premain(String args, Instrumentation inst) {
        new AgentBuilder.Default()
                .ignore(nameStartsWith("net.bytebuddy."))
                .ignore(nameStartsWith("Byte Buddy"))
                .with(AgentBuilder.Listener.NoOp.INSTANCE)
                .type(hasSuperType(named("java.lang.reflect.InvocationHandler")))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) -> builder.method(named("invoke")).intercept(MethodDelegation.to(LLMInterceptor.class)))
                .installOn(inst);
    }
}