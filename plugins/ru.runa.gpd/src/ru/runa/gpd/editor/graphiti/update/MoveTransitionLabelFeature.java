package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveConnectionDecoratorContext;
import org.eclipse.graphiti.features.impl.DefaultMoveConnectionDecoratorFeature;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.Transition;

public class MoveTransitionLabelFeature extends DefaultMoveConnectionDecoratorFeature {

    public MoveTransitionLabelFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public void moveConnectionDecorator(IMoveConnectionDecoratorContext context) {
        super.moveConnectionDecorator(context);
        Connection connection = context.getConnectionDecorator().getConnection();
        Transition transition = (Transition) getBusinessObjectForPictogramElement(connection);
        transition.setLabelLocation(new Point(context.getX(), context.getY()));
    }

    @Override
    public boolean canMoveConnectionDecorator(IMoveConnectionDecoratorContext context) {
        ConnectionDecorator decorator = context.getConnectionDecorator();
        return !(PropertyUtil.hasProperty(decorator, GaProperty.CLASS, GaProperty.ACTION_ICON)
                || PropertyUtil.hasProperty(decorator, GaProperty.ID, GaProperty.TRANSITION_NUMBER)
                || PropertyUtil.hasProperty(decorator, GaProperty.ID, GaProperty.TRANSITION_COLOR_MARKER))
                && super.canMoveConnectionDecorator(context);
    }
    
}
