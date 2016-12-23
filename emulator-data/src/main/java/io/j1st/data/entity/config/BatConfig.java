package io.j1st.data.entity.config;

import io.j1st.util.util.GttRetainValue;

import java.util.Random;

/**
 * 电池参数
 */
public class BatConfig {
    //随机配置值
    public final Double Ran = GttRetainValue.getRealVaule(Math.random(), 3).doubleValue();
    //分布式能源设备类型。
    public static final String DERTyp = "DERTyp";
    //铭牌标识的DC存储总能量 A.h
    public final Double WHRtg = 30.0 + Ran;
    //uint16	W	Maximum rate of energy transfer into the storage device.最大充电功率
    public final Double WMaxChaRte = -0.666;
    //uint16	W	Maximum rate of energy transfer out of the storage device.最大放电功率
    public final Double WMaxDisChaRte = 1.0;
    //uint16	%WHRtg	Manufacturer maximum state of charge, expressed as a percentage.最大充电百分比
    public final Double SoCNpMaxPct = 0.95;
    //uint16	%WHRtg	Manufacturer minimum state of charge, expressed as a percentage.最小放电电百分比
    public final Double SoCNpMinPct = 0.05;
    //uint16	%WHRtg	Setpoint for maximum reserve for storage as a percentage of the nominal maximum storage.  实际使用的最大存储量，以标称存储总能量的百分比表示。
    public final Double MaxRsvPct = 0.8;
    //uint16	%WHRtg	Setpoint for minimum reserve for storage as a percentage of the nominal minimum storage.  实际使用的最小存储量，以标称存储总能量的百分比表示。
    public final Double MinRsvPct = 0.3;
    public final Double SoC = Ran < 0.2 ? 0.2 : Ran > 0.8 ? 0.8 : Ran;


}
