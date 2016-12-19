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
    //Datos de la conexión
    private static final int puerto = 1500;
    //Variables IN/OUT (E/S)
    DataInputStream flujo_entrada;
    DataOutputStream flujo_salida;
    Socket skClient;
    //Variables de trabajo
    int contador = 0; //Contador por las veces que nos hemos equivocado en la contraseña
    int cont2 = 0; //Contador para las oportunidades de nombre de archivo válido
    int validado = 0; //Valores 0 no validado - 1 validado - 2 demasiados intentos
    boolean siNO = false;
    boolean existe = false;
    File [] listaArchivos;
    
    /**
     * Pequeña función que se usa sólo para cerrar las conexiones E/S.
     */
    public void cierreConexion(){
        try {
            flujo_entrada.close();
            flujo_salida.close();
            skClient.close();
        } catch (IOException ex){
            System.err.println(ex);
        }
    }

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
    
    @Override
    public void run(){
        //Tareas
        try {
            //Creamos los flujos de entrada y salida
            flujo_entrada = new DataInputStream (skClient.getInputStream());
            flujo_salida = new DataOutputStream (skClient.getOutputStream());
            
            do {
                //Recibimos el usuario y contraseña
                String usuario = flujo_entrada.readUTF();
                String contra = flujo_entrada.readUTF();
                
                Validar user = new Validar(usuario, contra);
                boolean resp = user.comprobacion();
                
                if (resp){
                    validado = 1;
                    
                    //Enviamos la validación
                    flujo_salida.writeInt(1);
                    flujo_salida.flush();
                } else {
                    contador++;
                    System.out.println(" ");
                    System.out.println("** No aceptada la validación "+contador);
                    flujo_salida.writeInt(0);
                    flujo_salida.flush();
                    //System.out.println("El contador va "+contador);
                }
                
                if (contador >= 3){
                    validado = 2;
                }
                
            } while (validado == 0);
            
            //Reaccionamos según la validación
            switch (validado) {
                    
                case 1:
                    System.out.println(" ");
                    System.out.println("-> Usuario/Contraseña aceptados");
                    
                    //Conseguimos el listado de archivos
                    File busqueda = new File (".");
                    listaArchivos = busqueda.listFiles();
                    
                    //Enviamos el número de archivos que son primero para
                    //preparar la recepción :
                    flujo_salida.writeInt(listaArchivos.length);
                    flujo_salida.flush();
                    
                    //Enviamos el listado de archivos
                    System.out.println(" ");
                    System.out.println("-> Enviamos la lista de archivos");
                    for (File archivo:listaArchivos){
                        flujo_salida.writeUTF(archivo.getName());
                        flujo_salida.flush();
                    }
                    
                    break;
                case 2:
                    System.out.println("-> Demasiados intentos. Cerramos conexión.");
                    siNO = false;
                    
                    //Cerramos conexion
                    cierreConexion();
                    break;
                default:
                    System.out.println("-> Error en la validación. Cerramos conexión.");
                    siNO = false;
                    
                    //Cerramos conexion
                    cierreConexion();
                    break;

            }
            
            do {
                //Esperamos a que el cliente nos diga si quiere o no un archivo
                siNO = flujo_entrada.readBoolean();
                
                if (siNO){
                    //Leemos el archivo que desea recibir el cliente
                    String archivo = flujo_entrada.readUTF();
                    System.out.println(" ");
                    System.out.println("Archivo solicitado "+archivo);

                    for(File elemento:listaArchivos){
                        if (elemento.getName().matches(archivo)){
                            existe = true;
                        }
                    }

                    if (existe){
                        //Lo comunicamos al cliente
                        flujo_salida.writeBoolean(true);
                        flujo_salida.flush();

                        File fichero = new File (archivo);
                        int tam = (int) fichero.length();

                        //Enviamos el tamaño del archivo
                        flujo_salida.writeInt(tam);
                        flujo_salida.flush();

                        byte [] buffer = new byte [tam];
                        FileInputStream entradaFichero = new FileInputStream(fichero.getName());
                        BufferedInputStream bufferEntrada = new BufferedInputStream(entradaFichero);
                        bufferEntrada.read(buffer);

                        for (int i = 0; i < tam; i++) {
                            flujo_salida.write(buffer[i]);
                            flujo_salida.flush();
                        }
                        System.out.println("-> Enviado archivo solicitado");
                        cont2 = 4;
                    } else {
                        flujo_salida.writeBoolean(false);
                        flujo_salida.flush();
                        cont2++;
                        System.out.println(" ");
                        System.out.println("-> El archivo no existe, solicitamos nuevo nombre de archivo");
                        System.out.println(" ");
                    }

                }
            } while (cont2<3);
            
            if (cont2>=3){
                System.out.println("*** Demasiados intentos.");
            }
            
            System.out.println("-> Cerramos la conexión con el cliente");
            cierreConexion();
            
        }catch (IOException e) {
            System.err.println(e);
        }
    }

}
