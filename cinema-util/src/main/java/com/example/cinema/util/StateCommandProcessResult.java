package com.example.cinema.util;

import java.util.List;
import java.util.Optional;

public record StateCommandProcessResult<Event,CommandResponseError>(List<Event> events, Optional<CommandResponseError> error) {
    public static <Event,CommandResponseError> StateCommandProcessResult<Event,CommandResponseError> error(CommandResponseError error){
        return new StateCommandProcessResult<>(List.of(),Optional.of(error));
    }
    public static <Event,CommandResponseError> StateCommandProcessResult<Event,CommandResponseError> result(List<Event> events){
        return new StateCommandProcessResult<>(events,Optional.empty());
    }
    public static <Event,CommandResponseError> StateCommandProcessResult<Event,CommandResponseError> result(Event event){
        return new StateCommandProcessResult<>(List.of(event),Optional.empty());
    }
    public static <Event,CommandResponseError> StateCommandProcessResult<Event,CommandResponseError> resultWithError(List<Event> events, CommandResponseError error){
        return new StateCommandProcessResult<>(events,Optional.of(error));
    }
    public static <Event,CommandResponseError> StateCommandProcessResult<Event,CommandResponseError> resultWithError(Event event, CommandResponseError error){
        return new StateCommandProcessResult<>(List.of(),Optional.of(error));
    }

}
