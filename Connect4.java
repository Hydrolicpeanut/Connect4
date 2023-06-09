import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class Connect4 extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

	private static final int WIDTH, HEIGHT, wUnit, hUnit, bLen, bTop;

	private static JFrame frame;

	private static Connect4 con4;

	private static Point point1, point2;

	public static void main(String[] args) {
		con4 = new Connect4();
	}

	public Connect4() {
		setBackground(new Color(.1f,.2f,.6f));

		frame = new JFrame("Connect 4");
		frame.setBounds(50, 50, WIDTH, HEIGHT);
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		javax.swing.Timer timer = new javax.swing.Timer(10, this);
		timer.start();

		frame.addMouseListener(this);
		frame.addMouseMotionListener(this);
		
	}

	static {
		int initialWidth = 1000;
		int initialHeight = 600;
		bLen = 7;
		bTop = 6;
		wUnit = initialWidth / (bLen + 2);
		WIDTH = wUnit * (bLen + 2);
		hUnit = initialHeight / (bTop + 2);
		HEIGHT = hUnit * (bTop + 2);
	}
	
	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Board.draw(g);
	}

	public void mouseMoved(MouseEvent e) {
		Board.hoverStatus(e.getX());
	}

	public void mousePressed(MouseEvent e) {
		Board.drop();
	}

	public void mouseReleased(MouseEvent e) {
		//null
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseDragged(MouseEvent e) {}

	static class twoPoints {

		public Point point1, point2;

		twoPoints(int x1, int y1, int x2, int y2) {
			point1 = new Point(x1, y1);
			point2 = new Point(x2, y2);
		}
	}

	static class Board {
		static Color[][] board;
		static Color[] playerColors;
		static int turn;
		static int locX, locY;
		static boolean stateOfTheGame;

		static {
			board = new Color[bLen][bTop];

			for (Color[] colors: board) {
				Arrays.fill(colors, Color.WHITE);
			}

			playerColors = new Color[] {
				Color.YELLOW, Color.RED
			};
			turn = 0;
		}

		public static void draw(Graphics g) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ((Graphics2D)(g)).setStroke(new BasicStroke(2.0f));


			for (int i = wUnit; i <= WIDTH - wUnit; i += wUnit) {
				g.setColor(Color.BLACK);
				g.drawLine(i, hUnit, i, HEIGHT - hUnit);

				if (i == WIDTH - wUnit) continue;

				for (int j = hUnit; j < HEIGHT - hUnit; j += hUnit) {
					g.setColor(board[i / wUnit - 1][j / hUnit - 1]);
					g.fillOval(i + 5, j + 5, wUnit - 10, hUnit - 10);
					g.setColor(Color.BLACK);
					g.drawOval(i + 5, j + 5, wUnit - 10, hUnit - 10);
				}
			}

			g.drawLine(wUnit, HEIGHT - hUnit, WIDTH - wUnit, HEIGHT - hUnit);
			
			if (stateOfTheGame) {
			    g.setColor(Color.WHITE);
			} else {
			    g.setColor(playerColors[turn]);
			}			
			g.fillOval(locX + 5, locY + 5, wUnit - 10, hUnit - 10);
			g.setColor(Color.BLACK);
			g.drawOval(locX + 5, locY + 5, wUnit - 10, hUnit - 10);

			g.setColor(Color.BLACK);

			if (point1 != null) {
			    if (point2 != null) {
			        g.drawLine(point1.x, point1.y, point2.x, point2.y);
			    }
			}

		}

		public static void hoverStatus(int x) {
			x -= x % wUnit;

			if (x < wUnit) x = wUnit;

			if (x >= WIDTH - wUnit) x = WIDTH - 2 * wUnit;
			locX = x;
			locY = 0;
		}

		public static void drop() {
			if (board[locX / wUnit - 1][0] != Color.WHITE) {
				return;
				}
			new Thread(() -> {
				Color color = playerColors[turn];
				int curlocX = locX;
				int i;

				for (i = 0; i < board[curlocX / wUnit - 1].length && board[curlocX / wUnit - 1][i] == Color.WHITE; i++) {
					board[curlocX / wUnit - 1][i] = color;
					try {
						//Thread.currentThread().wait(200);
						Thread.sleep(200);
					} catch (Exception ignored) {}

					board[curlocX / wUnit - 1][i] = Color.WHITE;

					if (stateOfTheGame) return;
				}

				if (stateOfTheGame) return;
				board[curlocX / wUnit - 1][i - 1] = color;
				checkConnect(curlocX / wUnit - 1, i - 1);
			}).start();
			try {
				Thread.currentThread().sleep(100);
			} catch (Exception ignored) {}

			if (stateOfTheGame) { 
				return;
			}
			turn = (turn + 1) % playerColors.length;
		}

		public static void checkConnect(int x, int y) {
			if (stateOfTheGame) return;

			twoPoints winCon = look4TheWin(board, x, y);

			if (winCon != null) {
				point1 = new Point((winCon.point1.x + 1) * wUnit + wUnit / 2, (winCon.point1.y + 1) * hUnit + hUnit / 2);
				point2 = new Point((winCon.point2.x + 1) * wUnit + wUnit / 2, (winCon.point2.y + 1) * hUnit + hUnit / 2);
				frame.removeMouseListener(con4);
				stateOfTheGame = true;
			}
		}

		public static twoPoints look4TheWin(Color[][] colorArray, int i, int j) {
			Color colorCheck = colorArray[i][j];
			int L, R, U, D;

			// check horizontally L to R
			L = R = i;

			while (L >= 0 && colorArray[L][j] == colorCheck) L--;
			L++;

			while (R < colorArray.length && colorArray[R][j] == colorCheck) R++;
			R--;

			if (R - L >= 3) {
				return new twoPoints(L, j, R, j);
			}

			// check vertically U to bottom
			D = j;

			while (D < colorArray[i].length && colorArray[i][D] == colorCheck) D++;
			D--;

			if (D - j >= 3) {
				return new twoPoints(i, j, i, D);
			}

			// check diagonal top L to bottom R
			L = R = i;
			U = D = j;

			while (L >= 0 && U >= 0 && colorArray[L][U] == colorCheck) {
				L--;
				U--;
			}

			L++;
			U++;

			while (R < colorArray.length && D  <  colorArray[R].length && colorArray[R][D] == colorCheck) {
				R++;
				D++;
			}

			R--;
			D--;

			if (R - L >= 3 && D - U >= 3) {
				return new twoPoints(L, U, R, D);
			}

			// check diagonal top R to bottom L
			L = R = i;
			U = D = j;

			while (L >= 0 && D  <  colorArray[L].length && colorArray[L][D] == colorCheck) {
				L--;
				D++;
			}

			L++;
			D--;

			while (R < colorArray.length && U >= 0 && colorArray[R][U] == colorCheck) {
				R++;
				U--;
			}

			R--;
			U++;

			if (R - L >= 3 && D - U >= 3) {
				return new twoPoints(L, D, R, U);
			}

			return null;
		}

	}
}