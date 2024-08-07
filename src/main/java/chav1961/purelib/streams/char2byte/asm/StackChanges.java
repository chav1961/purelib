package chav1961.purelib.streams.char2byte.asm;

enum StackChanges {
	none,
	clear,
	swap,	
	
	pop,
	pop2,
	pop3,
	pop4,
	pushInt,
	pushLong,
	pushDouble,
	pushFloat,
	pushReference,
	pushNull,
	
	dup,
	dup_x1,
	dup_x2,
	dup2,
	dup2_x1,
	dup2_x2,
	
	changeDouble2Float,
	changeDouble2Int,
	changeDouble2Long,
	changeFloat2Double,
	changeFloat2Int,
	changeFloat2Long,
	changeLong2Double,
	changeLong2Float,
	changeLong2Int,
	changeInt2Double,
	changeInt2Float,
	changeInt2Long,
	
	popAndPushFloat,
	popAndPushInt,
	popAndPushReference,
	pop2AndPushInt,
	pop2AndPushDouble,
	pop2AndPushReference,
	pop2AndPushFloat,
	pop2AndPushLong,
	pop4AndPushDouble,
	pop4AndPushInt,
	pop4AndPushLong,
	
	pushField,
	pushStatic,
	popField,
	popStatic,
	
	callStaticAndPush,
	callAndPush,
	multiarrayAndPushReference,
	
	changeType
}