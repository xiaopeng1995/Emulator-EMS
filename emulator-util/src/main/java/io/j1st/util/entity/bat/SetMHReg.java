package io.j1st.util.entity.bat;

/**
 * SetMHReg
 */
public class SetMHReg {
    private String dsn;

    private int Reg12551;

    public void setDsn(String dsn){
        this.dsn = dsn;
    }
    public String getDsn(){
        return this.dsn;
    }
    public void setReg12551(int Reg12551){
        this.Reg12551 = Reg12551;
    }
    public int getReg12551(){
        return this.Reg12551;
    }
}
