package chav1961.purelib.fsys.adapter;

import java.nio.file.ClosedFileSystemException;

final class ClosedFileSystemChecker extends ClosedChecker {

  void check() {
    if (!this.isOpen()) {
      throw new ClosedFileSystemException();
    }
  }
}