import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.*;
import java.lang.Math;

public class New_Server_Side {
	static int count11, count21, count31;
	static int d[];
	static float k[][];
	static float tempk[][];
	static float y[];
	static double diff[];
    static int  n,z;
    

	static List<Float> xarray = new ArrayList<Float>();
	static List<Float> yarray = new ArrayList<Float>();
	static List<Float> xarraytemp = new ArrayList<Float>();
	static List<Float> yarraytemp = new ArrayList<Float>();
	static List<Float> arrxy = new ArrayList<Float>();

	public static int byteArrayToInt(byte[] val1) 
	{
		return val1[3] & 0xFF | (val1[2] & 0xFF) << 8 | (val1[1] & 0xFF) << 16 | (val1[0] & 0xFF) << 24;
	}

	private static byte[] IntToByteArray(int val2) 
	{
		byte save[] = new byte[4];
		save[0] = (byte) ((val2 & 0xFF000000) >> 24);
		save[1] = (byte) ((val2 & 0x00FF0000) >> 16);
		save[2] = (byte) ((val2 & 0x0000FF00) >> 8);
		save[3] = (byte) ((val2 & 0x000000FF) >> 0);
		return save;
	}



	public static double cal_dist(double Xx1, double Yy1, double x1, double y1) 
	{
		double inter = Xx1 - x1;
		double dinter = inter * inter;
		inter = Yy1 - y1;
		double ydinter = inter * inter;
		double sum = dinter + ydinter;
		double dist = Math.sqrt(sum);
		return dist;
	}

	public static void main(String args[]) throws UnknownHostException, SocketException, IOException 
	{

		DatagramSocket sq = new DatagramSocket(517);
		byte ClientData[] = new byte[405];
		byte SentData[] = new byte[17];
		byte convert[] = new byte[4];
		byte TypeOfPacket[] = new byte[6]; 
		int c;
		int rr=0;
	    
	    // Packet Type Array Storage
		
		TypeOfPacket[1] = 0x01;
		TypeOfPacket[2] = 0x02;
		TypeOfPacket[3] = 0x03;
		TypeOfPacket[4] = 0x04;
		TypeOfPacket[5] = 0x05;
		int Trackerlength = 0;
		int TrackertotalLength = 0;
		float xmax, xmin, ymax, ymin;
		DatagramPacket packet = new DatagramPacket(ClientData, ClientData.length);

		int CheckSeq = 0;
		while (true) 
		{
			byte Sequence[] = IntToByteArray(CheckSeq);
			for (int i = 0; i < 405; i++)
				ClientData[i] = 0;
			for (int i = 0; i < 17; i++)
				SentData[i] = 0;
			System.out.println("Waiting for packets from client");
			sq.receive(packet);

			DatagramPacket sendPacket = new DatagramPacket(SentData, SentData.length, packet.getAddress(),packet.getPort());
			byte[] RecieveNewData = packet.getData();

			if (RecieveNewData[0] == (byte)0) {
				System.out.println("Recieved packets from Client");
				System.out.println("Recieved Packet:" + Arrays.toString(RecieveNewData));

				if (RecieveNewData[1] == Sequence[2] && RecieveNewData[2] == Sequence[3]) // Checking the Sequence Number																			// Number
				{
					SentData[0] = TypeOfPacket[1];
					SentData[1] = RecieveNewData[1];
					SentData[2] = RecieveNewData[2];
					Trackerlength = RecieveNewData[4];

					c = 5;
					TrackertotalLength = TrackertotalLength + Trackerlength;
					System.out.println("Recieved In Order Packet Sending Ackowledgement packet to the client");
					ArrayList arr1 = new ArrayList();
					
					for (int i = 2; i < Trackerlength * 8 + 2; i++) 
					{
						arr1.add(RecieveNewData[c++]);
					}
					
			
					sq.send(sendPacket);
					int vv = 0;
					
					for (int i = 0; i < arr1.size(); i = i + 4) 
					{
						convert[vv] = (byte) arr1.get(i);
						convert[vv + 1] = (byte) arr1.get(i + 1);
						convert[vv + 2] = (byte) arr1.get(i + 2);
						convert[vv + 3] = (byte) arr1.get(i + 3);
						int res = byteArrayToInt(convert); 
						arrxy.add((float) res); 
						rr=rr+1;
						vv = 0;
					}
					CheckSeq++;

				} 
				else 
				{
					SentData[0] = (byte)1;
					SentData[1] = Sequence[2]; // Reseting to old sequence number
					SentData[2] = Sequence[3]; // Reseting to old sequence number
					System.out.println("Recieved Out of Order Packet and sending previous Ackowledgement packet to client");
					sq.send(sendPacket);
				}

			}

			else if (RecieveNewData[0] == (byte)2) // Check for Request Packet Type
			{
				System.out.println("Recieved Request Packet and sending Ackowledgement back");
				SentData[0] = (byte)3;
				sq.send(sendPacket);
				sq.setSoTimeout(3000);
				try // Sending the request acknowledgement in case if the request arrives
				{
					sq.receive(packet);
					RecieveNewData = packet.getData();
					if (RecieveNewData[0] == TypeOfPacket[2]) {
						System.out.println("Request Message Recieved Again!");
						SentData[0] = (byte)3;
						sq.send(sendPacket);
					}
				} catch (SocketTimeoutException e) {
					for(int index=0;index<arrxy.size();index=index+2) 
					{
						xarraytemp.add(arrxy.get(index)/100); //Storing only the X co-ordinates
	
						yarraytemp.add(arrxy.get(index+1)/100);//Storing only the Y co-ordinates
						
						
					}
                  
					//Finding the maximum and minimum value of X and Y
					xmax = Collections.max(xarraytemp);
					xmin = Collections.min(xarraytemp);
					ymax = Collections.max(yarraytemp);
					ymin = Collections.min(yarraytemp);
					
					double Mx1 =  Math.random() * (xmax + xmin);
					double Mx2 =  Math.random() * (xmax + xmin);
					double My1 =  Math.random() * (ymax + ymin);
					double My2 =  Math.random() * (ymax + ymin);
					
					double oX1 = Mx1;
					double oX2 = Mx2;
					double oY1 = My1;
					double oY2 = My2;

					final float convergenace_Constant = .00001f;

					float avgX = 0.000f;
					float avgY = 0.000f;
					
					float clusX1[] = new float[500];
					float clusY1[] = new float[500];
					float clusX2[] = new float[500];
					float clusY2[] = new float[500];

					do {
						for (int i = 0; i < xarraytemp.size(); i++) 
						{
							if (cal_dist(xarraytemp.get(i), yarraytemp.get(i), Mx1, My1) < cal_dist(xarraytemp.get(i),
									yarraytemp.get(i), Mx2, My2)) 
							{
								clusX1[i] = xarraytemp.get(i);
								clusY1[i] = yarraytemp.get(i);

							}
						}
						for (int i = 0; i < xarraytemp.size(); i++) 
						{
							if (cal_dist(xarraytemp.get(i), yarraytemp.get(i), Mx1, My1) > cal_dist(xarraytemp.get(i),
									yarraytemp.get(i), Mx2, My2)) 
							{
								clusX2[i] = xarraytemp.get(i);
								clusY2[i] = yarraytemp.get(i);
							}
						}
						oX1 = Mx1;
						oY1 = My1;
						oX2 = Mx2;
						oY2 = My2;

						for (int i = 0; i < clusX1.length; i++) 
						{
							avgX = avgX + clusX1[i];
							Mx1 = avgX / clusX1.length;
							avgY = avgY + clusY1[i];
							My1 = avgY / clusY1.length;
						}
						avgX = 0; // Intermediate variable for calculation
						avgY = 0; // Intermediate variable for calculation
						for (int i = 0; i < clusX2.length; i++) 
						{
							avgX = avgX + clusX2[i];
							Mx2 = avgX / clusX2.length;
							avgY = avgY + clusY2[i];
							My2 = avgY / clusY2.length;

						}

					} while ((cal_dist(oX1, oY1, Mx1, My1) + cal_dist(oX2, oY2, Mx2, My2)) > convergenace_Constant);
					
					double result[] = new double[4];
					result[0] = Mx1 * 100;
					result[1] = My1 * 100;
					result[2] = Mx2 * 100;
					result[3] = My2 * 100;
					System.out.println("First cluster centroid x:"+Mx1);
					System.out.println("First cluster centroid y:"+My1);
					System.out.println("Second cluster centroid x:"+Mx2);
					System.out.println("Second cluster centroid y:"+My2);

					int a = (int) result[0];
					int b = (int) result[1];
					int bc = (int) result[2];
					int d = (int) result[3];
					
					byte result1[] = IntToByteArray(a);
					byte result2[] = IntToByteArray(b);
					byte result3[] = IntToByteArray(bc);
					byte result4[] = IntToByteArray(d);

					SentData[0] = (byte) 4;
					for (int counter = 0; counter < 4; counter++) 
					{
						SentData[counter + 1] = result1[counter];
					}
					for (int counter = 0; counter < 4; counter++) 
					{
						SentData[counter + 5] = result2[counter];
					}
					for (int counter = 0; counter < 4; counter++) 
					{
						SentData[counter + 9] = result3[counter];
					}
					for (int counter = 0; counter < 4; counter++) 
					{
						SentData[counter + 13] = result4[counter];
					}
					DatagramPacket req_to_server = new DatagramPacket(SentData, SentData.length, packet.getAddress(),
							packet.getPort());
					sq.send(req_to_server);
					int flagOfTimeOut = 0;
					int lTimeOut = 1000;
					try {
						sq.receive(packet);
						RecieveNewData = packet.getData();
						if (RecieveNewData[0] == (byte) 5) // Checking Cluster Acknowledgement Packet
						{ 
							System.out.println("Cluster Acknowledgement Packet Recieved from client");
							 
							System.exit(0);
						}
						if (RecieveNewData[0] != (byte) 5) 
						{    System.out.println("The values asre being retransmitted to client");
							sq.send(req_to_server);
						}

					} catch (SocketTimeoutException e1) 
					{
						flagOfTimeOut++;
						System.out.println("Timeout Reached .....");
						if (flagOfTimeOut == 4) 
						{
							System.out.println("Communication Failure! Unable to contact the client"  );
							System.exit(0);
						} else 
						{
							lTimeOut = 2 * lTimeOut;
							sq.setSoTimeout(lTimeOut);
							System.out.println("Retransmitting the packet with calculated values");
							sq.send(req_to_server);
							
						}
					}
				}
			}
		}
	}
}
