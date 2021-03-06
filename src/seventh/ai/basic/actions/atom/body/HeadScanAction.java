/*
 * The Seventh
 * see license.txt 
 */
package seventh.ai.basic.actions.atom.body;

import java.util.List;

import seventh.ai.basic.AttackDirection;
import seventh.ai.basic.Brain;
import seventh.ai.basic.PathPlanner;
import seventh.ai.basic.actions.AdapterAction;
import seventh.game.PlayerEntity;
import seventh.game.SmoothOrientation;
import seventh.math.Vector2f;
import seventh.shared.TimeStep;

/**
 * @author Tony
 *
 */
public class HeadScanAction extends AdapterAction {

	private long sampleTime;	
	private long pickAttackDirectionTime;
	private int attackDirectionIndex;
	private int direction;
	private Vector2f destination;
	private Vector2f attackDir;

	private SmoothOrientation smoother;
	/**
	 */
	public HeadScanAction() {		
		this.destination = new Vector2f();
		this.attackDir = new Vector2f();
		this.direction = 1;
		
		this.smoother = new SmoothOrientation(Math.toRadians(15.0f));
	}


	/**
	 * Rest this action
	 */
	public void reset() {
		this.sampleTime = 0;
		this.destination.zeroOut();
		
		this.pickAttackDirectionTime = 0;
		this.attackDirectionIndex = -1;
	}
	
	/* (non-Javadoc)
	 * @see seventh.ai.Action#isFinished()
	 */
	@Override
	public boolean isFinished(Brain brain) {
		return false;
	}

	/* (non-Javadoc)
	 * @see seventh.ai.Action#update(seventh.ai.Brain, leola.live.TimeStep)
	 */
	@Override
	public void update(Brain brain, TimeStep timeStep) {
		PlayerEntity ent = brain.getEntityOwner();
		
		/* only update the destination once in a while to 
		 * avoid the jitters				
		 */
		this.sampleTime -= timeStep.getDeltaTime();
		this.pickAttackDirectionTime += timeStep.getDeltaTime();
		
		if(this.sampleTime < 0) {
			PathPlanner<?> feeder = brain.getMotion().getPathPlanner();
			
			Vector2f dest = null;
			if(feeder.hasPath()) {
				dest = feeder.nextWaypoint(ent);			
				
				this.pickAttackDirectionTime = 0;
				this.attackDirectionIndex = -1;
			}
			else {
				
				// If the bot is standing still, have them look at the directions in which they could
				// be attacked.
				
				List<AttackDirection> attackDirections = brain.getWorld().getAttackDirections(ent);
				if(!attackDirections.isEmpty()) {

					int numberOfAttackDirs = attackDirections.size();
					if(this.attackDirectionIndex < 0 || this.attackDirectionIndex>=numberOfAttackDirs 
							|| this.pickAttackDirectionTime > 800) {

						int index = this.attackDirectionIndex + this.direction;
						if(index < 0 || index >= numberOfAttackDirs) {
							this.direction = -this.direction;
						}
						
						this.attackDirectionIndex = (this.attackDirectionIndex + this.direction) % numberOfAttackDirs;
						this.pickAttackDirectionTime = 0;
					}
					
					Vector2f.Vector2fSubtract(attackDirections.get(this.attackDirectionIndex).getDirection(), ent.getCenterPos(), attackDir);
					dest = attackDir;
				}

				
				if(dest==null) {
					dest = ent.getMovementDir();
				}
			}
			
			Vector2f.Vector2fNormalize(dest, dest);
			
			destination.set(dest);
			this.sampleTime = 800 + (brain.getWorld().getRandom().nextInt(3) * 150);
		}
				
		this.smoother.setOrientation(ent.getOrientation());
		float destinationOrientation = (float)(Math.atan2(destination.y, destination.x));	
		this.smoother.setDesiredOrientation(destinationOrientation);
		this.smoother.update(timeStep);
		ent.setOrientation(this.smoother.getOrientation());
	}

}
