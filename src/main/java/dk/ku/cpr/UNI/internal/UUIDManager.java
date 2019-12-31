package dk.ku.cpr.UNI.internal;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsCreatedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

public class UUIDManager implements AddedNodesListener, SessionLoadedListener, RowsCreatedListener {
	
	private static final String NAMESPACE = "UNI";
	private static final String COLUMN_NAME = "UUID";
	private static long nextUUID=1;
	
	private List<CyTable> nodeTables;
	
	public UUIDManager() {
		nodeTables = new ArrayList<>();
	}

	@Override
	public void handleEvent(AddedNodesEvent e) {
		CyTable nodeTable = e.getSource().getDefaultNodeTable();
		
		if(!nodeTables.contains(nodeTable)) {
			nodeTables.add(nodeTable);
		}
		
		if(nodeTable.getColumn(NAMESPACE, COLUMN_NAME) == null) {
			nodeTable.createColumn(NAMESPACE, COLUMN_NAME, Long.class, true);
		}
		
		for(CyNode cyNode : e.getPayloadCollection()) {
			CyRow nodeRow = nodeTable.getRow(cyNode.getSUID());
			
			if(nodeRow.get(NAMESPACE, COLUMN_NAME, Long.class) == null) {
				nodeRow.set(NAMESPACE, COLUMN_NAME, nextUUID++);
			}
		}
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// We have to read twice, first to set the nextID, then to assign ID to unassigned nodes
		for(CyNetwork network : e.getLoadedSession().getNetworks()) {
			CyTable nodeTable = network.getDefaultNodeTable();
			
			if(!nodeTables.contains(nodeTable)) {
				nodeTables.add(nodeTable);
			}
			
			if(nodeTable.getColumn(NAMESPACE, COLUMN_NAME) == null) {
				continue;
			}
			
			for(CyRow nodeRow : nodeTable.getAllRows()) {
				Long uuid = nodeRow.get(NAMESPACE, COLUMN_NAME, Long.class); 
				if(uuid != null) {
					uuid++;
					nextUUID = (uuid > nextUUID ? uuid : nextUUID);
				}
			}
		}
		
		// We assign IDs
		for(CyNetwork network : e.getLoadedSession().getNetworks()) {
			CyTable nodeTable = network.getDefaultNodeTable();
			
			if(nodeTable.getColumn(NAMESPACE, COLUMN_NAME) == null) {
				nodeTable.createColumn(NAMESPACE, COLUMN_NAME, Long.class, true);
			}
			
			for(CyRow nodeRow : nodeTable.getAllRows()) {
				if(nodeRow.get(NAMESPACE, COLUMN_NAME, Long.class) == null) {
					nodeRow.set(NAMESPACE, COLUMN_NAME, nextUUID++);
				}
			}
		}
	}

	@Override
	public void handleEvent(RowsCreatedEvent e) {
		// This method handle the copy of nodes
		
		if(nodeTables.contains(e.getSource())) {
			CyTable nodeTable = e.getSource();
			
			// It's always best to check
			if(nodeTable.getColumn(NAMESPACE, COLUMN_NAME) == null) {
				nodeTable.createColumn(NAMESPACE, COLUMN_NAME, Long.class, true);
			}
			
			// The Payload Collection of a RowsCreatedEvent contains the SUID of created rows
			for(Object nodeSUID : e.getPayloadCollection()) {
				CyRow nodeRow = nodeTable.getRow(nodeSUID);
				
				// We give a new UUID even if there is one existing
				nodeRow.set(NAMESPACE, COLUMN_NAME, nextUUID++);
			}
		}
	}

}
