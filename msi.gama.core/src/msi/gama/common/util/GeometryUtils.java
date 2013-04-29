/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.common.util;

import java.io.IOException;
import java.util.*;
//import msi.gama.database.SqlConnection;
import msi.gama.database.sql.SqlConnection;
import msi.gama.database.sql.SqlUtils;
import msi.gama.metamodel.shape.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.file.IGamaFile;
import msi.gama.util.graph.IGraph;
import msi.gaml.operators.*;
import msi.gaml.types.GamaGeometryType;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.prep.*;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import com.vividsolutions.jts.triangulate.*;
import com.vividsolutions.jts.triangulate.quadedge.LocateFailureException;

/**
 * The class GamaGeometryUtils.
 * 
 * @author drogoul
 * @since 14 d�c. 2011
 * 
 */
public class GeometryUtils {

	// TODO static CoordinateSequenceFactory csf = new PackedCoordinateSequenceFactory(
	// PackedCoordinateSequenceFactory.DOUBLE, 2);

	public static GeometryFactory factory = new GeometryFactory();
	public static PreparedGeometryFactory pgfactory = new PreparedGeometryFactory();

	// TODO : see the possibility to use new LiteCoordinateSequenceFactory()

	public static GeometryFactory getFactory() {
		return factory;
	}

	// types of geometry
	private final static int NULL = -1;
	private final static int POINT = 0;
	private final static int MULTIPOINT = 1;
	private final static int LINE = 2;
	private final static int MULTILINE = 3;
	private final static int POLYGON = 4;
	private final static int MULTIPOLYGON = 5;

	public static GamaPoint pointInGeom(final Geometry geom, final RandomUtils rand) {
		if ( geom instanceof Point ) { return new GamaPoint(geom.getCoordinate()); }
		if ( geom instanceof LineString ) {
			int i = rand.between(0, geom.getCoordinates().length - 2);
			Coordinate source = geom.getCoordinates()[i];
			Coordinate target = geom.getCoordinates()[i + 1];
			if ( source.x != target.x ) {
				double a = (source.y - target.y) / (source.x - target.x);
				double b = source.y - a * source.x;
				double x = rand.between(source.x, target.x);
				double y = a * x + b;
				return new GamaPoint(x, y);
			}
			double x = source.x;
			double y = rand.between(source.y, target.y);
			return new GamaPoint(x, y);
		}
		if ( geom instanceof Polygon ) {
			Envelope env = geom.getEnvelopeInternal();
			double xMin = env.getMinX();
			double xMax = env.getMaxX();
			double yMin = env.getMinY();
			double yMax = env.getMaxY();
			double x = rand.between(xMin, xMax);
			Coordinate coord1 = new Coordinate(x, yMin);
			Coordinate coord2 = new Coordinate(x, yMax);
			Coordinate[] coords = { coord1, coord2 };
			Geometry line = getFactory().createLineString(coords);
			try {
				line = line.intersection(geom);
			} catch (Exception e) {
				PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
				line =
					GeometryPrecisionReducer.reducePointwise(line, pm).intersection(
						GeometryPrecisionReducer.reducePointwise(geom, pm));

			}
			return pointInGeom(line, rand);
		}
		if ( geom instanceof GeometryCollection ) { return pointInGeom(
			geom.getGeometryN(rand.between(0, geom.getNumGeometries() - 1)), rand); }

		return null;

	}

	private static Coordinate[] minimiseLength(final Coordinate[] coords) {
		GeometryFactory geomFact = getFactory();
		double dist1 = geomFact.createLineString(coords).getLength();
		Coordinate[] coordstest1 = new Coordinate[3];
		coordstest1[0] = coords[0];
		coordstest1[1] = coords[2];
		coordstest1[2] = coords[1];
		double dist2 = geomFact.createLineString(coordstest1).getLength();

		Coordinate[] coordstest2 = new Coordinate[3];
		coordstest2[0] = coords[1];
		coordstest2[1] = coords[0];
		coordstest2[2] = coords[2];
		double dist3 = geomFact.createLineString(coordstest2).getLength();

		if ( dist1 <= dist2 && dist1 <= dist3 ) { return coords; }
		if ( dist2 <= dist1 && dist2 <= dist3 ) { return coordstest1; }
		if ( dist3 <= dist1 && dist3 <= dist2 ) { return coordstest2; }
		return coords;
	}

	public static Coordinate[] extractPoints(final IShape triangle, final Geometry geom, final int degree) {
		Coordinate[] coords = triangle.getInnerGeometry().getCoordinates();
		Coordinate[] c1 = { coords[0], coords[1] };
		Coordinate[] c2 = { coords[1], coords[2] };
		Coordinate[] c3 = { coords[2], coords[3] };
		LineString l1 = getFactory().createLineString(c1);
		LineString l2 = getFactory().createLineString(c2);
		LineString l3 = getFactory().createLineString(c3);
		Coordinate[] pts = new Coordinate[degree];
		if ( degree == 3 ) {
			pts[0] = l1.getCentroid().getCoordinate();
			pts[1] = l2.getCentroid().getCoordinate();
			pts[2] = l3.getCentroid().getCoordinate();
			return minimiseLength(pts);
		} else if ( degree == 2 ) {
			Geometry bounds = geom.getBoundary().buffer(1);
			double val1 = bounds.intersection(l1).getLength() / l1.getLength();
			double val2 = bounds.intersection(l2).getLength() / l2.getLength();
			double val3 = bounds.intersection(l3).getLength() / l3.getLength();
			if ( val1 > val2 ) {
				if ( val1 > val3 ) {
					pts[0] = l2.getCentroid().getCoordinate();
					pts[1] = l3.getCentroid().getCoordinate();
				} else {
					pts[0] = l1.getCentroid().getCoordinate();
					pts[1] = l2.getCentroid().getCoordinate();
				}
			} else {
				if ( val2 > val3 ) {
					pts[0] = l1.getCentroid().getCoordinate();
					pts[1] = l3.getCentroid().getCoordinate();
				} else {
					pts[0] = l1.getCentroid().getCoordinate();
					pts[1] = l2.getCentroid().getCoordinate();
				}
			}
		} else {
			return null;
		}
		return pts;
	}

	public static GamaList<IShape> hexagonalGridFromGeom(final IShape geom, final int nbRows, final int nbColumns) {
		double widthEnv = geom.getEnvelope().getWidth();
		double heightEnv = geom.getEnvelope().getHeight();
		double xmin = geom.getEnvelope().getMinX();
		double ymin = geom.getEnvelope().getMinY();
		double widthHex = widthEnv / (nbColumns * 0.75 + 0.25);
		double heightHex = heightEnv / nbRows;
		GamaList<IShape> geoms = new GamaList<IShape>();
		xmin += widthHex / 2.0;
		ymin += heightHex / 2.0;
		for ( int l = 0; l < nbRows; l++ ) {
			for ( int c = 0; c < nbColumns; c = c + 2 ) {
				GamaShape poly =
					(GamaShape) GamaGeometryType.buildHexagon(widthHex, heightHex, new GamaPoint(xmin + c * widthHex *
						0.75, ymin + l * heightHex));
				// GamaShape poly = (GamaShape) GamaGeometryType.buildHexagon(size, xmin + (c * 1.5)
				// * size, ymin + 2* size*val * l);
				if ( geom.covers(poly) ) {
					geoms.add(poly);
				}
			}
		}
		for ( int l = 0; l < nbRows; l++ ) {
			for ( int c = 1; c < nbColumns; c = c + 2 ) {
				GamaShape poly =
					(GamaShape) GamaGeometryType.buildHexagon(widthHex, heightHex, new GamaPoint(xmin + c * widthHex *
						0.75, ymin + (l + 0.5) * heightHex));
				// GamaShape poly = (GamaShape) GamaGeometryType.buildHexagon(size, xmin + (c * 1.5)
				// * size, ymin + 2* size*val * l);
				if ( geom.covers(poly) ) {
					geoms.add(poly);
				}
			}
		}
		/*
		 * for(int l=0;l<nbColumns;l++){
		 * for(int c=0;c<nbRows;c = c+2){
		 * GamaShape poly = (GamaShape) GamaGeometryType.buildHexagon(size, xmin + ((c +1) * 1.5) *
		 * size, ymin + 2* size*val * (l+0.5));
		 * if (geom.covers(poly))
		 * geoms.add(poly);
		 * }
		 * }
		 */
		return geoms;
	}

	public static List<Geometry> discretisation(final Geometry geom, final double size, final boolean complex) {
		List<Geometry> geoms = new ArrayList<Geometry>();
		if ( geom instanceof GeometryCollection ) {
			GeometryCollection gc = (GeometryCollection) geom;
			for ( int i = 0; i < gc.getNumGeometries(); i++ ) {
				geoms.addAll(discretisation(gc.getGeometryN(i), size, complex));
			}
		} else {
			Envelope env = geom.getEnvelopeInternal();
			double xMax = env.getMaxX();
			double yMax = env.getMaxY();
			double x = env.getMinX();
			double y = env.getMinY();
			GeometryFactory geomFact = getFactory();
			while (x < xMax) {
				y = env.getMinY();
				while (y < yMax) {
					Coordinate c1 = new Coordinate(x, y);
					Coordinate c2 = new Coordinate(x + size, y);
					Coordinate c3 = new Coordinate(x + size, y + size);
					Coordinate c4 = new Coordinate(x, y + size);
					Coordinate[] cc = { c1, c2, c3, c4, c1 };
					Geometry square = geomFact.createPolygon(geomFact.createLinearRing(cc), null);
					y += size;
					try {
						Geometry g = null;
						try {
							g = square.intersection(geom);
						} catch (Exception e) {
							PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
							g =
								GeometryPrecisionReducer.reducePointwise(geom, pm).intersection(
									GeometryPrecisionReducer.reducePointwise(square, pm));
						}
						// geoms.add(g);
						if ( complex ) {
							geoms.add(g);
						} else {
							if ( g instanceof Polygon ) {
								geoms.add(g);
							} else if ( g instanceof MultiPolygon ) {
								MultiPolygon mp = (MultiPolygon) g;
								for ( int i = 0; i < mp.getNumGeometries(); i++ ) {
									if ( mp.getGeometryN(i) instanceof Polygon ) {
										geoms.add(mp.getGeometryN(i));
									}
								}
							}
						}
					} catch (TopologyException e) {}
				}
				x += size;
			}
		}
		return geoms;
	}

	public static GamaList<IShape> triangulation(final IScope scope, final IList<IShape> lines) {
		GamaList<IShape> geoms = new GamaList<IShape>();
		ConformingDelaunayTriangulationBuilder dtb = new ConformingDelaunayTriangulationBuilder();

		Geometry points = GamaGeometryType.geometriesToGeometry(scope, lines).getInnerGeometry();
		double sizeTol = Math.sqrt(points.getEnvelope().getArea()) / 100.0;

		dtb.setSites(points);
		dtb.setConstraints(points);
		dtb.setTolerance(sizeTol);
		GeometryCollection tri = (GeometryCollection) dtb.getTriangles(getFactory());
		int nb = tri.getNumGeometries();
		for ( int i = 0; i < nb; i++ ) {
			Geometry gg = tri.getGeometryN(i);
			geoms.add(new GamaShape(gg));
		}
		return geoms;
	}

	public static GamaList<IShape> triangulation(final IScope scope, final Geometry geom) {
		GamaList<IShape> geoms = new GamaList<IShape>();
		if ( geom instanceof GeometryCollection ) {
			GeometryCollection gc = (GeometryCollection) geom;
			for ( int i = 0; i < gc.getNumGeometries(); i++ ) {
				geoms.addAll(triangulation(scope, gc.getGeometryN(i)));
			}
		} else if ( geom instanceof Polygon ) {
			Polygon polygon = (Polygon) geom;
			double sizeTol = Math.sqrt(polygon.getArea()) / 100.0;
			ConformingDelaunayTriangulationBuilder dtb = new ConformingDelaunayTriangulationBuilder();
			GeometryCollection tri = null;
			try {
				dtb.setSites(polygon);
				dtb.setConstraints(polygon);
				dtb.setTolerance(sizeTol);
				tri = (GeometryCollection) dtb.getTriangles(getFactory());
			} catch (LocateFailureException e) {
				throw new GamaRuntimeException("Impossible to draw Geometry");
			}
			PreparedGeometry pg = pgfactory.create(polygon.buffer(sizeTol, 5, 0));
			PreparedGeometry env = pgfactory.create(pg.getGeometry().getEnvelope());
			int nb = tri.getNumGeometries();
			for ( int i = 0; i < nb; i++ ) {

				Geometry gg = tri.getGeometryN(i);

				if ( env.covers(gg) && pg.covers(gg) ) {
					geoms.add(new GamaShape(gg));
				}
			}
		}
		return geoms;
	}

	public class contraintVertexFactory3D implements ConstraintVertexFactory {

		@Override
		public ConstraintVertex createVertex(final Coordinate p, final Segment constraintSeg) {
			Coordinate c = new Coordinate(p);
			c.z = p.z;
			return new ConstraintVertex(c);
		}

	}

	public static List<LineString> squeletisation(final IScope scope, final Geometry geom) {
		IList<LineString> network = new GamaList<LineString>();
		IList polys = new GamaList(GeometryUtils.triangulation(scope, geom));
		IGraph graph = Graphs.spatialLineIntersection(scope, polys);

		Collection<GamaShape> nodes = graph.vertexSet();
		GeometryFactory geomFact = GeometryUtils.getFactory();
		for ( GamaShape node : nodes ) {
			Coordinate[] coordsArr = GeometryUtils.extractPoints(node, geom, graph.degreeOf(node) / 2);
			if ( coordsArr != null ) {
				network.add(geomFact.createLineString(coordsArr));
			}
		}

		return network;
	}

	public static Geometry buildGeometryJTS(final List<List<List<ILocation>>> listPoints) {
		int geometryType = geometryType(listPoints);
		if ( geometryType == NULL ) {
			return null;
		} else if ( geometryType == POINT ) {
			return buildPoint(listPoints.get(0));
		} else if ( geometryType == LINE ) {
			return buildLine(listPoints.get(0));
		} else if ( geometryType == POLYGON ) {

			return buildPolygon(listPoints.get(0));
		} else if ( geometryType == MULTIPOINT ) {
			int nb = listPoints.size();
			Point[] geoms = new Point[nb];
			for ( int i = 0; i < nb; i++ ) {
				geoms[i] = (Point) buildPoint(listPoints.get(i));
			}
			return getFactory().createMultiPoint(geoms);
		} else if ( geometryType == MULTILINE ) {
			int nb = listPoints.size();
			LineString[] geoms = new LineString[nb];
			for ( int i = 0; i < nb; i++ ) {
				geoms[i] = (LineString) buildLine(listPoints.get(i));
			}
			return getFactory().createMultiLineString(geoms);
		} else if ( geometryType == MULTIPOLYGON ) {
			int nb = listPoints.size();
			Polygon[] geoms = new Polygon[nb];
			for ( int i = 0; i < nb; i++ ) {
				geoms[i] = (Polygon) buildPolygon(listPoints.get(i));
			}
			return getFactory().createMultiPolygon(geoms);
		}
		return null;
	}

	private static Geometry buildPoint(final List<List<ILocation>> listPoints) {
		return getFactory().createPoint(listPoints.get(0).get(0).toCoordinate());
	}

	private static Geometry buildLine(final List<List<ILocation>> listPoints) {
		List<ILocation> coords = listPoints.get(0);
		int nb = coords.size();
		Coordinate[] coordinates = new Coordinate[nb];
		for ( int i = 0; i < nb; i++ ) {
			coordinates[i] = coords.get(i).toCoordinate();
		}
		return getFactory().createLineString(coordinates);
	}

	private static Geometry buildPolygon(final List<List<ILocation>> listPoints) {
		List<ILocation> coords = listPoints.get(0);
		int nb = coords.size();
		Coordinate[] coordinates = new Coordinate[nb];
		for ( int i = 0; i < nb; i++ ) {
			coordinates[i] = coords.get(i).toCoordinate();
		}
		int nbHoles = listPoints.size() - 1;
		LinearRing[] holes = null;
		if ( nbHoles > 0 ) {
			holes = new LinearRing[nbHoles];
			for ( int i = 0; i < nbHoles; i++ ) {
				List<ILocation> coordsH = listPoints.get(i + 1);
				int nbp = coordsH.size();
				Coordinate[] coordinatesH = new Coordinate[nbp];
				for ( int j = 0; j < nbp; j++ ) {
					coordinatesH[j] = coordsH.get(j).toCoordinate();
				}
				holes[i] = getFactory().createLinearRing(coordinatesH);
			}
		}
		Polygon poly = getFactory().createPolygon(getFactory().createLinearRing(coordinates), holes);
		return poly;
	}

	private static int geometryType(final List<List<List<ILocation>>> listPoints) {
		if ( listPoints.size() == 0 ) { return NULL; }
		if ( listPoints.size() == 1 ) { return geometryTypeSimp(listPoints.get(0)); }
		int type = geometryTypeSimp(listPoints.get(0));
		if ( type == POINT ) { return MULTIPOINT; }
		if ( type == LINE ) { return MULTILINE; }
		if ( type == POLYGON ) { return MULTIPOLYGON; }
		return NULL;
	}

	private static int geometryTypeSimp(final List<List<ILocation>> listPoints) {
		if ( listPoints.isEmpty() || listPoints.get(0).isEmpty() ) { return NULL; }
		if ( listPoints.get(0).size() == 1 || listPoints.get(0).size() == 2 &&
			listPoints.get(0).get(0).equals(listPoints.get(0).get(listPoints.size() - 1)) ) { return POINT; }

		if ( !listPoints.get(0).get(0).equals(listPoints.get(0).get(listPoints.size() - 1)) ||
			listPoints.get(0).size() < 3 ) { return LINE; }

		return POLYGON;
	}

	public static GamaList<GamaPoint> locExteriorRing(final Geometry geom, final Double distance) {
		GamaList<GamaPoint> locs = new GamaList<GamaPoint>();
		if ( geom instanceof Point ) {
			locs.add(new GamaPoint(geom.getCoordinate()));
		} else if ( geom instanceof LineString ) {
			double dist_cur = 0;
			int nbSp = geom.getNumPoints();
			Coordinate[] coordsSimp = geom.getCoordinates();
			boolean same = false;
			double x_t = 0, y_t = 0, x_s = 0, y_s = 0;
			for ( int i = 0; i < nbSp - 1; i++ ) {
				if ( !same ) {
					Coordinate s = coordsSimp[i];
					Coordinate t = coordsSimp[i + 1];
					x_t = t.x;
					y_t = t.y;
					x_s = s.x;
					y_s = s.y;
				} else {
					i = i - 1;
				}
				double dist = Math.sqrt(Math.pow(x_s - x_t, 2) + Math.pow(y_s - y_t, 2));
				if ( dist_cur < dist ) {
					double ratio = dist_cur / dist;
					x_s = x_s + ratio * (x_t - x_s);
					y_s = y_s + ratio * (y_t - y_s);
					locs.add(new GamaPoint(x_s, y_s));
					dist_cur = distance;
					same = true;
				} else if ( dist_cur > dist ) {
					dist_cur = dist_cur - dist;
					same = false;
				} else {
					locs.add(new GamaPoint(x_t, y_t));
					dist_cur = distance;
					same = false;
				}
			}
		} else if ( geom instanceof Polygon ) {
			Polygon poly = (Polygon) geom;
			locs.addAll(locExteriorRing(poly.getExteriorRing(), distance));
			for ( int i = 0; i < poly.getNumInteriorRing(); i++ ) {
				locs.addAll(locExteriorRing(poly.getInteriorRingN(i), distance));
			}
		}
		return locs;
	}

	// ---------------------------------------------------------------------------------------------
	// Thai.truongminh@gmail.com
	// Created date:24-Feb-2013: Process for SQL - MAP type
	// Modified: 29-Apr-2013

	public static Envelope computeEnvelopeFromSQLData(final IScope scope, final Map<String, Object> bounds) 
	{
//		Map<String, Object> params = bounds;
//
//		String dbtype = (String) params.get("dbtype");
//		String host = (String) params.get("host");
//		String port = (String) params.get("port");
//		String database = (String) params.get("database");
//		String user = (String) params.get("user");
//		String passwd = (String) params.get("passwd");
//		String crs = (String) params.get("crs");
//		String srid = (String) params.get("srid");
//		Boolean longitudeFirst = params.get("longitudeFirst") == null ? true : (Boolean) params.get("longitudeFirst");
//		SqlConnection sqlConn;
//		Envelope env = null;
//		// create connection
//		if ( dbtype.equalsIgnoreCase(SqlConnection.SQLITE) ) {
//			String DBRelativeLocation = scope.getSimulationScope().getModel().getRelativeFilePath(database, true);
//
//			// sqlConn=new SqlConnection(dbtype,database);
//			sqlConn = new SqlConnection(dbtype, DBRelativeLocation);
//		} else {
//			sqlConn = new SqlConnection(dbtype, host, port, database, user, passwd);
//		}
//
//		GamaList<Object> gamaList = sqlConn.selectDB((String) params.get("select"));
//
//		try {
//			env = SqlConnection.getBounds(scope, gamaList);
//			double latitude = env.centre().x;
//			double longitude = env.centre().y;
//
//			if ( crs != null || srid != null ) {
//				GisUtils gis = scope.getSimulationScope().getGisUtils();
//				if ( crs != null ) {
//					gis.setTransformCRS(crs, latitude, longitude);
//				} else {
//					gis.setTransformCRS(srid, longitudeFirst, latitude, longitude);
//				}
//				env = gis.transform(env);
//
//			}
//		} catch (IOException e) {
//			throw new GamaRuntimeException(e);
//		}
//		return env;
// Edit 29/April/2013
//---------------------------------------------------------------------------------------------------------

		Map<String, Object> params = bounds;
		String crs = (String) params.get("crs");
		String srid = (String) params.get("srid");
		Boolean longitudeFirst = params.get("longitudeFirst") == null ? true : (Boolean) params.get("longitudeFirst");
		SqlConnection sqlConn;
		Envelope env = null;
		// create connection
		try {
			sqlConn = SqlUtils.createConnectionObject(scope,bounds);
			//get data 
			GamaList<Object> gamaList = sqlConn.selectDB((String) params.get("select"));
			env = SqlConnection.getBounds(scope, gamaList);
			double latitude = env.centre().x;
			double longitude = env.centre().y;
			if ( crs != null || srid != null ) {
				GisUtils gis = scope.getSimulationScope().getGisUtils();
				if ( crs != null ) {
					gis.setTransformCRS(crs, latitude, longitude);
				} else {
					gis.setTransformCRS(srid, longitudeFirst, latitude, longitude);
				}
				env = gis.transform(env);
			}
		} catch (IOException e) {
			throw new GamaRuntimeException("GeometryUtils.computeEnvelopeFromSQLData:" + e.toString());
		}
		//GuiUtils.debug("GeometryUtils.computeEnvelopeFromSQLData.Envelop:"+env.toString());
		return env;
//----------------------------------------------------------------------------------------------------
	}

	public static Envelope computeEnvelopeFrom(final IScope scope, final Object obj) {
		Envelope result = null;
		if ( obj instanceof Number ) {
			double size = ((Number) obj).doubleValue();
			result = new Envelope(0, size, 0, size);
		} else if ( obj instanceof ILocation ) {
			ILocation size = (ILocation) obj;
			result = new Envelope(0, size.getX(), 0, size.getY());
		} else if ( obj instanceof IShape ) {
			result = ((IShape) obj).getEnvelope();
		} else if ( obj instanceof Envelope ) {
			result = (Envelope) obj;
		} else if ( obj instanceof String ) {
			result = computeEnvelopeFrom(scope, Files.from(scope, (String) obj));
		} else if ( obj instanceof Map ) {
			result = computeEnvelopeFromSQLData(scope, (Map) obj);
		} else if ( obj instanceof IGamaFile ) {
			result = ((IGamaFile) obj).computeEnvelope(scope);
		} else if ( obj instanceof IList ) {
			Envelope boundsEnv = null;
			for ( Object bounds : (IList) obj ) {
				Envelope env = computeEnvelopeFrom(scope, bounds);
				if ( boundsEnv == null ) {
					boundsEnv = env;
				} else {
					boundsEnv.expandToInclude(env);
				}
			}
			result = boundsEnv;
		}
		return result;
	}
}
