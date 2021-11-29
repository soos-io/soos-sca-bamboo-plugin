package io.soos;

public enum OperatingEnvironment {
    LINUX("Linux", "unix"),
    MAC("MacOS", "mac"),
    WINDOWS("Windows", "win");

    OperatingEnvironment(String name, String value){
        this.name = name;
        this.value = value;
    }
    private String name;
    private String value;

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
