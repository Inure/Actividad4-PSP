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

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
            System.out.println("Escuchando el puerto "+puerto);
            
            while (true){ //Se ha puesto una conexión sin límite
                //Se aceptan conexiones
                Socket skCliente = skServer.accept();
                System.out.println("Cliente conectado");
                
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
                System.out.println(usuario);
                String contra = flujo_entrada.readUTF();
                System.out.println(contra);

                if ((usuario.matches("Prueba")) && (contra.matches("123456"))){
                    System.out.println("Usuario/Contraseña aceptados");
                    validado = false;
                }
            
            } while ((validado) || (contador == 3));
            flujo_entrada.close();
            flujo_salida.close();
            skClient.close();
            
            
        }catch (Exception e) {
            System.err.println(e);
        }
    }

}
