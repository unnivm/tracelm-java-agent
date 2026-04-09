package org.usbtechno.deve;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class AgentMain {

    public static void premain(String args, Instrumentation inst) {
        System.out.println("🔥 AGENT LOADED FIRST");
        new AgentBuilder.Default()
                .ignore(nameStartsWith("net.bytebuddy."))
                .ignore(nameStartsWith("Byte Buddy"))
                .with(AgentBuilder.Listener.NoOp.INSTANCE)
                .type(hasSuperType(named("java.lang.reflect.InvocationHandler")))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) -> builder.method(named("invoke")).intercept(MethodDelegation.to(LLMInterceptor.class)))
                .installOn(inst);

//        new AgentBuilder.Default()
//                .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
//                .ignore(nameStartsWith("net.bytebuddy."))
//                .type(nameContains("langchain4j"))
//                .transform((builder, td, cl, module, pd) -> {
//
//                    System.out.println("🔥 Instrumenting: " + td.getName());
//
//                    return builder.visit(
//                            Advice.to(ResponseAdvice.class)
//                                    .on(
//                                            ElementMatchers.not(ElementMatchers.isConstructor())
//                                                    .and(ElementMatchers.not(ElementMatchers.isTypeInitializer()))
//                                    )
//                    );
//                })
//                .installOn(inst);

        new AgentBuilder.Default()
                .ignore(nameStartsWith("net.bytebuddy."))
                .type(named("dev.langchain4j.model.openai.OpenAiChatModel"))
                .transform((builder, td, cl, module, pd) -> {

                    System.out.println("🔥 Instrumenting: " + td.getName());

                    return builder.visit(
                            Advice.to(ResponseAdvice.class)
                                    .on(named("generate"))
                    );
                })
                .installOn(inst);
    }

}