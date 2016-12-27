package io.j1st.test;

import io.j1st.storage.MongoStorage;
import io.j1st.test.util.Util;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by xiaopeng on 2016/12/23.
 */
public class Interface666 extends JFrame implements ActionListener {

    //提示文本
    private JTextField show[] = new JTextField[2];
    //log
    JTextArea logapp = new JTextArea("");
    //滚动条
    JScrollPane jsp;
    //块
    JPanel topnew[] = new JPanel[9];
    //下发指令
    private JPanel downvalue = new JPanel();
    JTextField time=new JTextField("30");
    JTextField ttime=new JTextField("0");
    JTextField Reg12551=new JTextField("666");
    // TOP块导航
    private JPanel topPanel = new JPanel();
    private MongoStorage mogo;
    //data
    private JTabbedPane jtptop = new JTabbedPane();
    private JTextField TCkWh = new JTextField();
    /**
     * 构造函数
     */
    public Interface666(MongoStorage mogo) {
        super();
        this.mogo = mogo;

        // 初始化计算器
        init();
        // 设置计算器的背景颜色
        this.setBackground(Color.LIGHT_GRAY);
        this.setTitle("模拟器配置");
        // 在屏幕(500, 300)坐标处显示计算器
        int x = Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 500;
        int y = Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 350;
        this.setBounds(x, y, 1000, 700);
        this.setBackground(Color.black);
        // 不许修改计算器的大小
        //this.setResizable(true);
        // 使计算器中各组件大小合适
        //this.pack();
    }

    private void init() {
        //product_id
        show[0] = new JTextField("Agent ID :");
        //可写入
        show[1] = new JTextField("");
        for (int i = 0; i < show.length; i++) {
            show[i].setHorizontalAlignment(JTextField.LEFT);
            show[i].setEditable(false);
            show[i].setBackground(Color.WHITE);
        }
        show[1].setEditable(true);
        show[1].setSize(0, 0);
        // 最上面的块
        JPanel top = new JPanel();
        top.setLayout(null);
        show[0].setBackground(Color.decode("#EEE9BF"));
        show[0].setBounds(5, 0, 80, 30);
        top.add("North", show[0]);
        show[1].setBackground(Color.decode("#EEE9BF"));
        show[1].setBounds(85, 0, 300, 30);
        top.add("productId", show[1]);
        JButton online = new JButton("连接");
        online.addActionListener(this);
        online.setBounds(390, 0, 100, 30);
        top.add("North", online);
        downvalue.setBounds(-370, 32, 1000, 500);
        //时间下发
        JTextField timeshow=new JTextField("间隔时间:");
        timeshow.setBackground(Color.decode("#EEE9BF"));
        timeshow.setEditable(true);
        downvalue.setBackground(Color.decode("#EEE9BF"));
        downvalue.add(timeshow);
        downvalue.add(time);
        downvalue.add(ttime);
        JButton downubutton = new JButton("GO");

        //指令下发
        JTextField pwshow=new JTextField("充放电指令:");
        pwshow.setBackground(Color.decode("#EEE9BF"));
        pwshow.setEditable(true);
        downvalue.setBackground(Color.decode("#EEE9BF"));
        downvalue.add(pwshow);
        downvalue.add(Reg12551);

        //
        downvalue.add(downubutton);
        top.add("downvalue", downvalue);
        top.setBackground(Color.decode("#EEE9BF"));

        // 日志块
        JPanel calmsPanel = new JPanel();
        // 用网格布局管理器，5行，1列的网格，网格之间的水平方向间隔为3个象素，垂直方向间隔为3个象素
        calmsPanel.setLayout(new GridLayout(1, 1, 0, 0));
        JTabbedPane jtp = new JTabbedPane();
        jsp = new JScrollPane(logapp);
        jtp.add("日志", jsp);
        calmsPanel.add(jtp);
        // 整体布局
        setmenu();
        getContentPane().setLayout(new GridLayout(3, 1, 3, 3));
        getContentPane().add(top, 0);
        getContentPane().add(topPanel, 1);
        getContentPane().add(calmsPanel, 2);

        //  getContentPane().setBounds(100,100,1000,1000);
        //低导航
        JToolBar jtoolbar = new JToolBar();
        JLabel jl = new JLabel("state");
        jtoolbar.add(jl);
        // getContentPane().add(jtoolbar, 3);
    }

    public void actionPerformed(ActionEvent e) {
        // 获取事件源的标签
        String label = e.getActionCommand();
        if (label.equals("连接")) {
            // 用户按了"Backspace"键
            handleBackspace();
        }
    }

    /**
     * 处理Backspace键被按下的事件
     */
    private void handleBackspace() {
        String text = show[1].getText();
        Document d = mogo.findEmulatorDate(text);
        if (d != null) {
            logapp.append(Util.getCurrentDataString() + "----Agent:" + text + "连接成功!" + "\n");
            //downvalue.setText(d.toJson());
            TCkWh.setText("TCkWh:"+d);
        } else {
            logapp.append(Util.getCurrentDataString() + "----Agent:错误不存在的Agent\n");
        }
    }


    private void setmenu() {
            topPanel.setLayout(new GridLayout(1, 1, 0, 0));
            //storage001内容块
            JPanel storage001 = new JPanel();
            storage001.setLayout(null);
            TCkWh.setBounds(0, 0, 1000, 100);
            TCkWh.setText("");
            storage001.add(TCkWh, 0);
            storage001.add(new JTextField("TCkWh_value"), 1);
            jtptop.add("storage001", storage001);
            jtptop.add("storage002", new JButton("2"));
            jtptop.add("graid", new JButton("2"));
            jtptop.add("PV", new JButton("2"));
            jtptop.add("Load", new JButton("2"));
            topPanel.add(jtptop);

    }
}
