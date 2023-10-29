package net.floodlightcontroller.statistics;


import java.util.*;

import net.floodlightcontroller.core.IOFSwitch;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.DatapathId;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.internal.IOFSwitchService;


import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;

public class FlowStatCollector implements Runnable {

    private IOFSwitchService switchService;


    public FlowStatCollector(FloodlightModuleContext context) {
        switchService = context.getServiceImpl(IOFSwitchService.class);
    }

    @Override
    public void run() {
        Map<DatapathId, List<OFFlowStatsReply>> replies = getSwitchStatistics(switchService.getAllSwitchDpids(), OFStatsType.FLOW);

        for (Map.Entry<DatapathId, List<OFFlowStatsReply>> entry : replies.entrySet()) {
            DatapathId switchId = entry.getKey();
            List<OFFlowStatsReply> flowStatsReplies = entry.getValue();

            for (OFFlowStatsReply flowStatsReply : flowStatsReplies) {
                for (OFFlowStatsEntry flowStatsEntry : flowStatsReply.getEntries()) {
                    System.out.println("Switch ID: " + switchId + ", Flow Stats: " + flowStatsEntry);
                }
            }
        }
    }

    // Método para obtener las estadísticas de los switches
    private Map<DatapathId, List<OFFlowStatsReply>> getSwitchStatistics(Set<DatapathId> switchDpids, OFStatsType statsType) {
        Map<DatapathId, List<OFFlowStatsReply>> statistics = new HashMap<>();

        for (DatapathId dpid : switchDpids) {
            IOFSwitch sw = switchService.getSwitch(dpid);
            if (sw != null && sw.getOFFactory().getVersion().compareTo(OFVersion.OF_12) == 0) {
                List<OFFlowStatsReply> replies = new ArrayList<>();
                // Enviar solicitud de estadísticas para OpenFlow 1.2
                OFFlowStatsRequest flowStatsRequest = sw.getOFFactory().buildFlowStatsRequest()
                        .setMatch(sw.getOFFactory().buildMatch().build())
                        .setOutPort(OFPort.ANY)
                        .setTableId(TableId.ALL)
                        .build();
                sw.write(flowStatsRequest);
                statistics.put(dpid, replies);
            }
        }

        return statistics;
    }


}

