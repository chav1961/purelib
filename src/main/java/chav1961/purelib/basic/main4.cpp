//

#include <vcruntime_string.h>
unsigned char x, UBRR0, USCR0B, RXEN0, TXEN0, RXCIE0, URDIE0, USCR0C, UCSZ01, UPM01, UDR0, EICRA, ISC00, EIMSK, INT0, ADC, ADMUX, REFS1, REFS0, ADCSRA, ADEN, ADATE, ADIE, ADPS2, ADPS1, ADPS0, ADSC;
unsigned char PCICR, PCIE1, PCMSK1, PCINT8, PCINT9, TCCR1B, CS11, CS10, TIMSK1, TOIE1, SREG, EEAR, EECR, EEDR, EERE, EEPE, EEMPE, EERIE, EEPE;


//
//	Led manager
//

#define LED_PORT		x
#define LED_DDR			x
#define LED_RED_PIN		1
#define LED_YELLOW_PIN	2
#define LED_GREEN_PIN	3

#define LED_RED		0
#define LED_YELLOW	1
#define LED_GREEN	2

void led(unsigned char ledNo, bool state) {
	switch (ledNo) {
		case LED_RED: if (state) LED_PORT &= ~(1 << LED_RED_PIN); else LED_PORT |= (1 << LED_RED_PIN); break;
		case LED_YELLOW: if (state) LED_PORT &= ~(1 << LED_YELLOW_PIN); else LED_PORT |= (1 << LED_YELLOW_PIN); break;
		case LED_GREEN: if (state) LED_PORT &= ~(1 << LED_GREEN_PIN); else LED_PORT |= (1 << LED_GREEN_PIN); break;
	}
}

//
//	Control procedures
//

#define TURN_PORT	x
#define TURN_DDR	x
#define TURN_PIN	3

#define TIMEOUT_VAL1	1
#define TIMEOUT_VAL2	2

#define VAL_135	135
#define VAL_144	144
#define VAL_160	160

unsigned short	samples[8] = {0,0,0,0,0,0,0,0};
unsigned char	samplesPos = 0;
volatile unsigned short	value;
volatile bool	tick = false;
volatile unsigned char	leds = 0, masks = 0xFF, count = 0;
volatile unsigned char	timeout = TIMEOUT_VAL2, loops = 0;

void isrTmr() {
	if (timeout > 0) {
		if (--timeout == 0) {
			tick = true;
		}
	}
	count++;
	if ((leds & (1 << LED_RED)) != 0) {
		led(LED_RED, (masks & (1 << LED_RED)) != 0 || (count & 0x01) != 0);
	}
	else {
		led(LED_RED, false);
	}
	if ((leds & (1 << LED_YELLOW)) != 0) {
		led(LED_YELLOW, (masks & (1 << LED_YELLOW)) != 0 || (count & 0x01) != 0);
	}
	else {
		led(LED_YELLOW, false);
	}
	if ((leds & (1 << LED_GREEN)) != 0) {
		led(LED_GREEN, (masks & (1 << LED_GREEN)) != 0 || (count & 0x01) != 0);
	}
	else {
		led(LED_GREEN, false);
	}
}

void isrADC() {
	unsigned short  temp = 0;
	unsigned char	index;

	samples[samplesPos++] = ADC;

	if (samplesPos >= sizeof(samples) / sizeof(samples[0])) {
		samplesPos = 0;
	}

	for (index = 0; index < sizeof(samples) / sizeof(samples[0]); index++) {
		temp += samples[index];
	}
	value = temp / (sizeof(samples) / sizeof(samples[0]));
}

//
//	Automat
//

#define	TERM_TIMEOUT	0
#define	TERM_MORE_144	1
#define	TERM_MORE_160	2
#define	TERM_LESS_135	3
#define	TERM_LAST		4

#define	X_TURNON		0
#define	X_TURNOFF		1
#define	X_RED_ON		2
#define	X_RED_OFF		3
#define	X_RED_BLINK		4
#define	X_YELLOW_ON		5
#define	X_YELLOW_OFF	6
#define	X_YELLOW_BLINK	7
#define	X_GREEN_ON		8
#define	X_GREEN_OFF		9
#define	X_GREEN_BLINK	10
#define	X_TIMEOUT1		11
#define	X_TIMEOUT2		12
#define	X_COUNT			13
#define	X_LOOP			14

#define LOOP_COUNT		3

typedef struct {
	unsigned char	state;
	unsigned char	terminal;
	unsigned char	newState;
	unsigned short	action;
} Automat;

static Automat	table[] = { 
							{0,	TERM_TIMEOUT, 1, X_TURNON | X_RED_BLINK | X_TIMEOUT1}
							{1,	TERM_TIMEOUT, 2, X_TURNOFF | X_RED_ON | X_TIMEOUT2}
							{1,	TERM_LAST, 4, X_TURNON | X_GREEN_BLINK | X_TIMEOUT1}
							{2,	TERM_TIMEOUT, 1, X_TURNON | X_RED_BLINK | X_TIMEOUT1}
							{2,	TERM_MORE_144, 3, X_TURNOFF | X_YELLOW_BLINK | X_COUNT}
							{3,	TERM_LESS_135, 1, X_TURNON | X_RED_BLINK | X_TIMEOUT1 | X_LOOP}
							{4,	TERM_MORE_160, 5, X_TURNOFF | X_GREEN_ON}
						};

unsigned char		state = 0;

unsigned char read(void *addr) {
	return 0;
}

void automat(unsigned char terminal) {
	unsigned short		action;

	for (unsigned char index = 0; index < sizeof(table) / sizeof(table[0]); index++) {
		if (state == read(&table[index].state) && terminal == read(&table[index].terminal)) {
			state = read(&table[index].newState);
			action = read(&table[index].action);
			
			if (action & (1 << X_TURNON)) {
				TURN_PORT |= (1 << TURN_PIN);
			}
			if (action & (1 << X_TURNOFF)) {
				TURN_PORT &= ~(1 << TURN_PIN);
			}
			if (action & (1 << X_RED_ON)) {
				leds |= (1 << LED_RED);
				masks |= (1 << LED_RED);
			}
			if (action & (1 << X_RED_OFF)) {
				leds &= ~(1 << LED_RED);
			}
			if (action & (1 << X_RED_BLINK)) {
				leds |= (1 << LED_RED);
				masks &= ~(1 << LED_RED);
			}
			if (action & (1 << X_YELLOW_ON)) {
				leds |= (1 << LED_YELLOW);
				masks |= (1 << LED_YELLOW);
			}
			if (action & (1 << X_YELLOW_OFF)) {
				leds &= ~(1 << LED_YELLOW);
			}
			if (action & (1 << X_YELLOW_BLINK)) {
				leds |= (1 << LED_YELLOW);
				masks &= ~(1 << LED_YELLOW);
			}
			if (action & (1 << X_GREEN_ON)) {
				leds |= (1 << LED_GREEN);
				masks |= (1 << LED_GREEN);
			}
			if (action & (1 << X_GREEN_OFF)) {
				leds &= ~(1 << LED_GREEN);
			}
			if (action & (1 << X_GREEN_BLINK)) {
				leds |= (1 << LED_GREEN);
				masks &= ~(1 << LED_GREEN);
			}
			if (action & (1 << X_TIMEOUT1)) {
				timeout = TIMEOUT_VAL1;
			}
			if (action & (1 << X_TIMEOUT2)) {
				timeout = TIMEOUT_VAL2;
			}
			if (action & (1 << X_COUNT)) {
				loops++;
			}
			if (action & (1 << X_LOOP)) {
				if (loops > LOOP_COUNT) {
					automat(TERM_LAST);
				}
			}
			return;
		}
	}
}

//
//	Initialization and main
//

void init(void) {
	LED_PORT |= (1 << LED_RED_PIN) | (1 << LED_YELLOW_PIN) | (1 << LED_GREEN_PIN);	// Prepare LEDs
	LED_DDR |= (1 << LED_RED_PIN) | (1 << LED_YELLOW_PIN) | (1 << LED_GREEN_PIN);

	TURN_PORT &= ~(1 << TURN_PIN);	// Prepare turn port
	TURN_DDR |= (1 << TURN_PIN);

	TCCR1B = (1 << CS11) | (1 << CS10); // Prepare polling timer
	TIMSK1 = (1 << TOIE1);

	ADCSRA |= (1 << ADSC);	// Start ADC
}

void prepareSleepMode(void) {

}

void sleep(void) {
}

int main(int argc, char** argv) {
	init();

	prepareSleepMode();
	for (;;) {
		sleep();
		if (tick) {
			tick = false;
			automat(TERM_TIMEOUT);
		}
		if (value < VAL_135) {
			automat(TERM_LESS_135);
		}
		else if (value > VAL_160) {
			automat(TERM_MORE_160);
		}
		else if (value > VAL_144) {
			automat(TERM_MORE_144);
		}
	}

	return 0;
}