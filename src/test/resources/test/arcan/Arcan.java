package arcan;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Arcan extends JFrame {
	
	public Arcan() {
		final Dimension		screen = Toolkit.getDefaultToolkit().getScreenSize();
		final ArcanField	af = new ArcanField();
		
		setSize(screen.width*3/4,screen.height*3/4);
		setLocationRelativeTo(null);
		getContentPane().add(af , BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		new Arcan().setVisible(true);
	}
}


class ArcanField extends JComponent {
	private static final long 	serialVersionUID = -3569101649084092958L;
	private static final int	WINDOW_WIDTH = 80;
	private static final int	WINDOW_HEIGHT = 60;
	private static final int	WALL_THICKNESS = 2;
	private static final int	SHIELD_THICKNESS = 2;
	private static final int	SHIELD_HEIGHT = 6;
	private static final int	BALL_SIZE = 1;
	private static final int	TICK_DELAY = 20;

	private static final int	STATE_BEFORE_START = 0;
	private static final int	STATE_STARTED = 1;
	private static final int	STATE_PAUSED = 2;
	private static final int	STATE_CRUSHED = 3;
	
	private int			state = STATE_BEFORE_START;
	
	private Timer		t = null;
	private TimerTask	tt = null;
	private float		currentBallX, currentBallY; 
	private float		deltaBallX, deltaBallY; 
	private float		currentShieldX, currentShieldY; 
	
	ArcanField() {
		setFocusable(true);
		initVariables();
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				final int	rots = e.getWheelRotation();
				
				if (state == STATE_STARTED) {
					if (rots < 0) {
						shieldDown(- 1.0f * rots);
					}
					else if (rots > 0) {
						shieldUp(1.0f * rots);
					}
				}
			}
		});
		addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {}
			@Override public void keyReleased(KeyEvent e) {}
			
			@Override 
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_ESCAPE 	:
						switch (state) {
							case STATE_BEFORE_START :
								break;
							case STATE_STARTED		:
								state = STATE_PAUSED; 
								pause();
								break;
							case STATE_PAUSED		:
								state = STATE_STARTED; 
								resume();
								break;
							case STATE_CRUSHED		:
								state = STATE_BEFORE_START;
								repaint();
								break;
						}
						break;
					case KeyEvent.VK_ENTER	 	:
						switch (state) {
							case STATE_BEFORE_START :
								state = STATE_STARTED;
								start();
								break;
						}
						break;
					case KeyEvent.VK_UP		 	:
						if (state == STATE_STARTED) {
							shieldUp(1.0f);
						}
						break;
					case KeyEvent.VK_DOWN	 	:
						if (state == STATE_STARTED) {
							shieldDown(1.0f);
						}
						break;
				}
			}
		});
	}

	void start() {
		initVariables();
		resume();
	}
	
	void pause() {
		t.purge();
		t.cancel();
	}

	void resume() {
		t = new Timer(true);
		tt = new TimerTask() {
				@Override
				public void run() {
					tick();
				}
			};
		t.scheduleAtFixedRate(tt, TICK_DELAY, TICK_DELAY);
	}
	
	void stop() {
		pause();
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D		g2d = (Graphics2D)g;
		final AffineTransform	oldAt = g2d.getTransform();

		normalizeWindow(g2d);
		drawBackground(g2d);
		drawBall(g2d);
		drawWall(g2d);
		drawShield(g2d);
		switch (state) {
			case STATE_BEFORE_START :
				drawMessage(g2d,"Press Enter to start (Esc - paused)");
				break;
			case STATE_STARTED		:
				break;
			case STATE_PAUSED		:
				drawMessage(g2d,"Paused. Press Esc to continue");
				break;
			case STATE_CRUSHED		:
				drawMessage(g2d,"Crush! Press Esc to continue");
				break;
		}
		
		g2d.setTransform(oldAt);
	}

	private void normalizeWindow(final Graphics2D g2d) {
		final AffineTransform	at = new AffineTransform(g2d.getTransform());
		
		at.scale(1.0f * getWidth() / WINDOW_WIDTH, 1.0f * getHeight() / WINDOW_HEIGHT);
		g2d.setTransform(at);
	}

	private void drawBackground(final Graphics2D g2d) {
		final Color		oldColor = g2d.getColor();
	
		g2d.setColor(Color.black);
		g2d.fillRect(0,0,WINDOW_WIDTH,WINDOW_HEIGHT);
		g2d.setColor(oldColor);
	}

	private void drawBall(final Graphics2D g2d) {
		final Color		oldColor = g2d.getColor();
		final Shape		sh = new Ellipse2D.Float(currentBallX,currentBallY,BALL_SIZE,BALL_SIZE);
		final Stroke	oldStroke = g2d.getStroke();

		g2d.setStroke(new BasicStroke(0.05f));
		g2d.setColor(Color.white);
		g2d.fill(sh);
		g2d.setColor(Color.red);
		g2d.draw(sh);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
	}

	private void drawWall(final Graphics2D g2d) {
		for (int x = 0; x < WINDOW_WIDTH; x+= WALL_THICKNESS) {
			drawBrick(g2d,x,0);
			drawBrick(g2d,x,WINDOW_HEIGHT-WALL_THICKNESS);
		}
		for (int y = WALL_THICKNESS; y < WINDOW_HEIGHT; y+= WALL_THICKNESS) {
			drawBrick(g2d,0,y);
		}
	}

	private void drawBrick(final Graphics2D g2d, final int x, final int y) {
		final Color		oldColor = g2d.getColor();
		final Stroke	oldStroke = g2d.getStroke();

		g2d.setStroke(new BasicStroke(0.05f));
		g2d.setColor(Color.RED);
		g2d.fillRect(x,y,WALL_THICKNESS,WALL_THICKNESS);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawRect(x,y,WALL_THICKNESS,WALL_THICKNESS);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
	}

	private void drawShield(Graphics2D g2d) {
		final Color		oldColor = g2d.getColor();
		final Shape		sh = new Rectangle2D.Float(currentShieldX-SHIELD_THICKNESS/2,currentShieldY-SHIELD_HEIGHT/2,SHIELD_THICKNESS,SHIELD_HEIGHT);
		final Stroke	oldStroke = g2d.getStroke();

		g2d.setStroke(new BasicStroke(0.05f));
		g2d.setColor(Color.BLUE);
		g2d.fill(sh);
		g2d.setColor(Color.CYAN);
		g2d.draw(sh);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
	}

	private void drawMessage(final Graphics2D g2d, final String message) {
		final Color		oldColor = g2d.getColor();
		final Font		oldFont = g2d.getFont();
		
		g2d.setColor(Color.GREEN);
		g2d.setFont(new Font("Courier",Font.PLAIN,2));
		g2d.drawChars(message.toCharArray(),0,message.length(), WINDOW_WIDTH / 2 - message.length() / 2, WINDOW_HEIGHT/2 + 1);
		g2d.setFont(oldFont);
		g2d.setColor(oldColor);
	}
	
	private void tick() {
		if (state == STATE_STARTED) {
			if (deltaBallY > 0 && currentBallY + deltaBallY >= WINDOW_HEIGHT - WALL_THICKNESS) {
				deltaBallY = -deltaBallY;
			}
			else if (deltaBallY < 0 && currentBallY + deltaBallY <= WALL_THICKNESS) {
				deltaBallY = -deltaBallY;
			}
			if (deltaBallX > 0) {
				if (currentBallX + deltaBallX >= WINDOW_WIDTH - SHIELD_THICKNESS) {
					if (currentBallY + deltaBallY >= currentShieldY - SHIELD_HEIGHT / 2 && currentBallY + deltaBallY <= currentShieldY + SHIELD_HEIGHT / 2) {
						deltaBallX = -deltaBallX;
					}
					else {
						state = STATE_CRUSHED;
						stop();
					}
				}
			}
			else if (deltaBallX < 0 && currentBallX + deltaBallX <= WALL_THICKNESS) {
				deltaBallX = -deltaBallX;
			}
			currentBallX += deltaBallX;
			currentBallY += deltaBallY;
			repaint();
		}
	}

	private void shieldDown(final float delta) {
		if (currentShieldY + delta + SHIELD_HEIGHT / 2 < WINDOW_HEIGHT - WALL_THICKNESS) {
			currentShieldY += delta;
		}
		repaint();
	}
	
	private void shieldUp(final float delta) {
		if (currentShieldY - delta - SHIELD_HEIGHT / 2 > WALL_THICKNESS) {
			currentShieldY -= delta;
		}
		repaint();
	}
	
	private void initVariables() {
		currentBallX = 2.0f * WALL_THICKNESS + BALL_SIZE / 2;
		currentBallY = 0.5f * WINDOW_HEIGHT;
		deltaBallX = 0.5f;
		deltaBallY = 0.5f;
		currentShieldX = 1.0f * WINDOW_WIDTH - SHIELD_THICKNESS / 2;
		currentShieldY = 0.5f * WINDOW_HEIGHT;
	}
}
