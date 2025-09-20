package net.raumzeitfalle.gradle.gocd.versioning;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.SystemReader;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class GitTagCollector {

    public static String DEFAULT_VERSIONABLE_TAG_REGEX = "^([vV])?\\d+([.]\\d+)?([.]\\d+)?([.]\\d+)?$";

    private final Logger logger;
    private final Repository repo;
    private final Pattern versionTagPattern;

    public GitTagCollector(Repository repo, String versionTagRegex, Logger logger) {
        this.repo = repo;
        this.versionTagPattern = compilePatternAndWarnOnError(versionTagRegex);
        this.logger = logger;
    }

    private Pattern compilePatternAndWarnOnError(String regex) {
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (IllegalArgumentException error) {
            logWarn("The given string is not a valid regular expression: " + regex, error);
            return Pattern.compile(DEFAULT_VERSIONABLE_TAG_REGEX, Pattern.CASE_INSENSITIVE);
        }
    }

    public Map<ObjectId, String> collect() {
        try {
            return collectTags();
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    public Map<ObjectId, String> collectTags() throws IOException {
        Map<ObjectId, String> tags = new HashMap<>();
        List<Ref> refs = repo.getRefDatabase().getRefsByPrefix(Constants.R_TAGS);
        for (Ref ref : refs) {
            storeSuitableTag(tags, ref);
        }
        return tags;
    }

    private String getTag(String refName) {
        return refName.replace(Constants.R_TAGS, "");
    }

    private void storeSuitableTag(Map<ObjectId, String> tags, Ref commitRef) {
        String refName = commitRef.getName();
        String tag = getTag(refName);
        if (isSuitable(tag)) {
            ObjectId key = commitRef.getObjectId();
            tags.put(key, refName);
        } else {
            this.logDebug("Ignoring unsuitable tag: {0}", tag);
        }
    }

    private boolean isSuitable(String tag) {
        return this.versionTagPattern
                .matcher(tag)
                .matches();
    }

    private void logDebug(String format, Object arg) {
        if (this.logger != null) {
            this.logger.debug(format, arg);
        }
    }

    private void logWarn(String format, Object arg) {
        if (this.logger != null) {
            this.logger.warn(format, arg);
        }
    }

}
