/*
 * see license.txt 
 */
package seventh.ai.basic.actions.evaluators;

import seventh.ai.basic.Brain;
import seventh.ai.basic.TargetingSystem;
import seventh.ai.basic.actions.Action;
import seventh.ai.basic.actions.Goals;
import seventh.game.PlayerEntity;

/**
 * @author Tony
 *
 */
public class TakeCoverEvaluator extends ActionEvaluator {

	/**
	 * @param goals
	 * @param characterBias
	 */
	public TakeCoverEvaluator(Goals goals, double characterBias) {
		super(goals, characterBias);
	}

	/* (non-Javadoc)
	 * @see seventh.ai.basic.actions.evaluators.ActionEvaluator#calculateDesirability(seventh.ai.basic.Brain)
	 */
	@Override
	public double calculateDesirability(Brain brain) {
		double score = 0;
		TargetingSystem system = brain.getTargetingSystem();
		if(system.hasTarget()) {
			PlayerEntity bot = brain.getEntityOwner();
			
			final double tweaker = 1.0;
			
			score = tweaker 
					* 1.0 - Evaluators.healthScore(bot) 
					* 1.0 - Evaluators.currentWeaponAmmoScore(bot)
					* 1.0 - Evaluators.weaponDistanceScore(bot, system.getCurrentTarget())
					;
			
			if(!system.targetInLineOfFire()) {
				score *= 0.9;
			}
			
			score *= getCharacterBias();
		}
		
		
		return score;
	}

	/* (non-Javadoc)
	 * @see seventh.ai.basic.actions.evaluators.ActionEvaluator#getAction(seventh.ai.basic.Brain)
	 */
	@Override
	public Action getAction(Brain brain) {
		return getGoals().takeCover(brain.getTargetingSystem().getLastRemeberedPosition());
	}

}
