package io.j1st.util.entity.data;

/**
 * Values
 */
public class Values {
    /*《DLT645-—2007多功能电能表通信协议.pdf》*/

    public static final String TotWh="TotWh";           //Total?Real Energy (当前)组合有功总电能
    public static final String TotWhImp="TotWhImp";     //Total?Real Energy Imported 正向有功总电能
    public static final String TotWhExp="TotWhExp";     //Total?Real Energy Exported 反向有功总电能
    public static final String W="W";                   //Total Real Power 瞬时总有功功率
    public static final String VAR="VAR";               //Reactive Power 瞬时总无功功率
    // public static final String PF="";                 //Power Factor 总功率因数
    public static final String Hz="Hz";                 //Frequency 电网频率
    public static final String Evt="Evt";               //Meter?Event Flags 负载电表事件标志
    public static final String WHRtg="WHRtg"; //铭牌标识的DC存储总能量
    public static final String MaxRsvPct="MaxRsvPct";
    public static final String MinRsvPct="MinRsvPct";
    public static final String SoC="SoC";
    public static final String SoCNpMaxPct="SoCNpMaxPct";
    public static final String SoCNpMinPct="SoCNpMinPct";
    /*储能侧Key*/
    // ATTRIBUTES
    public static final String TCAh="TCAh";//TCAh"="";           //Total charge power(Ah)
    public static final String TCkWh="TCkWh";//TCkWh"="";         //Total charge power(kWh)
    public static final String TCh="TCh";//TCh"="";             //Total charge Time(h)
    public static final String TC="TC";//TC"="";               //Total charge times
    public static final String TDAh="TDAh";//TDAh"="";           //Total discharge power(Ah)
    public static final String TDkWh="TDkWh";//TDkWh"="";         //Total discharge power(kWh)
    public static final String TDh="TDh";//TDh"="";             //Total discharge Time(h)
    public static final String TD="TD";//TD"="";               //Total discharge times
    public static final String DCAh="DCAh";//DCAh"="";           //Daily charge power(Ah)
    public static final String DCkWh="DCkWh";//DCkWh"="";         //Daily charge power(kWh)
    public static final String DCm="DCm";//DCm"="";             //Daily charge Time(min)
    public static final String DC="DC";//DC"="";               //Daily charge times
    public static final String DDAh="DDAh";//DDAh"="";           //Daily discharge power(Ah)
    public static final String DDkWh="DDkWh";//DDkWh"="";         //Daily discharge power(kWh)
    public static final String DDm="DDm";//DDm"="";             //Daily discharge Time(min)
    public static final String DD="DD";//DD"="";               //Daily discharge times
    public static final String UAB="UAB";//UAB"="";             //Line Voltage Uab
    public static final String UBC="UBC";//UBC"="";             //Line Voltage Ubc
    public static final String UCA="UCA";//UCA"="";             //Line Voltage Uca
    public static final String FAC="FAC";//FAC"="";             //Grid frequent
    public static final String IA="IA";//IA"="";               // Phase A current
    public static final String IB="IB";//IB"="";               // Phase B current
    public static final String IC="IC";//IC"="";               //Phase C current
    //public static final String Tmod="";//Tmod"="";           //Module Temperature
    //public static final String Tamb="";//Tamb"="";           //Environment Temperature
    public static final String BV="BV";//BV"="";               //Battery Voltage 电池电压
    public static final String BI="BI";//BI"="";               //Battery Current 电池电流
    public static final String SAC="SAC";//SAC"="";             //Apparent power S  视在功率
    public static final String PDC="PDC";//PDC"="";             //Input power from DC side DC端输入功率
    public static final String PAC="PAC";//PAC"="";             //Active power from inverter 来自逆变器的有功功率
    public static final String QAC="QAC";//QAC"="";             //Reactive power from inverter 来自逆变器的无功功率
    public static final String EFF="EFF";//EFF"="";             //Efficiency 效率
    public static final String PF="PF";//PF"="";               //Power factor 功率因素
    public static final String FaultV="FaultV";//FaultV"="";       //错误码
    public static final String FaultD="FaultD";//FaultD"="";       // 错误描述
    public static final String FaultT="FaultT";//FaultT"="";       //错误发送时间
    public static final String WarnV="WarnV";//WarnV"="";         //警告码
    public static final String WarnD="WarnD";//WarnD"="";         //警告描述
    public static final String WarnT="WarnT";//WarnT"="";         //警告发送时间
    //PV侧数据
    public static final String TYield="TYield";
    public static final String Pac="Pac";
}
