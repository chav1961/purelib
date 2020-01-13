package chav1961.purelib.fsys.adapter;

import java.io.IOException;
import java.nio.file.attribute.UserPrincipal;
import java.util.concurrent.Callable;
/**
 * Provides access to the current user.
 */
public final class CurrentUser {

  private CurrentUser() {
    throw new AssertionError("not instantiable");
  }

  private static final ThreadLocal<UserPrincipal> USER = new ThreadLocal<>();

  /**
   * Sets the current user for a certain period.
   *
   * @param user the user to use
   * @param task during this task the given user will be used, will be called
   *  immediately by the current thread
   * @param <V> the type of the return value
   * @return what the task returned
   * @throws IOException if any of the code in the task throws an
   *  {@link IOException}
   */
  public static <V> V useDuring(UserPrincipal user, UserTask<V> task) throws IOException {
    UserPrincipal previous = USER.get();
    try {
      USER.set(user);
      return task.call();
    } finally {
      if (previous == null) {
        USER.remove();
      } else {
        USER.set(previous);
      }
    }
  }

  static UserPrincipal get() {
    return USER.get();
  }

  /**
   * Functional interface for a task during which a certain user should be used.
   *
   * @param <V> the type of the return value
   */
  @FunctionalInterface
  public interface UserTask<V> extends Callable<V> {

    /**
     * Executes the task.
     *
     * @return the return value of the task
     * @throws IOException if any of the code in the task throws an
     *  {@link IOException}
     */
    @Override
    V call() throws IOException;

  }

}
