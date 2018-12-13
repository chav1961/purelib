# 0.0.2 Release notes

## New features

The most important feature is support of [macros](https://en.wikipedia.org/wiki/Macro_(computer_science)) in the AsmWriter class now. Assembler with macro support will be named **JMA** (Java macro assembler) now. Detailed description of the macro language for JMA see Java Macro Assembler User Guide.

JMA is now implicitly supports the **wide** byte code command and optimizes using of the specialized byte code commands instead of general-purpose commands. New method clone(...) was added to the AsmWrier class to support using preloaded class and macros inside it.

T� support access to assembler by the standard Java [script engine mechanism](https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/programmer_guide/) , a set of new classes was developed:

* AbstractScriptEngine and AbstractScriptEngineFactory to implement common functionality for script engine mechanism
* AsmScriptEngine and AsmScriptEngineFactory to get access to JMA abilities
To effectively support [JSON](https://en.wikipedia.org/wiki/JSON) format, a set of new classes was developed:
* JsonStaxParser and JsonStaxPrinter to support JSON parsing/printing. It's functionality is similar to [XML StAX](https://en.wikipedia.org/wiki/StAX) interface. Both of them are designed for high-speed stream parsing and printing.
* JsonSerializer factory to build JSON serializer/deserializer for referenced type. To increase performance, this factory dynamically builds byte code for serialization/deserialization methods

To support [Creole markup language](https://en.wikipedia.org/wiki/Creole_(markup)) , a new CreoleWriter stream was developed. It supports converting Creole stream to the HTML, XML and PDF format (using [XML-FO](https://en.wikipedia.org/wiki/XSL_Formatting_Objects)).

To support simple [preprocessing](https://en.wikipedia.org/wiki/Preprocessor) on the input streams, a new stream PreprocessingReader was developed

New class InOutGrowableByteArray was developed in the growablearrays package to support [data output](https://docs.oracle.com/javase/8/docs/api/java/io/DataOutput.html) functionality on the GrowableByteArray

New class PureLibSettings was created to collect and keep all settings of the Pure Library. Most of the classes in the Pure Library are refactored to use it.

All the xsd schemas used in the Pure Library are localized in the xsd package now. A new method was added to basic.Utils to get access to the xsd schemas

A set of useful classes was developed:

* FSM and StackedFSM to support [finite state machine](https://en.wikipedia.org/wiki/Finite-state_machine) functionality inside the application
* TemporaryStore to support in-memory input/output streams, which can be automatically expanded to the disk
* ReusableInstances cache of referenced instances to reduce memory allocations in the high-performance applications
* ZLibInputStream and ZLibOutputStream to support [ZLib-compressed](https://en.wikipedia.org/wiki/Zlib) input and output
- high speed BufferedInputStreamReader and BufferedOutputStreamWriter class
- GettersAndSettersFactory class. It's functionality is similar to MethodHandle class, but it dynamically builds byte code to get access to public fields
- SequenceIterator iterator class on the list of other iterators
- ReaderWrapper and WriterWrapper facade streams to support extending reader/writer functionality for streams not owned by the application

## Refactoring

- support of the Creole format is included into the i18n package (Localizer class) now.
- new method setLength(NNN) was added to all growable arrays
- GrowableByteArray and GrowableCharArray functionality was extended to support input/output and effective string operations

## Bug fix

- bugs were fixed in the memory manipulation algorithm of the growable arrays
- bugs were fixed in the code generation of the AsmWriter

1. new

The most important feature is support of macros (https://en.wikipedia.org/wiki/Macro_(computer_science)) in the AsmWriter class now. Assembler with macro support will be named JMA (Java macro assembler) now. Detailed description of the macro language for JMA see Java Macro Assembler User Guide.
JMA is now implicitly supports the 'wide' byte code command and optimizes using of the specialized byte code commands instead of general-purpose commands. New method clone(...) was added to the AsmWrier class to support using preloaded class and macros inside it.
T� support access to assembler by the standard Java script engine mechanism (https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/programmer_guide/) , a set of new classes was developed:
- AbstractScriptEngine and AbstractScriptEngineFactory to implement common functionality for script engine mechanism
- AsmScriptEngine and AsmScriptEngineFactory to get access to JMA abilities
To effectively support JSON (https://en.wikipedia.org/wiki/JSON) format, a set of new classes was developed:
- JsonStaxParser and JsonStaxPrinter to support JSON parsing/printing. It's functionality is similar to XML StAX (https://en.wikipedia.org/wiki/StAX) interface. Both of them are designed for high-speed stream parsing and printing.
- JsonSerializer factory to build JSON serializer/deserializer for referenced type. To increase performance, this factory dynamically builds byte code for serialization/deserialization methods
To support Creole (https://en.wikipedia.org/wiki/Creole_(markup)) markup language, a new CreoleWriter stream was developed. It supports converting Creole stream to the HTML, XML and PDF format (using XML-FO (https://en.wikipedia.org/wiki/XSL_Formatting_Objects)).
To support simple preprocessing (https://en.wikipedia.org/wiki/Preprocessor) on the input streams, a new stream PreprocessingReader was developed
New class InOutGrowableByteArray was developed in the growablearrays package to support data output (https://docs.oracle.com/javase/8/docs/api/java/io/DataOutput.html) functionality on the GrowableByteArray
New class PureLibSettings was created to collect and keep all settings of the Pure Library. Most of the classes in the Pure Library are refactored to use it.
All the xsd schemas used in the Pure Library are localized in the xsd package now. A new method was added to basic.Utils to get access to the xsd schemas
A set of useful classes was developed:
- FSM and StackedFSM to support finite state machine (https://en.wikipedia.org/wiki/Finite-state_machine) functionality inside the application
- TemporaryStore to support in-memory input/output streams, which can be automatically expanded to the disk
- ReusableInstances cache of referenced instances to reduce memory allocations in the high-performance applications
- ZLibInputStream and ZLibOutputStream to support ZLib-compressed (https://en.wikipedia.org/wiki/Zlib) input and output
- high speed BufferedInputStreamReader and BufferedOutputStreamWriter class
- GettersAndSettersFactory class. It's functionality is similar to MethodHandle class, but it dynamically builds byte code to get access to public fields
- SequenceIterator iterator class on the list of other iterators
- ReaderWrapper and WriterWrapper facade streams to support extending reader/writer functionality for streams not owned by the application

2. refactoring

- support of the Creole format is included into the i18n package (Localizer class) now.
- new method setLength(NNN) was added to all growable arrays
- GrowableByteArray and GrowableCharArray functionality was extended to support input/output and effective string operations

3. fix

- bugs were fixed in the memory manipulation algorithm of the growable arrays
- bugs were fixed in the code generation of the AsmWriter