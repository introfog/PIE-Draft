package com.introfog;

import javax.swing.*;

public class Main {
	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 600;
	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Demo PIE");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize (WINDOW_WIDTH, WINDOW_HEIGHT);
		frame.add(new Display ());
		frame.setVisible(true);
	}
}