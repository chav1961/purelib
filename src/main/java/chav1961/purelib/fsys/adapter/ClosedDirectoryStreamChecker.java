package chav1961.purelib.fsys.adapter;

import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.Path;

final class ClosedDirectoryStreamChecker extends ClosedChecker {

  void check(Path path) {
    if (!this.isOpen()) {
      throw new ClosedDirectoryStreamException();
    }
  }

}
