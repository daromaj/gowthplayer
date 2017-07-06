package com.dmajewski.gow.game;

public class Move {
	int x;
	int y;
	int direction; //0 - left; 1 - down
	int score;
	@Override
	public String toString() {
		return x + ", " + y + " " + (direction == 0 ? "right" : "down");
	}
}
