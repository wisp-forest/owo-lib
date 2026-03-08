package io.wispforest.owo.braid.widgets.physics;

import com.mojang.authlib.GameProfile;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.basic.Transform;
import io.wispforest.owo.braid.widgets.drag.DragArena;
import io.wispforest.owo.braid.widgets.drag.DragArenaElement;
import io.wispforest.owo.braid.widgets.object.EntityWidget;
import io.wispforest.owo.ui.component.EntityComponent;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3x2f;
import org.joml.Random;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.time.Duration;
import java.util.UUID;

public class WowzersThatsPhysics extends StatefulWidget {

    public final int nest;

    public WowzersThatsPhysics(int nest) {
        this.nest = nest;
    }

    @Override
    public WidgetState<WowzersThatsPhysics> createState() {
        return new State();
    }

    public static class State extends WidgetState<WowzersThatsPhysics> {

        private PhysicsWorld thePhysics;
        private Entity chyz;

        @Override
        public void init() {
            this.chyz = EntityComponent.createRenderablePlayer(new GameProfile(
                UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"),
                "chyzman"
            ));

            this.thePhysics = new PhysicsWorld();

            // 1. Create the Floor
            // Parameters: x, y, width, height, mass
            // We set mass to 0 to make it static (immovable).
            double floorWidth = 400;
            double floorHeight = 10;
            var floor = new RigidBody(0, 195, floorWidth, floorHeight, 0);
            floor.restitution = .6; // Give the floor some bounce
            floor.staticFriction = 0.9;
            this.thePhysics.bodies.add(floor);

            double ceilingWidth = 400;
            double ceilingHeight = 10;
            var ceiling = new RigidBody(0, -195, ceilingWidth, ceilingHeight, 0);
            ceiling.restitution = .6; // Give the floor some bounce
            ceiling.staticFriction = 0.9;
            this.thePhysics.bodies.add(ceiling);

            double leftWallWidth = 10;
            double leftWallHeight = 400;
            var leftWall = new RigidBody(-195, 0, leftWallWidth, leftWallHeight, 0);
            leftWall.restitution = .6; // Give the floor some bounce
            leftWall.staticFriction = 0.9;
            this.thePhysics.bodies.add(leftWall);

            double rightWallWidth = 10;
            double rightWallHeight = 400;
            var rightWall = new RigidBody(195, 0, rightWallWidth, rightWallHeight, 0);
            rightWall.restitution = .6; // Give the floor some bounce
            rightWall.staticFriction = 0.9;
            this.thePhysics.bodies.add(rightWall);

            // 2. Create a Dynamic Falling Box
            // This box has a mass of 10.0 and starts at an angle
            var random = new Random();
            for (var i = 0; i < 25; i++) {
                var height = random.nextInt(25) + 25;
                var box = new RigidBody(random.nextInt(300) - 150, random.nextInt(200) - 200, height / 2d, height, random.nextInt(20) + 5);
                box.angle = Math.toRadians(random.nextInt(360)); // Start tilted to test rotation
                box.restitution = 0.8;
                box.widget = new EntityWidget(1.5, this.chyz, widget -> widget.displayMode(EntityWidget.DisplayMode.CURSOR));

                this.thePhysics.bodies.add(box);
            }

            if (this.widget().nest > 0) {
                var morePhysics = new RigidBody(-50, -125, 100, 100, 100);
                morePhysics.widget = new DragArena(
                    new DragArenaElement(0, 0,
                        new Transform(
                            new Matrix3x2f().scale(1 / 4f).translate(-600, -600),
                            new WowzersThatsPhysics(this.widget().nest - 1)
                        )
                    )
                );

                this.thePhysics.bodies.add(morePhysics);
            }

            // ---

            this.scheduleAnimationCallback(this::update);
        }

        private void update(Duration delta) {
            this.setState(() -> {
                var transform3x2 = this.context().instance().computeGlobalTransform();
                this.thePhysics.gravity = new Vector2d(transform3x2.transformDirection(new Vector2f(0, 500.0f)));

                this.thePhysics.step((double) delta.toNanos() / Duration.ofSeconds(1).toNanos());
            });

            this.scheduleAnimationCallback(this::update);
        }

        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                400, 400,
                new DragArena(
                    this.thePhysics.bodies.stream()
                        .map(rigidBody -> {
                            return new DragArenaElement(
                                rigidBody.pos.x + 200 - rigidBody.width / 2,
                                rigidBody.pos.y + 200 - rigidBody.height / 2,
                                new Transform(
                                    new Matrix3x2f().rotate((float) rigidBody.angle),
                                    new Sized(
                                        rigidBody.width, rigidBody.height,
                                        rigidBody.widget
                                    )
                                )
                            );
                        })
                        .toList()
                )
            );
        }
    }
}
