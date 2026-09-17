package dev.wolfieboy09.tfmgjs.wrappers;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.wolfieboy09.tfmgjs.content.WrappedFluid;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
public interface WrappedFluidWrapper {
    TypeInfo TYPE_INFO = TypeInfo.of(WrappedFluid.class);

    static boolean isFluidLike(Object from) {
        return from instanceof Fluid
                || from instanceof WrappedFluid
                || from instanceof TagKey<?>
                || from instanceof ResourceLocation;
    }

    static WrappedFluid wrapFluid(Context cx, Object from) {
        return switch (from) {
            case null -> WrappedFluid.EMPTY;
            case WrappedFluid wrapped -> wrapped;
            case Fluid fluid -> WrappedFluid.ofFluid(fluid);
            case FluidStack fluid -> WrappedFluid.ofFluid(fluid.getFluid());
            case TagKey<?> tag -> WrappedFluid.ofTag(castFluidTag(cx, tag));
            case ResourceLocation id -> WrappedFluid.ofFluid(parseFluidId(cx, id));
            case String id -> parseFluidString(cx, id);
            case List<?> list -> wrapFluidList(cx, list);
            default -> throw new KubeRuntimeException("Failed to read fluid %s".formatted(from)).source(SourceLine.of(cx));
        };
    }

    private static WrappedFluid parseFluidString(Context cx, @NotNull String from) {
        String trimmed = from.trim();

        if (trimmed.startsWith("#")) {
            return WrappedFluid.ofTag(parseFluidTagId(cx, trimmed.substring(1)));
        }

        return WrappedFluid.ofFluid(parseFluidId(cx, trimmed));
    }

    private static WrappedFluid wrapFluidList(Context cx, List<?> from) {
        List<Fluid> fluids = new ArrayList<>(from.size());

        for (Object entry : from) {
            fluids.add(switch (entry) {
                case Fluid fluid -> fluid;
                case ResourceLocation id -> parseFluidId(cx, id);
                case String id when !id.trim().startsWith("#") -> parseFluidId(cx, id.trim());
                default -> throw new KubeRuntimeException("Tags aren't allowed inside a fluid list, use a single \"#tag\" instead: %s".formatted(entry)).source(SourceLine.of(cx));
            });
        }

        return WrappedFluid.ofFluids(fluids);
    }

    static Fluid parseFluidId(Context cx, String from) {
        ResourceLocation id = ResourceLocation.tryParse(from);

        if (id == null) {
            throw new KubeRuntimeException("Invalid fluid id %s".formatted(from)).source(SourceLine.of(cx));
        }

        return parseFluidId(cx, id);
    }

    static Fluid parseFluidId(Context cx, ResourceLocation id) {
        Fluid fluid = BuiltInRegistries.FLUID.get(id);

        if (fluid == Fluids.EMPTY) {
            throw new KubeRuntimeException("Unknown fluid %s".formatted(id)).source(SourceLine.of(cx));
        }

        return fluid;
    }

    static TagKey<Fluid> parseFluidTagId(Context cx, String from) {
        ResourceLocation id = ResourceLocation.tryParse(from);

        if (id == null) {
            throw new KubeRuntimeException("Invalid fluid tag id #%s".formatted(from)).source(SourceLine.of(cx));
        }

        return TagKey.create(Registries.FLUID, id);
    }

    static TagKey<Fluid> castFluidTag(Context cx, TagKey<?> tag) {
        if (tag.registry() != Registries.FLUID) {
            throw new KubeRuntimeException("Tag %s is not a fluid tag".formatted(tag)).source(SourceLine.of(cx));
        }

        return FluidTags.create(tag.location());
    }
}