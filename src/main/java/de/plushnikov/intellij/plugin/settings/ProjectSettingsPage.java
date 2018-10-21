package de.plushnikov.intellij.plugin.settings;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import de.plushnikov.intellij.plugin.Version;
import de.plushnikov.intellij.plugin.provider.LombokProcessorProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProjectSettingsPage implements SearchableConfigurable, Configurable.NoScroll {

  private JPanel myGeneralPanel;
  private JPanel myLombokPanel;

  private JCheckBox myEnableLombokInProject;

  private JCheckBox myEnableValSupport;
  private JCheckBox myEnableBuilderSupport;
  private JCheckBox myEnableLogSupport;
  private JCheckBox myEnableConstructorSupport;
  private JCheckBox myEnableDelegateSupport;
  private JPanel mySettingsPanel;
  private JCheckBox myEnableLombokVersionWarning;
  private JCheckBox myMissingLombokWarning;
  private JPanel mySupportPanel;

  private PropertiesComponent myPropertiesComponent;
  private LombokProcessorProvider myLombokProcessorProvider;

  public ProjectSettingsPage(PropertiesComponent propertiesComponent,
                             LombokProcessorProvider lombokProcessorProvider) {
    myPropertiesComponent = propertiesComponent;
    myLombokProcessorProvider = lombokProcessorProvider;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return Version.PLUGIN_NAME;
  }

  @Nullable
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getHelpTopic() {
    return null;
  }

  @Override
  public JComponent createComponent() {
    initFromSettings();

    // Add Listener to deactivate all checkboxes if plugin is deactivated
    myEnableLombokInProject.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        JCheckBox checkBox = (JCheckBox) actionEvent.getSource();
        boolean selected = checkBox.getModel().isSelected();

        myLombokPanel.setEnabled(selected);
        myEnableValSupport.setEnabled(selected);
        myEnableBuilderSupport.setEnabled(selected);
        myEnableLogSupport.setEnabled(selected);
        myEnableConstructorSupport.setEnabled(selected);
        myEnableDelegateSupport.setEnabled(selected);
      }
    });
    myEnableConstructorSupport.setVisible(false);
    return myGeneralPanel;
  }

  private void initFromSettings() {

    myEnableLombokInProject.setSelected(ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.LOMBOK_ENABLED_IN_PROJECT));
    myEnableValSupport.setSelected(ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_VAL_ENABLED));
    myEnableBuilderSupport.setSelected(ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_BUILDER_ENABLED));
    myEnableDelegateSupport.setSelected(ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_DELEGATE_ENABLED));

    myEnableLogSupport.setSelected(ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_LOG_ENABLED));
    myEnableConstructorSupport.setSelected(ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_CONSTRUCTOR_ENABLED));

    myEnableLombokVersionWarning.setSelected(ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_LOMBOK_VERSION_CHECK_ENABLED, false));
    myMissingLombokWarning.setSelected(ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_MISSING_LOMBOK_CHECK_ENABLED, false));
  }

  @Override
  public boolean isModified() {
    return myEnableLombokInProject.isSelected() != ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.LOMBOK_ENABLED_IN_PROJECT) ||
      myEnableValSupport.isSelected() != ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_VAL_ENABLED) ||
      myEnableBuilderSupport.isSelected() != ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_BUILDER_ENABLED) ||
      myEnableDelegateSupport.isSelected() != ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_DELEGATE_ENABLED) ||
      myEnableLogSupport.isSelected() != ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_LOG_ENABLED) ||
      myEnableConstructorSupport.isSelected() != ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_CONSTRUCTOR_ENABLED) ||
      myEnableLombokVersionWarning.isSelected() != ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_LOMBOK_VERSION_CHECK_ENABLED, false) ||
      myMissingLombokWarning.isSelected() != ProjectSettings.isEnabled(myPropertiesComponent, ProjectSettings.IS_MISSING_LOMBOK_CHECK_ENABLED, false);
  }

  @Override
  public void apply() {
    ProjectSettings.setEnabled(myPropertiesComponent, ProjectSettings.LOMBOK_ENABLED_IN_PROJECT, myEnableLombokInProject.isSelected());

    ProjectSettings.setEnabled(myPropertiesComponent, ProjectSettings.IS_VAL_ENABLED, myEnableValSupport.isSelected());
    ProjectSettings.setEnabled(myPropertiesComponent, ProjectSettings.IS_BUILDER_ENABLED, myEnableBuilderSupport.isSelected());
    ProjectSettings.setEnabled(myPropertiesComponent, ProjectSettings.IS_DELEGATE_ENABLED, myEnableDelegateSupport.isSelected());

    ProjectSettings.setEnabled(myPropertiesComponent, ProjectSettings.IS_LOG_ENABLED, myEnableLogSupport.isSelected());
    ProjectSettings.setEnabled(myPropertiesComponent, ProjectSettings.IS_CONSTRUCTOR_ENABLED, myEnableConstructorSupport.isSelected());

    ProjectSettings.setEnabled(myPropertiesComponent, ProjectSettings.IS_LOMBOK_VERSION_CHECK_ENABLED, myEnableLombokVersionWarning.isSelected());
    ProjectSettings.setEnabled(myPropertiesComponent, ProjectSettings.IS_MISSING_LOMBOK_CHECK_ENABLED, myMissingLombokWarning.isSelected());

    myLombokProcessorProvider.initProcessors();
  }

  @Override
  public void reset() {
    initFromSettings();
  }

  @Override
  public void disposeUIResources() {
  }

  @NotNull
  @Override
  public String getId() {
    return getDisplayName();
  }

  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

}
