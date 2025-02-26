package chav1961.purelib.basic;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chav1961.purelib.basic.interfaces.WatchServiceMaintenanceCallback;
import chav1961.purelib.basic.interfaces.WatchServiceMaintenanceCallback.EventType;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;

/**
 * <p>This class is used to listen in the directories changes. It implements {@linkplain ExecutionControl} service to start/stop/suspend/resume watching. Any changes when watcher is started and
 * not suspended. will produce call {@linkplain WatchServiceMaintenanceCallback#process(EventType, File)} method. It's strongly recommended to use this class in <b>try-with-resource</b> statement.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class DirectoryListener implements ExecutionControl, Closeable {
	private static final long		WATCH_PERIOD = 5000;
	
	private final WatchServiceMaintenanceCallback	callback;
	private final List<FileAndWatcher>				dirs = new ArrayList<>();
	private final boolean							watchSubdir;
	private final long								watchPeriod;
	private volatile SimpleTimerTask				tt = null;
	private volatile boolean						started = false, suspended = false; 

	/**
	 * <p>Constructor of the class instance.</p>
	 * @param callback callback to process directory changes. Can't be null.
	 * @param directories directories to watch. Can't be neither null nor empty, and can't contains nulls inside.
	 * @throws NullPointerException callback is null
	 * @throws IllegalArgumentException directories list is null, empty, contains nulls inside, contains files instead of directories or has non-accessible items for current user. 
	 * @throws IOException on any I/O errors.
	 */
	public DirectoryListener(final WatchServiceMaintenanceCallback callback, final File... directories) throws NullPointerException, IllegalArgumentException, IOException {
		this(callback, true, true, WATCH_PERIOD, directories);
	}

	/**
	 * <p>Constructor of the class instance.</p>
	 * @param callback callback to process directory changes. Can't be null.
	 * @param watchSubdir watch sub-directories too.
	 * @param skipNotAccessible skip non-accessible directories instead of throwing exception
	 * @param watchPeriod minimal watch period to observe. 
	 * @param directories directories to watch. Can't be neither null nor empty, and can't contains nulls inside.
	 * @throws NullPointerException callback is null
	 * @throws IllegalArgumentException directories list is null, empty, contains nulls inside, contains files instead of directories or has non-accessible items for current user. 
	 * @throws IOException on any I/O errors.
	 */
	public DirectoryListener(final WatchServiceMaintenanceCallback callback, final boolean watchSubdir, final boolean skipNotAccessible, final long watchPeriod, final File... directories) throws NullPointerException, IllegalArgumentException, IOException {
		if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else if (watchPeriod <= 0) {
			throw new IllegalArgumentException("Watch period ["+watchPeriod+"] must be positive"); 
		}
		else if (directories == null || directories.length == 0 || Utils.checkArrayContent4Nulls(directories) >= 0) {
			throw new IllegalArgumentException("Directories list is null, empty or contains nulls inside"); 
		}
		else {
			this.callback = callback;
			this.watchSubdir = watchSubdir;
			this.watchPeriod = watchPeriod;
			
			for (File item : directories) {
				if (item.isDirectory()) {
					if (item.canRead()) {
						dirs.addAll(prepareWatchers(item, skipNotAccessible));
					}
					else if (!skipNotAccessible) {
						throw new IllegalArgumentException("Directory list item ["+item+"] is not accessible for you"); 
					}
				}
				else {
					throw new IllegalArgumentException("Directory list item ["+item+"] is not a directory"); 
				}
			}
		}
	}
	
	@Override
	public synchronized void start() throws IOException {
		if (isStarted()) {
			throw new IllegalStateException("Listener already started"); 
		}
		else {
			tt = SimpleTimerTask.start(()->poll(), watchPeriod, watchPeriod);
			started = true;
		}
	}

	@Override
	public synchronized void suspend() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Listener not started yet"); 
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Listener already suspended"); 
		}
		else {
			suspended = true;
		}
	}

	@Override
	public synchronized void resume() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Listener not started yet"); 
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Listener is not suspended yet"); 
		}
		else {
			suspended = false;
		}
	}

	@Override
	public synchronized void stop() throws IOException {
		if (!isStarted()) {
			throw new IllegalStateException("Listener not started yet"); 
		}
		else {
			if (isSuspended()) {
				resume();
			}
			tt.cancel();
			tt = null;
			started = false;
		}
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}

	@Override
	public synchronized void close() throws IOException {
		if (isStarted()) {
			stop();
		}
		
		for (FileAndWatcher item : dirs) {
			item.dwd.close();
		}
	}
	
	protected void poll() {
		if (!isSuspended()) {
			final List<FileAndWatcher> toAdd = new ArrayList<>();
			final List<File> toRemove = new ArrayList<>();
			
			for (FileAndWatcher item : dirs) {
				try{item.dwd.maintenance(false, (evt, f)->processCallback(evt, f, toAdd, toRemove));
				} catch (IOException | InterruptedException e) {
				}
			}
			
			for (File item : toRemove) {
				for (int index = dirs.size(); index >= 0; index--) {
					if (item.equals(dirs.get(index).f)) {
						try{dirs.remove(index).dwd.close();
						} catch (IOException e) {
						}
					}
				}
			}
			
			dirs.addAll(toAdd);
		}
	}

	private void processCallback(final EventType type, final File file, final List<FileAndWatcher> toAdd, final List<File> toRemove) throws IOException {
		switch (type) {
			case CREATED	:
				if (watchSubdir && file.isDirectory() && file.canRead()) {
					toAdd.add(new FileAndWatcher(file,new DirectoryWatchDescriptor(file)));
				}
				callback.process(type, file);
				break;
			case MODIFIED	:
				callback.process(type, file);
				break;
			case REMOVED	:
				callback.process(type, file);
				if (watchSubdir) {
					toRemove.add(file);
				}
				break;
			case OVERFOW	:
				callback.process(type, null);
				break;
			default:
				throw new UnsupportedOperationException("Event type ["+type+"] is not supported yet"); 
		}
	}
	
	private static List<FileAndWatcher> prepareWatchers(final File dir, final boolean skipNotAccessible) throws IOException {
		if (dir == null || !dir.isDirectory()) {
			throw new IllegalArgumentException("File cant be null and must be an accessible directory");
		}
		else {
			final Set<File>				dirSet = new HashSet<>();
			final List<FileAndWatcher>	faw = new ArrayList<>();
			
			collectDirs(dir, dirSet, skipNotAccessible);
			for (File item : dirSet) {
				faw.add(new FileAndWatcher(item, new DirectoryWatchDescriptor(item)));
			}
			return faw;
		}
	}

	private static void collectDirs(final File dir, final Set<File> dirList, final boolean skipNotAccessible) {
		dirList.add(dir);
		dir.listFiles((File f)->{
			if (f.isDirectory()) {
				if (f.canRead()) {
					collectDirs(f, dirList, skipNotAccessible);
				}
				else {
					throw new IllegalArgumentException("Directory list item ["+f+"] is not accessible for you"); 
				}
			}
			return false;
		});
	}
	
	static class DirectoryWatchDescriptor implements Closeable {
		private final File			dir;
		private final Path			dirPath;
		private final WatchService 	watcher;		
		private final WatchKey 		regKey;
		
		public DirectoryWatchDescriptor(final File dir) throws IOException {
			if (dir == null || !dir.isDirectory()) {
				throw new IllegalArgumentException("File can'tbe null and must be acessible directory"); 
			}
			else {
				this.dir = dir;
				this.dirPath = dir.toPath();
				this.watcher = FileSystems.getDefault().newWatchService();		
				this.regKey = this.dirPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			}
		}

		public File getDirWatched() {
			return dir;
		}
		
		public boolean maintenance(final boolean await, final WatchServiceMaintenanceCallback callback) throws IOException, InterruptedException, NullPointerException {
			if (callback ==null) {
				throw new NullPointerException("Callback can't be null"); 
			}
			else {
				final WatchKey	wk = await ? watcher.take() : watcher.poll();
				
				if (wk != null) {
					try{
						for (WatchEvent<?> event: wk.pollEvents()) {
					        final WatchEvent.Kind<?> 	kind = event.kind();
			
					        if (kind != StandardWatchEventKinds.OVERFLOW) {
						        callback.process(WatchServiceMaintenanceCallback.EventType.valueOf(kind), dirPath.resolve(((WatchEvent<Path>)event).context()).toFile());
					        }
					    }
						return true;
					} finally {
						wk.reset();
					}
				}
				else {
					return false;
				}
			}
		}
		
		@Override
		public void close() throws IOException {
			regKey.cancel();
			watcher.close();
		}
	}

	private static class FileAndWatcher {
		private final File						f;
		private final DirectoryWatchDescriptor	dwd;
		
		private FileAndWatcher(final File f, final DirectoryWatchDescriptor dwd) {
			this.f = f;
			this.dwd = dwd;
		}
	}
}
