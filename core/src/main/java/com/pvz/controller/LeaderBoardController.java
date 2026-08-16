package com.pvz.controller;

import java.util.ArrayList;
import java.util.List;

import com.pvz.models.AppContext;
import com.pvz.models.leaderboard.Leaderboard;
import com.pvz.models.leaderboard.Leaderboard.LeaderBoardEntry;
import com.pvz.models.leaderboard.LeaderboardSortField;
import com.pvz.models.leaderboard.SortTypes;

/**
 * Controller for the Leaderboard screen. Loads every saved user's stats
 * through the existing Leaderboard model and sorts them by whichever
 * column is currently selected.
 */
public class LeaderBoardController {

    private LeaderboardSortField sortField = LeaderboardSortField.HIGHEST_SCORING_GAME_SCORE;
    private SortTypes sortOrder = SortTypes.DESCENDING;

    /**
     * Cached list of all saved players. Reading and deserializing every user JSON
     * from disk on each sort click was the main delay, so it's loaded once and only
     * re-read when {@link #reload()} is called (each time the screen opens).
     */
    private List<LeaderBoardEntry> cachedEntries;

    public List<LeaderBoardEntry> getEntries() {
        if (cachedEntries == null) {
            cachedEntries = Leaderboard.loadAll();
        }
        // sort() mutates the list in place, so hand it a copy to keep the cache pristine.
        return Leaderboard.sort(new ArrayList<>(cachedEntries), sortField, sortOrder);
    }

    /** Forces the next {@link #getEntries()} to re-read all user JSONs from disk. */
    public void reload() {
        cachedEntries = null;
    }

    public LeaderboardSortField getSortField() {
        return sortField;
    }

    public SortTypes getSortOrder() {
        return sortOrder;
    }

    public void setSortField(LeaderboardSortField field) {
        this.sortField = field;
    }

    public void toggleSortOrder() {
        sortOrder = (sortOrder == SortTypes.DESCENDING) ? SortTypes.ASCENDING : SortTypes.DESCENDING;
    }

    /** Username of the currently logged-in user, so the view can highlight their row. */
    public String getCurrentUsername() {
        return AppContext.getInstance().getCurrentUser() != null
            ? AppContext.getInstance().getCurrentUser().getUsername()
            : null;
    }
}
