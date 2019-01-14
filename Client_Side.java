import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.*;
import java.util.Arrays;

public class NewClient {
	public static int convert_byte_value_to_integer(byte[] bytes) {
		return bytes[3] & 0xFF |  (bytes[2] & 0xFF) << 8 | (bytes[1] & 0xFF) << 16 | (bytes[0] & 0xFF) << 24;
	}

	public static byte[] convert_int_to_byte_4(int i_bit_integer) {
		byte buffer[] = new byte[4];
		buffer[0] = (byte) ((i_bit_integer & 0xFF000000) >> 24);
		buffer[1] = (byte) ((i_bit_integer & 0x00FF0000) >> 16);
		buffer[2] = (byte) ((i_bit_integer & 0x0000FF00) >> 8);
		buffer[3] = (byte) ((i_bit_integer & 0x000000FF) >> 0);
		return buffer;
	}

	public static void main(String args[])
			throws UnknownHostException, SocketException, IOException, FileNotFoundException {
		DatagramSocket sock = new DatagramSocket();
		byte reciever[] = new byte[1024];
		DatagramPacket recievePacket = new DatagramPacket(reciever, reciever.length);
		byte Packet_type_1[] = new byte[405];
		byte temper[] = new byte[4];

		int count = 0;
		int special_packet = 0;
		int time_out = 8;
		int time_out_checker = 1;
		int[][] f = new int[243][2];
		InetAddress inet = InetAddress.getByName("localhost");
		// function to extract the text file

		java.io.File file = new java.io.File("/Users/gaurav/Downloads/data01.txt");
		BufferedReader br= new BufferedReader(new FileReader(file));
		String st = " ";
		String str = " ";
		String string = " ";
		time_out_checker = 0;
		try {

			while ((st = br.readLine()) != null) {
				count = count + 1;
				Pattern p = Pattern.compile("(\\))");
				Matcher m = p.matcher(st);
				str = str + m.replaceAll(" ");
			}
			

			
			Pattern p = Pattern.compile("[-.\\d[^,(]]");
			Matcher m = p.matcher(str);
			while (m.find())
				string = string + m.group();
			string = string.trim();
			

			String[] strings = string.split(" ");
			
			int lolwa = 0;
			for (int i = 0; i < strings.length - 1; i++) {
				
				f[lolwa][0] = (int) (Float.parseFloat(strings[i]) * 100);

				f[lolwa][1] = (int) (Float.parseFloat(strings[i + 1]) * 100);
				
				i = i + 1;
				lolwa = lolwa + 1;
			}
		} catch (FileNotFoundException e) {
			System.err.format("File not found");
		}

		
		
		int num_packets = count / 50;
		
		int num_of_packets_less_than_50 = count % 50;
		if (num_of_packets_less_than_50 > 0) {
			special_packet = 1;
		}
		int total_to_send = num_packets + special_packet;
		
		int k = 0;
		int sequence = (int) (0 & 0xFFFF);
		int fifty1 = (int) (num_of_packets_less_than_50 & 0xFFFF);
		int fifty = (int) (50 & 0xFFFF);
		int all_complete_packet_done = 0;
		
		while (total_to_send > 0) {
			System.out.println("Preparing and sending packet number" + (sequence + 1));
			Packet_type_1[0] = (byte) 0;
			Packet_type_1[1] = (byte) 0;
			Packet_type_1[2] = (byte) sequence;
			if ((all_complete_packet_done == 1)) {
				Packet_type_1[3] = (byte) 0;
				Packet_type_1[4] = (byte) fifty1;
				
				for (int i = 5; i <= (num_of_packets_less_than_50 * 8); i = i + 4) {
					for (int j = 0; j < 2; j++) {
						
						byte temp[] = convert_int_to_byte_4(f[k][j]);
						Packet_type_1[i] = temp[0];
						Packet_type_1[i + 1] = temp[1];
						Packet_type_1[i + 2] = temp[2];
						Packet_type_1[i + 3] = temp[3];
						if (j == 0)
							i = i + 4;
						if (j == 1)
							k = k + 1;

					}

				}
			} else {
				Packet_type_1[3] = (byte) 0;
				Packet_type_1[4] = (byte) fifty;
				for (int i = 5; i <= (50 * 8); i = i + 4) {
					for (int j = 0; j < 2; j++) {
						byte temp[] = convert_int_to_byte_4(f[k][j]);
						Packet_type_1[i] = temp[0];
						Packet_type_1[i + 1] = temp[1];
						Packet_type_1[i + 2] = temp[2];
						Packet_type_1[i + 3] = temp[3];
						
						if (j == 0) {
							i = i + 4;

						}
						if (j == 1)
							k = k + 1;
					}

				}
				if (sequence + 1 == num_packets)
					all_complete_packet_done = 1;

			}
			System.out.println("Packet being send is :"+Arrays.toString(Packet_type_1));
			DatagramPacket sender = new DatagramPacket(Packet_type_1, Packet_type_1.length, inet, 517);
			sock.send(sender);
			sock.setSoTimeout(time_out);

			try {
				sock.receive(recievePacket);
				byte[] recieved = recievePacket.getData();
				if (recieved[0] == (byte) (1)) {
					if (recieved[1] == (sequence))

						System.out.println("Acknowledgement aquired and preparing next packet");

					sequence++;
					time_out_checker = 0;
					total_to_send--;

				}
			} catch (SocketTimeoutException e) {

				time_out_checker++;

				if (time_out_checker == 4) {
					System.out.println("Acknowledgement not recieved in time");
					System.exit(0);
				} else {
					time_out = 2 * time_out;
					sock.setSoTimeout(time_out);

					sock.send(sender);
					System.out.println("Resending the packet");
				}
			}
		}

		time_out = 8;
		System.out.println("Sending request to start the centroid  calculation");
		while (true) {
			{
				byte Packet_type_2[] = new byte[1];

				Packet_type_2[0] = (byte) 2;
				
				DatagramPacket req_to_server = new DatagramPacket(Packet_type_2, Packet_type_2.length, inet, 517);
				sock.send(req_to_server);
				sock.setSoTimeout(time_out);

				try {
					sock.receive(recievePacket);
					time_out = 8;
					time_out_checker = 0;
					byte[] recieved_ack_for_req = recievePacket.getData();
					if (recieved_ack_for_req[0] == (byte) (3)) {
						
						break;
					}
				} catch (SocketTimeoutException e) {
					time_out_checker = time_out_checker + 1;

					if (time_out_checker == 4) {
						System.out.println("Server Unreachable");
						System.exit(0);
					} else {
						time_out_checker = 2 * time_out_checker;
						sock.setSoTimeout(time_out_checker);

						sock.send(req_to_server);
					}
				}
			}
		}
		
		sock.setSoTimeout(30000);
		System.out.println("Waiting for centroid calculation from server");
		while (true) {
			int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
			try {
				sock.receive(recievePacket);
				byte[] cl_values = recievePacket.getData();
				if (cl_values[0] == (byte) 4) {
					for (int fev = 1, position = 0; fev < 16; fev = fev + 4) {

						temper[position] = cl_values[fev];
						temper[position + 1] = cl_values[fev + 1];
						temper[position + 2] = cl_values[fev + 2];
						temper[position + 3] = cl_values[fev + 3];
						position = 0;
						if (fev == 1) {
							x1 = convert_byte_value_to_integer(temper);
							
						} else if (fev == 5)
							y1 = convert_byte_value_to_integer(temper);
						else if (fev == 9)
							x2 = convert_byte_value_to_integer(temper);
						else
							y2 = convert_byte_value_to_integer(temper);
					}
					float fx1, fx2, fy1, fy2;
					fx1 = x1 / 100f;
					fy1 = y1 / 100f;
					fx2 = x2 / 100f;
					fy2 = y2 / 100f;
					System.out.println("First set of centroid value is , x: " + fx1 + " y: " + fy1);
					System.out.println("Second set of centroid value is , x: " + fx2 + " y: " + fy2);
					byte Packet_type_5[] = new byte[1];

					Packet_type_5[0] = (byte) 5;

					DatagramPacket req_to_server = new DatagramPacket(Packet_type_5, Packet_type_5.length, inet, 517);
					sock.send(req_to_server);
					sock.setSoTimeout(30000);
					try {
						sock.receive(recievePacket);

					} // try
					catch (SocketTimeoutException e) {
						sock.setSoTimeout(30000);
						sock.send(req_to_server); // Retransmitting CACK Packet again
						System.exit(0);
					}

				}
			} catch (SocketTimeoutException e) {
				System.out.println("Server Failure!");
				System.exit(0);

			}
		}

		// send and receive the acknowledgments stating to type 4 and type 5.
	}

}
