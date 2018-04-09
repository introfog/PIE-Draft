package com.introfog.PIE;

public class Manifold{
	private final float CORRECT_POSITION_PERCENT = 0.5f;
	private final float BORDER_SLOP = 1f;
	
	private float penetration;
	private Vector2f normal;
	private AABB aabbA;
	private AABB aabbB;
	private Circle circleA;
	private Circle circleB;
	private Body a;
	private Body b;
	
	
	private float clamp (float min, float max, float value){
		if (value < min){
			return min;
		}
		else if (value > max){
			return max;
		}
		return value;
	}
	
	private void circleVsCircle (Circle A, Circle B){
		normal = Vector2f.sub (B.position, A.position);
		penetration = A.radius + B.radius - (float) Math.sqrt (Vector2f.distanceWithoutSqrt (B.position, A.position));
	}
	
	private void AABBvsAABB (AABB A, AABB B){
		A.updateCentre ();
		B.updateCentre ();
		
		normal = Vector2f.sub (B.centre, A.centre);
		
		// Вычисление половины ширины вдоль оси x для каждого объекта
		float aExtentX = A.width / 2f;
		float bExtentX = B.width / 2f;
		
		// Вычисление наложения по оси x
		float xOverlap = aExtentX + bExtentX - Math.abs (normal.x);
		
		// Проверка SAT по оси x
		if (xOverlap > 0f){
			// Вычисление половины ширины вдоль оси y для каждого объекта
			float aExtentY = A.height / 2f;
			float bExtentY = B.height / 2f;
			
			// Вычисление наложения по оси y
			float yOverlap = aExtentY + bExtentY - Math.abs (normal.y);
			
			// Проверка SAT по оси y
			if (yOverlap > 0f){
				// Определяем, по какой из осей проникновение наименьшее
				if (xOverlap < yOverlap){
					// Указываем в направлении B, зная, что n указывает в направлении от A к B
					normal.set (MathPIE.signum (normal.x), 0f);
					penetration = xOverlap;
				}
				else{
					// Указываем в направлении B, зная, что n указывает в направлении от A к B
					normal.set (0f, MathPIE.signum (normal.y));
					penetration = yOverlap;
				}
			}
		}
	}
	
	private void AABBvsCircle (AABB A, Circle B, boolean areRevertObjects){
		A.updateCentre ();
		
		Vector2f tmpNormal = Vector2f.sub (B.position, A.centre);
		Vector2f closest = new Vector2f ();
		
		float xExtent = A.width / 2;
		float yExtent = A.height / 2;
		
		closest.x = clamp (-xExtent, xExtent, tmpNormal.x);
		closest.y = clamp (-yExtent, yExtent, tmpNormal.y);
		
		boolean inside = false;
		
		if (tmpNormal.equals (closest)){
			inside = true;
			
			if (Math.abs (tmpNormal.x) > Math.abs (tmpNormal.y)){
				closest.x = MathPIE.signum (closest.x) * xExtent;
			}
			else{
				closest.y = MathPIE.signum (closest.y) * yExtent;
			}
		}
		
		normal = Vector2f.sub (tmpNormal, closest);
		float distance = normal.lengthWithoutSqrt ();
		
		if (distance > B.radius * B.radius && !inside){
			penetration = 0f;
			if (B.velocity.x > A.velocity.x){
				normal.set (1f, 0f);
			}
			else{
				normal.set (-1f, 0f);
			}
			return;
		}
		
		distance = (float) Math.sqrt (distance);
		
		penetration = B.radius - distance; //penetration = -5!!!
		if (inside){
			normal.mul (-1f);
			if (penetration <= 0){
				penetration = distance;
			}
		}
		
		if (areRevertObjects){
			normal.mul (-1f);
		}
	}
	
	
	public Manifold (Body a, Body b){
		this.a = a;
		this.b = b;
		
		normal = new Vector2f ();
	}
	
	public void initializeCollision (){
		if (a.shape == Body.Shape.circle && b.shape == Body.Shape.circle){
			circleVsCircle (circleA, circleB);
		}
		else if (a.shape == Body.Shape.AABB && b.shape == Body.Shape.AABB){
			AABBvsAABB (aabbA, aabbB);
		}
		else if (a.shape == Body.Shape.AABB && b.shape == Body.Shape.circle){
			AABBvsCircle (aabbA, circleB, false);
		}
		else{
			AABBvsCircle (aabbB, circleA, true);
		}
		
	}
	
	public boolean isCollision (){
		if (a.shape == Body.Shape.circle && b.shape == Body.Shape.circle){
			circleA = (Circle) a;
			circleB = (Circle) b;
			return Circle.isIntersected (circleA, circleB);
		}
		else if (a.shape == Body.Shape.AABB && b.shape == Body.Shape.AABB){
			aabbA = (AABB) a;
			aabbB = (AABB) b;
			return AABB.isIntersected (aabbA, aabbB);
		}
		else if (a.shape == Body.Shape.AABB && b.shape == Body.Shape.circle){
			aabbA = (AABB) a;
			circleB = (Circle) b;
			return AABB.isIntersected (aabbA, circleB);
		}
		else if (a.shape == Body.Shape.circle && b.shape == Body.Shape.AABB){
			circleA = (Circle) a;
			aabbB = (AABB) b;
			return AABB.isIntersected (aabbB, circleA);
		}
		return false;
	}
	
	public void solve (){
		normal.normalize ();
		
		// Вычисляем относительную скорость
		Vector2f rv = Vector2f.sub (b.velocity, a.velocity);
		
		// Вычисляем относительную скорость относительно направления нормали
		float velAlongNormal = Vector2f.dotProduct (rv, normal);
		
		// Не выполняем вычислений, если скорости разделены
		if (velAlongNormal > 0){
			return;
		}
		
		// Вычисляем упругость
		float e = Math.min (a.restitution, b.restitution);
		
		// Вычисляем скаляр импульса силы
		float j = -(1 + e) * velAlongNormal;
		j /= a.invertMass + b.invertMass;
		
		// Прикладываем импульс силы
		Vector2f impulse = Vector2f.mul (normal, j);
		a.velocity.sub (Vector2f.mul (impulse, a.invertMass));
		b.velocity.add (Vector2f.mul (impulse, b.invertMass));
	}
	
	public void correctPosition (){
		if (penetration < BORDER_SLOP){
			return;
		}
		
		Vector2f correction = Vector2f.mul (normal,
											penetration * CORRECT_POSITION_PERCENT / (a.invertMass + b.invertMass));
		a.position.sub (Vector2f.mul (correction, a.invertMass));
		b.position.add (Vector2f.mul (correction, b.invertMass));
	}
}
