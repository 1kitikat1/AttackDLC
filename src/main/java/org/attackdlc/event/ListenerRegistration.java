package org.attackdlc.event;

record ListenerRegistration(Class<? extends Event> eventType, RegisteredListener listener) {
}

