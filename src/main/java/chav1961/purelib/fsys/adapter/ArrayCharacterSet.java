package chav1961.purelib.fsys.adapter;

class ArrayCharacterSet implements CharacterSet {

  private final char[] characters;

  ArrayCharacterSet(char[] characters) {
    this.characters = characters;
  }

  @Override
  public boolean containsAny(String s) {
    for (char each : this.characters) {
      if (s.indexOf(each) >= 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean contains(char c) {
    for (char each : this.characters) {
      if (each == c) {
        return true;
      }
    }
    return false;
  }

}
