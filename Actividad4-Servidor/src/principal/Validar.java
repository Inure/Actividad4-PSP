/*
    Esta clase se encargará de validar el usuario y la contraseña con los que
ya tenemos registrados.
*/

package principal;

/**
 *
 * @author C.RipPer
 * @date 19-dic-2016
 * 
 */

public class Validar {
    private String usuario;
    private String contrasena;
    
    String [] usuarios = {"Prueba", "Carina", "Curso", "DAM"};
    String [] contrasenas = {"prueba", "123456", "3dam", "clase"};
    
    public Validar(String usuario, String contra){
        this.usuario = usuario;
        this.contrasena = contra;
        
    }
    
    public boolean comprobacion (){
        boolean validado = false;
        
        for (int i = 0; i < usuarios.length; i++) {
            
            if ((usuarios[i].matches(usuario)) && (contrasenas[i].matches(contrasena))) {
                validado = true;
            }
            
        }
        
        return validado;
    }

}
