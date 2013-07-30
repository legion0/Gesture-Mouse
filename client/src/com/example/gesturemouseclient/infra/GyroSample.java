package com.example.gesturemouseclient.infra;

public class GyroSample {
	private float x, y, z;

	public GyroSample(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

}
