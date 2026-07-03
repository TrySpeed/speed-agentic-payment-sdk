package com.tryspeed.server;

record CachedResponse(int status, String contentType, byte[] body) {}
