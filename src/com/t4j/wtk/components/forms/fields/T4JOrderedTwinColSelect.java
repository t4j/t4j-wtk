
package com.t4j.wtk.components.forms.fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.ui.TwinColSelect;

public class T4JOrderedTwinColSelect extends TwinColSelect {

	private static final long serialVersionUID = 1L;

	private LinkedList<Object> orderedList;

	public T4JOrderedTwinColSelect(String caption) {

		super(caption);

		orderedList = new LinkedList<Object>();
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {

		super.changeVariables(source, variables);

		// New option entered (and it is allowed)
		if (isNewItemsAllowed()) {
			final String newitem = (String) variables.get("newitem");
			if (newitem != null && newitem.length() > 0) {
				getNewItemHandler().addNewItem(newitem);
			}
		}

		// Selection change
		if (variables.containsKey("selected")) {
			final String[] ka = (String[]) variables.get("selected");

			// Multiselect mode
			if (isMultiSelect()) {

				// TODO Optimize by adding repaintNotNeeded when applicable

				// Converts the key-array to id-set
				final LinkedList<Object> s = new LinkedList<Object>();

				orderedList.clear();
				for (int i = 0; i < ka.length; i++) {
					final Object id = itemIdMapper.get(ka[i]);
					if (!isNullSelectionAllowed() && (id == null || id == getNullSelectionItemId())) {
						// skip empty selection if nullselection is not allowed
						requestRepaint();
					} else if (id != null && containsId(id)) {
						s.add(id);
						orderedList.add(id);
					}
				}

				if (!isNullSelectionAllowed() && s.size() < 1) {
					// empty selection not allowed, keep old value
					requestRepaint();
					return;
				}

				// Limits the deselection to the set of visible items
				// (non-visible items can not be deselected)
				final Collection<?> visible = getVisibleItemIds();
				if (visible != null) {
					@SuppressWarnings("unchecked")
					Set<Object> newsel = (Set<Object>) getValue();
					if (newsel == null) {
						newsel = new HashSet<Object>();
					} else {
						newsel = new HashSet<Object>(newsel);
					}
					newsel.removeAll(visible);
					newsel.addAll(s);
					setValue(newsel, true);
				}
			} else {
				// Single select mode
				if (!isNullSelectionAllowed() && (ka.length == 0 || ka[0] == null || ka[0] == getNullSelectionItemId())) {
					requestRepaint();
					return;
				}
				if (ka.length == 0) {
					// Allows deselection only if the deselected item is
					// visible
					final Object current = getValue();
					final Collection<?> visible = getVisibleItemIds();
					if (visible != null && visible.contains(current)) {
						setValue(null, true);
					}
				} else {
					final Object id = itemIdMapper.get(ka[0]);
					if (!isNullSelectionAllowed() && id == null) {
						requestRepaint();
					} else if (id != null && id.equals(getNullSelectionItemId())) {
						setValue(null, true);
					} else {
						setValue(id, true);
					}
				}
			}
		}
	}

	public List<Object> getOrderedList() {

		return orderedList;
	}

}
