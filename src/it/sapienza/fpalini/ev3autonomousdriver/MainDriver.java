package it.sapienza.fpalini.ev3autonomousdriver;

import java.io.IOException;
import java.net.UnknownHostException;

import lejos.hardware.Button;
import lejos.utility.Delay;

public class MainDriver {

	public static void main(String[] args) throws UnknownHostException, IOException {
		
		System.out.println("Press a button to start...");
		
		int buttonID = Button.waitForAnyPress();
		
		if(buttonID == Button.ID_ESCAPE) 
			return;
		
		System.out.println("Let's drive!");
		
		Delay.msDelay(2000);
		
		new Driver().drive();
	}
}
