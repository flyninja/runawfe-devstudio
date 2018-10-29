package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.ui.dialog.MessageNodeDialog;

public class SendMessageConfAction extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        MessageNode messageNode = getSelection();
        MessageNodeDialog dialog = new MessageNodeDialog(messageNode.getProcessDefinition(), messageNode.getVariableMappings(), true,
                messageNode.getName());
        if (dialog.open() != Window.CANCEL) {
            messageNode.setVariableMappings(dialog.getVariableMappings());
        }
    }
}
