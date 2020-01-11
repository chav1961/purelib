package chav1961.purelib.fsys.adapter;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;

final class ClosedStreamChecker extends ClosedChecker {

  void check(Path path) throws IOException {
    if (!this.isOpen()) {
      throw new FileSystemException(path.toString(), null, "stream is closed");
    }
  }

}
