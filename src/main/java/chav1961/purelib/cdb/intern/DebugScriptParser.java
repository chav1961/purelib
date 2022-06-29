package chav1961.purelib.cdb.intern;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

/*
attach address
detach

start
stop
cont

print
dump
threads cond
thread
where
stop at
stop in
clear
clear all
step next
step into
step out
catch
ignore


run
await condition,...//timeout(20), breakpoint(...), exception(), termination  

source ...
import ...

if 
elsif
else
fi

do
od

for
rof

case @cond
of ...,....:
esac

continue
break
goto
label:

grep 
awk
exec 
echo

exit

str
int
bool
<>| file, string

try
catch
finally
yrt

read

*/


public class DebugScriptParser {
	private static final SyntaxTreeInterface<Object>	KEYWORDS = new AndOrTree<Object>();
	
	public static enum Keyword {
		ATTACH, DETACH,
		START, SUSPEND, RESUME,
		PRINT, DUMP, THREADS, THREAD, WHERE,
		STOP, AT, IN, CLEAR, ALL,
		STEP, NEXT, INTO, OUT,
		CATCH, IGNORE,
		RUN, AWAIT,
		SOURCE, IMPORT,
		IF, ELSIF, ELSE, FI,
		DO, WHILE, OD,
		FOR, ROF,
		CASE, OF, ESAC,
		CONTINUE, BREAK, GOTO,
		GREP, AWK, EXEC, ECHO, EXIT,
		STR, INT, BOOL,
		TRY, FINALLY, YRT,
		READ
	}
	
	static {
		placeKeyword("",null);
	}
	
	private static final void placeKeyword(final String keyword, final Object content) {
		KEYWORDS.placeName("attach", content);
	}
}
