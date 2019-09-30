package xyz.ressor.service.proxy.model;

public class PersonInfoImpl implements PersonInfo {
    private final String firstName;
    private final String lastName;

    public PersonInfoImpl(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String firstName() {
        return firstName;
    }

    @Override
    public String lastName() {
        return lastName;
    }
}
