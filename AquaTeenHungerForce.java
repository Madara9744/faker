package com.github.javafaker;

public class AquaTeenHungerForce {

    private final Faker faker;
    private final Test test;

    protected AquaTeenHungerForce(Faker faker,Test test) {
        this.faker = faker;
        this.test=test;
    }

    public String character() {
        return faker.fakeValuesService().resolve("aqua_teen_hunger_force.character", this, faker);
    }

}
