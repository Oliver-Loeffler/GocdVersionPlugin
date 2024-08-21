package net.raumzeitfalle.gradle.gocd.versioning;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class GitTagCollector {

    private final Logger logger;
    private final Repository repo;
    private final Pattern versionTagPattern;

    public GitTagCollector(Repository repo, String versionTagRegex, Logger logger) {
        this.repo = repo;
        this.versionTagPattern = Pattern.compile(versionTagRegex, Pattern.CASE_INSENSITIVE);
        this.logger = logger;
    }

    public Map<ObjectId,String> collect() {
        try {
            return collectTags();
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    public Map<ObjectId,String> collectTags() throws IOException {
        Map<ObjectId,String> tags = new HashMap<>();
        List<Ref> refs = repo.getRefDatabase().getRefsByPrefix(Constants.R_TAGS);
        for (Ref ref : refs) {
            storeSuitableTag(tags, ref);
        }
        return tags;
    }

    private String getTag(String refName) {
        return refName.replace(Constants.R_TAGS, "");
    }

    private void storeSuitableTag(Map<ObjectId,String> tags, Ref commitRef) {
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

}
