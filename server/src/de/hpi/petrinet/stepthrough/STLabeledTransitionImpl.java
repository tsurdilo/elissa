package de.hpi.petrinet.stepthrough;

import de.hpi.bpmn.DiagramObject;
import de.hpi.petrinet.LabeledTransition;
import de.hpi.petrinet.impl.LabeledTransitionImpl;

public class STLabeledTransitionImpl extends LabeledTransitionImpl implements STTransition, LabeledTransition {

	private DiagramObject BPMNObj;
	private AutoSwitchLevel level;
	private int timesExecuted = 0;
	
	@Override
	public DiagramObject getBPMNObj() {
		return BPMNObj;
	}

	@Override
	public void setBPMNObj(DiagramObject obj) {
		BPMNObj = obj;
	}
	
	@Override
	public int getTimesExecuted() {
		return timesExecuted;
	}

	@Override
	public void incTimesExecuted() {
		timesExecuted ++;
	}

	@Override
	public AutoSwitchLevel getAutoSwitchLevel() {
		return level;
	}
	
	@Override
	public void setAutoSwitchLevel(AutoSwitchLevel level) {
		this.level = level;
	}
	

//	@Override
//	public boolean isOnlyTransitionForObj() {
//		return isOnlyTransitionForObj;
//	}
//
//	@Override
//	public void isOnlyTransitionForObj(boolean isOnlyTransition) {
//		isOnlyTransitionForObj = isOnlyTransition;
//	}
}