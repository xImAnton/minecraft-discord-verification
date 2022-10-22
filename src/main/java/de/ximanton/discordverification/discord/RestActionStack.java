package de.ximanton.discordverification.discord;

import net.dv8tion.jda.api.requests.RestAction;

/**
 * Convenience class for `and`-ing together discord rest actions for less network usage/ processing time
 */
public class RestActionStack {

    private RestAction<Void> prev = null;

    /**
     * Add a rest action to the stack
     * @param toStack action to add
     */
    public void add(RestAction<Void> toStack) {
        if (prev == null) {
            prev = toStack;
            return;
        }

        prev = prev.and(toStack);
    }

    /**
     * Get the last action that can be used to execute all other previously consumed actions
     * @return the last action or null if no action was consumed
     */
    public RestAction<Void> getLast() {
        return prev;
    }

    /**
     * Queue all actions
     */
    public void queue() {
        if (prev == null) return;

        prev.queue();
    }

}
