/*
 * see license.txt
 */
package seventh.ai.basic.squad;

import seventh.shared.Updatable;

/**
 * @author Tony
 *
 */
public abstract class SquadAction implements Updatable {

    public abstract void start(Squad squad);
    public abstract void end(Squad squad);
    public abstract void cancel(Squad squad);
    public abstract boolean isFinished(Squad squad);

}
