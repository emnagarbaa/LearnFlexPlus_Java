package Services;

import entities.Ban;
import Utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ServiceBan {

    // ── private helper ────────────────────────────────────────────────────
    private Connection conn() throws SQLException {
        return MyDatabase.getInstance().getConnection();
    }

    // ── map a ResultSet row → Ban ──────────────────────────────────────────
    private Ban map(ResultSet rs) throws SQLException {
        Ban b = new Ban();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setReason(rs.getString("reason"));
        b.setBannedBy(rs.getInt("banned_by"));
        b.setBanType(rs.getString("ban_type"));
        b.setActive(rs.getBoolean("is_active"));

        Timestamp ca = rs.getTimestamp("created_at");
        Timestamp ea = rs.getTimestamp("expires_at");
        if (ca != null) b.setCreatedAt(ca.toLocalDateTime());
        if (ea != null) b.setExpiresAt(ea.toLocalDateTime());
        return b;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 1.  getActiveBan
    //     Returns the active TEMPORARY or PERMANENT ban for a user,
    //     or null if none.  Called in LoginController before letting
    //     the user in.
    // ─────────────────────────────────────────────────────────────────────
    public Ban getActiveBan(int userId) throws SQLException {

        String sql = """
                SELECT * FROM ban
                WHERE  user_id   = ?
                  AND  is_active  = 1
                  AND  ban_type  != 'WARNING'
                  AND  (expires_at IS NULL OR expires_at > NOW())
                ORDER BY created_at DESC
                LIMIT 1
                """;

        PreparedStatement ps = conn().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? map(rs) : null;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2.  getWarningCount
    //     How many active warnings does this user have?
    // ─────────────────────────────────────────────────────────────────────
    public int getWarningCount(int userId) throws SQLException {

        String sql = """
                SELECT COUNT(*) FROM ban
                WHERE  user_id  = ?
                  AND  ban_type = 'WARNING'
                  AND  is_active = 1
                """;

        PreparedStatement ps = conn().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3.  warnUser
    //     Adds a WARNING row.
    //     If the user now has ≥ 3 warnings → auto-ban for 24 h.
    //     Returns the NEW total warning count.
    //
    //     Called by: UserController  (admin panel)
    //                ForumController (teammate's module)
    // ─────────────────────────────────────────────────────────────────────
    public int warnUser(int userId, String reason, int adminId) throws SQLException {

        // Insert WARNING row
        String insert = """
                INSERT INTO ban (user_id, reason, banned_by, ban_type, is_active, created_at)
                VALUES (?, ?, ?, 'WARNING', 1, NOW())
                """;

        PreparedStatement ps = conn().prepareStatement(insert);
        ps.setInt(1, userId);
        ps.setString(2, reason);
        ps.setInt(3, adminId);
        ps.executeUpdate();

        // Check total
        int total = getWarningCount(userId);

        // Auto-ban at 3 warnings
        if (total >= 3) {
            banUser(userId,
                    "Auto-ban : 3 avertissements accumulés",
                    adminId,
                    true,     // temporary
                    24);      // 24 hours
        }

        return total;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4.  banUser
    //     Creates a TEMPORARY or PERMANENT ban.
    //     Any existing active ban for this user is deactivated first
    //     so you never stack duplicates.
    //
    //     temporary = false  →  PERMANENT (expires_at stays NULL)
    //     temporary = true   →  TEMPORARY (expires_at = NOW + hours)
    //
    //     Called by: UserController  (admin panel)
    //                ForumController (teammate's module — pass their adminId)
    // ─────────────────────────────────────────────────────────────────────
    public void banUser(int userId, String reason, int adminId,
                        boolean temporary, int hours) throws SQLException {

        // Deactivate any previous non-WARNING ban
        String deactivate = """
                UPDATE ban SET is_active = 0
                WHERE  user_id  = ?
                  AND  is_active = 1
                  AND  ban_type != 'WARNING'
                """;
        PreparedStatement deact = conn().prepareStatement(deactivate);
        deact.setInt(1, userId);
        deact.executeUpdate();

        // Insert new ban
        String insert = """
                INSERT INTO ban
                  (user_id, reason, banned_by, ban_type, is_active, created_at, expires_at)
                VALUES (?, ?, ?, ?, 1, NOW(), ?)
                """;

        PreparedStatement ps = conn().prepareStatement(insert);
        ps.setInt(1, userId);
        ps.setString(2, reason);
        ps.setInt(3, adminId);
        ps.setString(4, temporary ? "TEMPORARY" : "PERMANENT");

        if (temporary && hours > 0) {
            var expiry = LocalDateTime.now().plusHours(hours);
            ps.setTimestamp(5, Timestamp.valueOf(expiry));
        } else {
            ps.setNull(5, Types.TIMESTAMP);
        }

        ps.executeUpdate();
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5.  unbanUser
    //     Lifts all active bans (not warnings) for a user.
    //     Useful if admin wants to pardon someone early.
    // ─────────────────────────────────────────────────────────────────────
    public void unbanUser(int userId) throws SQLException {

        String sql = """
                UPDATE ban SET is_active = 0
                WHERE  user_id  = ?
                  AND  ban_type != 'WARNING'
                """;
        PreparedStatement ps = conn().prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
    }

    // ─────────────────────────────────────────────────────────────────────
    // 6.  getBanHistory
    //     Full ban log for one user.  Useful for a "history" tab.
    // ─────────────────────────────────────────────────────────────────────
    public List<Ban> getBanHistory(int userId) throws SQLException {

        List<Ban> list = new ArrayList<>();
        String sql = """
                SELECT * FROM ban
                WHERE  user_id = ?
                ORDER BY created_at DESC
                """;

        PreparedStatement ps = conn().prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 7.  expireOldBans  (optional maintenance)
    //     Call once on app start or via a scheduler to flip expired
    //     temporary bans to is_active = 0 automatically.
    // ─────────────────────────────────────────────────────────────────────
    public void expireOldBans() throws SQLException {

        String sql = """
                UPDATE ban SET is_active = 0
                WHERE  ban_type  = 'TEMPORARY'
                  AND  is_active  = 1
                  AND  expires_at IS NOT NULL
                  AND  expires_at <= NOW()
                """;
        conn().createStatement().executeUpdate(sql);
    }
    public Set<Integer> getAllActiveBannedUserIds() throws SQLException {

        Set<Integer> ids = new java.util.HashSet<>();

        String sql = """
            SELECT DISTINCT user_id FROM ban
            WHERE  is_active  = 1
              AND  ban_type   != 'WARNING'
              AND  (expires_at IS NULL OR expires_at > NOW())
            """;

        java.sql.ResultSet rs = conn().createStatement().executeQuery(sql);
        while (rs.next()) ids.add(rs.getInt("user_id"));
        return ids;
    }

}