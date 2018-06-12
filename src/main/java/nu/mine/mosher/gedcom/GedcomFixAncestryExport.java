package nu.mine.mosher.gedcom;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Created by Christopher Alan Mosher on 2017-09-02

public class GedcomFixAncestryExport {
    public static void main(final String... args) throws IOException {
        if (args.length != 1) {
            System.out.println("usage: java -jar gedcom-fixancestryexport-all.jar CharSetName");
        } else {
            new GedcomFixAncestryExport(args[0]).main();
        }
        System.out.flush();
        System.err.flush();
    }

    private final BufferedReader in;
    private int level = -1;
    private String tag = "";
    private String line;
    private int cBad;

    private GedcomFixAncestryExport(final String cs) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(new FileInputStream(FileDescriptor.in), cs));
    }

    private void main() throws IOException {
        process();
        this.in.close();
        if (this.cBad > 0) {
            System.err.println("ANCESTRY GEDCOM FIXER: bad lines found: " + Integer.toString(this.cBad));
            System.err.flush();
        }
    }

    private final Pattern pH = Pattern.compile("(0) (HEAD)");
    private final Pattern pT = Pattern.compile("(0) (TRLR)");
    private final Pattern p0 = Pattern.compile("(0) @[-A-Za-z0-9]+@ ([A-Z_]{3,5}).*");
    private final Pattern pN = Pattern.compile("([0-9]) ([A-Z_]{3,5}).*");

    private void process() throws IOException {
        for (next(); this.line != null; next()) {
            final int levelActual = match();
            if (isExpected(levelActual)) {
                ok();
                this.level = levelActual;
            } else {
                bad();
            }
        }
    }

    private int match() {
        Matcher m;
        m = this.pH.matcher(this.line);
        if (m.matches()) {
            this.tag = m.group(2);
            return Integer.parseInt(m.group(1));
        }
        m = this.pT.matcher(this.line);
        if (m.matches()) {
            this.tag = m.group(2);
            return Integer.parseInt(m.group(1));
        }
        m = this.p0.matcher(this.line);
        if (m.matches()) {
            this.tag = m.group(2);
            return Integer.parseInt(m.group(1));
        }
        m = this.pN.matcher(this.line);
        if (m.matches()) {
            this.tag = m.group(2);
            return Integer.parseInt(m.group(1));
        }
        return -999;
    }

    private void bad() {
        this.cBad++;
        this.level = this.level + conx();
        System.out.println(Integer.toString(this.level) + " CONT " + this.line);
        this.tag = "CONT";
    }

    private int conx() {
        return this.tag.equals("CONC") || this.tag.equals("CONT") ? 0 : 1;
    }

    private void ok() {
        System.out.println(this.line);
    }

    private void next() throws IOException {
        this.line = this.in.readLine();
    }

    private boolean isExpected(final int levelActual) {
        return 0 <= levelActual && levelActual <= this.level + 1;
    }
}
