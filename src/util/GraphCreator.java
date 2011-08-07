package util;

import java.io.StringWriter;
import org.gephi.graph.api.*;
import org.gephi.io.database.drivers.MySQLDriver;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.gephi.io.importer.plugin.database.ImporterEdgeList;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.layout.spi.Layout;
import org.gephi.project.api.*;
import org.openide.util.Lookup;

/**
 * 
 * @author yorn Helper class to create a graph from an sql database. Uses gephi.
 * 
 */
public class GraphCreator {
	private EdgeListDatabaseImpl db;
	private Layout layout = null;

	public GraphCreator(Config conf) {
		db = new EdgeListDatabaseImpl();
		db.setDBName(conf.getDatabase());
		db.setHost(conf.getHost());
		db.setUsername(conf.getUser());
		db.setPasswd(conf.getPass());
		db.setSQLDriver(new MySQLDriver());
		db.setPort(conf.getPort());
		db.setNodeQuery("SELECT nodes.id AS id, nodes.label AS label, nodes.url FROM nodes");
		db.setEdgeQuery("SELECT edges.source AS source, edges.target AS target, edges.name AS label, edges.weight AS weight FROM edges");
	}

	/**
	 * Create graph from database.
	 * 
	 * @return String contains the gexf
	 */
	public String createGraphFromSQL() {
		if (db == null) {
			System.out.println("[X] DB not configured!");
			return "";
		}
		ProjectController pc = Lookup.getDefault().lookup(
				ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		// Get controllers and models
		ImportController importController = Lookup.getDefault().lookup(
				ImportController.class);
		GraphModel graphModel = Lookup.getDefault()
				.lookup(GraphController.class).getModel();
		// AttributeModel attributeModel =
		// Lookup.getDefault().lookup(AttributeController.class).getModel();

		ImporterEdgeList edgeListImporter = new ImporterEdgeList();
		Container container = importController.importDatabase(db,
				edgeListImporter);
		container.setAllowAutoNode(false); // Don't create missing nodes
		 // Force UNDIRECTED
		container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);

		// Append imported data to GraphAPI
		importController.process(container, new DefaultProcessor(), workspace);

		// Layout - 100 Yifan Hu passes
		layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();
		for (int i = 0; i < 100 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}

		ExportController ec = Lookup.getDefault()
				.lookup(ExportController.class);
		Exporter exporter = ec.getExporter("gexf");
		CharacterExporter characterExporter = (CharacterExporter) exporter;
		StringWriter stringWriter = new StringWriter();
		ec.exportWriter(stringWriter, characterExporter);
		String result = stringWriter.toString();
		// The Explorer cannot use "for", so we need to use "id"
		result = result.replaceAll("for=\"url\"", "id=\"url\"");
		return result;
	}
	
	public void setLayout(String layout) {
		if (layout == "fruchterman") {
			this.layout = new FruchtermanReingold(null);
		}
		else if (layout == "atlas") {
			this.layout = new ForceAtlasLayout(null);
		}
		else {
			this.layout = new YifanHuLayout(null, new StepDisplacement(1f));
		}
	}

}
