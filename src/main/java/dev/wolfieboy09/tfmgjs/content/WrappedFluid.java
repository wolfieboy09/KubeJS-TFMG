package dev.wolfieboy09.tfmgjs.content;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WrappedFluid {
    public static final WrappedFluid EMPTY = new WrappedFluid(Collections.emptyList(), null);

    private final List<Fluid> fluids;
    private final TagKey<Fluid> tag;

    private WrappedFluid(List<Fluid> fluids, TagKey<Fluid> tag) {
        this.fluids = fluids;
        this.tag = tag;
    }

    @Contract("_, _ -> new")
    public static @NotNull WrappedFluid create(List<Fluid> fluids, TagKey<Fluid> tag) {
        return new WrappedFluid(fluids == null ? Collections.emptyList() : List.copyOf(fluids), tag);
    }

    @Contract("_ -> new")
    public static @NotNull WrappedFluid ofTag(TagKey<Fluid> tag) {
        return new WrappedFluid(Collections.emptyList(), tag);
    }

    @Contract("_ -> new")
    public static @NotNull WrappedFluid ofFluids(List<Fluid> fluids) {
        return new WrappedFluid(List.copyOf(fluids), null);
    }

    @Contract("_ -> new")
    public static @NotNull WrappedFluid ofFluid(Fluid fluid) {
        return new WrappedFluid(List.of(fluid), null);
    }

    public boolean isTag() {
        return tag != null;
    }

    public TagKey<Fluid> getTag() {
        return tag;
    }

    public boolean isEmpty() {
        return isTag()
                ? BuiltInRegistries.FLUID.getTag(tag).isEmpty()
                : fluids.isEmpty();
    }

    public boolean contains(Fluid fluid) {
        return isTag()
                ? fluid.builtInRegistryHolder().is(tag)
                : fluids.contains(fluid);
    }

    public List<Fluid> getFluids() {
        return isTag()
                ? BuiltInRegistries.FLUID.getTag(tag)
                .map(set -> set.stream()
                        .map(Holder::value)
                        .collect(Collectors.toList()))
                .orElseGet(List::of)
                : fluids;
    }

    @Override
    public String toString() {
        return isTag()
                ? "#" + tag.location()
                : fluids.stream()
                .map(f -> BuiltInRegistries.FLUID.getKey(f).toString())
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
