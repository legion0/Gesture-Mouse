/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008, 2009 Benjamin Poppinga
 * 
 * Developed at University of Oldenburg
 * Contact: wiigee@benjaminpoppinga.de
 *
 * This file is part of wiigee.
 *
 * wiigee is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.wiigee.logic;

import java.util.Vector;

import org.wiigee.event.AccelerationEvent;
import org.wiigee.event.ActionStartEvent;
import org.wiigee.event.ActionStopEvent;
import org.wiigee.event.ButtonPressedEvent;
import org.wiigee.event.ButtonReleasedEvent;
import org.wiigee.event.MotionStartEvent;
import org.wiigee.event.MotionStopEvent;

import android.util.Log;

/**
 * This class analyzes the AccelerationEvents emitted from a Wiimote and further creates and manages the different models for each type of gesture.
 * 
 * @author Benjamin 'BePo' Poppinga
 */
public class TriggeredProcessingUnit extends ProcessingUnit {

	// gesturespecific values
	private Gesture current; // current gesture

	private Vector<Gesture> trainingSequence;

	// State variables
	private boolean learning, analyzing;

	public TriggeredProcessingUnit() {
		super();
		this.learning = false;
		this.analyzing = false;
		this.current = new Gesture();
		this.trainingSequence = new Vector<Gesture>();
	}

	/**
	 * Since this class implements the WiimoteListener this procedure is necessary. It contains the filtering (directional equivalence filter) and adds the
	 * incoming data to the current motion, we want to train or recognize.
	 * 
	 * @param event
	 *            The acceleration event which has to be processed by the directional equivalence filter and which has to be added to the current motion in
	 *            recognition or training process.
	 */
	public void accelerationReceived(AccelerationEvent event) {
		if (isLearning() || isRecognizing()) {
			current.add(event); // add event to gesture
		}
	}

	/**
	 * This method is from the WiimoteListener interface. A button press is used to control the data flow inside the structures.
	 * 
	 */
	public void buttonPressReceived(ButtonPressedEvent event) {
		this.handleStartEvent(event);
	}

	public void buttonReleaseReceived(ButtonReleasedEvent event) {
		this.handleStopEvent(event);
	}

	public void motionStartReceived(MotionStartEvent event) {
		// this.handleStartEvent(event);
		Log.v("TriggeredProcessingUnit", "motionStartReceived");
	}

	public void motionStopReceived(MotionStopEvent event) {
		// this.handleStopEvent(event);
		Log.v("TriggeredProcessingUnit", "motionStopReceived");
	}

	public boolean startLearning() {
		if (isIdle()) {
			this.learning = true;
			return true;
		}
		return false;
	}

	public boolean isLearning() {
		return learning;
	}

	public boolean endLearning() {
		learning = false;
		if (current.getCountOfData() > 0) {
			Log.v("TriggeredProcessingUnit", "Finished recording (training)...");
			Log.v("TriggeredProcessingUnit", "Data: " + current.getCountOfData());
			Gesture gesture = new Gesture(current);
			trainingSequence.add(gesture);
			current = new Gesture();
			return true;
		} else {
			Log.v("TriggeredProcessingUnit", "There is no data.");
			Log.v("TriggeredProcessingUnit", "Please train the gesture again.");
			return false;
		}
	}

	public int saveLearningAsGesture() {
		if (trainingSequence.size() == 0) {
			Log.v("TriggeredProcessingUnit", "There is nothing to do. Please record some gestures first.");
			return -1;
		}
		Log.v("TriggeredProcessingUnit", "Training the model with " + trainingSequence.size() + " gestures...");
		// learning = true; // XXX Why is this here ?
		GestureModel gestureModel = new GestureModel();
		gestureModel.train(trainingSequence);
		// m.print(); // Prints model details after training
		int id = classifier.addGestureModel(gestureModel);
		trainingSequence = new Vector<Gesture>();
		learning = false;
		return id;
	}

	public boolean startRecognizing() {
		if (isIdle()) {
			this.analyzing = true;
			return true;
		}
		return false;
	}

	public boolean isRecognizing() {
		return analyzing;
	}

	public boolean endRecognizing() {
		analyzing = false;
		Gesture gesture = new Gesture(current);
		current = new Gesture();
		if (gesture.getCountOfData() > 0) {
			Log.v("TriggeredProcessingUnit", "Finished recording (recognition)...");
			Log.v("TriggeredProcessingUnit", "Compare gesture with " + classifier.getCountOfGestures() + " other gestures.");
			int recognized = classifier.classifyGesture(gesture);
			if (recognized != -1) {
				double recogprob = classifier.getLastProbability();
				fireGestureEvent(true, recognized, recogprob);
				Log.v("TriggeredProcessingUnit", "######");
				Log.v("TriggeredProcessingUnit", "Gesture No. " + recognized + " recognized: " + recogprob);
				Log.v("TriggeredProcessingUnit", "######");
			} else {
				this.fireGestureEvent(false, 0, 0.0);
				Log.v("TriggeredProcessingUnit", "######");
				Log.v("TriggeredProcessingUnit", "No gesture recognized.");
				Log.v("TriggeredProcessingUnit", "######");
			}
			return true;
		} else {
			Log.v("TriggeredProcessingUnit", "There is no data.");
			Log.v("TriggeredProcessingUnit", "Please recognize the gesture again.");
			return false;
		}
	}

	private boolean isIdle() {
		return !analyzing && !analyzing;
	}

	public void handleStartEvent(ActionStartEvent event) {

		// TrainButton = record a gesture for learning
		if (isIdle() && event.isTrainInitEvent()) {
			Log.v("TriggeredProcessingUnit", "Training started!");
			startLearning();
		}

		// RecognitionButton = record a gesture for recognition
		if (isIdle() && event.isRecognitionInitEvent()) {
			Log.v("TriggeredProcessingUnit", "Recognition started!");
			startRecognizing();
		}

		// CloseGestureButton = starts the training of the model with multiple
		// recognized gestures, contained in trainsequence
		if (isIdle() && event.isCloseGestureInitEvent()) {
			saveLearningAsGesture();
		}
	}

	public void handleStopEvent(ActionStopEvent event) {
		if (isLearning()) {
			endLearning();
		} else if (isRecognizing()) {
			endRecognizing();
		}
	}

	@Override
	public void loadGesture(String filename) {
		GestureModel g = org.wiigee.util.FileIO.readFromFile(filename);
		this.classifier.addGestureModel(g);
	}

	@Override
	public void saveGesture(int id, String filename) {
		org.wiigee.util.FileIO.writeToFile(this.classifier.getGestureModel(id), filename);
	}

}
