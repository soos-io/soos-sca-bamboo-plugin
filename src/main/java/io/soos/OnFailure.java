package io.soos;

public enum OnFailure {
    FAIL_THE_BUILD("Fail the build","fail_the_build"),
    CONTINUE_ON_FAILURE("Continue the build","continue_on_failure");

    private String name;
    private String value;

    OnFailure(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
