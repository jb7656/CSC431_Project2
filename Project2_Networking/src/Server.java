import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Server 
{
	static ServerSocket clientSocket;
	static ServerSocket incomingSocket;
	static Socket outgoingSocket;
	
	public static int SERVER_NUMBER;
	public static byte[] client_buffer;
	public static byte[] server_buffer;
	static int clientprocess_flag;
	static int serverprocess_flag;
	public static int[] page_table;
	public static InetAddress[] ip_addresses;
	
	public static void setupServer()
	{
		try {
			clientSocket = new ServerSocket(4446); //connect to local ip
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			incomingSocket = new ServerSocket(4450); //connect to local ip
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		clientprocess_flag = 0;
		serverprocess_flag = 0;
		client_buffer = new byte[10];
		server_buffer = new byte[10];
		page_table = new int[4];
		ip_addresses = new InetAddress[5];
		try {
			ip_addresses[0] = InetAddress.getByName("127.0.0.1");
			ip_addresses[1] = InetAddress.getByName("157.160.37.111");
			ip_addresses[2] = InetAddress.getByName("157.160.37.112");
			ip_addresses[3] = InetAddress.getByName("157.160.37.110");
			ip_addresses[4] = InetAddress.getByName("157.160.37.109");
		}
		catch (UnknownHostException e)
		{
			System.out.println("Ip addresses invalid");
		}
		System.out.println("Initial setup: ");
		System.out.print("Enter this servers number: ");
		Scanner s = new Scanner(System.in);
		SERVER_NUMBER = s.nextInt();
		ScanPageTable(SERVER_NUMBER);
	}
	
	private static void ScanPageTable(int sERVER_NUMBER2) 
	{
		String filename = "src/" + SERVER_NUMBER + ".txt";
		Scanner scan = null;
		try {
			scan = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i = 0;
		while(scan.hasNextInt())
		{
			page_table[i] = scan.nextInt();
			i++;
		}
	}
	
	public static void waitForClientMessage() throws IOException
	{
		DataInputStream in;
		byte[] b = new byte[20];
		Socket s;
		String received_input;
		String print_message;
		while(true)
		{
			s = clientSocket.accept();
			in = new DataInputStream(s.getInputStream());
			in.read(b);
			received_input = new String(b);
			print_message = received_input.substring(3, 10);
			System.out.println("server got: " + print_message + " from client");
			for(int i = 0; i < 10; i++)
			{
				client_buffer[i] = b[i];
			}
			clientprocess_flag = 1;
		}
	}
	
	public static void waitForServerMessage() throws IOException
	{
		DataInputStream in;
		byte[] b = new byte[20];
		Socket s;
		String received_input;
		while(true)
		{
			s = incomingSocket.accept();
			in = new DataInputStream(s.getInputStream());
			in.read(b);
			received_input = new String(b);
			for(int i = 0; i < 10; i++)
			{
				server_buffer[i] = b[i];
			}
			serverprocess_flag = 1;
			System.out.println("Server saw this from other server : " + received_input);
		}
	}
	public static void ProcessMessages() throws InterruptedException
	{
		while(true)
		{
			//System.out.println("Loop: " + clientprocess_flag);
			if(clientprocess_flag == 1)
			{
				byte sum = 0;
				for(int i = 3; i < client_buffer.length; i++)
				{
					sum += client_buffer[i];
				}
				if (sum != client_buffer[2])
				{
					System.out.println("Message is corrupt!");
					clientprocess_flag = 0;
				}
				else
				{
					SendMessage(client_buffer);
					clientprocess_flag = 0;
				}
			}
			else if (serverprocess_flag == 1)
			{
				byte sum = 0;
				for(int i = 3; i < server_buffer.length; i++)
				{
					sum += server_buffer[i];
				}
				
				if (sum != server_buffer[2])
				{
					System.out.println("Message is corrupt!");
					serverprocess_flag = 0;
				}
				else
				{
					SendMessage(server_buffer);
					serverprocess_flag = 0;
				}
			}
			Thread.sleep(5);
		}
	}
	
	private static void SendMessage(byte[] b) 
	{
		System.out.print("Sending Message from client: " + b[0]);
		int destination = page_table[b[1]-1];
		InetAddress out_addr = ip_addresses[destination];
		//System.out.println("destination: " + destination);
		DataOutputStream out; 
		try {
			if(destination  == 0)
			{
				System.out.print(" to my client");
				System.out.println();
				outgoingSocket = new Socket(out_addr, 4447);
			}
			else 
			{
				System.out.print(" to server: " + destination);
				System.out.println();
				outgoingSocket = new Socket(out_addr, 4450);
			}
			
			out = new DataOutputStream(outgoingSocket.getOutputStream());
			out.flush();
			out.write(b);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args)
	{
		setupServer();
		
		write_thread2 wt = new write_thread2();
		wt.start();
		
		receive_client recv1 = new receive_client();
		recv1.start();
		
		receive_router recv2 = new receive_router();
		recv2.start();
		
	}
}


final class receive_client extends Thread 
{
    public void run() {
        try 
        {
			Server.waitForClientMessage();
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
    }
}
final class receive_router extends Thread 
{
    public void run() {
        try 
        {
			Server.waitForServerMessage();
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
    }
}
final class write_thread2 extends Thread 
{
    public void run() 
    {
    	   try {
			Server.ProcessMessages();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}