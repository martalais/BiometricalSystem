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
package fingerprint.model;

import java.util.ArrayList;
import java.util.TreeSet;
import uaz.fingerprint.EnrollResult;

/**
 *
 * @author xmbeat
 */
public class Usuario {

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

   
    public String getMatricula() {
        return matricula;
    }

 
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

   
    public byte[] getImage() {
        return image;
    }

   
    public void setImage(byte[] image) {
        this.image = image;
    }

    public ArrayList<EnrollResult> getFingerprints() {
        return fingers;
    }
    public boolean addFingerprint(EnrollResult result){
        if (result != null && result.getCode() == EnrollResult.COMPLETE){
            this.fingers.add(result);
            return true;
        }
        return false;
    }
    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public boolean addPermiso(Permiso permiso){
        return permisos.add(permiso);
    }
    
    public boolean removePermiso(Permiso permiso){
        return permisos.remove(permiso);
    }
    public TreeSet<Permiso> getPermisos(){
        return permisos;
    }
   
    private String nombre;
    private String apellidos;
    private String email;
    private String matricula;
    private byte[] image;   
    private TreeSet<Permiso> permisos = new TreeSet<>();
    private ArrayList<EnrollResult> fingers = new ArrayList<>();
    private int id;
}
