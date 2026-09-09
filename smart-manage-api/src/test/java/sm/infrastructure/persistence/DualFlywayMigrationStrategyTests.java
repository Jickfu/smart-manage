package sm.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DualFlywayMigrationStrategyTests {
    @Test
    void disabledBusinessKeepsExistingPlatformOnlyBehavior() {
        Flyway platform = mock(Flyway.class);
        new DualFlywayMigrationStrategy(new BusinessMigrationProperties()).migrate(platform);
        verify(platform).migrate();
        verifyNoMoreInteractions(platform);
    }

    @Test
    void platformFailurePreventsBusinessInitialization() {
        Flyway platform = platform();
        when(platform.migrate()).thenThrow(new IllegalStateException("平台迁移失败"));
        assertThrows(IllegalStateException.class, () -> strategy().migrate(platform));
        verify(platform, never()).info();
        verify(platform.getConfiguration(), never()).getDataSource();
    }

    @Test
    void insufficientPlatformVersionPreventsBusinessInitialization() {
        Flyway platform = platform();
        MigrationInfoService info = mock(MigrationInfoService.class);
        MigrationInfo current = mock(MigrationInfo.class);
        when(platform.info()).thenReturn(info);
        when(info.current()).thenReturn(current);
        when(current.getVersion()).thenReturn(MigrationVersion.fromVersion("1"));
        assertThrows(IllegalStateException.class, () -> strategy().migrate(platform));
        verify(platform.getConfiguration(), never()).getDataSource();
    }

    @Test
    void sharedHistoryOrOverlappingLocationsAreRejectedBeforeAnyMigration() {
        Flyway platform = platform();
        Configuration configuration = platform.getConfiguration();
        when(configuration.getTable()).thenReturn(DualFlywayMigrationStrategy.BUSINESS_HISTORY_TABLE);
        assertThrows(IllegalArgumentException.class, () -> strategy().migrate(platform));
        when(configuration.getTable()).thenReturn("flyway_schema_history");
        when(configuration.getLocations()).thenReturn(new Location[]{new Location("classpath:db")});
        assertThrows(IllegalArgumentException.class, () -> strategy().migrate(platform));
        verify(platform, never()).migrate();
    }

    @Test
    void invalidRequiredVersionIsRejectedBeforePlatformMutation() {
        Flyway platform = platform();
        for (String version : new String[]{"", "0", "latest", "-1"}) {
            BusinessMigrationProperties properties = new BusinessMigrationProperties();
            properties.setEnabled(true);
            properties.setMinimumPlatformVersion(version);
            assertThrows(IllegalArgumentException.class,
                    () -> new DualFlywayMigrationStrategy(properties).migrate(platform));
        }
        verify(platform, never()).migrate();
    }

    private static DualFlywayMigrationStrategy strategy() {
        BusinessMigrationProperties properties = new BusinessMigrationProperties();
        properties.setEnabled(true);
        return new DualFlywayMigrationStrategy(properties);
    }

    private static Flyway platform() {
        Flyway platform = mock(Flyway.class);
        Configuration configuration = mock(Configuration.class);
        when(platform.getConfiguration()).thenReturn(configuration);
        when(configuration.getTable()).thenReturn("flyway_schema_history");
        when(configuration.getLocations()).thenReturn(new Location[]{new Location("classpath:db/migration")});
        return platform;
    }
}
