package fr.gouv.dsfr.faces.migrate;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Migration facultative (après le sed obligatoire) : enveloppe les champs des pages Facelets dans
 * {@code dsfr:field}.
 * <pre>
 * java -jar dsfr-faces-migrate.jar [--dry-run] &lt;dossier|fichier.xhtml&gt;...
 * </pre>
 * Modifie les fichiers en place (sauf {@code --dry-run}) et liste les champs laissés tels quels.
 * Code de sortie : 0, 1 si un fichier n'a pas pu être lu ou analysé, 2 si les arguments sont invalides.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) throws IOException {
        boolean dryRun = false;
        List<Path> inputs = new ArrayList<>();
        for (String arg : args) {
            if ("--dry-run".equals(arg)) {
                dryRun = true;
            } else {
                inputs.add(Path.of(arg));
            }
        }
        if (inputs.isEmpty()) {
            err.println("usage : java -jar dsfr-faces-migrate.jar [--dry-run] <dossier|fichier.xhtml>...");
            return 2;
        }

        List<Path> files = new ArrayList<>();
        for (Path input : inputs) {
            if (Files.isDirectory(input)) {
                try (Stream<Path> walk = Files.walk(input)) {
                    walk.filter(p -> p.toString().endsWith(".xhtml")).sorted().forEach(files::add);
                }
            } else if (Files.isRegularFile(input)) {
                files.add(input);
            } else {
                err.println(input + " : introuvable");
                return 2;
            }
        }

        int wrapped = 0;
        int notes = 0;
        int failures = 0;
        for (Path file : files) {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            FieldMigration.Result result;
            try {
                result = FieldMigration.migrate(source);
            } catch (XmlLite.ParseException e) {
                err.println(file + " : non analysé (" + e.getMessage() + ")");
                failures++;
                continue;
            }
            if (result.wrapped() > 0 || !result.notes().isEmpty()) {
                out.println(file + " : " + result.wrapped() + " champ(s) enveloppé(s) dans dsfr:field");
                for (FieldMigration.Note note : result.notes()) {
                    out.println("  ligne " + note.line() + " : " + note.message());
                }
            }
            if (!dryRun && result.changed(source)) {
                Files.writeString(file, result.content(), StandardCharsets.UTF_8);
            }
            wrapped += result.wrapped();
            notes += result.notes().size();
        }
        out.println(files.size() + " fichier(s), " + wrapped + " champ(s) enveloppé(s), " + notes
                + " à revoir à la main" + (dryRun ? " (--dry-run : aucun fichier modifié)" : ""));
        return failures == 0 ? 0 : 1;
    }
}
