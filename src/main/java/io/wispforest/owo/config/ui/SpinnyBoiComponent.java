package io.wispforest.owo.config.ui;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.definitions.Insets;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3f;

public class SpinnyBoiComponent extends LabelComponent {

    protected float rotation = 90;
    protected float targetRotation = 90;

    public SpinnyBoiComponent() {
        super(Text.literal(">"));
        this.margins(Insets.horizontal(4));
    }

    public void spin(float to) {
        this.targetRotation = to;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.rotation += (this.targetRotation - this.rotation) * delta * .65;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        matrices.push();
        matrices.translate(this.x + this.width / 2f - 1, this.y + this.height / 2f - 1, 0);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(this.rotation));
        matrices.translate(-(this.x + this.width / 2f - 1), -(this.y + this.height / 2f - 1), 0);

        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        matrices.pop();
    }
}
