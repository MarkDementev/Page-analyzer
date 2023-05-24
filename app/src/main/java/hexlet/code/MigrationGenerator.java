package hexlet.code;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;

import java.io.IOException;

public final class MigrationGenerator {
    public static void main(String[] args) throws IOException {
        DbMigration dBMigration = DbMigration.create();

        dBMigration.addPlatform(Platform.POSTGRES);
        dBMigration.addPlatform(Platform.H2);

        dBMigration.generateMigration();
    }
}
