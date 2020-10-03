package cliente;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;

public class mainCliente {
	public static int PUERTO;
	public static String SERVIDOR;
	public static String DIR_DESCARGA = "data/descargas/";
	
	public static void main(String[] args) throws IOException{
		
		//COMIENZA EJECUCION EN MAQUINA CLIENTE
		System.out.println("Cliente...");
		
		//PREPARACION DE SOCKET Y DE CONSOLA PARA ENTRADA DE USUARIO
		Socket socket = null;
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		
		try {
			//PETICION DE DATOS PARA COMENZAR INTENTO DE CONEXION
			System.out.println("Ingresa la dirección IP del servidor");
			SERVIDOR = br.readLine();
			System.out.println("Ingresa el puerto para conectarte");
			PUERTO = Integer.parseInt(br.readLine());
			
			//CREACION DE SOCKET CON SERVIDOR
			socket = new Socket(SERVIDOR,PUERTO);
			if(!socket.isClosed()) {
				
				//CONEXION CON SERVIDOR Y NOTIFICACION DE ESTADO DE CONEXION
				System.out.println("Estado de conexión: " + !socket.isClosed() + " Esperando ID y nombre de archivo...");
			}
			
			//CREACION DE CANAL ENTRADA Y SALIDA EN SOCKET
			DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
			DataInputStream din = new DataInputStream(socket.getInputStream());
			int id;
			String nombreArchivo;
			
			//PROTOCOLO PENSADO: CUANDO SERVIDOR CREA SOCKET, ENVÍA "0", LUEGO EL ID DEL CLIENTE Y LUEGO EL NOMBRE DE ARCHIVO QUE ENVIA
			if(din.readByte() == 0) {
				id = din.readInt();
				nombreArchivo = din.readUTF();
				System.out.println("El archivo a recibir es "+ nombreArchivo + " y su ID es " + id );
				
				//ENVIAR NOTIFICACION DE PREPARADO PARA RECIBIR ARCHIVO
				System.out.println("Notificando al servidor que se está esperando el archivo...");
				dout.writeUTF("OK");
				
				//LLAMADO A METODO PARA RECIBIR ARCHIVO
				descargar(socket, id, nombreArchivo);
			}
			else {
				System.out.println("Falla de servidor");
			}
			
		}
		catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		br.close();
		socket.close();
	}
	
	/**
	 * Método que recibe archivo del servidor
	 * @param s socket creado con servidor
	 * @param id id del cliente asignado por servidor
	 * @param arch nombre de archivo a descargar
	 */
	public static void descargar(Socket s, int id, String arch) {
		
		int bytesLeidos;
		int actual = 0;
		try {
			
			//CANAL DE LECTURA DE ARCHIVO 
			FileOutputStream fos = new FileOutputStream(DIR_DESCARGA + arch);
			//DataInputStream dis = new DataInputStream(s.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			InputStream is = s.getInputStream();
			//DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			
			byte[] buffer = new byte[70000000];
			
			//RECEPCION DE HASH DEL SERVIDOR
			//String hashServidor = dis.readUTF();
			//System.out.println("hash recibido server " + hashServidor);
			
			//LEE EN BUFFER
			bytesLeidos = is.read(buffer, 0, buffer.length);
			actual = bytesLeidos;
			
			//LEE ARCHIVO ENTRANTE
			while(bytesLeidos > -1) {
				bytesLeidos = is.read(buffer, actual, (buffer.length - actual));
				if(bytesLeidos >= 0) {
					actual+=bytesLeidos;
				}
			}
					
			bos.write(buffer, 0, actual);
			bos.flush();
			
			//NOTIFICACION DE FINALIZACION AL USUARIO Y A SERVIDOR
			System.out.println("Archivo " + arch + " descargado en " + DIR_DESCARGA + "(" + actual + " Bytes leídos).");
			//dout.writeInt(1);
			
			//VERIFICACION DE INTEGRIDAD DE ARCHIVO
			/*
			System.out.println("Comenzando a verificar integridad de archivo descargado...");
			File descargado = new File(DIR_DESCARGA+arch);
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			byte[] checksum = sha.digest(new byte[(int) descargado.length()]);
			String hashCliente = new String(checksum);
			
			if(hashCliente == hashServidor) {
				System.out.println("El archivo recibido ha sido examinado y no ha sido manipulado.");
			}
			else {
				System.out.println("¡¡ADVERTENCIA!! El archivo descargado no es igual al original");
			}
			*/
			bos.close();
			fos.close();
			s.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
