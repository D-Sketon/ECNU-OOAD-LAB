/*
 * Copyright (c) 2010-2021 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of the copyright holder nor the names of its contributors may be used to endorse or
 *     promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gizmoball.engine.collision.contact;

import gizmoball.engine.collision.Matrix22;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.collision.manifold.ManifoldPoint;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
public class ContactConstraint {
	/** The collision pair */
	private Pair<PhysicsBody,PhysicsBody> pair;

	private List<SolvableContact> contacts;

	/** The penetration normal */
	private Vector2 normal;

	/** The tangent of the normal */
	private Vector2 tangent;

	/** The coefficient of friction */
	private double friction;

	/** The coefficient of restitution */
	private double restitution;

	/** The minimum velocity at which to apply restitution */
	private double restitutionVelocity;

	/** Whether the contact is a sensor contact or not */
	private boolean sensor;

	/** The surface speed of the contact patch */
	private double tangentSpeed;

	/** True if the contact should be evaluated */
	private boolean enabled;

	/** The number of contacts to solve */
	private int size;

	/** The K matrix for block solving a contact pair */
	private Matrix22 K;

	/** The inverse of the {@link #K} matrix */
	private Matrix22 invK;

	public void update(Manifold manifold) {

		PhysicsBody body1 = this.pair.getKey();
		PhysicsBody body2 = this.pair.getValue();

		Vector2 normal = manifold.getNormal();
		this.normal.x = normal.x;
		this.normal.y = normal.y;

		this.tangent.x = normal.y;
		this.tangent.y = -normal.x;

		this.friction = this.getMixedFriction(body1, body2);
		this.restitution = this.getMixedRestitution(body1, body2);
		this.restitutionVelocity = this.getMixedRestitutionVelocity(body1, body2);
		this.sensor = body1.isTrigger() || body2.isTrigger();

		this.tangentSpeed = 0;
		this.enabled = true;

		List<ManifoldPoint> points = manifold.getPoints();
		int mSize = points.size();
		List<SolvableContact> contacts = new ArrayList<SolvableContact>(mSize);
		for (ManifoldPoint point : points) {
			SolvableContact newContact = new SolvableContact(point.getId(),
					point.getPoint(),
					point.getDepth(),
					body1.getShape().getLocalPoint(point.getPoint()),
					body2.getShape().getLocalPoint(point.getPoint()));
			contacts.add(newContact);
		}

		this.contacts.clear();
		this.contacts.addAll(contacts);
		this.size = this.contacts.size();
	}

	public PhysicsBody getBody1() {
		return pair.getKey();
	}

	public PhysicsBody getBody2() {
		return pair.getValue();
	}


	private double getMixedRestitutionVelocity(PhysicsBody body1, PhysicsBody body2) {
		return 0;
	}

	private double getMixedRestitution(PhysicsBody body1, PhysicsBody body2) {
		return (body1.getRestitution() + body2.getRestitution()) / 2;
	}

	private double getMixedFriction(PhysicsBody body1, PhysicsBody body2) {
		return (body1.getFriction() + body2.getFriction()) / 2;
	}

}
