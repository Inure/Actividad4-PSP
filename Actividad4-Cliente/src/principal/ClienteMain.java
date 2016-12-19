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
import java.util.Scanner;

/**
 * @author C.RipPer
 * @date 13-dic-2016
 */

public class ClienteMain {
    //Datos de conexión
    private static final String HOST = "localhost";
    private static final int Puerto = 1500;
    //Variables IN/OUT (E/S)
    DataInputStream flujo_entrada;
    DataOutputStream flujo_salida;
    Scanner entrada = new Scanner(System.in);
    //Variables de trabajo
    int validado = 0;
    int contador = 0;
    int cont2 = 0;
    String archivo;
    boolean existe = false;
    
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
                    System.out.println(" ");
                    System.out.println("*** Usuario/Contraseña no validos");
                    System.out.println(" ");
                    if (contador >= 3){
                        validado = 1;
                        System.out.println("*** Demasiados intentos, cerramos conexión");
                    } else {
                        System.out.println("Vuelva a introducirlos");
                    }
                }
            
            } while (validado == 0);
            
            if (validado == 1){
                
                //Primero recibimos el número de archivos a leer
                int numArc = flujo_entrada.readInt();
                //Recibimos los nombres de los archivos de la carpeta
                System.out.println(" ");
                System.out.println(" ");
                System.out.println("    LISTADO DE ARCHIVOS - CARPETA SERVIDOR");
                for (int i = 0; i < numArc; i++) {
                    System.out.println("    |   " + flujo_entrada.readUTF());
                }
                System.out.println(" ");
                System.out.println(" ");
                System.out.println("-> Listado terminado");
                
                do {
                    
                    System.out.print("-> Introduce el nombre del archivo que quieres"
                            + " leer o escribe (n/N) para salir: ");
                    archivo = entrada.nextLine();

                    if (archivo.toUpperCase().matches("N")){
                        System.out.println("Saliendo del programa ....");
                        flujo_salida.writeBoolean(false);
                        flujo_salida.flush();

                        existe = false;
                        cont2 = 4;

                    } else {
                        //Indicamos que sí enviamos archivo
                        flujo_salida.writeBoolean(true);
                        flujo_salida.flush();

                        //Enviamos el nombre del archivo que deseamos leer
                        flujo_salida.writeUTF(archivo);
                        flujo_salida.flush();

                        //El servidor nos comunica si existe o no el archivo
                        existe = flujo_entrada.readBoolean();
                        
                        if (existe){
                            cont2 = 4;
                        } else {
                            System.out.println(" ");
                            System.out.println("** El archivo no existe.");
                            cont2++;
                        }
                    }
                    
                } while (cont2<3);
                
                if (existe){
                    
                    //El servidor nos ha dicho que existe y nos preparamos para
                    //recibirlo
                    String nombreArchivo = "Recibido_"+archivo;

                    //Recibimos el tamaño del buffer para preparar la entrada
                    int tam = flujo_entrada.readInt();

                    //Creamos el flujo de salida
                    FileOutputStream fileOut = new FileOutputStream(nombreArchivo);
                    BufferedOutputStream salidaFichero = new BufferedOutputStream (fileOut);
                    BufferedInputStream entradaBuff = new BufferedInputStream (sCliente.getInputStream());

                    //Recibimos el tamaño del archivo y creamos el array 
                    byte [] buffer = new byte[tam];
                    for (int i = 0; i < buffer.length; i++) {
                        buffer[i] = (byte)entradaBuff.read();
                    }

                    //Escribimos el archivo
                    salidaFichero.write(buffer);
                    salidaFichero.flush();
                    System.out.println("-> Recepción finalizada");
                    File fichero = new File (nombreArchivo);
                    Desktop.getDesktop().open(fichero);
                    System.out.println("-> Abrimos el archivo");

                    //Cerramos los flujos de entrada/salida del buffer
                    entradaBuff.close();
                    salidaFichero.close();
                }
                
                if (cont2==3) {
                    System.out.println("*** Demasiados intentos cerramos conexión");
                }
            }
        
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
