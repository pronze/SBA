package io.pronze.hypixelify.message;

import io.pronze.hypixelify.SBAHypixelify;

import java.util.List;

public class Messages {

    //TODO: save memory by calling getConfigurator() directly
    public static List<String> message_cannot_invite;
    public static List<String> message_no_other_commands;
    public static List<String> message_leader_join_leave;
    public static List<String> message_invite_expired;
    public static List<String> message_invalid_command;
    public static List<String> message_access_denied;
    public static List<String> message_not_in_party;
    public static List<String> message_invited;
    public static List<String> message_already_invited;
    public static List<String> message_warped;
    public static List<String> message_warping;
    public static List<String> message_invite;
    public static List<String> message_accepted;
    public static List<String> message_offline_left;
    public static List<String> message_offline_quit;
    public static List<String> message_declined;
    public static List<String> message_kicked;
    public static List<String> message_disband_inactivity;
    public static List<String> message_disband;
    public static List<String> message_player_not_found;
    public static List<String> message_blank_yourself;
    public static List<String> message_not_invited;
    public static List<String> message_got_kicked;
    public static List<String> message_decline_inc;
    public static List<String> message_decline_user;
    public static List<String> message_p_chat_enabled_disabled;
    public static List<String> message_party_help;
    public static List<String> message_cannot_invite_yourself;
    public static List<String> message_party_left;
    public static String message_respawn_title, message_respawn_subtitle, message_respawned_title;
    public static String upgrade_Keyword;
    public static String message_you;
    public static List<String> message_not_in_game;
    public static String shoutFormat;
    public static List<String> message_shout_wait;
    public static List<String> message_party_disband_inactivity;
    public static String trapTriggered_title, trapTriggered_subtitle, blindnessTrapPurchased, generatorUpgrade;
    public static String message_greatest_enchantment;
    public static String ERROR_OCCURED;
    public static String message_upgrade_team_protection;
    public static String trap_timeout_message;
    public static String already_purchased_thing;
    public static String lobby_chat_format;
    public static String message_bed_destroyed_title, message_bed_destroyed_subtitle;
    public static String message_purchase_heal_pool;
    public static String command_invalid;
    public static String command_player_only;
    public static String command_no_permissions;
    public static String message_game_starts_in;


    public Messages() {

    }

    public String fetchString(String toFetch) {
        return SBAHypixelify.getConfigurator().config.getString(toFetch);
    }

    public String fetchString(String toFetch, String def) {
        return SBAHypixelify.getConfigurator().config.getString(toFetch, def);
    }

    public List<String> fetchStringList(String toFetch) {
        return SBAHypixelify.getConfigurator().config.getStringList(toFetch);
    }

    public void loadConfig() {
        message_game_starts_in = fetchString("message.game-starts-in", "&eThe game starts in &c{seconds} &eseconds");
        message_party_disband_inactivity=  fetchStringList("party.message.disband-inactivity");
        command_no_permissions = fetchString("commands.no-permissions");
        command_player_only = fetchString("commands.player-only");
        command_invalid = fetchString("commands.invalid-command");
        message_purchase_heal_pool = fetchString("message.purchase-heal-pool");
        message_bed_destroyed_title = fetchString("message.bed-destroyed.title");
        message_bed_destroyed_subtitle = fetchString("message.bed-destroyed.sub-title");
        lobby_chat_format = fetchString("main-lobby.chat-format");
        already_purchased_thing = fetchString("message.already-purchased");
        trap_timeout_message = fetchString("message.wait-trap");
        message_upgrade_team_protection = fetchString("message.upgrade-team-protection");
        ERROR_OCCURED = fetchString("message.error-occured");
        message_greatest_enchantment = fetchString("message.greatest-enchantment");
        generatorUpgrade = fetchString("message.generator-upgrade");
        blindnessTrapPurchased = fetchString("message.blindness-trap-purchased-title");
        trapTriggered_title = fetchString("message.trap-triggered.title");
        trapTriggered_subtitle = fetchString("message.trap-triggered.sub-title");
        message_respawned_title = fetchString("message.respawned-title");
        message_respawn_subtitle = fetchString("message.respawn-subtitle");
        message_respawn_title = fetchString("message.respawn-title");
        shoutFormat = fetchString("message.shout-format", "§6[SHOUT] {color}[{team}]§r {player}§7: §r{message}");
        message_you = fetchString("scoreboard.you", "§7YOU");
        upgrade_Keyword = fetchString("message.upgrade", "Upgrade: ");
        message_shout_wait = fetchStringList("message.shout-wait");
        message_not_in_game = fetchStringList("message.not-in-game");
        message_party_left = fetchStringList("party.message.left");
        message_cannot_invite = fetchStringList("party.message.cannotinvite");
        message_no_other_commands = fetchStringList("party.message.no-other-commands");
        message_leader_join_leave = fetchStringList("party.message.leader-join-leave");
        message_invite_expired = fetchStringList("party.message.expired");
        message_invalid_command = fetchStringList("party.message.invalid-command");
        message_access_denied = fetchStringList("party.message.access-denied");
        message_not_in_party = fetchStringList("party.message.notinparty");
        message_invited = fetchStringList("party.message.invited");
        message_already_invited = fetchStringList("party.message.alreadyInvited");
        message_warped = fetchStringList("party.message.warp");
        message_warping = fetchStringList("party.message.warping");
        message_invite = fetchStringList("party.message.invite");
        message_accepted = fetchStringList("party.message.accepted");
        message_offline_left = fetchStringList("party.message.offline-left");
        message_offline_quit = fetchStringList("party.message.offline-quit");
        message_declined = fetchStringList("party.message.declined");
        message_kicked = fetchStringList("party.message.kicked");
        message_disband_inactivity = fetchStringList("party.message.disband-inactivity");
        message_disband = fetchStringList("party.message.disband");
        message_player_not_found = fetchStringList("party.message.player-not-found");
        message_blank_yourself = fetchStringList("party.message.cannot-blank-yourself");
        message_not_invited = fetchStringList("party.message.not-invited");
        message_got_kicked = fetchStringList("party.message.got-kicked");
        message_decline_inc = fetchStringList("party.message.decline-inc");
        message_decline_user = fetchStringList("party.message.declined-user");
        message_p_chat_enabled_disabled = fetchStringList("party.message.chat-enable-disabled");
        message_party_help = fetchStringList("party.message.help");
        message_cannot_invite_yourself = fetchStringList("party.message.cannot-invite-yourself");

    }

}
