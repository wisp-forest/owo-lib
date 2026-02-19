package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.instance.CustomWidgetTransform;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetTransform;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.joml.Matrix3x2f;

import java.util.Objects;

public class Transform extends SingleChildInstanceWidget {

    public final Matrix3x2f matrix;

    public Transform(Matrix3x2f matrix, Widget child) {
        super(child);
        this.matrix = matrix;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<Transform> {

        public Instance(Transform widget) {
            super(widget);
            ((CustomWidgetTransform) this.transform).setMatrix(this.widget.matrix);
        }

        @Override
        public void setWidget(Transform widget) {
            if (Objects.equals(this.widget.matrix, widget.matrix)) {
                this.transform.recompute();
                return;
            }

            super.setWidget(widget);
            ((CustomWidgetTransform) this.transform).setMatrix(this.widget.matrix);

            this.markNeedsLayout();
        }

        @Override
        protected WidgetTransform createTransform() {
            return new CustomWidgetTransform();
        }
    }
}
