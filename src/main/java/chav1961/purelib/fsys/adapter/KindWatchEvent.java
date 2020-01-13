package chav1961.purelib.fsys.adapter;

import java.nio.file.Path;

final class KindWatchEvent extends PathWatchEvent {

  private final Kind<Path> kind;


  KindWatchEvent(Path path, Kind<Path> kind) {
    super(path);
    this.kind = kind;
  }

  @Override
  public Kind<Path> kind() {
    return this.kind;
  }

  @Override
  public int count() {
    return 1;
  }

}
