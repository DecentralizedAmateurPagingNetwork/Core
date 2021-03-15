# DAPNET Core TODOs
Some things to do in this refactoring branch. This is not intended to be a fully featured checklist that includes every single issue with detailed instructions. See it as a list of points to watch out for.

## Global state references
The state registers itself throughout the core as a singleton. It should be the other way around. We should use a service provider that can be queried for the current state instance, for example by using dependency injection or a global service provider instance.

## Global configuration references
Generally the same issue as with having the global state reference. However the settings are immutable so once they are loaded we don't have thread-safety issues.

## Static global variables
We have them and we shouldn't. Addressing the other points, especially thread-safety, is quite hard because you never know where someone is accessing those variables (no, having the "search references" feature in the IDE is no excuse). We might not be able to totally eliminate static global variables but we should minimize them and use the service provider pattern or similar patterns wherever possible.

## State thread-safety
The state itself is not thread-safe at all. It uses concurrent collections but cross-collection access is not properly synchronized.

## Nested objects
Some models have nested objects, like calls having callsigns, but those are looked up on-the-fly and by using a global static state reference. Idea: either remove this totally (so the caller is responsible for performing the lookup) or provide them as a convenience function. For example, to get the associated callsigns for a call the method signature could look like `Collection<CallSign> getCallsigns(Map<String, CallSign> mapping)`.

## Validation
The validators don't look too good either. For example, the `ValidNameValidator` doesn't use any of its parameters, uses `Object` as the generic type and just checks for `value != null`. So that is basically the same as `@NotNull` with a custom error message.

## Close rejected connections with a delay
When a connection is closed because of an error (authentication failure, etc.) it might be a good idea to add a little delay before the channel is actually closed. This way we would prevent the infamous "reconnect as fast as possible" loops in some client implementations.

## General thread-safety
There are some operations performed that are not properly synchronized, especially regarding state access and RPC event handling.

## REST models
The JSON REST API is reusing the model classes that are also used by the state. There might be cases where REST-specific classes could be more suitable, depending on the information that is exchanged. For example, if fields are ignored anyway, why are they present then? There might also security issues involved, check the JSON filter for hiding fields from REST clients.
