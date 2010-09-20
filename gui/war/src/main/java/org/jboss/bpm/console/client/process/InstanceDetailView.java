/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.bpm.console.client.process;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.CaptionLayoutPanel;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.ServerPlugins;
import org.jboss.bpm.console.client.common.PropertyGrid;
import org.jboss.bpm.console.client.common.WidgetWindowPanel;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.errai.workspaces.client.framework.Registry;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class InstanceDetailView extends CaptionLayoutPanel implements ViewInterface
{
  public final static String ID = InstanceDetailView.class.getName();

  private Controller controller;

  private PropertyGrid grid;

  private ProcessInstanceRef currentInstance;

  private Button diagramBtn;

  private Button instanceDataBtn;

  private WidgetWindowPanel diagramWindowPanel;

  private WidgetWindowPanel instanceDataWindowPanel;

  private ApplicationContext appContext;

  private ActivityDiagramView diagramView;

  private InstanceDataView instanceDataView;

  private boolean hasDiagramPlugin;

  private SimpleDateFormat dateFormat = new SimpleDateFormat();
  private ProcessDefinitionRef currentDefintion;

  private boolean isRiftsawInstance;

  public InstanceDetailView()
  {
    super("Execution details");    
    
    this.appContext = Registry.get(ApplicationContext.class);
    isRiftsawInstance = appContext.getConfig().getProfileName().equals("BPEL Console");

    super.setStyleName("bpm-detail-panel");
    super.setLayout(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));

    grid = new PropertyGrid(
        new String[] {"Process:", "Instance ID:", "Key:", "State", "Start Date:", "Activity:"}
    );

    this.add(grid, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    MosaicPanel buttonPanel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL) );
    diagramBtn = new Button("Diagram",
        new ClickHandler()
        {
          public void onClick(ClickEvent clickEvent)
          {
            String diagramUrl = getCurrentDefintion().getDiagramUrl();
            if(diagramUrl !=null && !diagramUrl.equals(""))
            {
              ProcessInstanceRef selection = getCurrentInstance();
              if(selection!=null)
              {
                createDiagramWindow(selection);
                controller.handleEvent(
                    new Event(LoadActivityDiagramAction.ID, selection)
                );
              }
            }
            else
            {
              MessageBox.alert("Incomplete deployment", "No diagram associated with process");
            }
          }
        }
    );
    diagramBtn.setVisible(!isRiftsawInstance);

    diagramBtn.setEnabled(false);
    buttonPanel.add(diagramBtn);

    instanceDataBtn = new Button("Instance Data",
        new ClickHandler()
        {
          public void onClick(ClickEvent clickEvent)
          {
            if(currentInstance!=null)
            {
              createDataWindow(currentInstance);
              controller.handleEvent(
                  new Event(UpdateInstanceDataAction.ID, currentInstance.getId())
              );
            }
          }
        }
    );
    instanceDataBtn.setEnabled(false);
    buttonPanel.add(instanceDataBtn);
    this.add(buttonPanel);

    // plugin availability
    this.hasDiagramPlugin =
        ServerPlugins.has("org.jboss.bpm.console.server.plugin.GraphViewerPlugin");

  }

  private void createDiagramWindow(ProcessInstanceRef inst)
  {

    MosaicPanel layout = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    layout.setStyleName("bpm-window-layout");
    layout.setPadding(5);

    Label header = new Label("Instance: "+inst.getId());
    header.setStyleName("bpm-label-header");
    layout.add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    layout.add(diagramView, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    diagramWindowPanel = new WidgetWindowPanel(
        "Process Instance Activity",
        layout, true
    );        
  }

  private void createDataWindow(ProcessInstanceRef inst)
  {
    instanceDataWindowPanel = new WidgetWindowPanel(
        "Process Instance Data: "+inst.getId(),
        instanceDataView, true
        );
  }

  public void setController(Controller controller)
  {
    this.controller = controller;

    this.diagramView = new ActivityDiagramView();
    this.instanceDataView = new InstanceDataView();

    controller.addView(ActivityDiagramView.ID, diagramView);
    controller.addView(InstanceDataView.ID, instanceDataView);
    controller.addAction(LoadActivityDiagramAction.ID, new LoadActivityDiagramAction());
    controller.addAction(UpdateInstanceDataAction.ID, new UpdateInstanceDataAction());
  }

  public void update(ProcessDefinitionRef def, ProcessInstanceRef instance)
  {
    this.currentDefintion = def;
    this.currentInstance = instance;

    String currentNodeName = instance.getRootToken() != null ?
        instance.getRootToken().getCurrentNodeName() : "n/a";

    String[] values = new String[] {
        def.getName(),
        instance.getId(),
        instance.getKey(),
        String.valueOf( instance.getState() ),
        dateFormat.format(instance.getStartDate()),
        currentNodeName
    };

    grid.update(values);

    if(hasDiagramPlugin)
      this.diagramBtn.setEnabled(true);

    instanceDataBtn.setEnabled(true);
  }

  public void clearView()
  {
    grid.clear();
    this.currentDefintion=null;
    this.currentInstance = null;
    this.diagramBtn.setEnabled(false);
    instanceDataBtn.setEnabled(false);

  }


  private ProcessDefinitionRef getCurrentDefintion()
  {
    return currentDefintion;
  }

  private ProcessInstanceRef getCurrentInstance()
  {
    return currentInstance;
  }
}
