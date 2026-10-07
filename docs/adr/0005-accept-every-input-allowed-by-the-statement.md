# 5. Accept every input allowed by the statement

Date: 2026-10-07

## Status

Accepted. Supersedes [ADR-0004](0004-domain-invariants-vs-configurable-limits.md).

## Context

ADR-0004 made the domain reject `int1`, `int2` or `limit` lower than 1 and empty strings. The statement asks for "three integers" and "two strings" with no further restriction, and every such input has a well-defined result, so those rules were deviations from the statement rather than invariants.

## Decision

`FizzBuzzQuery` only requires its five parameters to be present. Integers are `BigInteger`, so any integer is accepted exactly as sent, and statistics can return it unchanged. The generator follows the arithmetic definition of "multiple":

- a negative divisor has the same multiples as its absolute value;
- 0 and any divisor beyond the `int` range have no multiple between 1 and `limit`, so they replace nothing;
- `limit` lower than 1 gives an empty list ("from 1 to limit" contains no number);
- an empty string replaces multiples by an empty term.

As in ADR-0004, operational bounds (maximum `limit`, maximum string length) are not domain rules: they are configuration, enforced at the API boundary and documented as deliberate deviations justified by "ready for production".

## Consequences

- The domain matches the statement for every input; the only rejections are those the API makes on purpose.
- The generator converts each divisor once, so the loop still uses plain `int` arithmetic.
- A list cannot exceed `Integer.MAX_VALUE` elements; the generator rejects larger limits explicitly. The API bound keeps requests far below that.
