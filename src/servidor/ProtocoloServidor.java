package servidor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

public class ProtocoloServidor implements Runnable{
	
	//Rutas de archivos
	private static String ARCHIVO_1 = "data/archivos/ElAñañin.mp4";
	private static String ARCHIVO_2 = "data/archivos/toadload.wav";

	//Atributos
	private Socket sc = null;
	private String dlg;
	private int idP;
	private long time_start, time_end, time;
	private static File file;
	private int archivo;
	/*
	 * Metodo init para asignar el archivo de log
	 */
	public static void init(File pFile) {
		file = pFile;
	}

	/*
	 * Constructor del protocolo del servidor
	 * @param: csP socket designado
	 * @param: idP Numero de thread que atiende
	 */
	public ProtocoloServidor (Socket csP, int idP, int numArchivo) {
		sc = csP;
		this.idP=idP;
		dlg = new String("Delegado " + idP);
		archivo = numArchivo;
		this.run();

	}

	/*
	 * Generacion del archivo log. 
	 * Nota: 
	 * - Debe conservar el metodo . 
	 * - Es el ÃƒÂºnico metodo permitido para escribir en el log.
	 */
	private void escribirMensaje(String pCadena) {
		synchronized(file)
		{
			try {
				FileWriter fw = new FileWriter(file,true);
				fw.write(pCadena + "\n");
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void run() {

		try {
			int cuenta;
			
			//RECUPERA EL ARCHIVO A ENVIAR
			File file;
			if(archivo == 1) {
				file = new File(ARCHIVO_1);
			}
			else {
				file = new File(ARCHIVO_2);
			}
			//CREA BUFFER Y CANALES DE COMUNICACION EN SOCKET
			byte[] buffer = new byte[(int) file.length()];
			FileInputStream fi = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fi);
			//DataOutputStream dos = new DataOutputStream(sc.getOutputStream());
			//DataInputStream dis = new DataInputStream(sc.getInputStream());
			bis.read(buffer, 0, buffer.length);
			OutputStream os = sc.getOutputStream();
			
			//GENERA HASH DEL ARCHIVO PARA COMPROBACION
			/*
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			byte[] checksum = sha.digest(buffer);
			String hashEnviar = new String(checksum);
			dos.writeUTF(hashEnviar);
			System.out.println("Hash generado server " + hashEnviar);
			*/
			
			//NOTIFICA ENVIO DE ARCHIVO Y COMIENZA PROCESO
			System.out.println("Enviando "+ file.getName() + " tamano: " + buffer.length + " Bytes");
			os.write(buffer,0,buffer.length);
			os.flush();
			
			//NOTIFICA TERMINACION DE ENVIO Y RECEPCION DEL ARCHIVO POR PARTE EL CLIENTE
			/*
			if(dis.readInt() == 1) {
				System.out.println("Envío de archivo terminado. Cliente ya lo recibió.");
			}
			*/
			bis.close();
			os.close();
			sc.close();
		}
		catch(Exception e) {
			System.out.println("Error en proceso de envío... " + e.getMessage());
		}
		
	}



}
