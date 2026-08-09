package com.pvz.controller;

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

    public List<LeaderBoardEntry> getEntries() {
        List<LeaderBoardEntry> all = Leaderboard.loadAll();
        return Leaderboard.sort(all, sortField, sortOrder);
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
