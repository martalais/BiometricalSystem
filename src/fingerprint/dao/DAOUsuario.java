/*
 * The MIT License
 *
 * Copyright 2016 xmbeat.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fingerprint.dao;

import fingerprint.model.Permiso;
import fingerprint.model.Usuario;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.serial.SerialBlob;
import uaz.fingerprint.EnrollResult;
import uaz.fingerprint.Reader;
import uaz.fingerprint.VerifyResult;

/**
 *
 * @author xmbeat
 */
public class DAOUsuario {
    private Connection mConn;
    public DAOUsuario() throws SQLException, ClassNotFoundException{
        mConn = DBUtil.getConnection();
    }
    //Funciones para acceder a la base de datos
    public int add(Usuario usuario){
        try{

            String query = "INSERT INTO Usuario (nombre, apellidos, email, image";
            String values = ") VALUES(?, ?, ?, ?";
            ArrayList<EnrollResult> fingers = usuario.getFingerprints();
            
            for (int i = 0; i < fingers.size(); i++){
                query += ", fingerprint" + (i+1) ;
                values += ", ?";
            }
            query = query + values + ")";
            PreparedStatement stmt = mConn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            Blob blob = usuario.getImage() != null? new SerialBlob(usuario.getImage()):null;

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellidos());
            stmt.setString(3, usuario.getEmail());
            stmt.setBlob(4, blob);
            int index = 5;
            for (int i = 0; i < fingers.size(); i++){
                byte[] bytes = fingers.get(i).getData();
                blob = new SerialBlob(bytes);
                stmt.setBlob(index++, blob);
                
            }
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            int id = rs.getInt(1);
            stmt.close();
            //Agregar los permisos
            TreeSet<Permiso> permisos = usuario.getPermisos();
            query = "INSERT INTO PermisoUsuario (permiso_id, usuario_id) values (?, ?)";
            for (Permiso permiso: permisos){
                stmt = mConn.prepareStatement(query);
                stmt.setInt(permiso.getCode(), id);
                stmt.close();
            }
            mConn.commit();
            return id;
        }
        catch(SQLException exc){
            
        }
        return -1;
    }
    //TODO: remover tambien los permisos de tal usuario
    public boolean remove(int id){
        try {
            PreparedStatement stmt = mConn.prepareStatement("delete from Usuario where id=?");
            stmt.setInt(1, id);
            if (stmt.executeUpdate() > 0){
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAOUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    //TODO: insertar tambien los permisos de los usuarios
    public ArrayList<Usuario> getUsuarios(int from, int rows){
        ArrayList<Usuario> lista = new ArrayList<>();
        try{
            
            String query = "select * from Usuario LIMIT " + from + "," + rows;
            Statement st = mConn.createStatement();
            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);
            // iterate through the java resultset
            while (rs.next())
            {
                Usuario usuario = new Usuario();
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellidos(rs.getString("apellidos"));
                usuario.setEmail(rs.getString("email"));
                usuario.setImage(rs.getBytes("image"));
                for (int i = 1; i <= 10; i++){
                    String column = "fingerprint" + i;
                    if (rs.getBytes(column) != null){
                        EnrollResult fingerprint = new EnrollResult();
                        fingerprint.setCode(EnrollResult.COMPLETE);
                        fingerprint.setData(rs.getBytes(column));
                        usuario.addFingerprint(fingerprint);
                    }
                }
                lista.add(usuario);
            }
            st.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return lista;
    }

    public Usuario findByFingerprint(EnrollResult fingerprint){
        int step = 20;
        int index = 0;
        ArrayList<Usuario> usuarios = this.getUsuarios(index, step);
        while(usuarios.size() > 0){
            //Vemos usuario por usuario 
            for (int i = 0; i < usuarios.size(); i++){
                //Por cada usuario vemos los dedos que estan registrados y hacemos el match
                ArrayList<EnrollResult> fingers = usuarios.get(i).getFingerprints();                
                for (EnrollResult finger: fingers){
                    VerifyResult result = Reader.verify(finger.getData(), fingerprint.getData());
                    if (result.getCode() == VerifyResult.MATCH){
                        return usuarios.get(i);
                    }
                }                 
            }
            index += step;
            usuarios = this.getUsuarios(index, step);
        }        
        return null;
    }
}
