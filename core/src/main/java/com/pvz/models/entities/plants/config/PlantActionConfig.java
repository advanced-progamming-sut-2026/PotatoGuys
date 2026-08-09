package com.pvz.models.entities.plants.config;

/**
 * Polymorphic base type for a plant's data-driven behavior payload.
 * The concrete subtype is resolved at load time by LibGDX's {@link com.badlogic.gdx.utils.Json}
 * from the {@code "class"} tag written into {@code actionConfig} in the plant JSON
 * (see {@link PlantActionConfigLoader} for the class-tag registration).
 */
public abstract class PlantActionConfig {
}
