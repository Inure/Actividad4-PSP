/*
    Actividad 4 - PSP
    Crear un servidor para  que cuando se inicia sesión desde un cliente con un 
nombre de usuario y contraseña (por ejemplo javier / secreta) el sistema 
permita Ver el contenido del directorio actual, mostrar el contenido de un 
determinado archivo y salir.
    Para realizar el ejercicio primero debes crear un diagrama de estados que 
muestre el funcionamiento del servidor.
*/
package principal;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author C.RipPer
 * @date 13-dic-2016
 */

public class ClienteMain {
    //Datos de conexión
    private static final String HOST = "localhost";
    private static final int Puerto = 1500;
    DataInputStream flujo_entrada;
    DataOutputStream flujo_salida;
    Scanner entrada = new Scanner(System.in);
    
    int validado = 0;
    int contador = 0;
    boolean inOut = true;
    
    public ClienteMain(){
        
        try {
            Socket sCliente = new Socket (HOST, Puerto);
            
            //Creando los flujos
            flujo_entrada = new DataInputStream(sCliente.getInputStream());
            flujo_salida = new DataOutputStream(sCliente.getOutputStream());
            
            do {
                contador++;
                //Solicitamos el usuario
                System.out.print("-> Introduzca usuario: ");
                String usuario = entrada.nextLine();
                
                //Solicitamos la contraseña
                System.out.print("-> Introduzca la contraseña: ");
                String contra = entrada.nextLine();
                
                //Enviamos el usuario y la contraseña
                flujo_salida.writeUTF(usuario);
                flujo_salida.flush();

                flujo_salida.writeUTF(contra);
                flujo_salida.flush();

                //Esperamos confirmación por parte del Servidor

                validado = flujo_entrada.readInt();
                
                if (validado == 0){
                    System.out.println("-> Usuario/Contraseña no validos");
                    System.out.println("-> Vuelva a introducirlos");
                    if (contador >= 3){
                        validado = 1;
                        System.out.println("-> Demasiados intentos, cerramos conexión");
                    }
                }
            
            } while (validado == 0);
            
            if (validado == 1){
                
                int numArc = flujo_entrada.readInt();

                for (int i = 0; i < numArc; i++) {
                    System.out.println(flujo_entrada.readUTF());
                }
                System.out.println("-> Listado terminado");
                    
                }
                
                System.out.print("-> Introduce el nombre del archivo que quieres leer: ");
                String archivo = entrada.nextLine();

                //Enviamos el archivo que deseamos leer
                flujo_salida.writeUTF(archivo);
                flujo_salida.flush();
                
                String nombreArchivo = "Recibido_"+archivo;
                    
                //Creamos el flujo de salida
                FileOutputStream fos = new FileOutputStream(nombreArchivo);
                BufferedOutputStream out = new BufferedOutputStream (fos);
                BufferedInputStream in = new BufferedInputStream (sCliente.getInputStream());

                //Recibimos el tamaño del archivo y creamos el array 
                int tam = flujo_entrada.readInt();
                byte [] buffer = new byte[tam];
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (byte)in.read();
                }

                //Escribimos el archivo
                out.write(buffer);
                out.flush();
                System.out.println("-> Recepción finalizada");
                File fichero = new File (nombreArchivo);
                Desktop.getDesktop().open(fichero);
                System.out.println("-> Abrimos el archivo");

                in.close();
                out.close();
                
                
            
            
            //Cerramos conexiones
            System.out.println("Cerramos conexiones");
            sCliente.close();
            
        } catch (Exception e){
            System.err.println(e);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ClienteMain();
    }

}
