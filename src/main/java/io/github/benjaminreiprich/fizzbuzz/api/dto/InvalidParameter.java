package io.github.benjaminreiprich.fizzbuzz.api.dto;

/** One entry of the {@code errors} list in a 400 response. The rejected value is never echoed back. */
public record InvalidParameter(String field, String message) {}
