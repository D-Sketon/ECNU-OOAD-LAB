package gizmoball.engine.collision.manifold;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndexedManifoldPointId implements ManifoldPointId {

    private int referenceEdgeIndex;

    /**
     * The index of the edge on the incident convex
     */
    private int incidentEdgeIndex;

    /**
     * The index of the vertex on the incident convex
     */
    private int incidentVertexIndex;

    /**
     * Whether the reference and incident features flipped
     */
    private boolean flipped;


}
