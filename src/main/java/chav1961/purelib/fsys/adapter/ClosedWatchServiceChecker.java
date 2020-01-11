package chav1961.purelib.fsys.adapter;

import java.nio.file.ClosedWatchServiceException;

final class ClosedWatchServiceChecker extends ClosedChecker {

  void check() {
    if (!this.isOpen()) {
      throw new ClosedWatchServiceException();
    }
  }

}
