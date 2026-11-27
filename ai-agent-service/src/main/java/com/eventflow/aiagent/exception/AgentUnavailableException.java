package com.eventflow.aiagent.exception;

public class AgentUnavailableException extends RuntimeException {

    public AgentUnavailableException(Throwable cause) {
        super("The language model is unavailable", cause);
    }
}
