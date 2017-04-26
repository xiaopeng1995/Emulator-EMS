模拟器总程序说明：

模块描述描述：

    emulator-data：
        模拟器主程序，负责模拟硬件数据上发，接收指令。

    emulator-http：
        模拟器WEB相关接口，和一些HTTP接口。。

    emulator-end：
        模拟终端控制模拟数据，下发指令。

    emulator-mongo：
        模拟器跟数据库的处理方法。

    emulator-util：
        模拟器所有模块常用的工具。

//        //启动PV系统数据任务
//        int pvagunt = 0;
//
//        //获取所有需要运行ems的Agentid
//        List<String> pvAgentall = islocal ? new ArrayList<>() : mogo.findEmulatorAgentInfoBy(1, 0);
//        if (islocal) {
//            pvAgentall.add(emulatorConfig.getString("pvagent_id"));
//        }
//        for (String pvAgentId : pvAgentall) {
//            List<Agent> pvagents = new ArrayList<>();
//            try {
//                pvagents.add(mogo.getAgentsById(new ObjectId(pvAgentId)));
//            } catch (NullPointerException e) {
//                logger.info("agentID不存在跳过:" + pvAgentId);
//            } catch (IllegalArgumentException es) {
//                logger.info("没有PV系统启动的ID");
//            }
//            for (Agent pvagent : pvagents) {
//                pvagunt++;
//                String agentID = pvagent.getId().toString();
//                mqtt = new MqttClient(mqttConfig.getString("mqtt.url"), pvagent.getId().toHexString(), persistence);
//                options = new MqttConnectOptions();
//                options.setUserName(pvagent.getId().toHexString());
//                options.setPassword(pvagent.getToken().toCharArray());
//                //add now data
//                if (dmogo.findGendDataByTime(agentID, "pVPower") == null)
//                    pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 1, EmsJob.pvcloud());
//                //add predict data
//                if (dmogo.findycdata(agentID, Integer.parseInt(date.substring(0, 8)))) {
//                    pVpredict.PVInfo(date.substring(0, 8) + "000000", agentID, 0, EmsJob.pvcloud());
//
//                }
//                //mqtt
//                MqttConnThread mqttConnThread = new MqttConnThread(mqtt, options, mogo, dmogo, emulatorConfig);
//                //seve mqtt info
//                Registry.INSTANCE.saveSession(agentID, mqttConnThread);
//                //add a agent mqtt Send and receive sever
//                Registry.INSTANCE.startThread(mqttConnThread);
//                Thread.sleep(90);
//                //设置间隔时间
//                Registry.INSTANCE.saveKey(agentID + "_jgtime", defaultTime);
//                //初始话数据
//                deleteNum = dmogo.deleteGendDataByTime(agentID);
//                logger.info("{}:已删除历史数据{}条", agentID, deleteNum);
//                mogo.updateEmulatorRegister(agentID, "created_at", new Date());
//                //防止MQTT先启动线程做判断
//
//                PVjob thread = new PVjob(agentID, "upstream", mogo, dmogo);
//                // Registry.INSTANCE.startJob(thread);
//                threadallPv.add(thread);
//                Registry.INSTANCE.saveKey(agentID + "_Job", thread);
//                logger.debug(agentID + "PV所有设备准备成功开始上传数据..");
//
//            }
//        }









