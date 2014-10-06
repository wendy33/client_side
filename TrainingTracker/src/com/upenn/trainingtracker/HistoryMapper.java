package com.upenn.trainingtracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.upenn.trainingtracker.customviews.HistoryEntryWidget;

public class HistoryMapper 
{
	private Map<String, List<HistoryEntryWidget>> subCatToWidgets = new HashMap<String, List<HistoryEntryWidget>>();
	private Map<String, List<HistoryEntryWidget>> parentCatToWidgets = new HashMap<String, List<HistoryEntryWidget>>();
	private Map<String, List<HistoryEntryWidget>> userNameToWidgets = new HashMap<String, List<HistoryEntryWidget>>();
	private Map<String, List<HistoryEntryWidget>> fullNameToWidgets = new HashMap<String, List<HistoryEntryWidget>>();
	private Map<HistoryEntryWidget.Type, List<HistoryEntryWidget>> typeToWidgets = new HashMap<HistoryEntryWidget.Type, List<HistoryEntryWidget>>();
	private List<HistoryEntryWidget> allWidgets = new ArrayList<HistoryEntryWidget>();
	
	
	public void addWidgetToMaps(HistoryEntryWidget widget, String subCat, String parentCat, String userName, String fullName, HistoryEntryWidget.Type type)
	{
		this.addWidgetToMap(widget, this.subCatToWidgets, subCat);
		this.addWidgetToMap(widget, this.parentCatToWidgets, parentCat);
		this.addWidgetToMap(widget, this.userNameToWidgets, userName);
		this.addWidgetToMap(widget, this.fullNameToWidgets, fullName);
		this.addWidgetToMap(widget, this.typeToWidgets, type);
		this.allWidgets.add(widget);
	}
	private void addWidgetToMap(HistoryEntryWidget widget, Map<String, List<HistoryEntryWidget>> widgetMap, String key)
	{
		List<HistoryEntryWidget> widgetList = widgetMap.get(key);
		if (widgetList == null)
		{
			widgetList = new ArrayList<HistoryEntryWidget>();
			widgetMap.put(key, widgetList);
		}	
		widgetList.add(widget);
	}
	private void addWidgetToMap(HistoryEntryWidget widget, Map<HistoryEntryWidget.Type, List<HistoryEntryWidget>> widgetMap, HistoryEntryWidget.Type key)
	{
		List<HistoryEntryWidget> widgetList = widgetMap.get(key);
		if (widgetList == null)
		{
			widgetList = new ArrayList<HistoryEntryWidget>();
			widgetMap.put(key, widgetList);
		}	
		widgetList.add(widget);
	}
	public List<HistoryEntryWidget> getListForFilter(String filter)
	{
		List<HistoryEntryWidget> result = new ArrayList<HistoryEntryWidget>();
		if (this.stringInList(filter, new String[]{"Passed", "Aborted", "Failed", "Planned"}))
		{
			this.addSubSetToParentForTypeKey(result, this.typeToWidgets, filter);
		}
		this.addSubSetToParentForStringKey(result, this.subCatToWidgets, filter);
		this.addSubSetToParentForStringKey(result, this.parentCatToWidgets, filter);
		this.addSubSetToParentForStringKey(result, this.userNameToWidgets, filter);
		this.addSubSetToParentForStringKey(result, this.fullNameToWidgets, filter);
		return result;
	}
	public List<HistoryEntryWidget> getAllWidgets()
	{
		return this.allWidgets;
	}
	private boolean stringInList(String query, String[] list)
	{
		for (String token : list)
		{
			if (token.equals(query))
			{
				return true;
			}
		}
		return false;
	}

	private void addSubSetToParentForStringKey(List<HistoryEntryWidget> parent, Map<String, List<HistoryEntryWidget>> map, String filter)
	{
		List<HistoryEntryWidget> subset = map.get(filter);
		if (subset != null)
		{
			parent.addAll(subset);
		}
	}
	private void addSubSetToParentForTypeKey(List<HistoryEntryWidget> parent, Map<HistoryEntryWidget.Type, List<HistoryEntryWidget>> map, String filter)
	{
		if (filter.equals("Passed"))
		{
			List<HistoryEntryWidget> subset = map.get(HistoryEntryWidget.Type.Passed);
			parent.addAll(subset);
		}
		else if (filter.equals("Aborted"))
		{
			List<HistoryEntryWidget> subset = map.get(HistoryEntryWidget.Type.Aborted);
			parent.addAll(subset);
		}
		else if (filter.equals("Failed"))
		{
			List<HistoryEntryWidget> subset = map.get(HistoryEntryWidget.Type.Failed);
			parent.addAll(subset);
		}
		else if (filter.equals("Planned"))
		{
			List<HistoryEntryWidget> subset = map.get(HistoryEntryWidget.Type.Planned);
			parent.addAll(subset);
		}
	}
	

}
