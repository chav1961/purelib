//

#include <vcruntime_string.h>
unsigned char x, UBRR0, USCR0B, RXEN0, TXEN0, RXCIE0, URDIE0, USCR0C, UCSZ01, UPM01, UDR0, EICRA, ISC00, EIMSK, INT0, ADC, ADMUX, REFS1, REFS0, ADCSRA, ADEN, ADATE, ADIE, ADPS2, ADPS1, ADPS0, ADSC;
unsigned char PCICR, PCIE1, PCMSK1, PCINT8, PCINT9, TCCR1B, CS11, CS10, TIMSK1, TOIE1, SREG;


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
		case LED_RED	: if (state) LED_PORT &= ~(1 << LED_RED_PIN); else LED_PORT |= (1 << LED_RED_PIN); break;			
		case LED_GREEN	: if (state) LED_PORT &= ~(1 << LED_GREEN_PIN); else LED_PORT |= (1 << LED_GREEN_PIN); break;
		case LED_BLUE	: if (state) LED_PORT &= ~(1 << LED_BLUE_PIN); else LED_PORT |= (1 << LED_BLUE_PIN); break;
	}
}

//
//	USART I/O
//

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
	if (bufferPosIn >= sizeof(bufferIn)/ sizeof(bufferIn[0]) - 1 || symbol == '\n') {
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

//
//	1-Wire I/O
//

#define	TEMP_SENS_PORT	x
#define	TEMP_SENS_DDR	x
#define	TEMP_SENS_PIN	x
#define	TEMP_SENS_PIN_LINE1	0
#define	TEMP_SENS_PIN_LINE2	1

#define OFFSET_CYCLES	13
#define CPU_FREQ		8
#define DELAY_A			((6   * CPU_FREQ) - OFFSET_CYCLES)
#define DELAY_B			((64  * CPU_FREQ) - OFFSET_CYCLES)
#define DELAY_C			((60  * CPU_FREQ) - OFFSET_CYCLES)
#define DELAY_D			((10  * CPU_FREQ) - OFFSET_CYCLES)
#define DELAY_E			((9   * CPU_FREQ) - OFFSET_CYCLES)
#define DELAY_F			((55  * CPU_FREQ) - OFFSET_CYCLES)
#define DELAY_H			((480 * CPU_FREQ) - OFFSET_CYCLES)
#define DELAY_I			((70  * CPU_FREQ) - OFFSET_CYCLES)
#define DELAY_J			((410 * CPU_FREQ) - OFFSET_CYCLES)

#define CMD_SKIP_ROM		0xCC
#define CMD_CONVERT			0x44
#define CMD_READ_SCRATCHPAD	0xBE



void di(void) {

}

void ei(void) {

}

void __delay_cycles(unsigned short value) {

}

void pullBus(unsigned char mask) {
	TEMP_SENS_DDR |= mask;
	TEMP_SENS_PORT &= ~mask;
}

void releaseBus(unsigned char mask) {
	TEMP_SENS_DDR &= ~mask;
	TEMP_SENS_PORT |= mask;
}

void W1Init(unsigned char pins) {
	releaseBus(pins);
	__delay_cycles(DELAY_H);
}


void W1WriteBit1(unsigned char pins)  {
	unsigned char intState = SREG;

	di();
	pullBus(pins);			__delay_cycles(DELAY_A);
	releaseBus(pins);		__delay_cycles(DELAY_B);
	SREG = intState;
}


void W1WriteBit0(unsigned char pins) {
	unsigned char intState = SREG;

	di();
	pullBus(pins);			__delay_cycles(DELAY_C);
	releaseBus(pins);		__delay_cycles(DELAY_D);
	SREG = intState;
}


unsigned char W1ReadBit(unsigned char pins) {
	unsigned char intState = SREG;
	unsigned char intState;
	unsigned char bitsRead;

	di();
	pullBus(pins);			__delay_cycles(DELAY_A);
	releaseBus(pins);		__delay_cycles(DELAY_E);

	bitsRead = TEMP_SENS_PIN & pins;
	__delay_cycles(DELAY_F);

	SREG = intState;
	return bitsRead;
}

unsigned char W1DetectPresence(unsigned char pins) {
	unsigned char intState = SREG;
	unsigned char intState;
	unsigned char presenceDetected;

	di();
	pullBus(pins);			__delay_cycles(DELAY_H);

	releaseBus(pins);		__delay_cycles(DELAY_I);

	presenceDetected = ((~TEMP_SENS_PIN) & pins);
	__delay_cycles(DELAY_J);

	SREG = intState;
	return presenceDetected;
}


void sendByte(unsigned char data, unsigned char pin) {
	for (unsigned char index = 0; index < 8; index++, data >>= 1) {
		if (data & 0x01) {
			W1WriteBit1(pin);
		}
		else {
			W1WriteBit0(pin);
		}
	}
}


unsigned char receiveByte(unsigned char pin) {
	unsigned char data = 0;

	for (unsigned char index = 0; index < 8; index++) {
		data >>= 1;
		if (W1ReadBit(pin)) {
			data |= 0x80;
		}
	}
	return data;
}


//
//	Answering procedures
//

static char	ERR_ILLEGAL_PREFIX[] = "Err: illegal command prefix\n";
static char	ERR_UNKNOWN_COMMAND[] = "Err: unknown command\n";
static char	ERR_UNPARSED_TAIL[] = "Err: unparsed tail\n";
static char	ERR_UNKNOWN_OPTION[] = "Err: unknown option\n";
static char	ERR_NUMBER_MISSING[] = "Err: number missing\n";
static char	ANS_OK[] = "OK\n";
static char	ANS_VER[] = "ver\n";
static char	ANS_T1[] = "t1=";
static char	ANS_T2[] = ", t2=";
static char	ANS_LIGHT[] = ", light=";
static char	ANS_SENSE[] = ", sens=";
static char	ANS_NL[] = "\n";

static char	CMD_VER[] = "ver";
static char	CMD_STATE[] = "state";
static char	CMD_LIGHT[] = "light";
static char	CMD_SENS[] = "sens";
static char	CMD_BOOT[] = "boot";

static char	OPT_ON[] = "on";
static char	OPT_OFF[] = "off";
static char	OPT_SPY[] = "spy";

char read(char* address) {
	return 0;
}

void printP(char *content) {
	char symbol;

	while ((symbol = read(content++)) != 0) {
		writeChar(symbol);
	}
}

void print(char* content) {
	char symbol;

	while ((symbol = *content++) != 0) {
		writeChar(symbol);
	}
}

unsigned short	steps[] = { 10000, 1000, 100, 10, 1 };
char	zero[] = "0";

void printValue(unsigned short val) {
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
		print(value);
	}
}

//
//	Control procedures
//

unsigned short	lightSamples[16], t1Samples[16], t2Samples[16];
unsigned char	lightSamplesPos = 0, t1SamplesPos = 0, t2SamplesPos = 0;

volatile unsigned short	t1 = 0, t2 = 0, sense = 0, currentSense = 0;
volatile bool	lightState = false, enableLight, receiverChanged = false, tick = false;
volatile unsigned char	lightMode;

#define PANEL_PORT		x
#define PANEL_DDR		x
#define PANEL_IND_1_PIN	0
#define PANEL_IND_2_PIN	1
#define PANEL_IND_3_PIN	2

#define PANEL_IND_1	0
#define PANEL_IND_2	1
#define PANEL_LIGHT	2

#define LIGHT_ON 0
#define LIGHT_OFF 1
#define LIGHT_SPY 2

void setPanelIndicator(unsigned char ind, bool state) {
	switch (ind) {
		case PANEL_IND_1	: 
			if (state) PANEL_PORT |= (1 << PANEL_IND_1_PIN); else PANEL_PORT &= ~(1 << PANEL_IND_1_PIN); 
			break;
		case PANEL_IND_2	: 
			if (state) PANEL_PORT |= (1 << PANEL_IND_2_PIN); else PANEL_PORT &= ~(1 << PANEL_IND_2_PIN); 
			break;
		case PANEL_LIGHT	: 
			if (state) PANEL_PORT |= (1 << PANEL_IND_3_PIN); else PANEL_PORT &= ~(1 << PANEL_IND_3_PIN); 
			lightState = state;
			break;
	}
}

#define MOTION_SENSOR_PORT		x
#define MOTION_SENSOR_DDR		x
#define MOTION_SENSOR_PIN		x
#define MOTION_SENSOR_LINE_PIN	1

void isrDL() {
	if (lightMode == LIGHT_SPY && enableLight) {
		setPanelIndicator(PANEL_LIGHT, (MOTION_SENSOR_PIN & (1 << MOTION_SENSOR_LINE_PIN)) == 0);
	}
}

unsigned short calcSamples(unsigned short *values, unsigned char size) {
	unsigned short result = 0;

	for (unsigned char index = 0; index < size; index++) {
		result += *values++;
	}
	return result;
}

void isrTmr() {
	tick = true;
}

void isrADC() {
	unsigned char	index;

	lightSamples[lightSamplesPos++] = ADC;

	if (lightSamplesPos >= sizeof(lightSamples)/ sizeof(lightSamples[0])) {
		lightSamplesPos = 0;
	}

	unsigned short	val = calcSamples(lightSamples, sizeof(lightSamples) / sizeof(lightSamples[0]));
	
	if (val > 1.1f * sense) {
		setPanelIndicator(PANEL_IND_2, true);
		enableLight = true;
	}
	else if (val < 0.9f * sense) {
		setPanelIndicator(PANEL_IND_2, false);
		enableLight = false;
	}
	currentSense = val;
}

void startTempConversion(unsigned char line) {
	sendByte(CMD_SKIP_ROM, line);
	sendByte(CMD_CONVERT, line);
}

unsigned short readTempValue(unsigned char line) {
	unsigned char	mask = (1 << line);
	unsigned short	result = 0;

	sendByte(CMD_SKIP_ROM,line);
	sendByte(CMD_READ_SCRATCHPAD, line);
	result = (receiveByte(line) << 8) | receiveByte(line);
	startTempConversion(line);
	return result;
}

#define TEMP_SENS_1	0
#define TEMP_SENS_2	1

void readTemp(unsigned char sens) {
	switch (sens) {
		case TEMP_SENS_1	:
			t1Samples[t1SamplesPos++] = readTempValue(1 << TEMP_SENS_PIN_LINE1);
			if (t1SamplesPos > sizeof(t1Samples)/ sizeof(t1Samples[0])) {
				t1SamplesPos = 0;
			}
			t1 = calcSamples(t1Samples, sizeof(t1Samples) / sizeof(t1Samples[0]));
			break;
		case TEMP_SENS_2	:
			t2Samples[t2SamplesPos++] = readTempValue(1 << TEMP_SENS_PIN_LINE2);
			if (t2SamplesPos > sizeof(t2Samples) / sizeof(t2Samples[0])) {
				t2SamplesPos = 0;
			}
			t2 = calcSamples(t2Samples, sizeof(t2Samples) / sizeof(t2Samples[0]));
			break;
	}
}

void setLightMode(unsigned char mode) {
	switch (mode) {
		case LIGHT_ON	:
			setPanelIndicator(PANEL_IND_1, false);
			setPanelIndicator(PANEL_IND_2, false);
			setPanelIndicator(PANEL_LIGHT, true);
			lightMode = LIGHT_ON;
			break;
		case LIGHT_OFF	:
			setPanelIndicator(PANEL_IND_1, false);
			setPanelIndicator(PANEL_IND_2, false);
			setPanelIndicator(PANEL_LIGHT, false);
			lightMode = LIGHT_OFF;
			break;
		case LIGHT_SPY	:
			setPanelIndicator(PANEL_IND_1, true);
			setPanelIndicator(PANEL_IND_2, false);
			setPanelIndicator(PANEL_LIGHT, false);
			lightMode = LIGHT_SPY;
			break;
	}
}

#define PIN_SENSOR_PORT x
#define PIN_SENSOR_DDR	x
#define PIN_SENSOR_PIN	x
#define PIN_SENSOR_LINE1_PIN	1
#define PIN_SENSOR_LINE2_PIN	2

#define PIN_SENSOR1 1
#define PIN_SENSOR2 2

void isrRCV() {
	receiverChanged = true;
}

unsigned char readReceiver(void) {
	unsigned char	result = 0;

	if ((PIN_SENSOR_PIN & (1 << PIN_SENSOR_LINE1_PIN)) ==  0) {
		result |= PIN_SENSOR1;
	}
	if ((PIN_SENSOR_PIN & (1 << PIN_SENSOR_LINE2_PIN)) == 0) {
		result |= PIN_SENSOR2;
	}
	return result;
}

unsigned char	oldPins = 0, currentPins = oldPins, state =  0;

#define	TERM_BUTTON1_PRESSED	0
#define	TERM_BUTTON1_RELEASED	1
#define	TERM_BUTTON2_PRESSED	2
#define	TERM_BUTTON2_RELEASED	3

typedef struct {
	unsigned char	state;
	unsigned char	terminal;
	unsigned char	newState;
	unsigned char	action;
} AutomatTable;

static AutomatTable		table[] = { {0, TERM_BUTTON1_PRESSED, 1, LIGHT_ON},
									{1, TERM_BUTTON2_PRESSED, 2, LIGHT_OFF},
									{1, TERM_BUTTON1_PRESSED, 0, LIGHT_SPY},
									{2, TERM_BUTTON2_PRESSED, 1, LIGHT_ON},
									{2, TERM_BUTTON2_PRESSED, 0, LIGHT_SPY},
								};

void automat(unsigned char terminal) {
	for (unsigned char index = 0; index < sizeof(table) / sizeof(table[0]); index++) {
		if (table[index].state == state && table[index].terminal == terminal) {
			state = table[index].newState;
			setLightMode(table[index].action);
			return;
		}
	}
}

void processReceiver() {
	unsigned char	newPins = readReceiver();

	if (currentPins != newPins) {
		newPins = currentPins;
	}
	else if (oldPins != newPins) {
		oldPins ^= newPins;
		if (oldPins & PIN_SENSOR1) {
			automat(newPins & PIN_SENSOR1 ? TERM_BUTTON1_PRESSED : TERM_BUTTON1_RELEASED);
		}
		if (oldPins & PIN_SENSOR2) {
			automat(newPins & PIN_SENSOR2 ? TERM_BUTTON2_PRESSED : TERM_BUTTON2_RELEASED);
		}
		oldPins = currentPins = newPins;
	}
}


//
//	Boot loader
//

void boot(void) {

}

//
//	Command parser
//

// ::ver -> .....
// ::state ->t1=xxxx t2=xxxx light={on|off|spy} sens=xxxx
// ::light {on|off|spy}
// ::sens xxxx
// ::boot

char* skipSpace(char *source) {
	while (*source <= ' ' && *source != '\n') {
		source++;
	}
	return source;
}

char* readNumber(char *source, unsigned short *val) {
	unsigned short	temp = 0;

	while (*source >= '0' && *source <= '9') {
		temp = 10 * temp + *source++ - '0';
	}
	*val = temp;
	return source;
}

bool compareP(char* command, char* templ) {
	char symbol;

	while ((symbol = read(templ++)) !=  0) {
		if (*command++ != symbol) {
			return false;
		}
	}
	return true;
}

void parseCommand(char *command) {
	if (*command++ == ':' && *command++ == ':') {
		switch (*command) {
			case 'v' :
				if (compareP(command,CMD_VER)) {
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
						printP(ANS_T1);
						printValue(t1);
						printP(ANS_T2);
						printValue(t2);
						printP(ANS_LIGHT);
						switch (lightMode) {
						case LIGHT_ON: printP(OPT_ON);  break;
						case LIGHT_OFF: printP(OPT_OFF);  break;
						case LIGHT_SPY: printP(OPT_SPY);  break;
						}
						printP(ANS_SENSE);
						printValue(sense);
						printP(ANS_NL);
					}
					else {
						printP(ERR_UNPARSED_TAIL);
					}
					return;
				}
				else if (compareP(command, CMD_SENS)) {
					command = skipSpace(command + sizeof(CMD_SENS));
					if (*command >= '0' && *command <= '0') {
						unsigned short	val;

						command = skipSpace(readNumber(command, &val));
						if (*command == '\n') {
							sense = val;
							printP(ANS_OK);
						}
						else {
							printP(ERR_UNPARSED_TAIL);
						}
					}
					else {
						printP(ERR_NUMBER_MISSING);
					}
					return;
				}
				else {
					break;
				}
			case 'l':
				if (compareP(command, CMD_LIGHT)) {
					command = skipSpace(command + sizeof(CMD_LIGHT));
					if (compareP(command, OPT_ON)) {
						command = skipSpace(command + sizeof(OPT_ON));
						if (*command == '\n') {
							setLightMode(LIGHT_ON);
							printP(ANS_OK);
						}
						else {
							printP(ERR_UNPARSED_TAIL);
						}
					}
					else if (compareP(command, OPT_OFF)) {
						command = skipSpace(command + sizeof(OPT_OFF));
						if (*command == '\n') {
							setLightMode(LIGHT_OFF);
							printP(ANS_OK);
						}
						else {
							printP(ERR_UNPARSED_TAIL);
						}
					}
					else if (compareP(command, OPT_SPY)) {
						command = skipSpace(command + sizeof(OPT_SPY));
						if (*command == '\n') {
							setLightMode(LIGHT_SPY);
							printP(ANS_OK);
						}
						else {
							printP(ERR_UNPARSED_TAIL);
						}
					}
					else {
						printP(ERR_UNKNOWN_OPTION);
					}
					return;
				}
				else {
					break;
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
		printP(ERR_UNKNOWN_COMMAND);
	}
	else {
		printP(ERR_ILLEGAL_PREFIX);
	}
}

//
//	Initialization and main
//

void init(void) {
	LED_PORT |= (1 << LED_RED_PIN) | (1 << LED_GREEN_PIN) | (1 << LED_BLUE_PIN);	// Prepare LEDs
	LED_DDR |= (1 << LED_RED_PIN) | (1 << LED_GREEN_PIN) | (1 << LED_BLUE_PIN);

	PANEL_PORT &= ~((1 << PANEL_IND_1_PIN) | (1 << PANEL_IND_2_PIN) | (1 << PANEL_IND_3_PIN));	// Prepare panel lines
	PANEL_DDR &= ~((1 << PANEL_IND_1_PIN) | (1 << PANEL_IND_2_PIN) | (1 << PANEL_IND_3_PIN));

	MOTION_SENSOR_PORT |= (1 << MOTION_SENSOR_LINE_PIN);	// Prepare motion sensor
	MOTION_SENSOR_DDR &= !(1 << MOTION_SENSOR_LINE_PIN);
	EICRA = (1 << ISC00);
	EIMSK = (1 << INT0);

	PIN_SENSOR_PORT |= (1 << PIN_SENSOR_LINE1_PIN) | (1 << PIN_SENSOR_LINE2_PIN); // Prepare receiver line sensor
	PIN_SENSOR_DDR &= ~((1 << PIN_SENSOR_LINE1_PIN) | (1 << PIN_SENSOR_LINE2_PIN));
	PCICR = (1 << PCIE1);	
	PCMSK1 = (1 << PCINT8) | (1 << PCINT9);

	TEMP_SENS_PORT |= (1 << TEMP_SENS_PIN_LINE1) | (1 << TEMP_SENS_PIN_LINE2);	// Prepare temp sensors
	TEMP_SENS_DDR |= (1 << TEMP_SENS_PIN_LINE1) | (1 << TEMP_SENS_PIN_LINE2);

	TCCR1B = (1 << CS11) | (1 << CS10); // Prepare polling timer
	TIMSK1 = (1 << TOIE1);

	ADMUX = (1 << REFS1) | (1 << REFS0);	// Prepare ADC
	ADCSRA = (1 << ADEN) | (1 << ADATE) | (1 << ADIE) | (1 << ADPS2) | (1 << ADPS1) | (1 << ADPS0);

	UBRR0 = 12;	// Prepare USART0 for 8 Mhz (38400, 7, PE, 1-s)
	USCR0B = (1 << RXEN0) | (1 << TXEN0);
	USCR0C = (1 << UCSZ01) | (1 << UPM01);
	enableRX(true);

	memset(lightSamples, 0, sizeof(lightSamples));
	memset(t1Samples, 0, sizeof(t1Samples));
	memset(t2Samples, 0, sizeof(t2Samples));

	ADCSRA |= (1 << ADSC);	// Start ADC
}

bool allSensorArePresented(void) {
	return W1DetectPresence(1 << TEMP_SENS_PIN_LINE1) && W1DetectPresence(1 << TEMP_SENS_PIN_LINE2);
}

void prepareSleepMode(void) {

}

void startTempConversions(void) {
	startTempConversion(1 << TEMP_SENS_PIN_LINE1);
	startTempConversion(1 << TEMP_SENS_PIN_LINE2);
}


void sleep(void) {
}

int main(int argc, char** argv) {
	init();

	if (!allSensorArePresented()) {
		led(LED_RED, true);
	}
	prepareSleepMode();
	startTempConversions();
	ei();
	for (;;) {
		led(LED_GREEN, true);
		sleep();
		led(LED_GREEN, false);
		if (ready) {
			ready = false;
			parseCommand(bufferIn);
		}
		if (tick) {
			tick = false;
			readTemp(TEMP_SENS_1);
			readTemp(TEMP_SENS_2);
		}
		if (receiverChanged) {
			receiverChanged = false;
			processReceiver();
		}
	}

	return 0;
}