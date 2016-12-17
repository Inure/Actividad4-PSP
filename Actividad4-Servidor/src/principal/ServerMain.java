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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author C.RipPer
 * @date 13-dic-2016
 */

public class ServerMain extends Thread {
    DataInputStream flujo_entrada;
    DataOutputStream flujo_salida;
    Socket skClient;
    int contador = 0;
    boolean validado = true;
    //Datos de la conexión
    private static final int puerto = 1500;

    public ServerMain(Socket skCliente){
        //Constructor
        this.skClient = skCliente;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //Inicio del servidor en el puerto
            ServerSocket skServer = new ServerSocket (puerto);
            System.out.println("-> Escuchando el puerto "+puerto);
            
            while (true){ //Se ha puesto una conexión sin límite
                //Se aceptan conexiones
                Socket skCliente = skServer.accept();
                System.out.println("-> Cliente conectado");
                
                //Se abre un hilo por cada cliente
                new ServerMain(skCliente).start();
            }
        } catch (IOException ex){
            System.err.println(ex);
        }
    }
    
    public void run(){
        //Tareas
        try {
            //Creamos los flujos de entrada y salida
            flujo_entrada = new DataInputStream (skClient.getInputStream());
            flujo_salida = new DataOutputStream (skClient.getOutputStream());
            
            do {
                contador++;
           
                //Recibimos el usuario y contraseña
                String usuario = flujo_entrada.readUTF();
                String contra = flujo_entrada.readUTF();

                if ((usuario.matches("Prueba")) && (contra.matches("123456"))){
                    System.out.println("-> Usuario/Contraseña aceptados");
                    validado = false;

                    //Enviamos la validación
                    flujo_salida.writeInt(1);
                    flujo_salida.flush();
                    
                    //Conseguimos el listado de archivos
                    File busqueda = new File (".");
                    File [] listaArchivos = busqueda.listFiles();
                    System.out.println("-> Enviamos el número de archivos que son: ");
                    flujo_salida.writeInt(listaArchivos.length);
                    flujo_salida.flush();
                    
                    System.out.println("-> Enviamos la lista de archivos");
                    for (File archivo:listaArchivos){
                        //System.out.println(archivo.getName());
                        flujo_salida.writeUTF(archivo.getName());
                        flujo_salida.flush();
                    }
                    
                    //Leemos el archivo que desea recibir el cliente
                    String archivo = flujo_entrada.readUTF();
                    System.out.println("Archivo solicitado "+archivo);
                    
                    File fichero = new File (archivo);
                    int tam = (int) fichero.length();
                    
                    //Enviamos el tamaño del archivo
                    flujo_salida.writeInt(tam);
                    flujo_salida.flush();
                    
                    byte [] envioFichero = new byte [tam];
                    FileInputStream entradaFichero = new FileInputStream(fichero.getName());
                    BufferedInputStream bufferEntrada = new BufferedInputStream(entradaFichero);
                    bufferEntrada.read(envioFichero);
                    
                    flujo_salida.writeInt(envioFichero.length);
                    flujo_salida.flush();
                    
                    for (int i = 0; i < envioFichero.length; i++) {
                        flujo_salida.write(envioFichero[i]);
                        flujo_salida.flush();
                    }
                    
                    System.out.println("-> Enviado archivo solicitado");
                    
                    
                } else {
                    System.out.println("-> No aceptada la validación");
                    flujo_salida.writeInt(0);
                    flujo_salida.flush();
                    System.out.println("-> El contador va "+contador);
                }
                
                if (contador >= 3){
                    validado = false;
                    System.out.println("-> Demasiados intentos");
                }
            
            } while (validado);
            
            System.out.println("-> Cerramos la conexión con el cliente");
            flujo_entrada.close();
            flujo_salida.close();
            skClient.close();
            
            
            
        }catch (Exception e) {
            System.err.println(e);
        }
    }

}
