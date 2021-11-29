package io.soos;

public enum Mode {
    RUN_AND_WAIT("Run and wait","run_and_wait"),
    ASYNC_INIT("Async init","async_init"),
    ASYNC_RESULT("Async result","async_result");

    private String name;
    private String mode;

    private Mode(String name, String mode) {
        this.name = name;
        this.mode = mode;
    }

    public String getMode() {
        return this.mode;
    }

    public String getName() {
        return name;
    }
}
