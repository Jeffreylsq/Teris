package com.tarena.tetris;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** 
 * 俄罗斯方块 
 */
public class Tetris extends JPanel{
	/** 当前游戏的工作状态 */
	private int state;
	public static final int RUNNING = 0;
	public static final int PAUSE = 1;
	public static final int GAME_OVER = 2;
	
	/** 分数 */
	private int score;
	/** 行数 */
	private int lines;
	/** 级别 */
	private int level;

	private long index = 0;
	
	private int speed = 40;
	/** 行数 */
	public static final int ROWS = 20;
	/** 列数 */
	public static final int COLS = 10;
	/** 方块墙 */
	private Cell[][] wall = new Cell[ROWS][COLS];
	/** 正在下落的方块 */
	private Tetromino tetromino;
	/** 下一个出场的方块 */
	private Tetromino nextOne;
	
	/** 在Tetris类中添加 */
	private static BufferedImage background;
	public static BufferedImage T;
	public static BufferedImage S;
	public static BufferedImage Z;
	public static BufferedImage L;
	public static BufferedImage J;
	public static BufferedImage O;
	public static BufferedImage I;
	public static BufferedImage gameOver;
	public static BufferedImage pause;
	
	/** 在Tetris中添加 */
	private Timer timer;

	/** 静态代码块会在类加载期间执行 */
	/** 利用Java的API 将图片读为内存对象 */
	static{
		try{
			//Tetris 与 tetris.png 在同一个package中
			background = ImageIO.read( 
				Tetris.class.getResource("tetris.png"));
			T = ImageIO.read(
					Tetris.class.getResource("T.png")); 
			S = ImageIO.read(
					Tetris.class.getResource("S.png")); 
			Z = ImageIO.read(
					Tetris.class.getResource("Z.png")); 
			J = ImageIO.read(
					Tetris.class.getResource("J.png")); 
			L = ImageIO.read(
					Tetris.class.getResource("L.png")); 
			O = ImageIO.read(
					Tetris.class.getResource("O.png")); 
			I = ImageIO.read(
					Tetris.class.getResource("I.png"));
			gameOver = ImageIO.read(
					Tetris.class.getResource("game-over.png")); 
			pause = ImageIO.read(
					Tetris.class.getResource("pause.png")); 

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/** 软件的启动方法 action:动作，活动 */
	public void action(){
		// randomOne 工厂方法，用于生成方块
		// 工厂方法：用于创建对象的方法
		tetromino = Tetromino.randomOne();
		nextOne = Tetromino.randomOne();
	//	wall[10][4] = new Cell(2,4,T);
		state = RUNNING;
		KeyListener l = new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
				//按键按下
				int key = e.getKeyCode();
				switch(state){
				case RUNNING:processRunningKey(key);
					break;
				case PAUSE:processPauseKey(key);
					break;
				case GAME_OVER:processGameOverKey(key);
					break;
				}
				//re重新  paint绘制  重绘：尽快执行paint
				Tetris.this.repaint();
			}
		};
		//Tetris 是面板类型 l 监听 面板的动作
		this.addKeyListener(l);
		this.setFocusable(true);
		//request 请求  Focus 焦点  
		this.requestFocus();
		//在action方法中添加
		timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				//下落速度控制逻辑
				speed = 40-(score/1000);
				speed = speed<=1? 1: speed;
				level = 41-speed;
				if(index % speed==0){
					if(state==RUNNING){
						softDropAction();
					}
				}
				index ++;
				repaint();
			}
		};
		timer.schedule(task, 10, 10);
	}
	/** 在Tetris类中添加方法 */
	protected void processGameOverKey(int key) {
		switch(key){
		case KeyEvent.VK_Q: System.exit(0);break;
		case KeyEvent.VK_S: 
			wall=new Cell[ROWS][COLS];
			tetromino = Tetromino.randomOne();
			nextOne = Tetromino.randomOne();
			score = 0;
			lines = 0;
			index=0;
			state = RUNNING;
		}
	}
	protected void processPauseKey(int key) {
		switch(key){
		case KeyEvent.VK_Q: System.exit(0);break;
		case KeyEvent.VK_C: 
			state=RUNNING;
			index = 1;
			break;
		}
	}
	/** 处理Running状态的按键情况 */
	protected void processRunningKey(int key){
		switch(key){
		case KeyEvent.VK_RIGHT:moveRightAction();break;
		case KeyEvent.VK_LEFT:moveLeftAction();break;
		case KeyEvent.VK_DOWN:softDropAction();break;
		case KeyEvent.VK_UP:rotateRightAction();break;
		case KeyEvent.VK_Z:rotateLeftAction();break;
		case KeyEvent.VK_SPACE:hardDropAction();break;
		case KeyEvent.VK_Q:System.exit(0);break;
		case KeyEvent.VK_P:state=PAUSE;break;
		}
	}


	/** 在Tetris类中，利用重写修改JPanel的
	 *  paint()方法修改原有的paint()方法 
	 *  g 引用了绑定到当前面板上的画笔
	 **/
	public void paint(Graphics g) {
		//paint: 涂， draw：绘
		//修改原有的绘制，变成自定义绘制！
		g.drawImage(background, 0, 0, null);
		//g.drawString("Hello World", 100, 50);
		//画墙，将绑定到当前面板的画笔传递给了
		//paintWall(g) 因为是同一个画笔， 所以
		//paintWall会在同一个面板上绘制墙。
		// g 的类型 Graphics
		g.translate(15, 15);//坐标系平移x+15 y+15
		paintWall(g);
		//this 的类型 Tetris
		this.paintTetromino(g);
		paintNextOne(g);
		// 在paint方法中增加分数的绘制
		paintScore(g);
		//绘制游戏的状态
		paintState(g);
	}
	/** 绘制游戏的状态 */
	private void paintState(Graphics g) {
		switch (state) {
		case PAUSE:
			g.drawImage(pause, -15, -15, null);
			break;
		case GAME_OVER:
			g.drawImage(gameOver, -15, -15, null);
			break;
		}
	}
	private void paintScore(Graphics g) {
		//在合适的地方绘制分数
		int x = 292;
		int y = 162;
		int color = 0xffffff;//0x667799
		g.setColor(new Color(color));
		Font f = 
			new Font(Font.SERIF,Font.BOLD,30);
		g.setFont(f);
		g.drawString("SCORE:"+score,x,y);
		y+=56;
		g.drawString("LINES:"+lines,x,y);
		y+=56;
		g.drawString("LEVEL:"+level,x,y);
		x = 290;
		y = 160;
		color = 0x667799;
		//            R G B
		g.setColor(new Color(color));
		g.setFont(f);
		g.drawString("SCORE:"+score,x,y);
		y+=56;
		g.drawString("LINES:"+lines,x,y);
		y+=56;
		g.drawString("LEVEL:"+level,x,y);
	}

	private void paintNextOne(Graphics g) {
		if(nextOne==null)
			return;
		Cell[] cells = nextOne.cells;
		for(int i=0; i<cells.length; i++){
			Cell c = cells[i];
			int row = c.getRow() + 1;
			int col = c.getCol() + 10;
			int x = col * CELL_SIZE;
			int y = row * CELL_SIZE;
			g.drawImage(c.getImage(), x, y, null);
		}
	}

	private void paintTetromino( Graphics g){
		//cells 引用了正在下落方块的4个格子数组
		if(tetromino==null)
			return;
		Cell[] cells = tetromino.cells;
		for(int i=0; i < cells.length; i++){
			//c 是正在下落方块的每个格子的引用
			Cell c = cells[i];
			int col = c.getCol();
			int row = c.getRow();
			int x = col * CELL_SIZE;
			int y = row * CELL_SIZE;
			g.drawImage(c.getImage(), x, y, null);
		}
	}
	
	public static final int CELL_SIZE = 26;
	//是封装复杂的画墙逻辑！
	private void paintWall(Graphics g){
		for(int row=0; row<ROWS; row++){
			//row = 0 1 2
			for(int col=0; col<COLS; col++){
				//col=0 1 ... 9
				Cell cell = wall[row][col];
				int x = col * CELL_SIZE;
				int y = row * CELL_SIZE;
				if(cell==null){
					//g.drawRect(x,y,CELL_SIZE,CELL_SIZE);
				}else{
					g.drawImage(cell.getImage(),x,y,null);
				}
			}
		}
	}
	
	/**Tetris类中添加方法，实现右移动流程控制 */
	public void moveRightAction(){
		tetromino.moveRight();
		//concide()重合，下落方块的某个格子与墙上
		//的格子重合
		if(outOfBounds() || concide()){
			tetromino.moveLeft();
		}
	}
	/** Tetris 添加旋转流程控制*/
	public void rotateRightAction(){
		tetromino.rotateRight();
		if(outOfBounds() || concide()){
			tetromino.rotateLeft();
		}
	}
	public void rotateLeftAction(){
		tetromino.rotateLeft();
		if(outOfBounds() || concide()){
			tetromino.rotateRight();
		}
	}

	/** 越俎代庖 */
	private boolean concide() {
		Cell[] cells = tetromino.cells;
		for(int i=0; i<cells.length; i++){
			Cell cell = cells[i];
			int row = cell.getRow();
			int col = cell.getCol();
			if(wall[row][col]!=null){
				return true;
			}
		}
		return false;
	}

	public void moveLeftAction(){
		tetromino.moveLeft();
		if(outOfBounds() || concide()){
			tetromino.moveRight();
		}
	}
	/** 检查正在下落的方块是否出界了
	 * 返回true表示出界了，否则没有出界 */
	private boolean outOfBounds() {
		Cell[] cells = tetromino.cells;
		for(int i=0; i<cells.length; i++){
			Cell c = cells[i];
			int row = c.getRow();
			int col = c.getCol();
			if(row<0||row>=ROWS||col<0||col>=COLS){
				return true;
			}
		}
		return false;
	}
	public void hardDropAction(){
		while(canDrop()){
			tetromino.softDrop();
		}
		landIntoWall();
		int lines = destroyLines() ;
		
		this.lines+=lines;
		score+=scoreTable[lines];
		
		if(isGameOver()){ 
			//System.out.println("C U!"); 
			state = GAME_OVER;
		}else{
			tetromino = nextOne;
			nextOne = Tetromino.randomOne();
		}
	}
	private int[] scoreTable = 
					{0,10,50,80,200};
	//       0  1  2  3  4
	/** Tetris 类中添加方法 */
	public void softDropAction() {
		if (canDrop()) {
			tetromino.softDrop();
		} else {
			landIntoWall();
			int lines = destroyLines();// 0 ~ 4
			this.lines += lines;
			score += scoreTable[lines];
			if (isGameOver()) {
				// System.out.println("C U!");
				state = GAME_OVER;
			} else {
				tetromino = nextOne;
				nextOne = Tetromino.randomOne();
			}
		}
	}

	private boolean canDrop() {
		Cell[] cells = tetromino.cells;
		for (int i = 0; i < cells.length; i++) {
			Cell cell = cells[i];
			int row = cell.getRow();
			int col = cell.getCol();
			if(row==(ROWS-1)){
				return false;
			}
		}
		for (int i = 0; i < cells.length; i++) {
			Cell cell = cells[i];
			int row = cell.getRow();
			int col = cell.getCol();
			if(wall[row+1][col]!=null){
				return false;
			}
		}
		return true;
	}

	private void landIntoWall() {
		Cell[] cells = tetromino.cells;
		for (int i = 0; i < cells.length; i++) {
			Cell cell = cells[i];
			int row = cell.getRow();
			int col = cell.getCol();
			wall[row][col] = cell;
		}
	}

	private int destroyLines() {
		int lines = 0;
		for(int row=0; row<ROWS; row++){
			if(fullCells(row)){
				deleteRow(row);
				lines++;
			}
		}
		return lines;
	}
	private void deleteRow(int row) {
		for(int i=row; i>=1; i--){
			System.arraycopy(wall[i-1], 0,
					wall[i], 0, COLS);
		}
		Arrays.fill(wall[0], null);
	}

	private boolean fullCells(int row) {
		Cell[] line = wall[row];
		for (int i = 0; i < line.length; i++) {
			Cell cell = line[i];
			if(cell==null){
				return false;
			}
		}
		return true;
	}

	private boolean isGameOver() {
		Cell[] cells = nextOne.cells;
		for (int i = 0; i < cells.length; i++) {
			Cell cell = cells[i];
			int row = cell.getRow();
			int col = cell.getCol();
			if(wall[row][col]!=null){
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		//创建了窗口框对象
		JFrame frame = new JFrame();
		//JPanel panel = new JPanel();
		//panel.setBackground(Color.blue);
		//画框添加面板
		//frame.add(panel);
		//创建对象之前要先加载类
		//在加载类期间，初始化静态变量，执行静态
		//代码块，Tetris中静态代码块加载了图片
		Tetris tetris = new Tetris();
		//tetris.setBackground(Color.BLACK);
		frame.add(tetris);
		frame.setSize(525, 550);
		frame.setLocationRelativeTo(null);
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(
				JFrame.EXIT_ON_CLOSE);
		//显示窗口时候会尽快调用面板的paint()方法
		//绘制显示的内容（绘制背景等）
		frame.setVisible(true);
		//被重写的paint（）方法绘制了图片！
		tetris.action();
	}
}
