package io;

import java.io.*;

public abstract class TempFileWriter implements Writer, IOConstants {
    private final File file;
    protected final ObjectOutputStream stream;

    public TempFileWriter() throws IOException {
        file = File.createTempFile("osm", "");
        file.deleteOnExit();
        stream =
                new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE));
    }

    protected final InputStream finish() throws IOException {
        stream.flush();
        stream.close();
        return new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        try (var in = finish()) {
            in.transferTo(out);
            out.flush();
        }
        file.delete();
    }
}
