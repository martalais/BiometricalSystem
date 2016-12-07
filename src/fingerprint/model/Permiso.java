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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author xmbeat
 */
public class Permiso implements Comparable<Permiso>{
    private final StringProperty mDescription;
    private final IntegerProperty mCode;
    private final BooleanProperty mEnabled;
    public Permiso(int code, String description){
        this(code, description, false);
    }
    public Permiso(int code, String description, boolean enabled){
        mDescription = new SimpleStringProperty(description);
        mCode = new SimpleIntegerProperty(code);
        mEnabled = new SimpleBooleanProperty(enabled);
    }
    public BooleanProperty enabledProperty(){
        return mEnabled;
    }
    public StringProperty descriptionProperty(){
        return mDescription;
    }
    public IntegerProperty codeProperty(){
        return mCode;
    }
    public void setCode(int code){
        mCode.set(code);
    }
    public int getCode(){
        return mCode.get();
    }
    public void setDescription(String description){
        mDescription.set(description);
    }
    public String getDescription(){
        return mDescription.get();
    }
    public void setEnabled(boolean value){
        mEnabled.set(value);
    }
    public boolean getEnabled(){
        return mEnabled.get();
    }

    @Override
    public int compareTo(Permiso o) {
        if (o != null){
            return o.getCode() - this.getCode();
        }
        return -1;
    }
}
