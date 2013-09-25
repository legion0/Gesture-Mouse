package org.wiigee.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.wiigee.logic.GestureModel;
import org.wiigee.logic.HMM;
import org.wiigee.logic.Quantizer;

public class Serializer {

	public static void write(GestureModel gestureModel, OutputStream outputStream) throws IOException {
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
		DataOutputStream out = new DataOutputStream(bufferedOutputStream);

		int numStates = gestureModel.getNumStates();
		HMM hmm = gestureModel.getHMM();
		double defaultProbability = gestureModel.getDefaultProbability();
		int numObservations = gestureModel.getNumObservations();
		double[] pi = hmm.getPi();
		Quantizer quantizer = gestureModel.getQuantizer();
		double[][] map = quantizer.getHashMap();
		double[][] a = hmm.getA();
		double[][] b = hmm.getB();

		out.writeInt(numStates);
		out.writeInt(numObservations);
		out.writeDouble(defaultProbability);
		out.writeDouble(quantizer.getRadius());

		for (int i = 0; i < numStates; i++) {
			out.writeDouble(pi[i]);
		}
		assert numObservations == map.length;
		for (int v = 0; v < numObservations; v++) {
			out.writeDouble(map[v][0]);
			out.writeDouble(map[v][1]);
			out.writeDouble(map[v][2]);
		}
		for (int i = 0; i < numStates; i++) {
			for (int j = 0; j < numStates; j++) {
				out.writeDouble(a[i][j]);
			}
		}
		for (int i = 0; i < numStates; i++) {
			for (int j = 0; j < numObservations; j++) {
				out.writeDouble(b[i][j]);
			}
		}
		out.flush();
	}

	public static GestureModel read(InputStream stream) throws IOException {
		BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
		DataInputStream in = new DataInputStream(bufferedInputStream);

		int numStates = in.readInt();
		int numObservations = in.readInt();
		double defaultprobability = in.readDouble();
		double radius = in.readDouble();

		double[] pi = new double[numStates];
		double[][] map = new double[numObservations][3];
		double[][] a = new double[numStates][numStates];
		double[][] b = new double[numStates][numObservations];

		for (int i = 0; i < numStates; i++) {
			pi[i] = in.readDouble();
		}
		for (int v = 0; v < numObservations; v++) {
			map[v][0] = in.readDouble();
			map[v][1] = in.readDouble();
			map[v][2] = in.readDouble();
		}
		for (int i = 0; i < numStates; i++) {
			for (int j = 0; j < numStates; j++) {
				a[i][j] = in.readDouble();
			}
		}
		for (int i = 0; i < numStates; i++) {
			for (int j = 0; j < numObservations; j++) {
				b[i][j] = in.readDouble();
			}
		}

		GestureModel gestureModel = new GestureModel();
		gestureModel.setDefaultProbability(defaultprobability);
		Quantizer quantizer = new Quantizer(numStates);
		quantizer.setUpManually(map, radius);
		gestureModel.setQuantizer(quantizer);
		HMM hmm = new HMM(numStates, numObservations);
		hmm.setPi(pi);
		hmm.setA(a);
		hmm.setB(b);
		gestureModel.setHMM(hmm);
		return gestureModel;
	}

}
