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
import java.sql.SQLException;
import uaz.fingerprint.EnrollResult;
import uaz.fingerprint.Reader;
import uaz.fingerprint.ReaderListener;

/**
 *
 * @author xmbeat
 */
public class TestDAO implements ReaderListener {
    static DAOUsuario dao;
    public static void main(String args[]) throws SQLException, ClassNotFoundException{
        dao = new DAOUsuario();
        Reader reader = Reader.getDefault();
        reader.setDaemon(false);
        reader.open();
        reader.startCapture();
        reader.addListener(new TestDAO());
    }

    @Override
    public void onStartCapture(Reader reader) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onCapture(Reader reader, EnrollResult result) {
        Usuario usuario = dao.findByFingerprint(result);
        System.out.println(usuario);
    }

    @Override
    public void onStopCapture(Reader reader) {
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onOpen(Reader reader) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onClose(Reader reader) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onError(Reader reader, int code) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
