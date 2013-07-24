package no.kantega.lab.wicket.async.components;

import no.kantega.lab.wicket.async.task.AbstractTaskModel;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.HashMap;
import java.util.Map;

public class ProgressBar extends Panel {

    private final ProgressButton progressButton;

    private final Map<StateDescription, IModel<String>> stateCssClasses;

    public ProgressBar(String id, ProgressButton progressButton) {
        super(id);

        this.progressButton = progressButton;

        WebMarkupContainer wrapper = makeWrapper("wrapper");
        add(wrapper);

        Component bar = makeBar("bar").add(new AttributeAppender("style", new TaskProgressPercentageStyleModel()));
        wrapper.add(bar);
        wrapper.add(new Label("message", new TaskProgressMessageModel()));

        stateCssClasses = new HashMap<StateDescription, IModel<String>>();
        this.add(new AttributeAppender("class", progressButton.new StateDispatcherModel<String>(new Model<String>(), stateCssClasses), " "));

        progressButton.addRefreshDependant(this);

        this.setOutputMarkupId(true);
    }

    private AbstractTaskModel getTaskModel() {
        return progressButton.getTaskModel();
    }

    protected WebMarkupContainer makeWrapper(String id) {
        return new WebMarkupContainer(id);
    }

    protected WebMarkupContainer makeBar(String id) {
        return new WebMarkupContainer(id);
    }

    protected boolean isShowPercentage() {
        return true;
    }

    protected double getDefaultWidth() {
        return 0d;
    }

    private class TaskProgressMessageModel extends AbstractReadOnlyModel<String> {
        @Override
        public String getObject() {
            Double progress = getTaskModel().getProgress();
            String suffix = "";
            if (isShowPercentage()) {
                if (progress != null) {
                    suffix = String.format("(%d%%)", getPercentProgress());
                }
            }
            String message = getTaskModel().getProgressMessage();
            if (message == null) {
                message = "";
            }
            return String.format("%s %s", message, suffix);
        }
    }

    private class TaskProgressPercentageStyleModel extends AbstractReadOnlyModel<String> {
        @Override
        public String getObject() {
            int percentProgress = getPercentProgress();
            return String.format("width: %d%%;", percentProgress);
        }
    }

    private int getPercentProgress() {
        double width = getTaskModel().getProgress() == null ? getDefaultWidth() : getTaskModel().getProgress();
        return (int) Math.round(Math.max(Math.min(width, 1d), 0d) * 100d);
    }

    public void registerCssClassModel(IModel<String> textModel, TaskState taskState, InteractionState interactionState) {
        stateCssClasses.put(new StateDescription(taskState, interactionState), textModel);
    }

    public void registerCssClassModel(IModel<String> textModel, TaskState... taskStates) {
        for (TaskState taskState : taskStates) {
            for (InteractionState interactionState : InteractionState.values()) {
                registerCssClassModel(textModel, taskState, interactionState);
            }
        }
    }

    public void registerCssClassModel(IModel<String> textModel, InteractionState... interactionStates) {
        for (InteractionState interactionState : interactionStates) {
            for (TaskState taskState : TaskState.values()) {
                registerCssClassModel(textModel, taskState, interactionState);
            }
        }
    }
}
