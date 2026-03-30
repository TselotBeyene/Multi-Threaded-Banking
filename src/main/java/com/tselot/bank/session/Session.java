package com.tselot.bank.session;

import java.time.Instant;

public record Session(String sessionId, String ownerName, Instant loginTime) {}
