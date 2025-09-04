package com.cleanroommc.bogosorter.compat.data_driven.condition;

import com.cleanroommc.bogosorter.compat.data_driven.utils.json.JsonSchema;
import com.github.bsideup.jabel.Desugar;
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
record ModCond(
    String id,
    Optional<Pattern> versionPattern,
    Optional<VersionRange> versionRange
) implements BogoCondition {
    public static final JsonSchema<ModCond> SCHEMA = JsonSchema.object(
        JsonSchema.STRING.toField("id"),
        JsonSchema.STRING.map(Pattern::compile).toOptionalField("versionPattern"),
        JsonSchema.STRING.map(ModCond::fromVersionSpecOrThrow).toOptionalField("versionRange"),
        ModCond::new
    ).describe("Return `true` if there's a mod with matching id and/or version");

    @Override
    public boolean test() {
        var mod = Loader.instance().getIndexedModList().get(id);
        if (mod == null) {
            return false;
        } else if (versionRange.isPresent()) {
            return versionRange.get().containsVersion(new DefaultArtifactVersion(mod.getVersion()));
        } else if (versionPattern.isPresent()) {
            return versionPattern.get().matcher(mod.getVersion()).matches();
        }
        return true;
    }

    private static VersionRange fromVersionSpecOrThrow(String versionSpec) {
        try {
            return VersionRange.createFromVersionSpec(versionSpec);
        } catch (InvalidVersionSpecificationException e) {
            throw new RuntimeException(e);
        }
    }
}
