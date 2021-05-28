//

#include <vcruntime_string.h>
unsigned char x, UBRR0, USCR0B, RXEN0, TXEN0, RXCIE0, URDIE0, USCR0C, UCSZ01, UPM01, UDR0, EICRA, ISC00, EIMSK, INT0, ADC, ADMUX, REFS1, REFS0, ADCSRA, ADEN, ADATE, ADIE, ADPS2, ADPS1, ADPS0, ADSC;
unsigned char PCICR, PCIE1, PCMSK1, PCINT8, PCINT9, TCCR1B, CS11, CS10, TIMSK1, TOIE1, SREG, ISC10;


//
//	Led manager
//

#define LED_PORT		x
#define LED_DDR			x
#define LED_RED_PIN		1
#define LED_GREEN_PIN	2
#define LED_BLUE_PIN	3

#define LED_RED		0
#define LED_GREEN	1
#define LED_BLUE	2

void led(unsigned char ledNo, bool state) {
	switch (ledNo) {
	case LED_RED: if (state) LED_PORT &= ~(1 << LED_RED_PIN); else LED_PORT |= (1 << LED_RED_PIN); break;
	case LED_GREEN: if (state) LED_PORT &= ~(1 << LED_GREEN_PIN); else LED_PORT |= (1 << LED_GREEN_PIN); break;
	case LED_BLUE: if (state) LED_PORT &= ~(1 << LED_BLUE_PIN); else LED_PORT |= (1 << LED_BLUE_PIN); break;
	}
}

char read(char* address) {
	return 0;
}



//
//	USART I/O
//

#define SW_PORT			x
#define SW_DDR			x
#define SW_CHANNEL_1	1
#define SW_CHANNEL_2	1

char			bufferIn[128];
unsigned char	bufferPosIn = 0;
char			bufferOut[128];
unsigned char	bufferPosOut = 0;
volatile bool	ready = false;

void enableRX(bool state) {
	if (state) {
		USCR0B |= (1 << RXCIE0);
	}
	else {
		USCR0B &= ~(1 << RXCIE0);
	}
}

void enableTX(bool state) {
	if (state) {
		USCR0B |= (1 << URDIE0);
	}
	else {
		USCR0B &= ~(1 << URDIE0);
	}
}

void isrRP() {
	char	symbol = UDR0;

	if (bufferPosIn == 0) {
		led(LED_BLUE, true);
	}

	bufferIn[bufferPosIn++] = symbol;
	if (bufferPosIn >= sizeof(bufferIn) / sizeof(bufferIn[0]) - 1 || symbol == '\n') {
		bufferIn[bufferPosIn] = '\n';
		bufferPosIn = 0;
		ready = true;
		enableRX(false);
		led(LED_BLUE, false);
	}
}

void isrWP() {
	char symbol = bufferOut[bufferPosOut];

	if (symbol != '\n') {
		UDR0 = bufferOut[++bufferPosOut];
	}
	else {
		bufferPosOut = 0;
		enableTX(false);
		enableRX(true);
	}
}

void writeChar(char symbol) {
	bufferOut[bufferPosOut++] = symbol;
	if (bufferPosOut >= sizeof(bufferOut) / sizeof(bufferOut[0]) - 1 || symbol == '\n') {
		bufferOut[bufferPosOut] = '\n';
		UDR0 = bufferOut[bufferPosOut = 0];
		enableTX(true);
	}
}

void switchUSART(unsigned char channel) {
	SW_PORT &= (1 << SW_CHANNEL_1) | (1 << SW_CHANNEL_2);
	switch (channel) {
		case 1	: SW_PORT |= (1 << SW_CHANNEL_1); break;
		case 2	: SW_PORT |= (1 << SW_CHANNEL_2); break;
	}
}

//
//	FT245 I/O
//

char			bufferMainIn[128];
unsigned char	total = 2, current = 0;
bool			enabled1 = false, enabled2 = false;
volatile bool	mainReady = false;

#define FT245_DATA_PORT1	x
#define FT245_DATA_PORT2	x
#define FT245_DATA_DDR1		x
#define FT245_DATA_DDR2		x
#define FT245_DATA_PIN1		x
#define FT245_DATA_PIN2		x
#define FT245_DATA_MASK1	x
#define FT245_DATA_MASK2	x
#define FT245_DATA_SHIFT	x

#define FT245_CTRL_PORT		x
#define FT245_CTRL_DDR		x
#define FT245_CTRL_WR		1
#define FT245_CTRL_RD		1

#define FT245_INT_PORT		x
#define FT245_INT_DDR		x
#define FT245_INT_PIN		x
#define FT245_INT_RX		1
#define FT245_INT_TX		1

void __delay_us(unsigned char) {

}

void writeFT245(char symbol) {
	while ((FT245_INT_PIN & FT245_INT_TX) == 0) {
	}
	FT245_DATA_DDR1 |= FT245_DATA_MASK1;
	FT245_DATA_DDR2 |= FT245_DATA_MASK2;

	FT245_CTRL_PORT &= ~(1 << FT245_CTRL_WR);

	FT245_DATA_PORT1 &= ~FT245_DATA_MASK1;
	FT245_DATA_PORT1 |= (symbol & FT245_DATA_MASK1);
	FT245_DATA_PORT2 &= ~FT245_DATA_MASK2;
	FT245_DATA_PORT2 |= ((symbol >> FT245_DATA_SHIFT) & FT245_DATA_MASK2);

	__delay_us(1);
	FT245_CTRL_PORT |= (1 << FT245_CTRL_WR);

	FT245_DATA_DDR1 &= ~FT245_DATA_MASK1;
	FT245_DATA_DDR2 &= ~FT245_DATA_MASK2;
}

char readFT245(void) {
	char	part1, part2;

	while ((FT245_INT_PIN & FT245_INT_TX) == 0) {
	}

	FT245_CTRL_PORT &= ~(1 << FT245_CTRL_RD);
	part1 = FT245_DATA_PIN1 & FT245_DATA_MASK1;
	part2 = (FT245_DATA_PIN2 & FT245_DATA_MASK2) << FT245_DATA_SHIFT;
	FT245_CTRL_PORT |= (1 << FT245_CTRL_RD);

	return part1 | part2;
}

void isrFT245RX() {
	mainReady = true;
}

void readFT245String() {
	unsigned char	index;
	char			symbol = 0;

	for (index = 0; index < sizeof(bufferMainIn) / sizeof(bufferMainIn[0]) - 1 && symbol != '\n'; index++) {
		bufferMainIn[index] = symbol = readFT245();
	}
	bufferMainIn[index] = '\n';
}

//
//	Answering procedures
//


void printP(char* content) {
	char symbol;

	while ((symbol = read(content++)) != 0) {
		writeChar(symbol);
	}
}

void printFT245P(char* content) {
	char symbol;

	while ((symbol = read(content++)) != 0) {
		writeFT245(symbol);
	}
}

void print(char* content) {
	char symbol;

	while ((symbol = *content++) != 0) {
		writeChar(symbol);
	}
}

void printFT245(char* content) {
	char symbol;

	while ((symbol = *content++) != 0) {
		writeFT245(symbol);
	}
}

typedef void (*printSomewhere)(char* content);

unsigned short	steps[] = { 10000, 1000, 100, 10, 1 };
char	zero[] = "0";

void printValueSomeWhere(unsigned short val, printSomewhere somePrint) {
	if (val == 0) {
		printP(zero);
	}
	else {
		char value[] = { 0,0,0,0,0,0 };
		unsigned char index = 0, to = 0;

		for (index = 0; index < sizeof(steps) / sizeof(steps[0]); index++) {
			while (val >= steps[index]) {
				value[to]++;
				val -= steps[index];
			}
			if (value[to] > 0) {
				to++;
			}
		}
		for (index = 0; index < to; index++) {
			value[index] += '0';
		}
		(*somePrint)(value);
	}
}

void printValue(unsigned short val) {
	printValueSomeWhere(val, print);
}

void printValueFT245(unsigned short val) {
	printValueSomeWhere(val, printFT245);
}

//
//	Boot loader
//

void boot(void) {

}

//
//	Command parser
//

static char	ERR_ILLEGAL_PREFIX[] = "Err: illegal prefix\n";
static char	ERR_UNKNOWN_COMMAND[] = "Err: unknown command\n";
static char	ERR_UNPARSED_TAIL[] = "Err: unparsed tail\n";
static char	ERR_NUMBER_MISSING[] = "Err: number missing\n";
static char	ERR_NUMBER_TOO_BIG[] = "Err: number too big\n";

static char	ANS_VER[] = "ver\n";
static char	ANS_OK[] = "OK\n";
static char	ANS_TOTAL[] = "total=";
static char	ANS_CURRENT[] = ",current=";
static char	ANS_DIV[] = ",";
static char	ANS_EQU[] = "=";
static char	ANS_ON[] = "on";
static char	ANS_OFF[] = "off";
static char	ANS_NL[] = "\n";

static char	CMD_VER[] = "ver";
static char	CMD_STATE[] = "state";
static char	CMD_SEL[] = "sel";
static char	CMD_REFRESH[] = "refresh";
static char	CMD_BOOT[] = "boot";

static char	PING_VER[] = "::ver\n";


char* skipSpace(char* source) {
	while (*source <= ' ' && *source != '\n') {
		source++;
	}
	return source;
}

char* readNumber(char* source, unsigned short* val) {
	unsigned short	temp = 0;

	while (*source >= '0' && *source <= '9') {
		temp = 10 * temp + *source++ - '0';
	}
	*val = temp;
	return source;
}

bool compareP(char* command, char* templ) {
	char symbol;

	while ((symbol = read(templ++)) != 0) {
		if (*command++ != symbol) {
			return false;
		}
	}
	return true;
}


// ::ver -> .....
// ::state ->total=zzz,current=z,Z={on|off}...
// ::sel {0|nnn}
// ::refresh [zzz]
// ::boot

volatile bool timerReady;

void isrTOVF() {
	timerReady = true;
}


void delayOrAnswer(void) {
	timerReady = false;
	TCCR1B = (1 << CS11) | (1 << CS10);	
	do {
		sleep();
	} while (!ready && !timerReady);
	TCCR1B = 0;
	timerReady = false;
}

void parseCommand(char* command) {
	if (*command++ == ':' && *command++ == ':') {
		if (current != 0) {
			if (compareP(command, CMD_SEL)) {
				command = skipSpace(command + sizeof(CMD_VER));
				if (*command >= '0' && *command <= '0') {
					unsigned short	val;

					command = skipSpace(readNumber(command, &val));
					if (*command == '\n') {
						if (current > total) {
							printP(ERR_NUMBER_TOO_BIG);
						}
						else {
							switchUSART(current = val);
							printP(ANS_OK);
						}
					}
					else {
						printP(ERR_UNPARSED_TAIL);
					}
				}
				else {
					printP(ERR_NUMBER_MISSING);
				}
			}
		}
		else {
			switch (*command) {
				case 'v':
					if (compareP(command, CMD_VER)) {
						command = skipSpace(command + sizeof(CMD_VER));
						if (*command == '\n') {
							printP(ANS_VER);
						}
						else {
							printP(ERR_UNPARSED_TAIL);
						}
						return;
					}
					else {
						break;
					}
				case 's':
					if (compareP(command, CMD_STATE)) {
						command = skipSpace(command + sizeof(CMD_STATE));
						if (*command == '\n') {
							printP(ANS_TOTAL);
							printValue(total);
							printP(ANS_CURRENT);
							printValue(current);
							printP(ANS_DIV);
							printValue(1);
							printP(ANS_EQU);
							printP(enabled1 ? ANS_ON : ANS_OFF);
							printP(ANS_DIV);
							printValue(2);
							printP(ANS_EQU);
							printP(enabled2 ? ANS_ON : ANS_OFF);
							printP(ANS_NL);
						}
						else {
							printP(ERR_UNPARSED_TAIL);
						}
						return;
					}
					else if (compareP(command, CMD_SEL)) {
						command = skipSpace(command + sizeof(CMD_VER));
						if (*command >= '0' && *command <= '9') {
							unsigned short	val;

							command = skipSpace(readNumber(command, &val));
							if (*command == '\n') {
								if (current > total) {
									printP(ERR_NUMBER_TOO_BIG);
								}
								else {
									switchUSART(current = val);
									printP(ANS_OK);
								}
							}
							else {
								printP(ERR_UNPARSED_TAIL);
							}
						}
						else {
							printP(ERR_NUMBER_MISSING);
						}
					}
					else {
						break;
					}
				case 'r':
					if (compareP(command, CMD_REFRESH)) {
						command = skipSpace(command + sizeof(CMD_REFRESH));
						if (*command == '\n') {
							switchUSART(1);
							printP(PING_VER);
							delayOrAnswer();
							enabled1 = ready;
							switchUSART(2);
							printP(PING_VER);
							delayOrAnswer();
							enabled2 = ready;
							switchUSART(current);
							printP(ANS_OK);
						}
						else {
							printP(ERR_UNPARSED_TAIL);
						}
						return;
					}
				case 'b':
					if (compareP(command, CMD_BOOT)) {
						command = skipSpace(command + sizeof(CMD_BOOT));
						if (*command == '\n') {
							boot();
						}
						else {
							printP(ERR_UNPARSED_TAIL);
						}
						return;
					}
					else {
						break;
					}
			}
		}
		printP(ERR_UNKNOWN_COMMAND);
	}
	else {
		printP(ERR_ILLEGAL_PREFIX);
	}
}

void parseAnswer(char* command) {
	if (current != 0) {
		printFT245(command);
	}
}

//
//	Initialization and main
//

void init(void) {
	LED_PORT |= (1 << LED_RED_PIN) | (1 << LED_GREEN_PIN) | (1 << LED_BLUE_PIN);	// Prepare LEDs
	LED_DDR |= (1 << LED_RED_PIN) | (1 << LED_GREEN_PIN) | (1 << LED_BLUE_PIN);

	SW_PORT &= ~((1 << SW_CHANNEL_1) | (1 << SW_CHANNEL_2));	// Prepare channel switch
	SW_DDR |= (1 << SW_CHANNEL_1) | (1 << SW_CHANNEL_2);

	FT245_CTRL_PORT |= (1 << FT245_CTRL_WR) | (1 << FT245_CTRL_RD);	// Prepare FT245
	FT245_CTRL_DDR |= (1 << FT245_CTRL_WR) | (1 << FT245_CTRL_RD);
	FT245_INT_PORT |= (1 << FT245_INT_RX) | (1 << FT245_INT_TX);
	EICRA |= (1 << ISC10) | (1 << ISC00);

	TIMSK1 = (1 << TOIE1);	// Prepare delay timer

	UBRR0 = 12;	// Prepare USART0 for 8 Mhz (38400, 7, PE, 1-s)
	USCR0B = (1 << RXEN0) | (1 << TXEN0);
	USCR0C = (1 << UCSZ01) | (1 << UPM01);
	enableRX(true);
}

void prepareSleepMode(void) {

}

void ei(void) {

}

void sleep(void) {
}

int main(int argc, char** argv) {
	init();

	prepareSleepMode();
	ei();
	for (;;) {
		led(LED_GREEN, true);
		sleep();
		led(LED_GREEN, false);
		if (mainReady) {
			readFT245String();
			parseCommand(bufferMainIn);
			mainReady = false;
		}
	}

	return 0;
}