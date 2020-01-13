package chav1961.purelib.fsys.adapter;

import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;

interface AccessCheck {

  void checkAccess(AccessMode[] modes) throws AccessDeniedException;

  void checkAccess(AccessMode mode) throws AccessDeniedException;

}
