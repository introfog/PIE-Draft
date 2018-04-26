package com.introfog.PIE;

public class MathPIE{
	public static final Vector2f GRAVITY = new Vector2f (0f, 50f); //9.807f
	public static final float FIXED_DELTA_TIME = 1f / 60f;
	public static final float DEAD_LOOP_BORDER = FIXED_DELTA_TIME * 20f;
	
	public static final float CORRECT_POSITION_PERCENT = 0.5f;
	public static final float MIN_BORDER_SLOP = 0.1f;
	
	public static final float STATIC_BODY_DENSITY = 0f;
	
	public static final float PI = 3.141_592f;
	
	public static final int BIG_ENOUGH_INT = 16 * 1024;
	public static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT + 0.0000;
	public static int fastFloor (float f){
		return (int) (f + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
	}
	
	
	public static float signum (float value){
		if (value == 0f){
			return 1;
		}
		return Math.signum (value);
	}
	
	public static float clamp (float min, float max, float value){
		if (value < min){
			return min;
		}
		else if (value > max){
			return max;
		}
		return value;
	}
}
