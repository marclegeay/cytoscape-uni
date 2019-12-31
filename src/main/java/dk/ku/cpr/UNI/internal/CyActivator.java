package dk.ku.cpr.UNI.internal;

import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.RowsCreatedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.events.SessionLoadedListener;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		UUIDManager manager = new UUIDManager();
		registerService(context, manager, AddedNodesListener.class);
		registerService(context, manager, SessionLoadedListener.class);
		registerService(context, manager, RowsCreatedListener.class);
	}
}
