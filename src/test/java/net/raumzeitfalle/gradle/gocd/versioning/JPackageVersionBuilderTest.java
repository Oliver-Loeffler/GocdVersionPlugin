package net.raumzeitfalle.gradle.gocd.versioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPackageVersionBuilderTest {
    
    private JPackageVersionBuilder classUnderTest;
    
    private GocdVersionPluginExtension extension;
    
    private org.gradle.api.logging.Logger logger = org.gradle.api.logging.Logging.getLogger("TEST");;

    @BeforeEach
    public void prepareTest() {
        extension = new GocdVersionPluginExtension();
        extension.setTimestampSupplier(() -> LocalDateTime.of(2021, 9, 4, 19, 22, 13));
    }
    
    @Test
    void that_MSI_WIX_compatible_version_is_created_from_autoBuildVersion() {
        classUnderTest = new JPackageVersionBuilder(extension, "20211125.12", logger);
        assertEquals("21.47.12", classUnderTest.build());
    }
    
    @Test
    void that_MSI_WIX_compatible_version_is_created_by_default_for_blank_version_string() {
        classUnderTest = new JPackageVersionBuilder(extension, "", logger);
        assertEquals("21.35.06", classUnderTest.build());
    }
    
    @Test
    void that_exception_is_thrown_when_commit_distance_exceeds_255() {
        classUnderTest = new JPackageVersionBuilder(extension, "20211125.256", logger);
        Throwable t = assertThrows(GradleException.class, ()->classUnderTest.build());
        
        String expected = "GocdVersionPlugin: Cannot build MSI/WIX compatible version number. "
                        + "The patch version exceeds 255. "
                        + "Please rework the supplied version String (20211125.256). "
                        + "Expected: yyyyMMdd.patch";
        
        assertEquals(expected, t.getMessage());
    }
    
    @Test
    void that_exception_is_thrown_when_date_cannot_be_parsed_from_autoversion() {
        classUnderTest = new JPackageVersionBuilder(extension, "99999999.0", logger);
        Throwable t = assertThrows(GradleException.class, ()->classUnderTest.build());
        
        String expected = "GocdVersionPlugin: Cannot build MSI/WIX compatible version number. "
                        + "Failed to parse the timestamp from supplied version String (99999999.0). "
                        + "Expected: yyyyMMdd.patch";
        
        assertEquals(expected, t.getMessage());
    }

}
