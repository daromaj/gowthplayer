package com.dmajewski.gow.game;

import org.junit.Test;

public class WindowsPlayerTest {

	@Test
	public void testIsMainScreen() throws Exception {
		System.out.println("isMainScreen : " + WindowsPlayer.isMainScreenActive());
	}
	
	@Test
	public void testIfNoMoreMovesLeft() throws Exception {
		while(true){
			System.out.println("Is no more moves left: " + WindowsPlayer.noMoreMovesLeft());
			Thread.sleep(2000);
		}
	}

}
