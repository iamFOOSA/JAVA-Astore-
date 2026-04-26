package by.abram.astore.dto;

public record RaceConditionResponse(
        int threads,
        int operationsPerThread,
        int expectedTotal,
        int unsafeCounter,
        int synchronizedCounter,
        int atomicCounter,
        boolean raceConditionDetected
) {
}
