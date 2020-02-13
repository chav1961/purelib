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
						if (more.length == 1 && getSeparator().equals(more[0])) {
							return new PureLibPath(this,template.getScheme(),getSeparator());
						}
						else {
							final StringBuilder	sb = new StringBuilder();
							char				last = 0;
							
							for (String component : more) {
								if (!component.isEmpty()) {
									if (component.charAt(0) == '/' && last == '/') {
										sb.append(component,1,component.length());
									}
									else if (component.charAt(0) != '/' && last != '/') {
										sb.append('/').append(component);
									}
									else {
										sb.append(component);
									}
									last = component.charAt(component.length()-1);
								}
							}
							return new PureLibPath(this,template.getScheme(),sb.substring(1));
						}
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
	
	
	static Test compilePattern(final char[] source) {
		final LexType[]		lexType = new LexType[source.length];  
		final Object[]		assoc = new Object[source.length];  
		
		parseLexemas(source,lexType,assoc);
		
		return new Test() {
			@Override
			public boolean test(final URI uri) {
				final String	content = uri.toString();
				
				if (lexType[0] != LexType.PathSeparator) {
					final int	fileNameIndex = content.lastIndexOf(PureLibFileSystemProvider.PATH_SPLITTER);
					
					return checkContent(CharUtils.terminateAndConvert2CharArray(fileNameIndex > 0 ? content.substring(fileNameIndex+1) : content,PATTERN_TERMINAL),lexType,assoc,0,0);
				}
				else {
					return checkContent(CharUtils.terminateAndConvert2CharArray(content,PATTERN_TERMINAL),lexType,assoc,0,0);
				}
			}
		};
	}
	
	static int parseLexemas(final char[] source, final LexType[] lexType, final Object[] assoc) {
		final char[]	subseq = new char[source.length];
		int				from = 0, target = 0, subseqIndex = 0;
		boolean			insideGroup = false;
		
loop:	for (;;) {
			switch (source[from]) {
				case PATTERN_TERMINAL :
					if (subseqIndex > 0) {
						lexType[target] = LexType.AsIs;
						assoc[target] = Arrays.copyOfRange(subseq,0,subseqIndex);
						target++;
						subseqIndex = 0;
					}
					lexType[target++] = LexType.EOF;
					break loop;
				case '*' 	:
					if (subseqIndex > 0) {
						lexType[target] = LexType.AsIs;
						assoc[target] = Arrays.copyOfRange(subseq,0,subseqIndex);
						target++;
						subseqIndex = 0;
					}
					if (source[from+1] == '*') {
						lexType[target++] = LexType.WildCardDoubleAsterisk;
						from++;
					}
					else {
						lexType[target++] = LexType.WildCardAsterisk;
					}
					break;
				case '?' 	:
					if (subseqIndex > 0) {
						lexType[target] = LexType.AsIs;
						assoc[target] = Arrays.copyOfRange(subseq,0,subseqIndex);
						target++;
						subseqIndex = 0;
					}
					lexType[target++] = LexType.WildCardExactly;
					break;
				case ',' 	:
					if (insideGroup) {
						if (subseqIndex > 0) {
							lexType[target] = LexType.AsIs;
							assoc[target] = Arrays.copyOfRange(subseq,0,subseqIndex);
							target++;
							subseqIndex = 0;
						}
						lexType[target++] = LexType.Div;
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
						lexType[target] = LexType.AsIs;
						assoc[target] = Arrays.copyOfRange(subseq,0,subseqIndex);
						target++;
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
							lexType[target++] = LexType.PresenceSet;
							assoc[target] = cs;
						}
						else {
							lexType[target++] = LexType.AbsenceSet;
							assoc[target] = cs;
						}
						target++;
					}
					else {
						throw new IllegalArgumentException(); 
					}
					break;
				case '{' 	:
					if (subseqIndex > 0) {
						lexType[target] = LexType.AsIs;
						assoc[target] = Arrays.copyOfRange(subseq,0,subseqIndex);
						target++;
						subseqIndex = 0;
					}
					lexType[target++] = LexType.OrStart;
					insideGroup = true;
					break;
				case '}' 	:
					if (subseqIndex > 0) {
						lexType[target] = LexType.AsIs;
						assoc[target] = Arrays.copyOfRange(subseq,0,subseqIndex);
						target++;
						subseqIndex = 0;
					}
					lexType[target++] = LexType.OrEnd;
					insideGroup = false;
					break;
				case '/' 	:
					if (subseqIndex > 0) {
						lexType[target] = LexType.AsIs;
						assoc[target] = Arrays.copyOfRange(subseq,0,subseqIndex);
						target++;
						subseqIndex = 0;
					}
					lexType[target++] = LexType.PathSeparator;
					break;
				case ':' 	:
					if (subseqIndex > 0) {
						lexType[target] = LexType.AsIs;
						assoc[target] = Arrays.copyOfRange(subseq,0,subseqIndex);
						target++;
						subseqIndex = 0;
					}
					lexType[target++] = LexType.RootSeparator;
					break;
				default :
					subseq[subseqIndex++] = source[from];
					break;
			}
			from++;
		}
		return target;
	}
	
	static boolean checkContent(final char[] source, final LexType[] lexType, final Object[] assoc, final int sourcePos, final int checkPos) {
		switch (lexType[checkPos]) {
			case AbsenceSet					:
				return !((ExtendedBitCharSet)assoc[checkPos]).contains(source[sourcePos]) && checkContent(source,lexType,assoc,sourcePos + 1,checkPos + 1);
			case AsIs						:
				return CharUtils.compare(source,sourcePos,(char[])assoc[checkPos]) && checkContent(source,lexType,assoc,sourcePos + ((char[])assoc[checkPos]).length, checkPos + 1);
			case EOF						:
				return true;
			case OrStart					:
				int	endOr = checkPos, groupCount = 1;
				
				while (lexType[endOr] != LexType.OrEnd && lexType[endOr] != LexType.EOF) {
					if (lexType[endOr] == LexType.Div) {
						groupCount++;
					}				
					endOr++;
				}
				for (int index = 0; index < groupCount; index++) {
					if (CharUtils.compare(source,sourcePos,(char[])assoc[checkPos + 2 * index + 1]) && checkContent(source,lexType,assoc,sourcePos + ((char[])assoc[checkPos + 2 * index + 1]).length, endOr + 1)) {
						return true;
					}
				}
				return false;
			case PathSeparator				:
				return source[sourcePos] == '/' && checkContent(source,lexType,assoc,sourcePos + 1,checkPos + 1); 
			case PresenceSet				:
				return ((ExtendedBitCharSet)assoc[checkPos]).contains(source[sourcePos]) && checkContent(source,lexType,assoc,sourcePos + 1,checkPos + 1);
			case RootSeparator				:
				return source[sourcePos] == ':' && checkContent(source,lexType,assoc,sourcePos + 1,checkPos + 1); 
			case WildCardAsterisk			:
				int index = sourcePos;
				
				while (source[index] != PATTERN_TERMINAL && source[index] != '/') {
					index++;
				}
				return checkContent(source,lexType,assoc,index,checkPos + 1); 
			case WildCardDoubleAsterisk		:
				int wcIndex = sourcePos;
				
				while (source[wcIndex] != PATTERN_TERMINAL) {
					if (checkContent(source,lexType,assoc,wcIndex,checkPos + 1)) {
						return true;
					}
					wcIndex++;
				}
				return false;
			case WildCardExactly			:
				return source[sourcePos] != PATTERN_TERMINAL && checkContent(source,lexType,assoc,sourcePos + 1,checkPos + 1); 
			default:
				throw new UnsupportedOperationException("Lexema type ["+lexType[checkPos]+"] is not supported yet"); 
		}
	}
}
