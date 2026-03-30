# tracelm-java-agent
TraceLM – LLM Observability for Java (Agent-Based, Zero Code Changes)

Gain deep visibility into your LLM calls — track latency, errors, and model usage with zero code changes.

🚀 Overview

TraceLens is a lightweight LLM observability system for Java applications built using a Java Agent.

It enables you to monitor and analyze LLM interactions without modifying your application code.

TraceLens captures:

📊 Latency metrics
🧠 Model usage
❌ Errors
📈 Request statistics

All data is sent to a collector service for aggregation and analysis.

🏗️ Architecture
Application → Java Agent → HTTP → Collector → Metrics API
Components
llm-agent
Java Agent using ByteBuddy
Intercepts LLM calls
Extracts metadata (latency, model, status)
llm-collector
Quarkus-based REST service
Receives traces
Aggregates metrics
demo-app (optional)
Sample app using LangChain4J
