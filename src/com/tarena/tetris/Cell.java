package com.tarena.tetris;

import java.awt.image.BufferedImage;

/** 格子 */
public class Cell {
	private int row;
	private int col;
	/** 贴图 */
	private BufferedImage image;
	
	public Cell(
			int row, int col,
			BufferedImage image) {
		this.row = row;
		this.col = col;
		this.image = image;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}
	/** 下落 */
	public void softDrop(){
		this.row++;
	}
	public void moveRight(){
		col++;
	}
	public void moveLeft(){
		col--;
	}
	/** 重写Object的方法，便于调试软件！ */
	public String toString(){
		return row+","+col;
	}
}










