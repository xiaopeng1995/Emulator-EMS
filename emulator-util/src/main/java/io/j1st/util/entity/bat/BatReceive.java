package io.j1st.util.entity.bat;

import java.util.List;
import java.util.Map;

/**
 * 电池接收指令实体类
 */
public class BatReceive {
    private List<Map> SetMHReg ;

    public void setSetMHReg(List<Map> SetMHReg){
        this.SetMHReg = SetMHReg;
    }
    public List<Map> getSetMHReg(){
        return this.SetMHReg;
    }
}
