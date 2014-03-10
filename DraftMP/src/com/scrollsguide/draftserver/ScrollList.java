package com.scrollsguide.draftserver;
import java.util.ArrayList;

public class ScrollList extends ArrayList<Scroll> {

	@Override
	public boolean add(Scroll s) {
		if (!super.contains(s)) {
			super.add(s);
			return true;
		}
		return false;
	}

	public Scroll getById(int id) {
		for (int i = 0; i < super.size(); i++) {
			Scroll s = super.get(i);
			if (s.getId() == id) {
				return s;
			}
		}
		return null;
	}
}
