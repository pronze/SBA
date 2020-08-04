package org.pronze.hypixelify.message;

import org.pronze.hypixelify.Hypixelify;

import java.util.List;

public class Messages {

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

    public Messages(){

    }

    public void loadConfig(){
        message_you = Hypixelify.getConfigurator().config.getString("scoreboard.you", "§7YOU");
        upgrade_Keyword = Hypixelify.getConfigurator().config.getString("message.upgrade", "Upgrade: ");
        message_respawned_title = Hypixelify.getConfigurator().config.getString("message.respawned-title");
        message_respawn_subtitle = Hypixelify.getConfigurator().config.getString("message.respawn-subtitle");
        message_respawn_title = Hypixelify.getConfigurator().config.getString("message.respawn-title");
        message_party_left = Hypixelify.getConfigurator().config.getStringList("party.message.left");
        message_cannot_invite = Hypixelify.getConfigurator().config.getStringList("party.message.cannotinvite");
        message_no_other_commands = Hypixelify.getConfigurator().config.getStringList("party.message.no-other-commands");
        message_leader_join_leave =  Hypixelify.getConfigurator().config.getStringList("party.message.leader-join-leave");
        message_invite_expired = Hypixelify.getConfigurator().config.getStringList("party.message.expired");
        message_invalid_command = Hypixelify.getConfigurator().config.getStringList("party.message.invalid-command");
        message_access_denied = Hypixelify.getConfigurator().config.getStringList("party.message.access-denied");
        message_not_in_party = Hypixelify.getConfigurator().config.getStringList("party.message.notinparty");
        message_invited = Hypixelify.getConfigurator().config.getStringList("party.message.invited");
        message_already_invited = Hypixelify.getConfigurator().config.getStringList("party.message.alreadyInvited");
        message_warped = Hypixelify.getConfigurator().config.getStringList("party.message.warp");
        message_warping = Hypixelify.getConfigurator().config.getStringList("party.message.warping");
        message_invite = Hypixelify.getConfigurator().config.getStringList("party.message.invite");
        message_accepted = Hypixelify.getConfigurator().config.getStringList("party.message.accepted");
        message_offline_left = Hypixelify.getConfigurator().config.getStringList("party.message.offline-left");
        message_offline_quit = Hypixelify.getConfigurator().config.getStringList("party.message.offline-quit");
        message_declined = Hypixelify.getConfigurator().config.getStringList("party.message.declined");
        message_kicked = Hypixelify.getConfigurator().config.getStringList("party.message.kicked");
        message_disband_inactivity = Hypixelify.getConfigurator().config.getStringList("party.message.disband-inactivity");
        message_disband = Hypixelify.getConfigurator().config.getStringList("party.message.disband");
        message_player_not_found = Hypixelify.getConfigurator().config.getStringList("party.message.player-not-found");
        message_blank_yourself = Hypixelify.getConfigurator().config.getStringList("party.message.cannot-blank-yourself");
        message_not_invited = Hypixelify.getConfigurator().config.getStringList("party.message.not-invited");
        message_got_kicked = Hypixelify.getConfigurator().config.getStringList("party.message.got-kicked");
        message_decline_inc = Hypixelify.getConfigurator().config.getStringList("party.message.decline-inc");
        message_decline_user = Hypixelify.getConfigurator().config.getStringList("party.message.declined-user");
        message_p_chat_enabled_disabled = Hypixelify.getConfigurator().config.getStringList("party.message.chat-enable-disabled");
        message_party_help = Hypixelify.getConfigurator().config.getStringList("party.message.help");
        message_cannot_invite_yourself = Hypixelify.getConfigurator().config.getStringList("party.message.cannot-invite-yourself");
    }

}
