package servidor;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import servidor.ProtocoloServidor;


public class mainServ {
	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	public static int contInst;
	private static final String ARCHIVO_1 = "ElAñañin.mp4";
	private static final String ARCHIVO_2 = "toadload.wav";
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{

		
		System.out.println(MAESTRO + "Establezca puerto de conexion:");
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		int ip = Integer.parseInt(br.readLine());
		System.out.println(MAESTRO + "Empezando servidor maestro en puerto " + ip);
		
		System.out.println(MAESTRO + "Escribe el número del archivo a transmitir: 1 para el archivo de 100MiB o 2 para el archio de 250MiB");
		int archivo = Integer.parseInt(br.readLine());
		// Crea el archivo de log
		/*
		File file = null;
		String ruta = "./log.txt";

		file = new File(ruta);
		if (!file.exists()) {
			file.createNewFile();
		}
		
		ruta= "./medidas.txt";
		File fileMedidas = null;
		fileMedidas = new File(ruta);
		if (!fileMedidas.exists()) {
			try {
				fileMedidas.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/

		//ProtocoloServidor.init(file);

		// Crea el socket que escucha en el puerto seleccionado.
		ss = new ServerSocket(ip);
		System.out.println("Por favor introduzca el número máximo de cientes que quiere atender (no más de 25) ");
		int nThreads = Integer.parseInt(br.readLine());

		Socket[] socketClientes = new Socket[nThreads];
		
		System.out.println("Esperando conextiones...");
		
		int conectados = 0;
		while(conectados < nThreads) {
			try {
				//INICIA PROTOCOLO ACEPTANDO CONEXION
				socketClientes[conectados] = ss.accept();
				DataOutputStream dout = new DataOutputStream(socketClientes[conectados].getOutputStream());
				DataInputStream din = new DataInputStream(socketClientes[conectados].getInputStream());
				System.out.println("Aceptando conexión de cliente numero: " + conectados);
				//TRAS ADMITIR CLIENTE Y CREAR COMUNICACION ENTRADA-SALIDA EN SOCKET SE ENVÍA EL ID AL CLIENTE
				
				dout.writeByte(0);
				
				//SE NOTIFICA EL NOMBRE DEL ARCHIVO QUE SE ENVIARÁ
				if(archivo == 1) {
					dout.writeInt(conectados);
					dout.writeUTF(ARCHIVO_1);
					System.out.println("Escogio archivo 1");
				}
				else {
					dout.writeInt(conectados);
					dout.writeUTF(ARCHIVO_2);
					dout.flush();
					System.out.println("Escogio archivo 2");

				}
				
				//SE PREPARA RECEPCION DE SIGUIENTE CONEXION Y SE NOTIFICA QUE CLIENTE ESPERA
				if(din.readUTF().contentEquals("OK")) {
					System.out.println("Cliente "+ conectados + " recibió ID y nombre de archivo");
				}
				//SE PREPARA LLEGADA DE SIGUIENTE CLIENTE
				conectados++;
			
			}
			catch(Exception e) {
				e.printStackTrace();
				System.out.println("Error en servidor! " );
			}
		}
		//ACABA WHILE, ES DECIR, YA LLEGARON CLIENTES ESPERADOS Y SE EMPIEZA ENVÍO DE ARCHIVO
		System.out.println("Comenzando envío de archivo a clientes...");
		//RECORRE N CLIENTES Y ENVÍA ARCHIVO
		for(int cli=0; cli<socketClientes.length;cli++) {
			
			ProtocoloServidor ps = new ProtocoloServidor(socketClientes[cli], cli, archivo);
			
		}
		

	}

 
}
