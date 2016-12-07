package io.j1st.util.entity.bat;

import java.util.List;

/**
 * 电池接收指令实体类
 */
public class BatReceive {
    private List<SetMHReg> SetMHReg ;

    public void setSetMHReg(List<SetMHReg> SetMHReg){
        this.SetMHReg = SetMHReg;
    }
    public List<SetMHReg> getSetMHReg(){
        return this.SetMHReg;
    }
}
