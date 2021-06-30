import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public class PingPong {

	JFrame frame;
	Canvas canvas;
	BufferStrategy bs;
	Graphics g;

	Keyboard keyboard;
	Mouse mouse;

	Field field;
	Ball ball;
	Pad pad1;
	static Pad pad2;
	ComputerPad cp;

	ModeMenu mode;
	MainMenu mainMenu;
	FinishMenu finishMenu;
	ReadyMenu readyMenu;
	PauseMenu pause;
	HelpMenu helpMenu;
	CreditsMenu creditsMenu;

	enum State {
		GAME, MENU, PAUSE, FINISH, READY, HELP, CREDITS, VSCOMP, MODE
	}

	static State state = State.MENU;

	public void init() {
		mode = new ModeMenu();
		field = new Field();
		pad1 = new Pad(Color.RED);
		pad2 = new Pad(new Color(0, 128, 255));
		ball = new Ball(pad1, pad2);
		pad2.x = 854 - pad2.width - 10;
		pad1.x = 10;
		mainMenu = new MainMenu();
		finishMenu = new FinishMenu();
		readyMenu = new ReadyMenu();
		pause = new PauseMenu();
		helpMenu = new HelpMenu();
		creditsMenu = new CreditsMenu();

		ball.newY = pad1.y + pad1.height / 2 - (ball.size) / 2;
	}

	public void createWindow() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setTitle("Ping Pong");
		frame.setVisible(true);
		canvas = new Canvas();
		canvas.setSize(854, 640);
		frame.add(canvas);
		frame.pack();
		frame.setLocationRelativeTo(null);
		keyboard = new Keyboard();
		canvas.addKeyListener(keyboard);
		mouse = new Mouse();
		canvas.addMouseListener(mouse);
		canvas.addMouseMotionListener(mouse);
		canvas.requestFocus();
	}

	public void run() {
		long currentTime = 0;
		long previousTime = System.currentTimeMillis();
		int elapsedTime = 0;
		int timePerTick = 1000 / 60;
		while (true) {
			currentTime = System.currentTimeMillis();
			elapsedTime += currentTime - previousTime;
			previousTime = currentTime;
			while (elapsedTime >= timePerTick) {
				tick();
				elapsedTime -= timePerTick;
			}
			render();
		}
	}

	boolean canChoose = false;
	boolean play = true;
	boolean tr1 = false;
	boolean tr2 = false;

	public void tick() {
		if (state == State.MODE) {
			ball.tick(pad1, pad2);
			mode.tick();
		}
		if (state == State.GAME) {
			if (play) {
				ball.tick(pad1, pad2);
			}
			pad1.tick(Keyboard.keys[KeyEvent.VK_W], Keyboard.keys[KeyEvent.VK_S]);
			if (pad2 instanceof ComputerPad) {
				pad2.tick(ball);
			} else {
				pad2.tick(Keyboard.keys[KeyEvent.VK_UP], Keyboard.keys[KeyEvent.VK_DOWN]);
			}
			if (ball.turn1) {
				ball.y = pad1.y + pad1.height / 2 - (ball.size + 4) / 2;
				ball.x = pad1.x + pad1.width - 2;
				ball.angle = -1 * Math.PI / 4 + Math.random() * Math.PI / 2;
				ball.anim = 1;
				ball.sin = Math.sin(ball.angle);
				ball.cos = Math.cos(ball.angle);
				ball.canPad1Rico = true;
				ball.canPad2Rico = true;
				ball.turn1 = false;
				play = false;
				tr1 = true;

			}
			if (ball.turn2) {
				ball.y = pad2.y + pad2.height / 2 - (ball.size + 4) / 2;
				ball.x = pad2.x - ball.size + 2;
				ball.angle = 3 * Math.PI / 4 + Math.random() * Math.PI / 2;
				ball.anim = 1;
				ball.sin = Math.sin(ball.angle);
				ball.cos = Math.cos(ball.angle);
				ball.canPad1Rico = true;
				ball.canPad2Rico = true;
				ball.turn2 = false;
				play = false;
				tr2 = true;
			}
			if (tr1) {
				ball.y = pad1.y + pad1.height / 2 - (ball.size + 4) / 2;
				if (pad1.direction == 1 && ball.sin < 0) {
					ball.sin *= -1;
				} else if (pad1.direction == -1 && ball.sin > 0) {
					ball.sin *= -1;
				}

			}
			if (tr2) {
				if (pad2 instanceof ComputerPad) {
					play = true;
					tr1 = false;
					tr2 = false;
				} else {

					ball.y = pad2.y + pad2.height / 2 - ball.size / 2;
					if (pad2.direction == 1 && ball.sin < 0) {
						ball.sin *= -1;
					} else if (pad2.direction == -1 && ball.sin > 0) {
						ball.sin *= -1;
					}
				}
			}
			if (pad1.score == 10 || pad2.score == 10) {
				state = State.FINISH;
			}
			if (Keyboard.keys[KeyEvent.VK_SPACE]) {
				if (canChoose) {
					if (tr1 || tr2) {
						ball.newY = Detector.detectNewY(ball.sin, ball.cos, ball.y);
						pad2.st = true;
						play = true;
						tr1 = false;
						tr2 = false;
					}
					canChoose = false;
				}
			} else if (Keyboard.keys[KeyEvent.VK_P]) {
				if (canChoose) {
					state = State.PAUSE;
					canChoose = false;
				}
			} else {
				canChoose = true;
			}
		}
		if (state == State.CREDITS) {
			creditsMenu.tick();
		}
		if (state == State.HELP) {
			helpMenu.tick();
		}
		if (state == State.MENU) {
			ball.tick(pad1, pad2);
			mainMenu.tick();
		}
		if (state == State.PAUSE) {
			pause.tick(ball, pad1, pad2);
			if (Keyboard.keys[KeyEvent.VK_P]) {
				if (canChoose) {
					if (ReadyMenu.trow) {
						state = State.READY;
					} else {
						state = State.GAME;
					}
					canChoose = false;
				}
			} else {
				canChoose = true;
			}
		}
		if (state == State.FINISH) {
			finishMenu.tick(pad1, pad2, ball);
		}
		if (state == State.READY) {
			if (!ReadyMenu.trow) {
				readyMenu.tick(ball, pad1, pad2);
				ball.y = pad1.y + pad1.height / 2 - (ball.size + 4) / 2;
			}
			if (ReadyMenu.trow) {
				pad1.tick(Keyboard.keys[KeyEvent.VK_W], Keyboard.keys[KeyEvent.VK_S]);
				if (pad2 instanceof ComputerPad) {
					ball.y = pad1.y + pad1.height / 2 - (ball.size + 4) / 2;
					if (pad1.direction == 1 && ball.sin < 0) {
						ball.sin *= -1;
					} else if (pad1.direction == -1 && ball.sin > 0) {
						ball.sin *= -1;
					}
				} else {
					pad2.tick(Keyboard.keys[KeyEvent.VK_UP], Keyboard.keys[KeyEvent.VK_DOWN]);
					if (readyMenu.l > 5) {
						ball.y = pad1.y + pad1.height / 2 - (ball.size + 4) / 2;
						if (pad1.direction == 1 && ball.sin < 0) {
							ball.sin *= -1;
						} else if (pad1.direction == -1 && ball.sin > 0) {
							ball.sin *= -1;
						}
					} else {
						ball.y = pad2.y + pad2.height / 2 - (ball.size + 4) / 2;
						if (pad2.direction == 1 && ball.sin < 0) {
							ball.sin *= -1;
						} else if (pad2.direction == -1 && ball.sin > 0) {
							ball.sin *= -1;
						}
					}
				}
				if (Keyboard.keys[KeyEvent.VK_SPACE]) {
					if (canChoose) {
						state = State.GAME;
						ball.newY = Detector.detectNewY(ball.sin, ball.cos, (int) ball.y);
						ReadyMenu.newGame = true;
						ReadyMenu.trow = false;
						canChoose = false;
					}
				} else if (Keyboard.keys[KeyEvent.VK_P]) {
					if (canChoose) {
						state = State.PAUSE;
						canChoose = false;
					}
				} else {
					canChoose = true;
				}
			}
		}
	}

	public void render() {
		bs = canvas.getBufferStrategy();
		if (bs == null) {
			canvas.createBufferStrategy(2);
			return;
		}
		g = bs.getDrawGraphics();
		g.setColor(new Color(0, 64, 0));
		g.fillRect(0, 0, 854, 640);
		if (state == State.VSCOMP) {
			field.render(g, pad1, cp);
			pad1.render(g);
			cp.render(g);
			ball.render(g);
		}
		if (state == State.MODE) {
			mode.render(g);
			pad1.render(g);
			pad2.render(g);
			ball.render(g);
		}
		if (state == State.GAME) {
			field.render(g, pad1, pad2);
			pad1.render(g);
			pad2.render(g);
			ball.render(g);
		}
		if (state == State.MENU) {
			mainMenu.render(g);
			pad1.render(g);
			pad2.render(g);
			ball.render(g);
		}
		if (state == State.PAUSE) {
			field.render(g, pad1, pad2);
			pad1.render(g);
			pad2.render(g);
			ball.render(g);
			g.setColor(new Color(0, 0, 0, 200));
			g.fillRect(0, 0, 854, 640);
			pause.render(g);
		}
		if (state == State.FINISH) {
			field.render(g, pad1, pad2);
			finishMenu.render(g, pad1, pad2);
		}
		if (state == State.READY) {
			if (!ReadyMenu.trow) {
				readyMenu.render(g, pad1, pad2, field);
				if (readyMenu.l > 5) {
					pad1.render(g);
				} else {
					pad2.render(g);
				}
				ball.render(g);
			} else {
				field.render(g, pad1, pad2);
				pad1.render(g);

				pad2.render(g);
				ball.render(g);
			}

		}
		if (state == State.CREDITS) {
			creditsMenu.render(g);
		}
		if (state == State.HELP) {
			helpMenu.render(g);
		}
		g.dispose();
		bs.show();
	}

	public static void main(String[] args) {
		PingPong db = new PingPong();
		db.createWindow();
		db.init();
		db.run();
	}

}

class Menu {

	boolean enter = false;

	public void tick() {

	}

	public void render(Graphics g) {

	}

}

class HelpMenu extends Menu {

	@Override
	public void tick() {
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				PingPong.state = PingPong.State.MENU;
			}
		}
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 25));
		String t = "The player who start and the angle he" + " will throw a ball by are defined by random. "
				+ "If in one of the rounds your opponent don't " + "catch a ball the player who get point will "
				+ "throw a ball in next round and the angle he " + "will throw a ball by are defined by random as "
				+ "well as in start of the game. To throw a ball " + "press space button on keyboard. The first player "
				+ "who get 10 points win the game. Press P button on keyboard if you want to pause game";
		String[] text = t.split(" ");
		int n = 0;
		int l = 0;
		while (n < text.length) {
			String line = text[n];
			n++;
			while (n < text.length && g.getFontMetrics().stringWidth(line + " " + text[n]) < 3 * 854 / 4) {
				line += " " + text[n];
				n++;
			}
			g.drawString(line, 854 / 8, 150 + g.getFontMetrics().getHeight() * l);
			l++;
		}
		g.setColor(new Color(128, 128, 128));
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			g.fillRect(854 / 2 - 125, 475, 250, 50);
		}
		g.setColor(Color.WHITE);
		g.drawRect(854 / 2 - 125, 475, 250, 50);

		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 30));

		g.drawString("Back To Menu", 854 / 2 - g.getFontMetrics().stringWidth("Back To Menu") / 2,
				475 + g.getFontMetrics().getHeight());
	}

}

class CreditsMenu extends Menu {

	@Override
	public void tick() {
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				PingPong.state = PingPong.State.MENU;
			}
		}
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 25));
		String t = "Hello! I am Grigoriev Vladislav. This is my Ping Pong game. I was programming this game in Java over the summer 2020 at age 17.";
		String[] text = t.split(" ");
		int n = 0;
		int l = 0;
		while (n < text.length) {
			String line = text[n];
			n++;
			while (n < text.length && g.getFontMetrics().stringWidth(line + " " + text[n]) < 3 * 854 / 4) {
				line += " " + text[n];
				n++;
			}
			g.drawString(line, 854 / 8, 150 + g.getFontMetrics().getHeight() * l);
			l++;
		}
		g.setColor(new Color(128, 128, 128));
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			g.fillRect(854 / 2 - 125, 475, 250, 50);
		}
		g.setColor(Color.WHITE);
		g.drawRect(854 / 2 - 125, 475, 250, 50);

		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 30));

		g.drawString("Back To Menu", 854 / 2 - g.getFontMetrics().stringWidth("Back To Menu") / 2,
				475 + g.getFontMetrics().getHeight());
	}

}

class ModeMenu extends Menu {

	public void tick() {
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 400 && Mouse.y <= 400 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				PingPong.pad2 = new Pad(new Color(0, 128, 255));

				PingPong.pad2.x = 854 - PingPong.pad2.width - 10;
				PingPong.state = PingPong.State.READY;
			}
		} else if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475
				&& Mouse.y <= 475 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				PingPong.pad2 = new ComputerPad(new Color(0, 128, 255));

				PingPong.pad2.x = 854 - PingPong.pad2.width - 10;
				PingPong.state = PingPong.State.READY;
			}
		} else if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 + 50 + 25
				&& Mouse.y <= 475 + 50 + 25 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				PingPong.state = PingPong.State.MENU;
			}
		}
	}

	public void render(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 65));
		g.drawString("PING PONG", 854 / 2 - g.getFontMetrics().stringWidth("PING PONG") / 2, 120);
		g.setColor(new Color(128, 128, 128));
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 400 && Mouse.y <= 400 + 50) {
			g.fillRect(854 / 2 - 125, 400, 250, 50);
		}
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			g.fillRect(854 / 2 - 125, 475, 250, 50);
		}
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 + 50 + 25
				&& Mouse.y <= 475 + 50 + 25 + 50) {
			g.fillRect(854 / 2 - 125, 475 + 50 + 25, 250, 50);
		}
		g.setColor(Color.WHITE);
		g.drawRect(854 / 2 - 125, 400, 250, 50);
		g.drawRect(854 / 2 - 125, 475, 250, 50);
		g.drawRect(854 / 2 - 125, 475 + 50 + 25, 250, 50);

		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 30));
		g.drawString("2 Player", 854 / 2 - g.getFontMetrics().stringWidth("2 Player") / 2,
				400 + g.getFontMetrics().getHeight());
		g.drawString("1 Player", 854 / 2 - g.getFontMetrics().stringWidth("1 Player") / 2,
				475 + g.getFontMetrics().getHeight());
		g.drawString("Back To Menu", 854 / 2 - g.getFontMetrics().stringWidth("Back To Menu") / 2,
				475 + 50 + 25 + g.getFontMetrics().getHeight());
	}

}

class PauseMenu extends Menu {

	public void tick(Ball ball, Pad pad1, Pad pad2) {
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				pad1.score = 0;
				pad2.score = 0;
				pad1.y = 320 - pad1.height / 2;
				pad2.y = 320 - pad2.height / 2;
				ball.spawnBall(pad1, pad2);
				ball.canPad1Rico = true;
				ball.canPad2Rico = true;
				ball.newY = pad1.y + pad1.height / 2 - (ball.size) / 2;
				ReadyMenu.newGame = true;
				ReadyMenu.trow = false;
				PingPong.state = PingPong.State.MENU;
			}
		}
	}

	@Override
	public void render(Graphics g) {
		g.setColor(new Color(128, 128, 128));
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			g.fillRect(854 / 2 - 125, 475, 250, 50);
		}
		g.setColor(Color.WHITE);
		g.drawRect(854 / 2 - 125, 475, 250, 50);

		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 30));

		g.drawString("Back To Menu", 854 / 2 - g.getFontMetrics().stringWidth("Back To Menu") / 2,
				475 + g.getFontMetrics().getHeight());
	}

}

class ReadyMenu extends Menu {

	double l;
	static boolean newGame = true;
	static boolean trow = false;

	public void tick(Ball ball, Pad pad1, Pad pad2) {
		if (newGame) {
			pad1.y = 320 - pad1.height / 2;
			pad2.y = 320 - pad2.height / 2;
			l = Math.random() * 10;
			if (l > 5) {
				ball.x = pad1.x + pad1.width - 2;
				ball.angle = -1 * Math.PI / 4 + Math.random() * Math.PI / 2;
				ball.anim = 1;
			} else {
				ball.x = pad2.x - ball.size + 2;
				ball.angle = 3 * Math.PI / 4 + Math.random() * Math.PI / 2;
				ball.anim = 1;
			}
			if (pad2 instanceof ComputerPad) {
				ball.x = pad1.x + pad1.width - 2;
				ball.angle = -1 * Math.PI / 4 + Math.random() * Math.PI / 2;
				ball.anim = 1;

				ball.newY = pad1.y + pad1.height / 2 - (ball.size + 4) / 2;
				l = 10;
			}
			ball.sin = Math.sin(ball.angle);
			ball.cos = Math.cos(ball.angle);
			ball.canPad1Rico = true;
			ball.canPad2Rico = true;
			newGame = false;
		}
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 400 && Mouse.y <= 400 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				trow = true;
				enter = false;
			}
		}
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				pad1.score = 0;
				pad2.score = 0;
				pad1.y = 320 - pad1.height / 2;
				pad2.y = 320 - pad2.height / 2;
				ball.spawnBall(pad1, pad2);
				newGame = true;
				PingPong.state = PingPong.State.MODE;
			}
		}
	}

	public void render(Graphics g, Pad pad1, Pad pad2, Field field) {
		field.render(g, pad1, pad2);
		g.setColor(new Color(0, 64, 0));
		g.fillRect(854 / 2 - 10, 0, 20, 640);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 35));
		if (l > 5) {
			g.setColor(Color.RED);
			g.drawString("The red player starts the game",
					854 / 2 - g.getFontMetrics().stringWidth("The red player starts the game") / 2,
					160 + g.getFontMetrics().getHeight() / 2 + 80);
		} else {
			g.setColor(new Color(0, 128, 255));
			g.drawString("The blue player starts the game",
					854 / 2 - g.getFontMetrics().stringWidth("The blue player starts the game") / 2,
					160 + g.getFontMetrics().getHeight() / 2 + 80);
		}
		g.setColor(new Color(128, 128, 128));
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 400 && Mouse.y <= 400 + 50) {
			g.fillRect(854 / 2 - 125, 400, 250, 50);
		}
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			g.fillRect(854 / 2 - 125, 475, 250, 50);
		}
		g.setColor(Color.WHITE);
		g.drawRect(854 / 2 - 125, 400, 250, 50);
		g.drawRect(854 / 2 - 125, 475, 250, 50);

		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 30));
		g.drawString("Start", 854 / 2 - g.getFontMetrics().stringWidth("Start") / 2,
				400 + g.getFontMetrics().getHeight());
		g.drawString("Back To Menu", 854 / 2 - g.getFontMetrics().stringWidth("Back To Menu") / 2,
				475 + g.getFontMetrics().getHeight());
	}

}

class FinishMenu extends Menu {
	public void tick(Pad pad1, Pad pad2, Ball ball) {
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 400 && Mouse.y <= 400 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				pad1.score = 0;
				pad2.score = 0;
				PingPong.state = PingPong.State.READY;
			}
		} else if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475
				&& Mouse.y <= 475 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				pad1.score = 0;
				pad2.score = 0;
				pad1.y = 320 - pad1.height / 2;
				pad2.y = 320 - pad2.height / 2;
				ball.spawnBall(pad1, pad2);
				PingPong.state = PingPong.State.MENU;
			}
		}
	}

	public void render(Graphics g, Pad pad1, Pad pad2) {
		g.setColor(new Color(0, 64, 0));
		g.fillRect(854 / 2 - 10, 0, 20, 640);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 35));
		if (pad1.score == 10) {
			g.setColor(Color.RED);
			g.drawString("The red player has won the game",
					854 / 2 - g.getFontMetrics().stringWidth("The red player has won the game") / 2,
					160 + g.getFontMetrics().getHeight() / 2 + 80);
		} else {
			g.setColor(new Color(0, 128, 255));
			g.drawString("The blue player has won the game",
					854 / 2 - g.getFontMetrics().stringWidth("The blue player has won the game") / 2,
					160 + g.getFontMetrics().getHeight() / 2 + 80);

		}
		g.setColor(new Color(128, 128, 128));
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 400 && Mouse.y <= 400 + 50) {
			g.fillRect(854 / 2 - 125, 400, 250, 50);
		}
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			g.fillRect(854 / 2 - 125, 475, 250, 50);
		}
		g.setColor(Color.WHITE);
		g.drawRect(854 / 2 - 125, 400, 250, 50);
		g.drawRect(854 / 2 - 125, 475, 250, 50);

		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 30));
		g.drawString("Play Again", 854 / 2 - g.getFontMetrics().stringWidth("Play Again") / 2,
				400 + g.getFontMetrics().getHeight());
		g.drawString("Back To Menu", 854 / 2 - g.getFontMetrics().stringWidth("Back To Menu") / 2,
				475 + g.getFontMetrics().getHeight());
	}
}

class MainMenu extends Menu {

	public void tick() {
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 400 && Mouse.y <= 400 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				PingPong.state = PingPong.State.MODE;
			}
		} else if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475
				&& Mouse.y <= 475 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				PingPong.state = PingPong.State.CREDITS;
			}
		} else if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 + 50 + 25
				&& Mouse.y <= 475 + 50 + 25 + 50) {
			if (Mouse.click) {
				enter = true;
			}
			if (enter && !Mouse.click) {
				enter = false;
				PingPong.state = PingPong.State.HELP;
			}
		}
	}

	public void render(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 65));
		g.drawString("PING PONG", 854 / 2 - g.getFontMetrics().stringWidth("PING PONG") / 2, 120);
		g.setColor(new Color(128, 128, 128));
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 400 && Mouse.y <= 400 + 50) {
			g.fillRect(854 / 2 - 125, 400, 250, 50);
		}
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 && Mouse.y <= 475 + 50) {
			g.fillRect(854 / 2 - 125, 475, 250, 50);
		}
		if (Mouse.x >= 854 / 2 - 125 && Mouse.x <= 854 / 2 - 125 + 250 && Mouse.y >= 475 + 50 + 25
				&& Mouse.y <= 475 + 50 + 25 + 50) {
			g.fillRect(854 / 2 - 125, 475 + 50 + 25, 250, 50);
		}
		g.setColor(Color.WHITE);
		g.drawRect(854 / 2 - 125, 400, 250, 50);
		g.drawRect(854 / 2 - 125, 475, 250, 50);
		g.drawRect(854 / 2 - 125, 475 + 50 + 25, 250, 50);

		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 30));
		g.drawString("New Game", 854 / 2 - g.getFontMetrics().stringWidth("New Game") / 2,
				400 + g.getFontMetrics().getHeight());
		g.drawString("Credits", 854 / 2 - g.getFontMetrics().stringWidth("Credits") / 2,
				475 + g.getFontMetrics().getHeight());
		g.drawString("Help", 854 / 2 - g.getFontMetrics().stringWidth("Help") / 2,
				475 + 50 + 25 + g.getFontMetrics().getHeight());
	}

}

class Field {

	public void render(Graphics g, Pad pad1, Pad pad2) {
		g.setColor(new Color(0, 64, 0));
		g.fillRect(0, 0, 854, 640);
		g.setColor(Color.WHITE);
		g.drawLine(854 / 2, 0, 854 / 2, 640);
		g.setFont(new Font("Courier New", Font.TRUETYPE_FONT, 50));
		g.setColor(new Color(255, 128, 128));
		String score = String.format("%02d", pad1.score);
		g.drawString(score, 854 / 4 - g.getFontMetrics().stringWidth(score) / 2, 5 + g.getFontMetrics().getHeight());
		score = String.format("%02d", pad2.score);
		g.setColor(new Color(128, 128, 255));
		g.drawString(score, 3 * 854 / 4 - g.getFontMetrics().stringWidth(score) / 2,
				5 + g.getFontMetrics().getHeight());
	}

}

class Ball {

	double x = 180;
	int size = 32;
	double y = 640 / 2 - size / 2;
	double speed = 15;
	double angle = 4 * Math.PI / 6;
	double sin = Math.sin(angle);
	double cos = Math.cos(angle);

	int anim = 0;

	boolean hit = false;

	public void setAngle(double angle) {
		angle = angle % 360;
		while (angle < 0) {
			angle += Math.PI * 2;
		}
		this.angle = angle;
	}

	public Ball(Pad pad01, Pad pad02) {
		spawnBall(pad01, pad02);
	}

	public void spawnBall(Pad pad1, Pad pad2) {
		double l = Math.random() * 10;
		if (l > 5) {
			x = 854 / 2;
			y = 640 / 2 - size / 2;
			angle = 0;
			sin = Math.sin(angle);
			cos = Math.cos(angle);
		} else {
			x = 854 / 2 - size;
			y = 640 / 2 - size / 2;
			angle = Math.PI * 2;
			sin = Math.sin(angle);
			cos = Math.cos(angle);
		}
	}

	boolean canPad1Rico = true;
	boolean canPad2Rico = true;

	double timer = 0;
	boolean startTimer = false;

	boolean turn1 = false;
	boolean turn2 = false;

	int newY;

	public void tick(Pad pad1, Pad pad2) {
		x += speed * cos;
		y += speed * sin;
		if (startTimer) {
			timer += 1.0 / 60.0;
			if (timer >= 0.4) {
				anim = 0;
				timer = 0;
				startTimer = false;
			}
		}
		if (x <= -1 * size) {
			pad2.score++;
			pad1.y = 320 - pad1.height / 2;
			pad2.y = 320 - pad2.height / 2;
			turn2 = true;
		}

		if (x >= 854) {
			pad1.score++;
			pad1.y = 320 - pad1.height / 2;
			pad2.y = 320 - pad2.height / 2;
			newY = pad1.y + pad1.height / 2 - (size) / 2;
			pad2.st = true;
			turn1 = true;
		}

		if (x <= 0 + pad1.width + 10) {
			if (canPad1Rico) {
				canPad2Rico = true;
				if (y >= (int) pad1.y - size / 2 && y <= (int) pad1.y + pad1.height - size / 2) {
					if (pad1.direction == 1 && sin < 0) {
						sin *= -1;
					} else if (pad1.direction == -1 && sin > 0) {
						sin *= -1;
					}
					cos *= -1;
					canPad1Rico = false;
					newY = Detector.detectNewY(sin, cos, y);
					pad2.st = true;
					anim = 1;
					startTimer = true;
				}
			}
		}

		if (x >= 854 - pad2.width - size - 10) {
			if (canPad2Rico) {
				canPad1Rico = true;
				if (y >= (int) pad2.y - size / 2 && y <= (int) pad2.y + pad2.height - size / 2) {
					if (pad2.direction == 1 && sin < 0) {
						sin *= -1;
					} else if (pad2.direction == -1 && sin > 0) {
						sin *= -1;
					}
					cos *= -1;
					canPad2Rico = false;
					anim = 1;
					startTimer = true;
				}
			}
		}
		if (y >= 640 - size || y <= 0) {
			sin *= -1;
			anim = 2;
			startTimer = true;
		}
	}

	public void render(Graphics g) {
		g.setColor(Color.ORANGE);
		if (anim == 0) {
			g.fillOval((int) x, (int) y, size, size);
		}
		if (anim == 1) {
			g.fillRoundRect((int) x + 2, (int) y - 2, size - 4, size + 4, 35, 35);
		}
		if (anim == 2) {
			g.fillRoundRect((int) x - 2, (int) y + 2, size + 4, size - 4, 35, 35);
		}
	}

}

class Pad {

	int score;
	int x = 0;
	int speed = 20;
	int width = 32;
	int height = 96;
	int y = 320 - height / 2;
	int direction = 0;
	Color color;

	public Pad(Color color) {
		this.color = color;
	}

	public void tick(Ball ball) {

	}

	public void tick(boolean up, boolean down) {
		direction = 0;
		if (up) {
			y -= speed;
			direction = -1;
		}
		if (down) {
			y += speed;
			direction = 1;
		}
		if (y <= 0) {
			y = 0;
		}
		if (y >= (640 - height)) {
			y = (640 - height);
		}

	}

	public void tick() {

	}

	boolean fi = false;
	int d = 0;
	int steps = 0;
	int s = 0;
	boolean st = true;

	public void render(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(color);
		g2d.fillRoundRect(x, y, width, height, 35, 50);
	}

}

class Keyboard extends KeyAdapter {

	public static boolean[] keys = new boolean[256];

	@Override
	public void keyPressed(KeyEvent e) {
		keys[e.getKeyCode()] = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keys[e.getKeyCode()] = false;
	}

}

class Mouse extends MouseAdapter {

	static int x;
	static int y;

	static boolean click = false;

	@Override
	public void mouseMoved(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		click = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		click = false;
	}

}

class Detector {

	public static int detectNewY(double sin, double cos, double y) {
		int t = (int) Math.abs((2 * (640 - 32) * cos / sin));
		int dx;
		if (sin <= 0) {
			dx = (int) Math.abs((640 - 32 - y) * cos / sin);
		} else {
			dx = (int) Math.abs(y * cos / sin);
		}
		int xs = 10 + 32 - dx;
		xs = 0 - xs;
		int newX = (854 - 32 - 10 - 32) + xs;
		int xInT = newX % t;
		int newY;
		if (xInT <= (t / 2)) {
			if (sin <= 0) {
				newY = 640 - 32 - (int) (xInT * Math.abs(sin) / Math.abs(cos));
			} else {
				newY = (int) (xInT * Math.abs(sin) / Math.abs(cos));
			}

		} else {
			xInT = newX - (t / 2);
			if (sin <= 0) {
				newY = (int) (xInT * Math.abs(sin) / Math.abs(cos));
			} else {
				newY = 640 - 32 - (int) (xInT * Math.abs(sin) / Math.abs(cos));
			}
		}
		return newY;
	}

}

class ComputerPad extends Pad {

	public ComputerPad(Color color) {
		super(color);
		speed = 10;
	}

	public void tick(Ball ball) {
		if (st) {
			d = ball.newY - y;
			steps = d / (speed / 3);
			st = false;
			fi = false;
			s = 0;
		}
		if (!fi) {
			if (s < Math.abs(steps)) {
				y += Math.signum(d) * speed / 3;
				s++;
			}
			if (s >= Math.abs(steps)) {
				s = 0;
				fi = true;
			}
		}
		if (y <= 0) {
			y = 0;
		}
		if (y >= (640 - height)) {
			y = (640 - height);
		}
	}

}