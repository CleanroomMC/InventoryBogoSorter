package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.Loader;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author ZZZank
 */
@Desugar
public record ModCond(
    String id,
    Optional<Pattern> versionPattern,
    Optional<VersionRange> versionRange
) implements BogoCondition {
    public static ModCond read(JsonObject object) {
        return new ModCond(
            object.get("id").getAsString(),
            Optional.ofNullable(object.get("version_range")).map(JsonElement::getAsString).map(Pattern::compile),
            Optional.ofNullable(object.get("version_pattern"))
                .map(JsonElement::getAsString)
                .map(spec -> {
                    try {
                        return VersionRange.createFromVersionSpec(spec);
                    } catch (InvalidVersionSpecificationException e) {
                        throw new RuntimeException(e);
                    }
                })
        );
    }

    @Override
    public boolean test() {
        var mod = Loader.instance().getIndexedModList().get(id);
        if (mod == null) {
            return false;
        } else if (versionPattern.isPresent()) {
            return versionPattern.get().matcher(mod.getVersion()).matches();
        } else if (versionRange.isPresent()) {
            return versionRange.get().containsVersion(new DefaultArtifactVersion(mod.getVersion()));
        }
        return true;
    }
}
