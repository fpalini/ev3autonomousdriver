package it.sapienza.fpalini.ev3autonomousdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Driver {
	public final String SERVER_NAME = "PLK-L01";
	
	private RegulatedMotor rightMotor, leftMotor, frontMotor;
	private Client client;
	private int speed;
	
	public Driver() {
		rightMotor = Motor.C;
		leftMotor  = Motor.B;
		frontMotor = Motor.A;
		
		rightMotor.synchronizeWith(new RegulatedMotor[]{ leftMotor });
		
		client = new Client("192.168.44.1", 4444);
	}
	
	public void drive() throws UnknownHostException, IOException
	{
		String command;
		String action;
		boolean isDriving = true;
		
		while(isDriving)
		{
			command = client.getCommand();
			action = command.split(" ")[0];
			
			switch (action) {
				case "MOVE":
					int speed = Integer.parseInt(command.split(" ")[1]);
					move(speed);
					break;
				case "TURN":
					int angle = Integer.parseInt(command.split(" ")[1]);
					turn(angle);
					break;
				case "STOP":
					stop();
					break;
				case "END":
					isDriving = false;
					break;
				default:
					break;
			}
			
			// System.out.println(command);
		}
		turn(0);
		stop();
	}
	
	public void move(int speed)
	{
		if(speed > 0 ) goForward(speed);
		else goBackward(-speed);
	}
	
	public void turn(int degree)
	{
		int currentAngle = frontMotor.getLimitAngle();
		degree = -currentAngle + degree;
		
		if(degree > 0) turnRight(degree);
		else turnLeft(-degree);
	}
	
	private void turnLeft(int degree) {
		int currentAngle = frontMotor.getLimitAngle();
		
		if (currentAngle - degree <= -90) frontMotor.rotate(-90-currentAngle);
		else frontMotor.rotate(-degree);
	}

	private void turnRight(int degree) {
		int currentAngle = frontMotor.getLimitAngle();
		
		if (currentAngle + degree >= 90) frontMotor.rotate(90-currentAngle);
		else frontMotor.rotate(degree);
	}
	
	private void goForward(int speed) {	
		rightMotor.startSynchronization();
		
		if(speed != this.speed)
		{
			rightMotor.setSpeed(speed);
			leftMotor.setSpeed(speed);
			
			this.speed = speed;
		}

		rightMotor.backward();
		leftMotor.backward();

		rightMotor.endSynchronization();
	}
	
	private void goBackward(int speed) {	
		rightMotor.startSynchronization();
		
		if(speed != this.speed)
		{
			rightMotor.setSpeed(speed);
			leftMotor.setSpeed(speed);
			
			this.speed = speed;
		}

		rightMotor.forward();
		leftMotor.forward();

		rightMotor.endSynchronization();
	}
	
	private void stop() {
		rightMotor.startSynchronization();
		
		rightMotor.stop();
		leftMotor.stop();
		
		rightMotor.endSynchronization();
	}
	
	private class Client{
		
		private String addr;
		private int port;
		
		public Client(String addr, int port)
		{
			this.addr = addr;
			this.port = port;
		}

		public String getCommand() throws UnknownHostException, IOException {
			Socket client;
			DataInputStream in;
			DataOutputStream out;
			
			String command = "";
			
			client = new Socket(addr, port);
				
			in = new DataInputStream(client.getInputStream());
			out = new DataOutputStream(client.getOutputStream());
			
			command = new String(in.readUTF());
			
			if(command.split(" ")[0].equals("MOVE")) out.writeUTF("STARTED");
			else if(command.equals("STOP")) out.writeUTF("STOPPED");
			else if(command.equals("END")) out.writeUTF("ENDED");
			else out.writeUTF("");
 
			client.close();
			
			return command;
		}
	}
}
