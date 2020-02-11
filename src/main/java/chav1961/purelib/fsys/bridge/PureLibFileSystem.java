package chav1961.purelib.fsys.bridge;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.ExtendedBitCharSet;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;

class PureLibFileSystem extends FileSystem {
	private static final String 	SYNTAX_GLOB = "glob";
	private static final String 	SYNTAX_REGEX = "regex";
	private static final char		PATTERN_TERMINAL = (char)0xFFFF;
	
	private final PureLibFileSystemProvider	provider;
	private final AtomicBoolean				isClosed = new AtomicBoolean(false);
	
	PureLibFileSystem(final PureLibFileSystemProvider provider) {
		this.provider = provider;
	}

	@Override
	public FileSystemProvider provider() {
		return provider;
	}

	@Override
	public void close() throws IOException {
		if (!isClosed.getAndSet(true)) {
		}
	}

	@Override
	public boolean isOpen() {
		return !isClosed.get();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getSeparator() {
		return PureLibFileSystemProvider.PATH_SPLITTER;
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		final List<Path>	result = new ArrayList<>();
		
		try{for (FileSystemInterfaceDescriptor item : FileSystemFactory.getAvailableFileSystems()) {
				final URI	template = URI.create(item.getUriTemplate().getSchemeSpecificPart());
				
				result.add(getPath(template.getScheme(),template.getSchemeSpecificPart()));
			}
		} catch (IOException e) {
		}
		return result;
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		final List<FileStore>	result = new ArrayList<>();

		try{for (FileSystemInterfaceDescriptor item : FileSystemFactory.getAvailableFileSystems()){
				result.add(new PureLibFileStore(item));
			}
		} catch (IOException e) {
		}
		return result;
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		return Set.of(PureLibFileSystemProvider.ATTRIBUTE_BASIC,PureLibFileSystemProvider.ATTRIBUTE_PURELIB);
	}

	@Override
	public Path getPath(final String first, final String... more) {
		if (first == null || first.isEmpty()) {
			throw new IllegalArgumentException("First part of path can't be null or empty string");
		}
		else if (Utils.checkArrayContent4Nulls(more) >= 0) {
			throw new IllegalArgumentException("More list contains nulls inside!");
		}
		else {
			try{for (FileSystemInterfaceDescriptor item : FileSystemFactory.getAvailableFileSystems()) {
					final URI	template = URI.create(item.getUriTemplate().getSchemeSpecificPart());

					if (first.equals(template.getScheme())) {
						return new PureLibPath(this,template.getScheme(),PureLibFileSystemProvider.PATH_SPLITTER+CharUtils.join(PureLibFileSystemProvider.PATH_SPLITTER,more));
					}
				}
			} catch (IOException e) {
				throw new InvalidPathException("["+first+"] and "+Arrays.toString(more),"Can't build path: "+e.getLocalizedMessage());
			}
			throw new InvalidPathException("["+first+"] and "+Arrays.toString(more),"No any known filesystem roots with subscheme ["+first+"]");
		}
	}

	@Override
	public PathMatcher getPathMatcher(final String syntaxAndPattern) {
		if (syntaxAndPattern == null || syntaxAndPattern.isEmpty()) {
			throw new IllegalArgumentException("Syntax and pattern string can't be null or empty!");
		}
		else {
			final int	colonIndex = syntaxAndPattern.indexOf(':');
			
			if (colonIndex > 0) {
				final String	syntax = syntaxAndPattern.substring(0,colonIndex).toLowerCase();
				final String	pattern = syntaxAndPattern.substring(colonIndex+1);
				
				switch (syntax) {
					case SYNTAX_GLOB	:
						final Test	test = compilePattern(CharUtils.terminateAndConvert2CharArray(pattern,PATTERN_TERMINAL));
						
						return new PathMatcher() {
							@Override
							public boolean matches(final Path path) {
								return test.test(path.toUri());
							}
						};
					case SYNTAX_REGEX	:
						return new PathMatcher() {
							final Pattern	p = Pattern.compile(pattern);
							@Override
							public boolean matches(final Path path) {
								return p.matcher(path.toUri().getPath()).matches();
							}
						};
					default :
						throw new IllegalArgumentException("Illegal content ["+syntaxAndPattern+"]: syntax type ["+syntax+"] is not supported");
				}
			}
			else {
				throw new IllegalArgumentException("Illegal content ["+syntaxAndPattern+"]: syntax type clause is missing");
			}
			// TODO Auto-generated method stub
			
//			*.java	Matches a path that represents a file name ending in .java
//			*.*	Matches file names containing a dot
//			*.{java,class}	Matches file names ending with .java or .class
//			foo.?	Matches file names starting with foo. and a single character extension
//			/home/*/*	Matches /home/gus/data on UNIX platforms
//			/home/**	Matches /home/gus and /home/gus/data on UNIX platforms
//			C:\\*	Matches C:\foo and C:\bar on the Windows platform (note that the backslash is escaped; as a string literal in the Java Language the pattern would be "C:\\\\*")
//			The following rules are used to interpret glob patterns:
//
//			The * character matches zero or more characters of a name component without crossing directory boundaries.
//
//			The ** characters matches zero or more characters crossing directory boundaries.
//
//			The ? character matches exactly one character of a name component.
//
//			The backslash character (\) is used to escape characters that would otherwise be interpreted as special characters. The expression \\ matches a single backslash and "\{" matches a left brace for example.
//
//			The [ ] characters are a bracket expression that match a single character of a name component out of a set of characters. For example, [abc] matches "a", "b", or "c". The hyphen (-) may be used to specify a range so [a-z] specifies a range that matches from "a" to "z" (inclusive). These forms can be mixed so [abce-g] matches "a", "b", "c", "e", "f" or "g". If the character after the [ is a ! then it is used for negation so [!a-c] matches any character except "a", "b", or "c".
//
//			Within a bracket expression the *, ? and \ characters match themselves. The (-) character matches itself if it is the first character within the brackets, or the first character after the ! if negating.
//
//			The { } characters are a group of subpatterns, where the group matches if any subpattern in the group matches. The "," character is used to separate the subpatterns. Groups cannot be nested.
//
//			Leading period/dot characters in file name are treated as regular characters in match operations. For example, the "*" glob pattern matches file name ".login". The Files.isHidden(java.nio.file.Path) method may be used to test whether a file is considered hidden.
//
//			All other characters match themselves in an implementation dependent manner. This includes characters representing any name-separators.
//
//			The matching of root components is highly implementation-dependent and is not specified.			
		}
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		throw new UnsupportedOperationException("This file system doesn't support principal service");
	}

	@Override
	public WatchService newWatchService() throws IOException {
		throw new UnsupportedOperationException("This file system doesn't support watcher service");
	}
	
	@FunctionalInterface
	private interface Test {
		boolean test(URI uri);
	}

	enum LexType {
		AsIs, WildCardAsterisk, WildCardDoubleAsterisk, WildCardExactly, PresenceSet, AbsenceSet, PathSeparator, RootSeparator, OrStart, Div, OrEnd, EOF 
	}
	
	static class Lexema {
		final LexType	type;
		final Object	association;
		
		Lexema(final LexType type) {
			this(type,null);
		}

		Lexema(final LexType type, final Object association) {
			this.type = type;
			this.association = association;
		}
	}
	
	static Test compilePattern(final char[] source) {
		// TODO Auto-generated method stub
		final SyntaxNode	root = new SyntaxNode<>(null);
		final int			term = parseSyntax(parseLexemas(source),0,root);
		
		return new Test() {
			@Override
			public boolean test(final URI uri) {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}
	
	static Lexema[] parseLexemas(final char[] source) {
		final Lexema[]	result = new Lexema[source.length];
		final char[]	subseq = new char[source.length];
		int				from = 0, target = 0, subseqIndex = 0;
		boolean			insideGroup = false;
		
loop:	for (;;) {
			switch (source[from]) {
				case PATTERN_TERMINAL :
					if (subseqIndex > 0) {
						result[target++] = new Lexema(LexType.AsIs,Arrays.copyOfRange(subseq,0,subseqIndex));
						subseqIndex = 0;
					}
					result[target++] = new Lexema(LexType.EOF);
					break loop;
				case '*' 	:
					if (subseqIndex > 0) {
						result[target++] = new Lexema(LexType.AsIs,Arrays.copyOfRange(subseq,0,subseqIndex));
						subseqIndex = 0;
					}
					if (source[from+1] == '*') {
						result[target++] = new Lexema(LexType.WildCardDoubleAsterisk);
						from++;
					}
					else {
						result[target++] = new Lexema(LexType.WildCardAsterisk);
					}
					break;
				case '?' 	:
					if (subseqIndex > 0) {
						result[target++] = new Lexema(LexType.AsIs,Arrays.copyOfRange(subseq,0,subseqIndex));
						subseqIndex = 0;
					}
					result[target++] = new Lexema(LexType.WildCardExactly);
					break;
				case ',' 	:
					if (insideGroup) {
						if (subseqIndex > 0) {
							result[target++] = new Lexema(LexType.AsIs,Arrays.copyOfRange(subseq,0,subseqIndex));
							subseqIndex = 0;
						}
						result[target++] = new Lexema(LexType.Div);
					}
					else {
						subseq[subseqIndex++] = source[from];
					}
					break;
				case '\\' 	:
					subseq[subseqIndex++] = source[++from];
					break;
				case '[' 	:
					if (subseqIndex > 0) {
						result[target++] = new Lexema(LexType.AsIs,Arrays.copyOfRange(subseq,0,subseqIndex));
						subseqIndex = 0;
					}
					
					final boolean	negation;
					
					if (source[from+1] == '!') {
						negation = true;
						from += 2;
					}
					else {
						negation = false;
						from++;
					}

					final ExtendedBitCharSet	cs = new ExtendedBitCharSet();
					
					while (source[from] != ']' && source[from] != PATTERN_TERMINAL) {
						if (source[from] == '\\') {
							from++;
						}
						if (source[++from] == '-' && source[from+1] != ']' && source[from+1] != PATTERN_TERMINAL) {
							cs.addRange(source[from-1],source[from+1]);
						}
						else {
							cs.add(source[from-1]);
						}
					}
					if (source[from] == ']') {
						from++;
						if (negation) {
							result[target++] = new Lexema(LexType.PresenceSet,cs);
						}
						else {
							result[target++] = new Lexema(LexType.AbsenceSet,cs);
						}
					}
					else {
						throw new IllegalArgumentException(); 
					}
					break;
				case '{' 	:
					if (subseqIndex > 0) {
						result[target++] = new Lexema(LexType.AsIs,Arrays.copyOfRange(subseq,0,subseqIndex));
						subseqIndex = 0;
					}
					result[target++] = new Lexema(LexType.OrStart);
					insideGroup = true;
					break;
				case '}' 	:
					if (subseqIndex > 0) {
						result[target++] = new Lexema(LexType.AsIs,Arrays.copyOfRange(subseq,0,subseqIndex));
						subseqIndex = 0;
					}
					result[target++] = new Lexema(LexType.OrEnd);
					insideGroup = false;
					break;
				case '/' 	:
					if (subseqIndex > 0) {
						result[target++] = new Lexema(LexType.AsIs,Arrays.copyOfRange(subseq,0,subseqIndex));
						subseqIndex = 0;
					}
					result[target++] = new Lexema(LexType.PathSeparator);
					break;
				case ':' 	:
					if (subseqIndex > 0) {
						result[target++] = new Lexema(LexType.AsIs,Arrays.copyOfRange(subseq,0,subseqIndex));
						subseqIndex = 0;
					}
					result[target++] = new Lexema(LexType.RootSeparator);
					break;
				default :
					subseq[subseqIndex++] = source[from];
					break;
			}
			from++;
		}
		return result;
	}
	
	static int parseSyntax(final Lexema[] source, final int from, final SyntaxNode root) {
		return 0;
	}
}
