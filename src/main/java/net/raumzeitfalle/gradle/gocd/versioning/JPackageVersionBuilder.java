package net.raumzeitfalle.gradle.gocd.versioning;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;

public class JPackageVersionBuilder {  
    
    private final GocdVersionPluginExtension extension;
    private final Object autoBuildVersion;
    private final Logger logger;
    
    public JPackageVersionBuilder(GocdVersionPluginExtension extension, 
                                  Object autoBuildVersion,
                                  Logger logger) {
        this.extension          = Objects.requireNonNull(extension, "extension must not be null");
        this.autoBuildVersion   = Objects.requireNonNull(autoBuildVersion, "autoBuildVersion must not be null");
        this.logger = logger;
    }
    
    public String build() {
        String versionBase = String.valueOf(autoBuildVersion).trim();
        if ("".equalsIgnoreCase(versionBase)) {
            String message = "Auto-generating JPackageVersion from timestamp. "
                           + "The provided autoBuildVersion String was empty but is supposed to "
                           + "match the pattern: yyyyMMdd.patch.";
            logger.warn(message);
            return getDefaultVersion();
        }
        
        if (versionBase.matches("^\\d{8}[.]\\d+$")) {
            int firstDot = versionBase.indexOf('.');
            String assumedTimestamp = versionBase.substring(0, firstDot);
            String commitDistance   = versionBase.substring(firstDot+1);
            
            LocalDate tagDate = null;
            try {
                tagDate = LocalDate.parse(assumedTimestamp,DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (DateTimeParseException parseError) {
                String message = "Cannot build MSI/WIX compatible version number. Failed to parse the timestamp "
                               + "from supplied version String (%s). Expected: yyyyMMdd.patch";
                throw new GradleException(String.format(message, versionBase));
            }

            int commitDist = Integer.parseInt(commitDistance);
            if (commitDist > 255) {
                String message = "Cannot build MSI/WIX compatible version number. The patch version exceeds 255. "
                               + "Please rework the supplied version String (%s). Expected: yyyyMMdd.patch";
                throw new GradleException(String.format(message, versionBase));
            }
            return tagWithCommitDistVersion(tagDate, commitDist);
        }
        
        String message = "Generating JPackageVersion from todays timestamp as the supplied autoBuildVersion value (%s) "
                       + "does not match the expected pattern: yyyyMMdd.patch";

        logger.warn(String.format(message, versionBase));
        return getDefaultVersion();
    }

    private String getDefaultVersion() {
        LocalDateTime date = extension.getTimestampSupplier().get();
        String major = DateTimeFormatter.ofPattern("YY").format(date);
        String minor = DateTimeFormatter.ofPattern("ww").format(date);
        String patch = DateTimeFormatter.ofPattern("ee").format(date);
        String version = major+"."+minor+"."+patch;
        String message = "JPackageVersion: %s (using default strategy major.minor.patch = year.weekOfYear.dayOfWeek)";
        logger.lifecycle(String.format(message, version));
        return version;
    }
    
    private String tagWithCommitDistVersion(LocalDate date, int commitDist) {
        String major = DateTimeFormatter.ofPattern("YY").format(date);
        String minor = DateTimeFormatter.ofPattern("ww").format(date);
        String patch = Integer.toString(commitDist);
        String version = major+"."+minor+"."+patch;
        String message = "JPackageVersion: %s (using LocalDate.CommitDistance strategy, year.weekOfYear.commitDistance)";
        logger.lifecycle(String.format(message, version));
        return version;
    }
}
