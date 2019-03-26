import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;

public class Client 
{
	static Socket clientSocket;
	static ServerSocket serverSocket;
	public static byte CLIENT_NUMBER;
	public static byte[] temp;
	static int send_corrupt;
	
	public static void main(String[] args)
	{
		setupClient();
		write_thread1 write = new write_thread1();
		write.start();
		
		receive_thread1 recv = new receive_thread1();
		recv.start();
	}
	
	public static void setupClient()
	{
		try {
			clientSocket = new Socket("127.0.0.1", 4446); //connect to local ip
			serverSocket = new ServerSocket(4447); //connect to local ip
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.print("Enter this clients number: ");
		Scanner s = new Scanner(System.in);
		CLIENT_NUMBER = s.nextByte();
		temp = new byte[10];
		System.out.println("===========================================");
	}
	
	public static void writeMessage() throws IOException
	{
		byte[] b = new byte[7];
		Scanner s;
		DataOutputStream out; 
		byte destination;
		while(true)
		{
			s = new Scanner(System.in);
			System.out.print("Write the message to send:");
			b = s.nextLine().getBytes();
			System.out.print("Write the destination number(1-4): ");
			destination = s.nextByte();
			System.out.print("Enter 0 for normal message, 1 for corrupt message: ");
			send_corrupt = s.nextInt();

			b = formatMessage(b, destination);
			out = new DataOutputStream(clientSocket.getOutputStream());
			out.flush();
			out.write(b);
			try {
				Thread.sleep(1100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clientSocket.close();
			clientSocket = new Socket("127.0.0.1", 4446);
		}
	}
	public static byte[] formatMessage(byte[] b, byte destination)
	{
		byte[] returnArray = new byte[10];
		byte sum = 0;
		returnArray[0] = CLIENT_NUMBER;
		returnArray[1] = destination;
		
		for(int i = 0; i < b.length; i++)
		{
			returnArray[i+3] = b[i];
			sum += b[i];
		}
		if(send_corrupt == 0 )
		{
			returnArray[2] = sum;
		}
		else
		{
			returnArray[2] = 0;
		}
		
		for(int i = 0; i < returnArray.length; i++)
		{
			//System.out.println(i + ": " + returnArray[i]);
		}
		return returnArray;
	}
	public static void waitForMessage() throws IOException
	{
		DataInputStream in;
		byte[] b = new byte[20];
		Socket s;
		String received_input;
		String print_message;
		while(true)
		{
			s = serverSocket.accept();
			in = new DataInputStream(s.getInputStream());
			in.read(b);
			received_input = new String(b);
			print_message = received_input.substring(3, 10);
			System.out.println("Client got: " + print_message);
		}
	}
}

final class receive_thread1 extends Thread 
{
    public void run() 
    {
        try 
        {
			Client.waitForMessage();
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
    }
}
final class write_thread1 extends Thread 
{
    public void run() {
    	    try 
    	    {
				Client.writeMessage();
			} 
    	    catch (IOException e) 
    	    {
				e.printStackTrace();
			}
    }
}
