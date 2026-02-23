/*
 * Copyright (c) 2013 L2jMobius
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.l2jmobius.gameserver.network;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.gameserver.config.custom.MultilingualSupportConfig;
import org.l2jmobius.gameserver.model.clientstrings.Builder;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class SystemMessageId
{
	private static final Logger LOGGER = Logger.getLogger(SystemMessageId.class.getName());
	
	private static final SMLocalisation[] EMPTY_SML_ARRAY = new SMLocalisation[0];
	private static final Map<Integer, SystemMessageId> VALUES = new HashMap<>();
	
	@ClientString(id = 0, message = "You have been disconnected from the server.")
	public static SystemMessageId YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER;
	
	@ClientString(id = 1, message = "The server will be disconnected in $s1 seconds. Please exit.")
	public static SystemMessageId THE_SERVER_WILL_BE_DISCONNECTED_IN_S1_SECONDS_PLEASE_EXIT;
	
	@ClientString(id = 2, message = "$s1 does not exist.")
	public static SystemMessageId S1_DOES_NOT_EXIST;
	
	@ClientString(id = 3, message = "$s1 is not logged in.")
	public static SystemMessageId S1_IS_NOT_LOGGED_IN;
	
	@ClientString(id = 4, message = "You cannot ask yourself to apply to a clan.")
	public static SystemMessageId YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN;
	
	@ClientString(id = 5, message = "$s1 already exists.")
	public static SystemMessageId S1_ALREADY_EXISTS;
	
	@ClientString(id = 6, message = "$s1 does not exist.")
	public static SystemMessageId S1_DOES_NOT_EXIST_2;
	
	@ClientString(id = 7, message = "You already belong to $s1.")
	public static SystemMessageId YOU_ALREADY_BELONG_TO_S1;
	
	@ClientString(id = 8, message = "You are working with another clan.")
	public static SystemMessageId YOU_ARE_WORKING_WITH_ANOTHER_CLAN;
	
	@ClientString(id = 9, message = "$s1 is not a clan leader.")
	public static SystemMessageId S1_IS_NOT_A_CLAN_LEADER;
	
	@ClientString(id = 10, message = "$s1 is working with another clan.")
	public static SystemMessageId S1_IS_WORKING_WITH_ANOTHER_CLAN;
	
	@ClientString(id = 11, message = "There are no applicants for this clan.")
	public static SystemMessageId THERE_ARE_NO_APPLICANTS_FOR_THIS_CLAN;
	
	@ClientString(id = 12, message = "Applicant information is incorrect.")
	public static SystemMessageId APPLICANT_INFORMATION_IS_INCORRECT;
	
	@ClientString(id = 13, message = "Unable to disperse: your clan has requested to participate in a castle siege.")
	public static SystemMessageId UNABLE_TO_DISPERSE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE;
	
	@ClientString(id = 14, message = "Unable to disperse: your clan owns one or more castles or hideouts.")
	public static SystemMessageId UNABLE_TO_DISPERSE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS;
	
	@ClientString(id = 15, message = "You are in siege.")
	public static SystemMessageId YOU_ARE_IN_SIEGE;
	
	@ClientString(id = 16, message = "You are not in siege.")
	public static SystemMessageId YOU_ARE_NOT_IN_SIEGE;
	
	@ClientString(id = 17, message = "Castle siege has begun.")
	public static SystemMessageId CASTLE_SIEGE_HAS_BEGUN;
	
	@ClientString(id = 18, message = "Castle siege is over.")
	public static SystemMessageId CASTLE_SIEGE_IS_OVER;
	
	@ClientString(id = 19, message = "The castellan has been changed!")
	public static SystemMessageId THE_CASTELLAN_HAS_BEEN_CHANGED;
	
	@ClientString(id = 20, message = "The gate is being opened.")
	public static SystemMessageId THE_GATE_IS_BEING_OPENED;
	
	@ClientString(id = 21, message = "The gate is being destroyed.")
	public static SystemMessageId THE_GATE_IS_BEING_DESTROYED;
	
	@ClientString(id = 22, message = "Target is too far.")
	public static SystemMessageId TARGET_IS_TOO_FAR;
	
	@ClientString(id = 23, message = "Not enough HP.")
	public static SystemMessageId NOT_ENOUGH_HP;
	
	@ClientString(id = 24, message = "Not enough MP.")
	public static SystemMessageId NOT_ENOUGH_MP;
	
	@ClientString(id = 25, message = "Rejuvenating HP.")
	public static SystemMessageId REJUVENATING_HP;
	
	@ClientString(id = 26, message = "Rejuvenating MP.")
	public static SystemMessageId REJUVENATING_MP;
	
	@ClientString(id = 27, message = "Casting has been interrupted.")
	public static SystemMessageId CASTING_HAS_BEEN_INTERRUPTED;
	
	@ClientString(id = 28, message = "You have obtained $s1 adena.")
	public static SystemMessageId YOU_HAVE_OBTAINED_S1_ADENA;
	
	@ClientString(id = 29, message = "You have obtained $s2 $s1.")
	public static SystemMessageId YOU_HAVE_OBTAINED_S2_S1;
	
	@ClientString(id = 30, message = "You have obtained $s1.")
	public static SystemMessageId YOU_HAVE_OBTAINED_S1;
	
	@ClientString(id = 31, message = "You cannot move while sitting.")
	public static SystemMessageId YOU_CANNOT_MOVE_WHILE_SITTING;
	
	@ClientString(id = 32, message = "You are not capable of combat. Move to the nearest restart point.")
	public static SystemMessageId YOU_ARE_NOT_CAPABLE_OF_COMBAT_MOVE_TO_THE_NEAREST_RESTART_POINT;
	
	@ClientString(id = 33, message = "You cannot move when using magic.")
	public static SystemMessageId YOU_CANNOT_MOVE_WHEN_USING_MAGIC;
	
	@ClientString(id = 34, message = "Welcome to the World of Lineage II.")
	public static SystemMessageId WELCOME_TO_THE_WORLD_OF_LINEAGE_II;
	
	@ClientString(id = 35, message = "You hit for $s1 damage.")
	public static SystemMessageId YOU_HIT_FOR_S1_DAMAGE;
	
	@ClientString(id = 36, message = "$s1 hit you for $s2 damage.")
	public static SystemMessageId S1_HIT_YOU_FOR_S2_DAMAGE;
	
	@ClientString(id = 37, message = "$s1 hit you for $s2 damage.")
	public static SystemMessageId S1_HIT_YOU_FOR_S2_DAMAGE_2;
	
	@ClientString(id = 38, message = "The TGS2002 event begins!")
	public static SystemMessageId THE_TGS2002_EVENT_BEGINS;
	
	@ClientString(id = 39, message = "The TGS2002 event is over. Thank you very much.")
	public static SystemMessageId THE_TGS2002_EVENT_IS_OVER_THANK_YOU_VERY_MUCH;
	
	@ClientString(id = 40, message = "This is the TGS demo: the character will immediately be restored.")
	public static SystemMessageId THIS_IS_THE_TGS_DEMO_THE_CHARACTER_WILL_IMMEDIATELY_BE_RESTORED;
	
	@ClientString(id = 41, message = "Getting ready to shoot arrows.")
	public static SystemMessageId GETTING_READY_TO_SHOOT_ARROWS;
	
	@ClientString(id = 42, message = "Avoided $s1's attack.")
	public static SystemMessageId AVOIDED_S1_S_ATTACK;
	
	@ClientString(id = 43, message = "Missed target.")
	public static SystemMessageId MISSED_TARGET;
	
	@ClientString(id = 44, message = "Critical hit!")
	public static SystemMessageId CRITICAL_HIT;
	
	@ClientString(id = 45, message = "You have earned $s1 experience.")
	public static SystemMessageId YOU_HAVE_EARNED_S1_EXPERIENCE;
	
	@ClientString(id = 46, message = "Use $s1.")
	public static SystemMessageId USE_S1;
	
	@ClientString(id = 47, message = "Using $s1.")
	public static SystemMessageId USING_S1;
	
	@ClientString(id = 48, message = "$s1 is not available at this time: being prepared for reuse.")
	public static SystemMessageId S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE;
	
	@ClientString(id = 49, message = "You are equipped with $s1.")
	public static SystemMessageId YOU_ARE_EQUIPPED_WITH_S1;
	
	@ClientString(id = 50, message = "Target can not be found.")
	public static SystemMessageId TARGET_CAN_NOT_BE_FOUND;
	
	@ClientString(id = 51, message = "You cannot use this on yourself.")
	public static SystemMessageId YOU_CANNOT_USE_THIS_ON_YOURSELF;
	
	@ClientString(id = 52, message = "Earned $s1 adena.")
	public static SystemMessageId EARNED_S1_ADENA;
	
	@ClientString(id = 53, message = "Earned $s2 $s1(s).")
	public static SystemMessageId EARNED_S2_S1_S;
	
	@ClientString(id = 54, message = "Earned $s1.")
	public static SystemMessageId EARNED_S1;
	
	@ClientString(id = 55, message = "Failed to pick up $s1 adena.")
	public static SystemMessageId FAILED_TO_PICK_UP_S1_ADENA;
	
	@ClientString(id = 56, message = "Failed to pick up $s1.")
	public static SystemMessageId FAILED_TO_PICK_UP_S1;
	
	@ClientString(id = 57, message = "Failed to pick up $s2 $s1(s).")
	public static SystemMessageId FAILED_TO_PICK_UP_S2_S1_S;
	
	@ClientString(id = 58, message = "Failed to earn $s1 adena.")
	public static SystemMessageId FAILED_TO_EARN_S1_ADENA;
	
	@ClientString(id = 59, message = "Failed to earn $s1.")
	public static SystemMessageId FAILED_TO_EARN_S1;
	
	@ClientString(id = 60, message = "Failed to earn $s2 $s1(s).")
	public static SystemMessageId FAILED_TO_EARN_S2_S1_S;
	
	@ClientString(id = 61, message = "Nothing happened.")
	public static SystemMessageId NOTHING_HAPPENED;
	
	@ClientString(id = 62, message = "$s1 has been successfully enchanted.")
	public static SystemMessageId S1_HAS_BEEN_SUCCESSFULLY_ENCHANTED;
	
	@ClientString(id = 63, message = "+$s1 $s2 has been successfully enchanted.")
	public static SystemMessageId S1_S2_HAS_BEEN_SUCCESSFULLY_ENCHANTED;
	
	@ClientString(id = 64, message = "Enchantment has failed. $s1 has been crystallized.")
	public static SystemMessageId ENCHANTMENT_HAS_FAILED_S1_HAS_BEEN_CRYSTALLIZED;
	
	@ClientString(id = 65, message = "Enchantment has failed.+$s1 $s2 has been evaporated.")
	public static SystemMessageId ENCHANTMENT_HAS_FAILED_S1_S2_HAS_BEEN_EVAPORATED;
	
	@ClientString(id = 66, message = "$s1 has invited you to his/her party. Do you accept the invitation?")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_HIS_HER_PARTY_DO_YOU_ACCEPT_THE_INVITATION;
	
	@ClientString(id = 67, message = "$s1 has invited you to the $s2 Clan. Do you want to join?")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_THE_S2_CLAN_DO_YOU_WANT_TO_JOIN;
	
	@ClientString(id = 68, message = "Withdraw from the $s1 Clan. Do you want to continue?")
	public static SystemMessageId WITHDRAW_FROM_THE_S1_CLAN_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 69, message = "Expel $s1 from the clan. Do you want to continue?")
	public static SystemMessageId EXPEL_S1_FROM_THE_CLAN_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 70, message = "Disperse the $s1 Clan. Do you want to continue?")
	public static SystemMessageId DISPERSE_THE_S1_CLAN_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 71, message = "How many $s1(s) do you want to discard?")
	public static SystemMessageId HOW_MANY_S1_S_DO_YOU_WANT_TO_DISCARD;
	
	@ClientString(id = 72, message = "How many $s1(s) do you want to move?")
	public static SystemMessageId HOW_MANY_S1_S_DO_YOU_WANT_TO_MOVE;
	
	@ClientString(id = 73, message = "How many $s1(s) do you want to destroy?")
	public static SystemMessageId HOW_MANY_S1_S_DO_YOU_WANT_TO_DESTROY;
	
	@ClientString(id = 74, message = "Destroy $s1. Do you want to continue?")
	public static SystemMessageId DESTROY_S1_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 75, message = "ID does not exist.")
	public static SystemMessageId ID_DOES_NOT_EXIST;
	
	@ClientString(id = 76, message = "Incorrect password.")
	public static SystemMessageId INCORRECT_PASSWORD;
	
	@ClientString(id = 77, message = "You cannot create another character. Please delete the existing character and try again.")
	public static SystemMessageId YOU_CANNOT_CREATE_ANOTHER_CHARACTER_PLEASE_DELETE_THE_EXISTING_CHARACTER_AND_TRY_AGAIN;
	
	@ClientString(id = 78, message = "Do you want to delete $s1?")
	public static SystemMessageId DO_YOU_WANT_TO_DELETE_S1;
	
	@ClientString(id = 79, message = "Name already exists.")
	public static SystemMessageId NAME_ALREADY_EXISTS;
	
	@ClientString(id = 80, message = "Character name must be 1-16 characters, without any spaces.")
	public static SystemMessageId CHARACTER_NAME_MUST_BE_1_16_CHARACTERS_WITHOUT_ANY_SPACES;
	
	@ClientString(id = 81, message = "Please select your race.")
	public static SystemMessageId PLEASE_SELECT_YOUR_RACE;
	
	@ClientString(id = 82, message = "Please select your occupation.")
	public static SystemMessageId PLEASE_SELECT_YOUR_OCCUPATION;
	
	@ClientString(id = 83, message = "Please select your gender.")
	public static SystemMessageId PLEASE_SELECT_YOUR_GENDER;
	
	@ClientString(id = 84, message = "You cannot attack in the peace zone.")
	public static SystemMessageId YOU_CANNOT_ATTACK_IN_THE_PEACE_ZONE;
	
	@ClientString(id = 85, message = "You cannot attack the target in the peace zone.")
	public static SystemMessageId YOU_CANNOT_ATTACK_THE_TARGET_IN_THE_PEACE_ZONE;
	
	@ClientString(id = 86, message = "Please insert your ID.")
	public static SystemMessageId PLEASE_INSERT_YOUR_ID;
	
	@ClientString(id = 87, message = "Please insert your password.")
	public static SystemMessageId PLEASE_INSERT_YOUR_PASSWORD;
	
	@ClientString(id = 88, message = "Protocol version is different. Please quit the program.")
	public static SystemMessageId PROTOCOL_VERSION_IS_DIFFERENT_PLEASE_QUIT_THE_PROGRAM;
	
	@ClientString(id = 89, message = "Protocol version is different. Please continue.")
	public static SystemMessageId PROTOCOL_VERSION_IS_DIFFERENT_PLEASE_CONTINUE;
	
	@ClientString(id = 90, message = "Unable to connect to server.")
	public static SystemMessageId UNABLE_TO_CONNECT_TO_SERVER;
	
	@ClientString(id = 91, message = "Please select your hairstyle.")
	public static SystemMessageId PLEASE_SELECT_YOUR_HAIRSTYLE;
	
	@ClientString(id = 92, message = "The effect of $s1 has worn off.")
	public static SystemMessageId THE_EFFECT_OF_S1_HAS_WORN_OFF;
	
	@ClientString(id = 93, message = "Not enough SP.")
	public static SystemMessageId NOT_ENOUGH_SP;
	
	@ClientString(id = 94, message = "2002 - 2005 Copyright NCsoft Corporation. All Rights Reserved.")
	public static SystemMessageId TWO_THOUSAND_TWO_TWO_THOUSAND_FIVE_COPYRIGHT_NCSOFT_CORPORATION_ALL_RIGHTS_RESERVED;
	
	@ClientString(id = 95, message = "You have earned $s1 experience and $s2 SP.")
	public static SystemMessageId YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP;
	
	@ClientString(id = 96, message = "You have increased your level!")
	public static SystemMessageId YOU_HAVE_INCREASED_YOUR_LEVEL;
	
	@ClientString(id = 97, message = "This item cannot be moved.")
	public static SystemMessageId THIS_ITEM_CANNOT_BE_MOVED;
	
	@ClientString(id = 98, message = "This item cannot be discarded.")
	public static SystemMessageId THIS_ITEM_CANNOT_BE_DISCARDED;
	
	@ClientString(id = 99, message = "This item cannot be traded or sold.")
	public static SystemMessageId THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD;
	
	@ClientString(id = 100, message = "$s1 requests a trade. Do you want to trade?")
	public static SystemMessageId S1_REQUESTS_A_TRADE_DO_YOU_WANT_TO_TRADE;
	
	@ClientString(id = 101, message = "You cannot logout while in combat.")
	public static SystemMessageId YOU_CANNOT_LOGOUT_WHILE_IN_COMBAT;
	
	@ClientString(id = 102, message = "You cannot restart while in combat.")
	public static SystemMessageId YOU_CANNOT_RESTART_WHILE_IN_COMBAT;
	
	@ClientString(id = 103, message = "ID is logged in.")
	public static SystemMessageId ID_IS_LOGGED_IN;
	
	@ClientString(id = 104, message = "You cannot use equipment when using other skills or magic.")
	public static SystemMessageId YOU_CANNOT_USE_EQUIPMENT_WHEN_USING_OTHER_SKILLS_OR_MAGIC;
	
	@ClientString(id = 105, message = "You have invited $s1 to your party.")
	public static SystemMessageId YOU_HAVE_INVITED_S1_TO_YOUR_PARTY;
	
	@ClientString(id = 106, message = "You have joined $s1's party.")
	public static SystemMessageId YOU_HAVE_JOINED_S1_S_PARTY;
	
	@ClientString(id = 107, message = "$s1 has joined the party.")
	public static SystemMessageId S1_HAS_JOINED_THE_PARTY;
	
	@ClientString(id = 108, message = "$s1 has left the party.")
	public static SystemMessageId S1_HAS_LEFT_THE_PARTY;
	
	@ClientString(id = 109, message = "Incorrect target.")
	public static SystemMessageId INCORRECT_TARGET;
	
	@ClientString(id = 110, message = "You can feel $s1's effect.")
	public static SystemMessageId YOU_CAN_FEEL_S1_S_EFFECT;
	
	@ClientString(id = 111, message = "Shield defense has succeeded.")
	public static SystemMessageId SHIELD_DEFENSE_HAS_SUCCEEDED;
	
	@ClientString(id = 112, message = "Not enough arrows.")
	public static SystemMessageId NOT_ENOUGH_ARROWS;
	
	@ClientString(id = 113, message = "$s1 cannot be used due to unsuitable terms.")
	public static SystemMessageId S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS;
	
	@ClientString(id = 114, message = "Enter the shadow of the World Tree.")
	public static SystemMessageId ENTER_THE_SHADOW_OF_THE_WORLD_TREE;
	
	@ClientString(id = 115, message = "Exit the shadow of the World Tree.")
	public static SystemMessageId EXIT_THE_SHADOW_OF_THE_WORLD_TREE;
	
	@ClientString(id = 116, message = "Entering the peace zone.")
	public static SystemMessageId ENTERING_THE_PEACE_ZONE;
	
	@ClientString(id = 117, message = "Exiting the peace zone.")
	public static SystemMessageId EXITING_THE_PEACE_ZONE;
	
	@ClientString(id = 118, message = "Request $s1 for trade.")
	public static SystemMessageId REQUEST_S1_FOR_TRADE;
	
	@ClientString(id = 119, message = "$s1 denied your request for trade.")
	public static SystemMessageId S1_DENIED_YOUR_REQUEST_FOR_TRADE;
	
	@ClientString(id = 120, message = "Begin trading with $s1.")
	public static SystemMessageId BEGIN_TRADING_WITH_S1;
	
	@ClientString(id = 121, message = "$s1 confirmed trade.")
	public static SystemMessageId S1_CONFIRMED_TRADE;
	
	@ClientString(id = 122, message = "You cannot move additional items because trade has been confirmed.")
	public static SystemMessageId YOU_CANNOT_MOVE_ADDITIONAL_ITEMS_BECAUSE_TRADE_HAS_BEEN_CONFIRMED;
	
	@ClientString(id = 123, message = "Trade has been successful.")
	public static SystemMessageId TRADE_HAS_BEEN_SUCCESSFUL;
	
	@ClientString(id = 124, message = "$s1 canceled the trade.")
	public static SystemMessageId S1_CANCELED_THE_TRADE;
	
	@ClientString(id = 125, message = "Quit game. Do you want to continue?")
	public static SystemMessageId QUIT_GAME_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 126, message = "Restart the game. Do you want to continue?")
	public static SystemMessageId RESTART_THE_GAME_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 127, message = "You have been disconnected from the server. Please login again.")
	public static SystemMessageId YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_PLEASE_LOGIN_AGAIN;
	
	@ClientString(id = 128, message = "You have failed to create a character.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_CREATE_A_CHARACTER;
	
	@ClientString(id = 129, message = "Inventory slot is full.")
	public static SystemMessageId INVENTORY_SLOT_IS_FULL;
	
	@ClientString(id = 130, message = "Warehouse slot is full.")
	public static SystemMessageId WAREHOUSE_SLOT_IS_FULL;
	
	@ClientString(id = 131, message = "$s1 has logged in.")
	public static SystemMessageId S1_HAS_LOGGED_IN;
	
	@ClientString(id = 132, message = "$s1 has been added to your friend list.")
	public static SystemMessageId S1_HAS_BEEN_ADDED_TO_YOUR_FRIEND_LIST;
	
	@ClientString(id = 133, message = "$s1 has been removed from your friend list.")
	public static SystemMessageId S1_HAS_BEEN_REMOVED_FROM_YOUR_FRIEND_LIST;
	
	@ClientString(id = 134, message = "Please check your friend list again.")
	public static SystemMessageId PLEASE_CHECK_YOUR_FRIEND_LIST_AGAIN;
	
	@ClientString(id = 135, message = "$s1 did not reply to your invitation: party invitation has been cancelled.")
	public static SystemMessageId S1_DID_NOT_REPLY_TO_YOUR_INVITATION_PARTY_INVITATION_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 136, message = "You did not reply to $s1's invitation: joining has been cancelled.")
	public static SystemMessageId YOU_DID_NOT_REPLY_TO_S1_S_INVITATION_JOINING_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 137, message = "There are no more items in the shortcut.")
	public static SystemMessageId THERE_ARE_NO_MORE_ITEMS_IN_THE_SHORTCUT;
	
	@ClientString(id = 138, message = "Designate shortcut.")
	public static SystemMessageId DESIGNATE_SHORTCUT;
	
	@ClientString(id = 139, message = "$s1 was unaffected by $s2.")
	public static SystemMessageId S1_WAS_UNAFFECTED_BY_S2;
	
	@ClientString(id = 140, message = "Skill was removed due to lack of MP.")
	public static SystemMessageId SKILL_WAS_REMOVED_DUE_TO_LACK_OF_MP;
	
	@ClientString(id = 141, message = "If trade is confirmed, the item cannot be moved again.")
	public static SystemMessageId IF_TRADE_IS_CONFIRMED_THE_ITEM_CANNOT_BE_MOVED_AGAIN;
	
	@ClientString(id = 142, message = "Already trading.")
	public static SystemMessageId ALREADY_TRADING;
	
	@ClientString(id = 143, message = "$s1 is trading with another person.")
	public static SystemMessageId S1_IS_TRADING_WITH_ANOTHER_PERSON;
	
	@ClientString(id = 144, message = "Target is incorrect.")
	public static SystemMessageId TARGET_IS_INCORRECT;
	
	@ClientString(id = 145, message = "Target is not found in the game.")
	public static SystemMessageId TARGET_IS_NOT_FOUND_IN_THE_GAME;
	
	@ClientString(id = 146, message = "Chatting is permitted.")
	public static SystemMessageId CHATTING_IS_PERMITTED;
	
	@ClientString(id = 147, message = "Chatting is prohibited.")
	public static SystemMessageId CHATTING_IS_PROHIBITED;
	
	@ClientString(id = 148, message = "You cannot use quest items.")
	public static SystemMessageId YOU_CANNOT_USE_QUEST_ITEMS;
	
	@ClientString(id = 149, message = "You cannot pick up or use items while trading.")
	public static SystemMessageId YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING;
	
	@ClientString(id = 150, message = "You cannot discard or destroy items while trading.")
	public static SystemMessageId YOU_CANNOT_DISCARD_OR_DESTROY_ITEMS_WHILE_TRADING;
	
	@ClientString(id = 151, message = "Too far to discard.")
	public static SystemMessageId TOO_FAR_TO_DISCARD;
	
	@ClientString(id = 152, message = "Wrong target has been invited.")
	public static SystemMessageId WRONG_TARGET_HAS_BEEN_INVITED;
	
	@ClientString(id = 153, message = "$s1 is busy. Please try again later.")
	public static SystemMessageId S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 154, message = "Only the leader can give out invitations.")
	public static SystemMessageId ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS;
	
	@ClientString(id = 155, message = "Party is full.")
	public static SystemMessageId PARTY_IS_FULL;
	
	@ClientString(id = 156, message = "Drain was only half successful.")
	public static SystemMessageId DRAIN_WAS_ONLY_HALF_SUCCESSFUL;
	
	@ClientString(id = 157, message = "You resisted $s1's drain.")
	public static SystemMessageId YOU_RESISTED_S1_S_DRAIN;
	
	@ClientString(id = 158, message = "Attack failed.")
	public static SystemMessageId ATTACK_FAILED;
	
	@ClientString(id = 159, message = "Resisted against $s1's magic.")
	public static SystemMessageId RESISTED_AGAINST_S1_S_MAGIC;
	
	@ClientString(id = 160, message = "$s1 is a member of another party and cannot be invited.")
	public static SystemMessageId S1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED;
	
	@ClientString(id = 161, message = "The user you invited is not online.")
	public static SystemMessageId THE_USER_YOU_INVITED_IS_NOT_ONLINE;
	
	@ClientString(id = 162, message = "Warehouse is too far.")
	public static SystemMessageId WAREHOUSE_IS_TOO_FAR;
	
	@ClientString(id = 163, message = "You cannot destroy it because the number is incorrect.")
	public static SystemMessageId YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT;
	
	@ClientString(id = 164, message = "Waiting for another reply.")
	public static SystemMessageId WAITING_FOR_ANOTHER_REPLY;
	
	@ClientString(id = 165, message = "You cannot add yourself to your own friend list.")
	public static SystemMessageId YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST;
	
	@ClientString(id = 166, message = "Friend list is not ready yet. Please register again later.")
	public static SystemMessageId FRIEND_LIST_IS_NOT_READY_YET_PLEASE_REGISTER_AGAIN_LATER;
	
	@ClientString(id = 167, message = "$s1 is already on your friend list.")
	public static SystemMessageId S1_IS_ALREADY_ON_YOUR_FRIEND_LIST;
	
	@ClientString(id = 168, message = "$s1 has requested to become friends.")
	public static SystemMessageId S1_HAS_REQUESTED_TO_BECOME_FRIENDS;
	
	@ClientString(id = 169, message = "Accept friendship 0/1 (1 to accept, 0 to deny)")
	public static SystemMessageId ACCEPT_FRIENDSHIP_0_1_1_TO_ACCEPT_0_TO_DENY;
	
	@ClientString(id = 170, message = "The user who requested to become friends is not found in the game.")
	public static SystemMessageId THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME;
	
	@ClientString(id = 171, message = "$s1 is not on your friend list.")
	public static SystemMessageId S1_IS_NOT_ON_YOUR_FRIEND_LIST;
	
	@ClientString(id = 172, message = "You have no money to pay for custody.")
	public static SystemMessageId YOU_HAVE_NO_MONEY_TO_PAY_FOR_CUSTODY;
	
	@ClientString(id = 173, message = "You don't have enough money to pay for custody.")
	public static SystemMessageId YOU_DON_T_HAVE_ENOUGH_MONEY_TO_PAY_FOR_CUSTODY;
	
	@ClientString(id = 174, message = "The person's inventory is full.")
	public static SystemMessageId THE_PERSON_S_INVENTORY_IS_FULL;
	
	@ClientString(id = 175, message = "HP was fully recovered, and skill was removed.")
	public static SystemMessageId HP_WAS_FULLY_RECOVERED_AND_SKILL_WAS_REMOVED;
	
	@ClientString(id = 176, message = "The person is in a message refusal mode.")
	public static SystemMessageId THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE;
	
	@ClientString(id = 177, message = "Message refusal mode.")
	public static SystemMessageId MESSAGE_REFUSAL_MODE;
	
	@ClientString(id = 178, message = "Message acceptance mode.")
	public static SystemMessageId MESSAGE_ACCEPTANCE_MODE;
	
	@ClientString(id = 179, message = "You cannot discard items here.")
	public static SystemMessageId YOU_CANNOT_DISCARD_ITEMS_HERE;
	
	@ClientString(id = 180, message = "You have $s1 day(s) left until deletion. Do you want to cancel deletion?")
	public static SystemMessageId YOU_HAVE_S1_DAY_S_LEFT_UNTIL_DELETION_DO_YOU_WANT_TO_CANCEL_DELETION;
	
	@ClientString(id = 181, message = "Cannot see target.")
	public static SystemMessageId CANNOT_SEE_TARGET;
	
	@ClientString(id = 182, message = "Do you want to quit the current quest?")
	public static SystemMessageId DO_YOU_WANT_TO_QUIT_THE_CURRENT_QUEST;
	
	@ClientString(id = 183, message = "There are too many users on the server. Please try again later.")
	public static SystemMessageId THERE_ARE_TOO_MANY_USERS_ON_THE_SERVER_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 184, message = "Please try again later.")
	public static SystemMessageId PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 185, message = "Select user to invite to your party.")
	public static SystemMessageId SELECT_USER_TO_INVITE_TO_YOUR_PARTY;
	
	@ClientString(id = 186, message = "Select user to invite to your clan.")
	public static SystemMessageId SELECT_USER_TO_INVITE_TO_YOUR_CLAN;
	
	@ClientString(id = 187, message = "Select user to expel.")
	public static SystemMessageId SELECT_USER_TO_EXPEL;
	
	@ClientString(id = 188, message = "Create clan name.")
	public static SystemMessageId CREATE_CLAN_NAME;
	
	@ClientString(id = 189, message = "Clan has been created.")
	public static SystemMessageId CLAN_HAS_BEEN_CREATED;
	
	@ClientString(id = 190, message = "You have failed to create a clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_CREATE_A_CLAN;
	
	@ClientString(id = 191, message = "Clan member $s1 has been expelled.")
	public static SystemMessageId CLAN_MEMBER_S1_HAS_BEEN_EXPELLED;
	
	@ClientString(id = 192, message = "You have failed to expel $s1 from the clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_EXPEL_S1_FROM_THE_CLAN;
	
	@ClientString(id = 193, message = "Clan has dispersed.")
	public static SystemMessageId CLAN_HAS_DISPERSED;
	
	@ClientString(id = 194, message = "You have failed to disperse the clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_DISPERSE_THE_CLAN;
	
	@ClientString(id = 195, message = "Entered the clan.")
	public static SystemMessageId ENTERED_THE_CLAN;
	
	@ClientString(id = 196, message = "$s1 refused to join the clan.")
	public static SystemMessageId S1_REFUSED_TO_JOIN_THE_CLAN;
	
	@ClientString(id = 197, message = "Withdrawn from the clan.")
	public static SystemMessageId WITHDRAWN_FROM_THE_CLAN;
	
	@ClientString(id = 198, message = "You have failed to withdraw from the $s1 clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_S1_CLAN;
	
	@ClientString(id = 199, message = "You have been dismissed from this clan. You are not allowed to join another clan for the next five days.")
	public static SystemMessageId YOU_HAVE_BEEN_DISMISSED_FROM_THIS_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_THE_NEXT_FIVE_DAYS;
	
	@ClientString(id = 200, message = "You have withdrawn from the party.")
	public static SystemMessageId YOU_HAVE_WITHDRAWN_FROM_THE_PARTY;
	
	@ClientString(id = 201, message = "$s1 was expelled from the party.")
	public static SystemMessageId S1_WAS_EXPELLED_FROM_THE_PARTY;
	
	@ClientString(id = 202, message = "You have been expelled from the party.")
	public static SystemMessageId YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY;
	
	@ClientString(id = 203, message = "The party has dispersed.")
	public static SystemMessageId THE_PARTY_HAS_DISPERSED;
	
	@ClientString(id = 204, message = "Incorrect name. Please try again.")
	public static SystemMessageId INCORRECT_NAME_PLEASE_TRY_AGAIN;
	
	@ClientString(id = 205, message = "Incorrect character name. Please ask the GM.")
	public static SystemMessageId INCORRECT_CHARACTER_NAME_PLEASE_ASK_THE_GM;
	
	@ClientString(id = 206, message = "Enter name of clan to declare war on.")
	public static SystemMessageId ENTER_NAME_OF_CLAN_TO_DECLARE_WAR_ON;
	
	@ClientString(id = 207, message = "$s2 of the $s1 clan requests declaration of war. Do you accept?")
	public static SystemMessageId S2_OF_THE_S1_CLAN_REQUESTS_DECLARATION_OF_WAR_DO_YOU_ACCEPT;
	
	@ClientString(id = 208, message = "Please include file type when entering file path.")
	public static SystemMessageId PLEASE_INCLUDE_FILE_TYPE_WHEN_ENTERING_FILE_PATH;
	
	@ClientString(id = 209, message = "The size of the image file is different. Please adjust to 16*12.")
	public static SystemMessageId THE_SIZE_OF_THE_IMAGE_FILE_IS_DIFFERENT_PLEASE_ADJUST_TO_16_12;
	
	@ClientString(id = 210, message = "Cannot find file. Please enter precise path.")
	public static SystemMessageId CANNOT_FIND_FILE_PLEASE_ENTER_PRECISE_PATH;
	
	@ClientString(id = 211, message = "Can only register 16*12 sized bmp files of 256 colors.")
	public static SystemMessageId CAN_ONLY_REGISTER_16_12_SIZED_BMP_FILES_OF_256_COLORS;
	
	@ClientString(id = 212, message = "You are not a clan member.")
	public static SystemMessageId YOU_ARE_NOT_A_CLAN_MEMBER;
	
	@ClientString(id = 213, message = "Not working. Please try again later.")
	public static SystemMessageId NOT_WORKING_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 214, message = "Title has changed.")
	public static SystemMessageId TITLE_HAS_CHANGED;
	
	@ClientString(id = 215, message = "War with the $s1 clan has begun.")
	public static SystemMessageId WAR_WITH_THE_S1_CLAN_HAS_BEGUN;
	
	@ClientString(id = 216, message = "War with the $s1 clan has ended.")
	public static SystemMessageId WAR_WITH_THE_S1_CLAN_HAS_ENDED;
	
	@ClientString(id = 217, message = "You have won the war over the $s1 clan!")
	public static SystemMessageId YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN;
	
	@ClientString(id = 218, message = "You have surrendered to the $s1 clan.")
	public static SystemMessageId YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN;
	
	@ClientString(id = 219, message = "Your clan leader has died.You have been defeated by the $s1 Clan.")
	public static SystemMessageId YOUR_CLAN_LEADER_HAS_DIED_YOU_HAVE_BEEN_DEFEATED_BY_THE_S1_CLAN;
	
	@ClientString(id = 220, message = "You have $s1 minutes left until the clan war ends.")
	public static SystemMessageId YOU_HAVE_S1_MINUTES_LEFT_UNTIL_THE_CLAN_WAR_ENDS;
	
	@ClientString(id = 221, message = "The time limit for the clan war is up.War with the $s1 clan is over.")
	public static SystemMessageId THE_TIME_LIMIT_FOR_THE_CLAN_WAR_IS_UP_WAR_WITH_THE_S1_CLAN_IS_OVER;
	
	@ClientString(id = 222, message = "$s1 has joined the clan.")
	public static SystemMessageId S1_HAS_JOINED_THE_CLAN;
	
	@ClientString(id = 223, message = "$s1 has withdrawn from the clan.")
	public static SystemMessageId S1_HAS_WITHDRAWN_FROM_THE_CLAN;
	
	@ClientString(id = 224, message = "$s1 did not respond: Invitation to the clan has been cancelled.")
	public static SystemMessageId S1_DID_NOT_RESPOND_INVITATION_TO_THE_CLAN_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 225, message = "You didn't respond to $s1's invitation: joining has been cancelled.")
	public static SystemMessageId YOU_DIDN_T_RESPOND_TO_S1_S_INVITATION_JOINING_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 226, message = "The $s1 clan did not respond: war proclamation has been refused.")
	public static SystemMessageId THE_S1_CLAN_DID_NOT_RESPOND_WAR_PROCLAMATION_HAS_BEEN_REFUSED;
	
	@ClientString(id = 227, message = "Clan war has been refused because you did not respond to $s1 clan's war proclamation.")
	public static SystemMessageId CLAN_WAR_HAS_BEEN_REFUSED_BECAUSE_YOU_DID_NOT_RESPOND_TO_S1_CLAN_S_WAR_PROCLAMATION;
	
	@ClientString(id = 228, message = "Request to end war has been denied.")
	public static SystemMessageId REQUEST_TO_END_WAR_HAS_BEEN_DENIED;
	
	@ClientString(id = 229, message = "You are not qualified to create a clan.")
	public static SystemMessageId YOU_ARE_NOT_QUALIFIED_TO_CREATE_A_CLAN;
	
	@ClientString(id = 230, message = "You cannot create a new clan within 10 days of dispersion.")
	public static SystemMessageId YOU_CANNOT_CREATE_A_NEW_CLAN_WITHIN_10_DAYS_OF_DISPERSION;
	
	@ClientString(id = 231, message = "A new member cannot join within 5 days of a clan member's expulsion.")
	public static SystemMessageId A_NEW_MEMBER_CANNOT_JOIN_WITHIN_5_DAYS_OF_A_CLAN_MEMBER_S_EXPULSION;
	
	@ClientString(id = 232, message = "You cannot join a clan within 5 days of expulsion or withdrawal.")
	public static SystemMessageId YOU_CANNOT_JOIN_A_CLAN_WITHIN_5_DAYS_OF_EXPULSION_OR_WITHDRAWAL;
	
	@ClientString(id = 233, message = "The clan is full and cannot accept new members.")
	public static SystemMessageId THE_CLAN_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS;
	
	@ClientString(id = 234, message = "The target must be a clan member.")
	public static SystemMessageId THE_TARGET_MUST_BE_A_CLAN_MEMBER;
	
	@ClientString(id = 235, message = "You cannot transfer your rights.")
	public static SystemMessageId YOU_CANNOT_TRANSFER_YOUR_RIGHTS;
	
	@ClientString(id = 236, message = "Only the clan leader is enabled.")
	public static SystemMessageId ONLY_THE_CLAN_LEADER_IS_ENABLED;
	
	@ClientString(id = 237, message = "Cannot find clan leader.")
	public static SystemMessageId CANNOT_FIND_CLAN_LEADER;
	
	@ClientString(id = 238, message = "Not joined in any clan.")
	public static SystemMessageId NOT_JOINED_IN_ANY_CLAN;
	
	@ClientString(id = 239, message = "The clan leader cannot withdraw.")
	public static SystemMessageId THE_CLAN_LEADER_CANNOT_WITHDRAW;
	
	@ClientString(id = 240, message = "Currently involved in clan war.")
	public static SystemMessageId CURRENTLY_INVOLVED_IN_CLAN_WAR;
	
	@ClientString(id = 241, message = "Leader of the $s1 Clan is not logged in.")
	public static SystemMessageId LEADER_OF_THE_S1_CLAN_IS_NOT_LOGGED_IN;
	
	@ClientString(id = 242, message = "Select target.")
	public static SystemMessageId SELECT_TARGET;
	
	@ClientString(id = 243, message = "Cannot proclaim war on allied clans.")
	public static SystemMessageId CANNOT_PROCLAIM_WAR_ON_ALLIED_CLANS;
	
	@ClientString(id = 244, message = "Unqualified to request declaration of clan war.")
	public static SystemMessageId UNQUALIFIED_TO_REQUEST_DECLARATION_OF_CLAN_WAR;
	
	@ClientString(id = 245, message = "5 days has not passed since you were refused war. Do you want to continue?")
	public static SystemMessageId FIVE_DAYS_HAS_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 246, message = "The other clan is currently at war.")
	public static SystemMessageId THE_OTHER_CLAN_IS_CURRENTLY_AT_WAR;
	
	@ClientString(id = 247, message = "You have already been at war with the $s1 clan: 5 days must pass before you can proclaim war again.")
	public static SystemMessageId YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_PROCLAIM_WAR_AGAIN;
	
	@ClientString(id = 248, message = "You cannot proclaim war: the $s1 clan does not have enough members.")
	public static SystemMessageId YOU_CANNOT_PROCLAIM_WAR_THE_S1_CLAN_DOES_NOT_HAVE_ENOUGH_MEMBERS;
	
	@ClientString(id = 249, message = "Do you wish to surrender to the $s1 clan?")
	public static SystemMessageId DO_YOU_WISH_TO_SURRENDER_TO_THE_S1_CLAN;
	
	@ClientString(id = 250, message = "You have personally surrendered to the $s1 clan. You are leaving the clan war.")
	public static SystemMessageId YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN_YOU_ARE_LEAVING_THE_CLAN_WAR;
	
	@ClientString(id = 251, message = "You cannot proclaim war: you are at war with another clan.")
	public static SystemMessageId YOU_CANNOT_PROCLAIM_WAR_YOU_ARE_AT_WAR_WITH_ANOTHER_CLAN;
	
	@ClientString(id = 252, message = "Enter the name of clan to surrender to.")
	public static SystemMessageId ENTER_THE_NAME_OF_CLAN_TO_SURRENDER_TO;
	
	@ClientString(id = 253, message = "Enter the name of clan to request end of war.")
	public static SystemMessageId ENTER_THE_NAME_OF_CLAN_TO_REQUEST_END_OF_WAR;
	
	@ClientString(id = 254, message = "Clan leader cannot surrender personally.")
	public static SystemMessageId CLAN_LEADER_CANNOT_SURRENDER_PERSONALLY;
	
	@ClientString(id = 255, message = "The $s1 Clan has requested to end war. Do you agree?")
	public static SystemMessageId THE_S1_CLAN_HAS_REQUESTED_TO_END_WAR_DO_YOU_AGREE;
	
	@ClientString(id = 256, message = "Enter name.")
	public static SystemMessageId ENTER_NAME;
	
	@ClientString(id = 257, message = "Do you propose to the $s1 clan to end the war?")
	public static SystemMessageId DO_YOU_PROPOSE_TO_THE_S1_CLAN_TO_END_THE_WAR;
	
	@ClientString(id = 258, message = "Not involved in clan war.")
	public static SystemMessageId NOT_INVOLVED_IN_CLAN_WAR;
	
	@ClientString(id = 259, message = "Select clan members from list.")
	public static SystemMessageId SELECT_CLAN_MEMBERS_FROM_LIST;
	
	@ClientString(id = 260, message = "Fame level has decreased: 5 days have not passed since you were refused war.")
	public static SystemMessageId FAME_LEVEL_HAS_DECREASED_5_DAYS_HAVE_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR;
	
	@ClientString(id = 261, message = "Clan name is incorrect.")
	public static SystemMessageId CLAN_NAME_IS_INCORRECT;
	
	@ClientString(id = 262, message = "Clan name's length is incorrect.")
	public static SystemMessageId CLAN_NAME_S_LENGTH_IS_INCORRECT;
	
	@ClientString(id = 263, message = "Dispersion has already been requested.")
	public static SystemMessageId DISPERSION_HAS_ALREADY_BEEN_REQUESTED;
	
	@ClientString(id = 264, message = "Cannot disperse clan in the middle of war.")
	public static SystemMessageId CANNOT_DISPERSE_CLAN_IN_THE_MIDDLE_OF_WAR;
	
	@ClientString(id = 265, message = "Cannot disperse clan during siege or while protecting castle.")
	public static SystemMessageId CANNOT_DISPERSE_CLAN_DURING_SIEGE_OR_WHILE_PROTECTING_CASTLE;
	
	@ClientString(id = 266, message = "A clan that owns a clan hall or castle cannot disperse.")
	public static SystemMessageId A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_CASTLE_CANNOT_DISPERSE;
	
	@ClientString(id = 267, message = "No requests for dispersion.")
	public static SystemMessageId NO_REQUESTS_FOR_DISPERSION;
	
	@ClientString(id = 268, message = "Player already belongs to a clan.")
	public static SystemMessageId PLAYER_ALREADY_BELONGS_TO_A_CLAN;
	
	@ClientString(id = 269, message = "You cannot expel yourself.")
	public static SystemMessageId YOU_CANNOT_EXPEL_YOURSELF;
	
	@ClientString(id = 270, message = "You have already surrendered.")
	public static SystemMessageId YOU_HAVE_ALREADY_SURRENDERED;
	
	@ClientString(id = 271, message = "A player can only be granted a title if the clan is level 3 or above.")
	public static SystemMessageId A_PLAYER_CAN_ONLY_BE_GRANTED_A_TITLE_IF_THE_CLAN_IS_LEVEL_3_OR_ABOVE;
	
	@ClientString(id = 272, message = "Clan crest registration is only possible when clan's skill levels are above 3.")
	public static SystemMessageId CLAN_CREST_REGISTRATION_IS_ONLY_POSSIBLE_WHEN_CLAN_S_SKILL_LEVELS_ARE_ABOVE_3;
	
	@ClientString(id = 273, message = "Proclamation of clan war is only possible when clan's skill levels are above 3.")
	public static SystemMessageId PROCLAMATION_OF_CLAN_WAR_IS_ONLY_POSSIBLE_WHEN_CLAN_S_SKILL_LEVELS_ARE_ABOVE_3;
	
	@ClientString(id = 274, message = "Clan's skill level has increased.")
	public static SystemMessageId CLAN_S_SKILL_LEVEL_HAS_INCREASED;
	
	@ClientString(id = 275, message = "Clan has failed to increase skill level.")
	public static SystemMessageId CLAN_HAS_FAILED_TO_INCREASE_SKILL_LEVEL;
	
	@ClientString(id = 276, message = "You do not have enough items to learn skills.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_LEARN_SKILLS;
	
	@ClientString(id = 277, message = "You have earned $s1.")
	public static SystemMessageId YOU_HAVE_EARNED_S1;
	
	@ClientString(id = 278, message = "You do not have enough SP to learn skills.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_SKILLS;
	
	@ClientString(id = 279, message = "You do not have enough adena.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_ADENA;
	
	@ClientString(id = 280, message = "You do not have any items to sell.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ANY_ITEMS_TO_SELL;
	
	@ClientString(id = 281, message = "You do not have enough custody fees.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_CUSTODY_FEES;
	
	@ClientString(id = 282, message = "There's nothing here that you've entrusted.")
	public static SystemMessageId THERE_S_NOTHING_HERE_THAT_YOU_VE_ENTRUSTED;
	
	@ClientString(id = 283, message = "You have entered a combat zone.")
	public static SystemMessageId YOU_HAVE_ENTERED_A_COMBAT_ZONE;
	
	@ClientString(id = 284, message = "You have left a combat zone.")
	public static SystemMessageId YOU_HAVE_LEFT_A_COMBAT_ZONE;
	
	@ClientString(id = 285, message = "Clan $s1 has succeeded in engraving the ruler!")
	public static SystemMessageId CLAN_S1_HAS_SUCCEEDED_IN_ENGRAVING_THE_RULER;
	
	@ClientString(id = 286, message = "Your base is being attacked.")
	public static SystemMessageId YOUR_BASE_IS_BEING_ATTACKED;
	
	@ClientString(id = 287, message = "The opponent clan has begun to engrave the ruler.")
	public static SystemMessageId THE_OPPONENT_CLAN_HAS_BEGUN_TO_ENGRAVE_THE_RULER;
	
	@ClientString(id = 288, message = "The castle gate has been broken down.")
	public static SystemMessageId THE_CASTLE_GATE_HAS_BEEN_BROKEN_DOWN;
	
	@ClientString(id = 289, message = "Since a headquarters already exists you cannot build another one.")
	public static SystemMessageId SINCE_A_HEADQUARTERS_ALREADY_EXISTS_YOU_CANNOT_BUILD_ANOTHER_ONE;
	
	@ClientString(id = 290, message = "You cannot set up a base here.")
	public static SystemMessageId YOU_CANNOT_SET_UP_A_BASE_HERE;
	
	@ClientString(id = 291, message = "Clan $s1 is victorious over $s2's castle siege!")
	public static SystemMessageId CLAN_S1_IS_VICTORIOUS_OVER_S2_S_CASTLE_SIEGE;
	
	@ClientString(id = 292, message = "$s1 has announced the castle siege time.")
	public static SystemMessageId S1_HAS_ANNOUNCED_THE_CASTLE_SIEGE_TIME;
	
	@ClientString(id = 293, message = "The registration term for $s1 has ended.")
	public static SystemMessageId THE_REGISTRATION_TERM_FOR_S1_HAS_ENDED;
	
	@ClientString(id = 294, message = "You cannot summon a base because you are not in battle.")
	public static SystemMessageId YOU_CANNOT_SUMMON_A_BASE_BECAUSE_YOU_ARE_NOT_IN_BATTLE;
	
	@ClientString(id = 295, message = "$s1's siege was canceled because there were no clans that participated.")
	public static SystemMessageId S1_S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED;
	
	@ClientString(id = 296, message = "You received $s1 damage from taking a high fall.")
	public static SystemMessageId YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL;
	
	@ClientString(id = 297, message = "You received $s1 damage because you were unable to breathe.")
	public static SystemMessageId YOU_RECEIVED_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE;
	
	@ClientString(id = 298, message = "You have dropped $s1.")
	public static SystemMessageId YOU_HAVE_DROPPED_S1;
	
	@ClientString(id = 299, message = "$s1 has obtained $s3 $s2.")
	public static SystemMessageId S1_HAS_OBTAINED_S3_S2;
	
	@ClientString(id = 300, message = "$s1 has obtained $s2.")
	public static SystemMessageId S1_HAS_OBTAINED_S2;
	
	@ClientString(id = 301, message = "$s2 $s1 has disappeared.")
	public static SystemMessageId S2_S1_HAS_DISAPPEARED;
	
	@ClientString(id = 302, message = "$s1 has disappeared.")
	public static SystemMessageId S1_HAS_DISAPPEARED;
	
	@ClientString(id = 303, message = "Select item to enchant.")
	public static SystemMessageId SELECT_ITEM_TO_ENCHANT;
	
	@ClientString(id = 304, message = "Clan member $s1 has logged into game.")
	public static SystemMessageId CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME;
	
	@ClientString(id = 305, message = "The player declined to join your party.")
	public static SystemMessageId THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY;
	
	@ClientString(id = 306, message = "You have failed to delete the character.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER;
	
	@ClientString(id = 307, message = "You have failed to trade with the warehouse.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_TRADE_WITH_THE_WAREHOUSE;
	
	@ClientString(id = 308, message = "Failed to join the clan.")
	public static SystemMessageId FAILED_TO_JOIN_THE_CLAN;
	
	@ClientString(id = 309, message = "Succeeded in expelling a clan member.")
	public static SystemMessageId SUCCEEDED_IN_EXPELLING_A_CLAN_MEMBER;
	
	@ClientString(id = 310, message = "Failed to expel a clan member.")
	public static SystemMessageId FAILED_TO_EXPEL_A_CLAN_MEMBER;
	
	@ClientString(id = 311, message = "Clan war has been accepted.")
	public static SystemMessageId CLAN_WAR_HAS_BEEN_ACCEPTED;
	
	@ClientString(id = 312, message = "Clan war has been refused.")
	public static SystemMessageId CLAN_WAR_HAS_BEEN_REFUSED;
	
	@ClientString(id = 313, message = "The cease war request has been accepted.")
	public static SystemMessageId THE_CEASE_WAR_REQUEST_HAS_BEEN_ACCEPTED;
	
	@ClientString(id = 314, message = "Failed to surrender.")
	public static SystemMessageId FAILED_TO_SURRENDER;
	
	@ClientString(id = 315, message = "Failed to personally surrender.")
	public static SystemMessageId FAILED_TO_PERSONALLY_SURRENDER;
	
	@ClientString(id = 316, message = "Failed to withdraw from the party.")
	public static SystemMessageId FAILED_TO_WITHDRAW_FROM_THE_PARTY;
	
	@ClientString(id = 317, message = "Failed to expel a party member.")
	public static SystemMessageId FAILED_TO_EXPEL_A_PARTY_MEMBER;
	
	@ClientString(id = 318, message = "Failed to disperse the party.")
	public static SystemMessageId FAILED_TO_DISPERSE_THE_PARTY;
	
	@ClientString(id = 319, message = "You are unable to unlock the door.")
	public static SystemMessageId YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR;
	
	@ClientString(id = 320, message = "You have failed to unlock the door.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR;
	
	@ClientString(id = 321, message = "It is not locked.")
	public static SystemMessageId IT_IS_NOT_LOCKED;
	
	@ClientString(id = 322, message = "Please decide on the sales price.")
	public static SystemMessageId PLEASE_DECIDE_ON_THE_SALES_PRICE;
	
	@ClientString(id = 323, message = "Your force has increased to $s1 level.")
	public static SystemMessageId YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL;
	
	@ClientString(id = 324, message = "You can no longer increase your force.")
	public static SystemMessageId YOU_CAN_NO_LONGER_INCREASE_YOUR_FORCE;
	
	@ClientString(id = 325, message = "The corpse has already disappeared.")
	public static SystemMessageId THE_CORPSE_HAS_ALREADY_DISAPPEARED;
	
	@ClientString(id = 326, message = "Select target from list.")
	public static SystemMessageId SELECT_TARGET_FROM_LIST;
	
	@ClientString(id = 327, message = "You cannot exceed 80 characters.")
	public static SystemMessageId YOU_CANNOT_EXCEED_80_CHARACTERS;
	
	@ClientString(id = 328, message = "Please input title using less than 128 characters.")
	public static SystemMessageId PLEASE_INPUT_TITLE_USING_LESS_THAN_128_CHARACTERS;
	
	@ClientString(id = 329, message = "Please input contents using less than 3000 characters.")
	public static SystemMessageId PLEASE_INPUT_CONTENTS_USING_LESS_THAN_3000_CHARACTERS;
	
	@ClientString(id = 330, message = "A one-line response may not exceed 128 characters.")
	public static SystemMessageId A_ONE_LINE_RESPONSE_MAY_NOT_EXCEED_128_CHARACTERS;
	
	@ClientString(id = 331, message = "You have acquired $s1 SP.")
	public static SystemMessageId YOU_HAVE_ACQUIRED_S1_SP;
	
	@ClientString(id = 332, message = "Do you want to be restored?")
	public static SystemMessageId DO_YOU_WANT_TO_BE_RESTORED;
	
	@ClientString(id = 333, message = "You have received $s1 damage by Core's barrier.")
	public static SystemMessageId YOU_HAVE_RECEIVED_S1_DAMAGE_BY_CORE_S_BARRIER;
	
	@ClientString(id = 334, message = "Please enter store message.")
	public static SystemMessageId PLEASE_ENTER_STORE_MESSAGE;
	
	@ClientString(id = 335, message = "$s1 is aborted.")
	public static SystemMessageId S1_IS_ABORTED;
	
	@ClientString(id = 336, message = "$s1 is crystallized. Do you want to continue?")
	public static SystemMessageId S1_IS_CRYSTALLIZED_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 337, message = "Soulshot does not match weapon grade.")
	public static SystemMessageId SOULSHOT_DOES_NOT_MATCH_WEAPON_GRADE;
	
	@ClientString(id = 338, message = "Not enough soulshots.")
	public static SystemMessageId NOT_ENOUGH_SOULSHOTS;
	
	@ClientString(id = 339, message = "Cannot use soulshots.")
	public static SystemMessageId CANNOT_USE_SOULSHOTS;
	
	@ClientString(id = 340, message = "Private store under way.")
	public static SystemMessageId PRIVATE_STORE_UNDER_WAY;
	
	@ClientString(id = 341, message = "Not enough materials.")
	public static SystemMessageId NOT_ENOUGH_MATERIALS;
	
	@ClientString(id = 342, message = "Power of the spirits enabled.")
	public static SystemMessageId POWER_OF_THE_SPIRITS_ENABLED;
	
	@ClientString(id = 343, message = "Sweeper failed, target not spoiled.")
	public static SystemMessageId SWEEPER_FAILED_TARGET_NOT_SPOILED;
	
	@ClientString(id = 344, message = "Power of the spirits disabled.")
	public static SystemMessageId POWER_OF_THE_SPIRITS_DISABLED;
	
	@ClientString(id = 345, message = "Chat enabled.")
	public static SystemMessageId CHAT_ENABLED;
	
	@ClientString(id = 346, message = "Chat disabled.")
	public static SystemMessageId CHAT_DISABLED;
	
	@ClientString(id = 347, message = "Incorrect item count.")
	public static SystemMessageId INCORRECT_ITEM_COUNT;
	
	@ClientString(id = 348, message = "Incorrect item price.")
	public static SystemMessageId INCORRECT_ITEM_PRICE;
	
	@ClientString(id = 349, message = "Private store already closed.")
	public static SystemMessageId PRIVATE_STORE_ALREADY_CLOSED;
	
	@ClientString(id = 350, message = "Item out of stock.")
	public static SystemMessageId ITEM_OUT_OF_STOCK;
	
	@ClientString(id = 351, message = "Incorrect item count.")
	public static SystemMessageId INCORRECT_ITEM_COUNT_2;
	
	@ClientString(id = 352, message = "Incorrect item.")
	public static SystemMessageId INCORRECT_ITEM;
	
	@ClientString(id = 353, message = "Cannot purchase.")
	public static SystemMessageId CANNOT_PURCHASE;
	
	@ClientString(id = 354, message = "Cancel enchant.")
	public static SystemMessageId CANCEL_ENCHANT;
	
	@ClientString(id = 355, message = "Inappropriate enchant conditions.")
	public static SystemMessageId INAPPROPRIATE_ENCHANT_CONDITIONS;
	
	@ClientString(id = 356, message = "Reject resurrection.")
	public static SystemMessageId REJECT_RESURRECTION;
	
	@ClientString(id = 357, message = "Already spoiled.")
	public static SystemMessageId ALREADY_SPOILED;
	
	@ClientString(id = 358, message = "$s1 hour(s) until castle siege conclusion.")
	public static SystemMessageId S1_HOUR_S_UNTIL_CASTLE_SIEGE_CONCLUSION;
	
	@ClientString(id = 359, message = "$s1 minute(s) until castle siege conclusion.")
	public static SystemMessageId S1_MINUTE_S_UNTIL_CASTLE_SIEGE_CONCLUSION;
	
	@ClientString(id = 360, message = "Castle siege $s1 second(s) left!")
	public static SystemMessageId CASTLE_SIEGE_S1_SECOND_S_LEFT;
	
	@ClientString(id = 361, message = "Over-hit!")
	public static SystemMessageId OVER_HIT;
	
	@ClientString(id = 362, message = "Acquired $s1 bonus experience through over-hit.")
	public static SystemMessageId ACQUIRED_S1_BONUS_EXPERIENCE_THROUGH_OVER_HIT;
	
	@ClientString(id = 363, message = "Chat available time: $s1 minute.")
	public static SystemMessageId CHAT_AVAILABLE_TIME_S1_MINUTE;
	
	@ClientString(id = 364, message = "Enter user's name to search.")
	public static SystemMessageId ENTER_USER_S_NAME_TO_SEARCH;
	
	@ClientString(id = 365, message = "Are you sure?")
	public static SystemMessageId ARE_YOU_SURE;
	
	@ClientString(id = 366, message = "Select hair color.")
	public static SystemMessageId SELECT_HAIR_COLOR;
	
	@ClientString(id = 367, message = "Cannot remove clan character.")
	public static SystemMessageId CANNOT_REMOVE_CLAN_CHARACTER;
	
	@ClientString(id = 368, message = "Equipped +$s1 $s2.")
	public static SystemMessageId EQUIPPED_S1_S2;
	
	@ClientString(id = 369, message = "You have obtained +$s1$s2.")
	public static SystemMessageId YOU_HAVE_OBTAINED_S1_S2;
	
	@ClientString(id = 370, message = "Failed to pick up $s1.")
	public static SystemMessageId FAILED_TO_PICK_UP_S1_2;
	
	@ClientString(id = 371, message = "Acquired +$s1 $s2.")
	public static SystemMessageId ACQUIRED_S1_S2;
	
	@ClientString(id = 372, message = "Failed to earn $s1.")
	public static SystemMessageId FAILED_TO_EARN_S1_2;
	
	@ClientString(id = 373, message = "Destroy +$s1 $s2. Do you wish to continue?")
	public static SystemMessageId DESTROY_S1_S2_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 374, message = "Crystallize +$s1 $s2. Do you wish to continue?")
	public static SystemMessageId CRYSTALLIZE_S1_S2_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 375, message = "Dropped +$s1 $s2.")
	public static SystemMessageId DROPPED_S1_S2;
	
	@ClientString(id = 376, message = "$s1 has obtained +$s2$s3.")
	public static SystemMessageId S1_HAS_OBTAINED_S2_S3;
	
	@ClientString(id = 377, message = "+$s1 $s2 disappeared.")
	public static SystemMessageId S1_S2_DISAPPEARED;
	
	@ClientString(id = 378, message = "$s1 purchased $s2.")
	public static SystemMessageId S1_PURCHASED_S2;
	
	@ClientString(id = 379, message = "$s1 purchased +$s2 $s3.")
	public static SystemMessageId S1_PURCHASED_S2_S3;
	
	@ClientString(id = 380, message = "$s1 purchased $s3 $s2(s).")
	public static SystemMessageId S1_PURCHASED_S3_S2_S;
	
	@ClientString(id = 381, message = "Cannot connect to petition server.")
	public static SystemMessageId CANNOT_CONNECT_TO_PETITION_SERVER;
	
	@ClientString(id = 382, message = "Currently there are no users that have checked out a GM ID.")
	public static SystemMessageId CURRENTLY_THERE_ARE_NO_USERS_THAT_HAVE_CHECKED_OUT_A_GM_ID;
	
	@ClientString(id = 383, message = "Request confirmed to end consultation at petition server.")
	public static SystemMessageId REQUEST_CONFIRMED_TO_END_CONSULTATION_AT_PETITION_SERVER;
	
	@ClientString(id = 384, message = "The client is not logged onto the game server.")
	public static SystemMessageId THE_CLIENT_IS_NOT_LOGGED_ONTO_THE_GAME_SERVER;
	
	@ClientString(id = 385, message = "Request confirmed to begin consultation at petition server.")
	public static SystemMessageId REQUEST_CONFIRMED_TO_BEGIN_CONSULTATION_AT_PETITION_SERVER;
	
	@ClientString(id = 386, message = "Petition requests must be over five characters.")
	public static SystemMessageId PETITION_REQUESTS_MUST_BE_OVER_FIVE_CHARACTERS;
	
	@ClientString(id = 387, message = "Ending petition consultation.")
	public static SystemMessageId ENDING_PETITION_CONSULTATION;
	
	@ClientString(id = 388, message = "Not under petition consultation.")
	public static SystemMessageId NOT_UNDER_PETITION_CONSULTATION;
	
	@ClientString(id = 389, message = "Petition application accepted. Receipt No. is $s1.")
	public static SystemMessageId PETITION_APPLICATION_ACCEPTED_RECEIPT_NO_IS_S1;
	
	@ClientString(id = 390, message = "Already applied for petition.")
	public static SystemMessageId ALREADY_APPLIED_FOR_PETITION;
	
	@ClientString(id = 391, message = "Receipt No. $s1, petition cancelled.")
	public static SystemMessageId RECEIPT_NO_S1_PETITION_CANCELLED;
	
	@ClientString(id = 392, message = "Under petition advice.")
	public static SystemMessageId UNDER_PETITION_ADVICE;
	
	@ClientString(id = 393, message = "Failed to cancel petition. Please try again later.")
	public static SystemMessageId FAILED_TO_CANCEL_PETITION_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 394, message = "Petition consultation with $s1, under way.")
	public static SystemMessageId PETITION_CONSULTATION_WITH_S1_UNDER_WAY;
	
	@ClientString(id = 395, message = "Ending petition consultation with $s1.")
	public static SystemMessageId ENDING_PETITION_CONSULTATION_WITH_S1;
	
	@ClientString(id = 396, message = "Please login after changing your temporary password.")
	public static SystemMessageId PLEASE_LOGIN_AFTER_CHANGING_YOUR_TEMPORARY_PASSWORD;
	
	@ClientString(id = 397, message = "Not a paid account.")
	public static SystemMessageId NOT_A_PAID_ACCOUNT;
	
	@ClientString(id = 398, message = "You have no more time left on your account.")
	public static SystemMessageId YOU_HAVE_NO_MORE_TIME_LEFT_ON_YOUR_ACCOUNT;
	
	@ClientString(id = 399, message = "System error.")
	public static SystemMessageId SYSTEM_ERROR;
	
	@ClientString(id = 400, message = "Discard $s1. Do you wish to continue?")
	public static SystemMessageId DISCARD_S1_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 401, message = "Too many quests in progress.")
	public static SystemMessageId TOO_MANY_QUESTS_IN_PROGRESS;
	
	@ClientString(id = 402, message = "You may not get on board without a pass.")
	public static SystemMessageId YOU_MAY_NOT_GET_ON_BOARD_WITHOUT_A_PASS;
	
	@ClientString(id = 403, message = "You have exceeded your pocket money limit.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_YOUR_POCKET_MONEY_LIMIT;
	
	@ClientString(id = 404, message = "Create Item level is too low to register this recipe.")
	public static SystemMessageId CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE;
	
	@ClientString(id = 405, message = "The total price of the product is too high.")
	public static SystemMessageId THE_TOTAL_PRICE_OF_THE_PRODUCT_IS_TOO_HIGH;
	
	@ClientString(id = 406, message = "Petition application accepted.")
	public static SystemMessageId PETITION_APPLICATION_ACCEPTED;
	
	@ClientString(id = 407, message = "Petition under process.")
	public static SystemMessageId PETITION_UNDER_PROCESS;
	
	@ClientString(id = 408, message = "Set Period")
	public static SystemMessageId SET_PERIOD;
	
	@ClientString(id = 409, message = "Set Time-$s1: $s2: $s3")
	public static SystemMessageId SET_TIME_S1_S2_S3;
	
	@ClientString(id = 410, message = "Registration Period")
	public static SystemMessageId REGISTRATION_PERIOD;
	
	@ClientString(id = 411, message = "Registration TIme-$s1: $s2: $s3")
	public static SystemMessageId REGISTRATION_TIME_S1_S2_S3;
	
	@ClientString(id = 412, message = "Battle begins in $s1: $s2: $s4")
	public static SystemMessageId BATTLE_BEGINS_IN_S1_S2_S4;
	
	@ClientString(id = 413, message = "Battle ends in $s1: $s2: $s5")
	public static SystemMessageId BATTLE_ENDS_IN_S1_S2_S5;
	
	@ClientString(id = 414, message = "Standby")
	public static SystemMessageId STANDBY;
	
	@ClientString(id = 415, message = "Under Siege")
	public static SystemMessageId UNDER_SIEGE;
	
	@ClientString(id = 416, message = "Cannot be exchanged.")
	public static SystemMessageId CANNOT_BE_EXCHANGED;
	
	@ClientString(id = 417, message = "$s1 has been disarmed.")
	public static SystemMessageId S1_HAS_BEEN_DISARMED;
	
	@ClientString(id = 418, message = "There is a significant difference between the item's price and its standard price. Please check again.")
	public static SystemMessageId THERE_IS_A_SIGNIFICANT_DIFFERENCE_BETWEEN_THE_ITEM_S_PRICE_AND_ITS_STANDARD_PRICE_PLEASE_CHECK_AGAIN;
	
	@ClientString(id = 419, message = "$s1 minute(s) of designated usage time left.")
	public static SystemMessageId S1_MINUTE_S_OF_DESIGNATED_USAGE_TIME_LEFT;
	
	@ClientString(id = 420, message = "Time expired.")
	public static SystemMessageId TIME_EXPIRED;
	
	@ClientString(id = 421, message = "Another person has logged in with the same account.")
	public static SystemMessageId ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT;
	
	@ClientString(id = 422, message = "You have exceeded the weight limit.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT;
	
	@ClientString(id = 423, message = "The scroll of enchant has been canceled.")
	public static SystemMessageId THE_SCROLL_OF_ENCHANT_HAS_BEEN_CANCELED;
	
	@ClientString(id = 424, message = "Does not fit strengthening conditions of the scroll.")
	public static SystemMessageId DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL;
	
	@ClientString(id = 425, message = "Your Create Item level is too low.")
	public static SystemMessageId YOUR_CREATE_ITEM_LEVEL_IS_TOO_LOW;
	
	@ClientString(id = 426, message = "Your account has been reported for intentionally not paying the cyber cafe fees.")
	public static SystemMessageId YOUR_ACCOUNT_HAS_BEEN_REPORTED_FOR_INTENTIONALLY_NOT_PAYING_THE_CYBER_CAF_FEES;
	
	@ClientString(id = 427, message = "Please contact us.")
	public static SystemMessageId PLEASE_CONTACT_US;
	
	@ClientString(id = 428, message = "In accordance with company policy, this account has been suspended due to suspicion of illegal use and/or misappropriation of another player's data. Details of the incident(s) in question have been sent to the email address on file with the company. If you are not directly involved with the reported conduct and wish to submit an appeal, visit the Lineage II official support home page (http://support.plaync.com) and go to the 'Ask a Question,' section to submit a ticket.")
	public static SystemMessageId IN_ACCORDANCE_WITH_COMPANY_POLICY_THIS_ACCOUNT_HAS_BEEN_SUSPENDED_DUE_TO_SUSPICION_OF_ILLEGAL_USE_AND_OR_MISAPPROPRIATION_OF_ANOTHER_PLAYER_S_DATA_DETAILS_OF_THE_INCIDENT_S_IN_QUESTION_HAVE_BEEN_SENT_TO_THE_EMAIL_ADDRESS_ON_FILE_WITH_THE_COMPANY_IF_YOU_ARE_NOT_DIRECTLY_INVOLVED_WITH_THE_REPORTED_CONDUCT_AND_WISH_TO_SUBMIT_AN_APPEAL_VISIT_THE_LINEAGE_II_OFFICIAL_SUPPORT_HOME_PAGE_HTTP_SUPPORT_PLAYNC_COM_AND_GO_TO_THE_ASK_A_QUESTION_SECTION_TO_SUBMIT_A_TICKET;
	
	@ClientString(id = 429, message = "?????")
	public static SystemMessageId EMPTY;
	
	@ClientString(id = 430, message = "?????")
	public static SystemMessageId EMPTY_2;
	
	@ClientString(id = 431, message = "In accordance with the company's operational, EULA and RoC, this account has been terminated due to a violation by the account holder. When a user violates the terms of the User Agreement, the company can impose a restriction on the applicable user's account {Section 14 of the End User License Agreement.} For more details please contact the company's Lineage II Customer Service Center via the support site at http://support.plaync.com.")
	public static SystemMessageId IN_ACCORDANCE_WITH_THE_COMPANY_S_OPERATIONAL_EULA_AND_ROC_THIS_ACCOUNT_HAS_BEEN_TERMINATED_DUE_TO_A_VIOLATION_BY_THE_ACCOUNT_HOLDER_WHEN_A_USER_VIOLATES_THE_TERMS_OF_THE_USER_AGREEMENT_THE_COMPANY_CAN_IMPOSE_A_RESTRICTION_ON_THE_APPLICABLE_USER_S_ACCOUNT_SECTION_14_OF_THE_END_USER_LICENSE_AGREEMENT_FOR_MORE_DETAILS_PLEASE_CONTACT_THE_COMPANY_S_LINEAGE_II_CUSTOMER_SERVICE_CENTER_VIA_THE_SUPPORT_SITE_AT_HTTP_SUPPORT_PLAYNC_COM;
	
	@ClientString(id = 432, message = "?????")
	public static SystemMessageId EMPTY_3;
	
	@ClientString(id = 433, message = "?????")
	public static SystemMessageId EMPTY_4;
	
	@ClientString(id = 434, message = "?????")
	public static SystemMessageId EMPTY_5;
	
	@ClientString(id = 435, message = "?????")
	public static SystemMessageId EMPTY_6;
	
	@ClientString(id = 436, message = "?????")
	public static SystemMessageId EMPTY_7;
	
	@ClientString(id = 437, message = "?????")
	public static SystemMessageId EMPTY_8;
	
	@ClientString(id = 438, message = "?????")
	public static SystemMessageId EMPTY_9;
	
	@ClientString(id = 439, message = "In accordance with the company's User Agreement and Operational Policy this account has been suspended at the account holder's request. If you have any questions regarding your account please contact support at http://support.plaync.com")
	public static SystemMessageId IN_ACCORDANCE_WITH_THE_COMPANY_S_USER_AGREEMENT_AND_OPERATIONAL_POLICY_THIS_ACCOUNT_HAS_BEEN_SUSPENDED_AT_THE_ACCOUNT_HOLDER_S_REQUEST_IF_YOU_HAVE_ANY_QUESTIONS_REGARDING_YOUR_ACCOUNT_PLEASE_CONTACT_SUPPORT_AT_HTTP_SUPPORT_PLAYNC_COM;
	
	@ClientString(id = 440, message = "?????")
	public static SystemMessageId EMPTY_10;
	
	@ClientString(id = 441, message = "Since the holder of this account has committed account theft, per our company's User Agreement, the use of this account has been suspended. If you have any questions regarding your account please contact support at http://support.plaync.com.")
	public static SystemMessageId SINCE_THE_HOLDER_OF_THIS_ACCOUNT_HAS_COMMITTED_ACCOUNT_THEFT_PER_OUR_COMPANY_S_USER_AGREEMENT_THE_USE_OF_THIS_ACCOUNT_HAS_BEEN_SUSPENDED_IF_YOU_HAVE_ANY_QUESTIONS_REGARDING_YOUR_ACCOUNT_PLEASE_CONTACT_SUPPORT_AT_HTTP_SUPPORT_PLAYNC_COM;
	
	@ClientString(id = 442, message = "?????")
	public static SystemMessageId EMPTY_11;
	
	@ClientString(id = 443, message = "The identity of the holder of this account has not been verified. Therefore, Lineage II service has been suspended for this account. To verify identity please contact support at http://support.plaync.com")
	public static SystemMessageId THE_IDENTITY_OF_THE_HOLDER_OF_THIS_ACCOUNT_HAS_NOT_BEEN_VERIFIED_THEREFORE_LINEAGE_II_SERVICE_HAS_BEEN_SUSPENDED_FOR_THIS_ACCOUNT_TO_VERIFY_IDENTITY_PLEASE_CONTACT_SUPPORT_AT_HTTP_SUPPORT_PLAYNC_COM;
	
	@ClientString(id = 444, message = "Since we have received a withdrawal request from the holder of this account, in order to process the withdrawal, access to all applicable accounts has been automatically suspended.")
	public static SystemMessageId SINCE_WE_HAVE_RECEIVED_A_WITHDRAWAL_REQUEST_FROM_THE_HOLDER_OF_THIS_ACCOUNT_IN_ORDER_TO_PROCESS_THE_WITHDRAWAL_ACCESS_TO_ALL_APPLICABLE_ACCOUNTS_HAS_BEEN_AUTOMATICALLY_SUSPENDED;
	
	@ClientString(id = 445, message = "(Reference Number Regarding Membership Withdrawal Request: $s1)")
	public static SystemMessageId REFERENCE_NUMBER_REGARDING_MEMBERSHIP_WITHDRAWAL_REQUEST_S1;
	
	@ClientString(id = 446, message = "For more details, please contact our customer service center at http://support.plaync.com")
	public static SystemMessageId FOR_MORE_DETAILS_PLEASE_CONTACT_OUR_CUSTOMER_SERVICE_CENTER_AT_HTTP_SUPPORT_PLAYNC_COM;
	
	@ClientString(id = 447, message = ".")
	public static SystemMessageId EMPTY_12;
	
	@ClientString(id = 448, message = "System error, please log in again later.")
	public static SystemMessageId SYSTEM_ERROR_PLEASE_LOG_IN_AGAIN_LATER;
	
	@ClientString(id = 449, message = "Password does not match this account.")
	public static SystemMessageId PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT;
	
	@ClientString(id = 450, message = "Confirm your account information and log in again later.")
	public static SystemMessageId CONFIRM_YOUR_ACCOUNT_INFORMATION_AND_LOG_IN_AGAIN_LATER;
	
	@ClientString(id = 451, message = "Password does not match this account.")
	public static SystemMessageId PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT_2;
	
	@ClientString(id = 452, message = "Please confirm your account information and try logging in again.")
	public static SystemMessageId PLEASE_CONFIRM_YOUR_ACCOUNT_INFORMATION_AND_TRY_LOGGING_IN_AGAIN;
	
	@ClientString(id = 453, message = "Your account information is incorrect.")
	public static SystemMessageId YOUR_ACCOUNT_INFORMATION_IS_INCORRECT;
	
	@ClientString(id = 454, message = "For more details, please contact our customer service center at http://support.plaync.com")
	public static SystemMessageId FOR_MORE_DETAILS_PLEASE_CONTACT_OUR_CUSTOMER_SERVICE_CENTER_AT_HTTP_SUPPORT_PLAYNC_COM_2;
	
	@ClientString(id = 455, message = "The account is already in use. Access denied.")
	public static SystemMessageId THE_ACCOUNT_IS_ALREADY_IN_USE_ACCESS_DENIED;
	
	@ClientString(id = 456, message = "Lineage II game services may be used by individuals 15 years of age or older except for PvP servers, which may only be used by adults 18 years of age and older. (Korea Only)")
	public static SystemMessageId LINEAGE_II_GAME_SERVICES_MAY_BE_USED_BY_INDIVIDUALS_15_YEARS_OF_AGE_OR_OLDER_EXCEPT_FOR_PVP_SERVERS_WHICH_MAY_ONLY_BE_USED_BY_ADULTS_18_YEARS_OF_AGE_AND_OLDER_KOREA_ONLY;
	
	@ClientString(id = 457, message = "Server under maintenance. Please try again later.")
	public static SystemMessageId SERVER_UNDER_MAINTENANCE_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 458, message = "Your usage term has expired.")
	public static SystemMessageId YOUR_USAGE_TERM_HAS_EXPIRED;
	
	@ClientString(id = 459, message = "Please visit the official Lineage II website at http://www.lineage2.com")
	public static SystemMessageId PLEASE_VISIT_THE_OFFICIAL_LINEAGE_II_WEBSITE_AT_HTTP_WWW_LINEAGE2_COM;
	
	@ClientString(id = 460, message = "to reactivate your account.")
	public static SystemMessageId TO_REACTIVATE_YOUR_ACCOUNT;
	
	@ClientString(id = 461, message = "Access failed.")
	public static SystemMessageId ACCESS_FAILED;
	
	@ClientString(id = 462, message = "Please try again later.")
	public static SystemMessageId PLEASE_TRY_AGAIN_LATER_2;
	
	@ClientString(id = 463, message = ".")
	public static SystemMessageId EMPTY_13;
	
	@ClientString(id = 464, message = "Feature available to alliance leaders only.")
	public static SystemMessageId FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY;
	
	@ClientString(id = 465, message = "No current alliances.")
	public static SystemMessageId NO_CURRENT_ALLIANCES;
	
	@ClientString(id = 466, message = "You have exceeded the limit.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_LIMIT;
	
	@ClientString(id = 467, message = "You may not accept any clan within a day after expelling another clan.")
	public static SystemMessageId YOU_MAY_NOT_ACCEPT_ANY_CLAN_WITHIN_A_DAY_AFTER_EXPELLING_ANOTHER_CLAN;
	
	@ClientString(id = 468, message = "A clan that has withdrawn or been expelled cannot enter into an alliance within one day of withdrawal or expulsion.")
	public static SystemMessageId A_CLAN_THAT_HAS_WITHDRAWN_OR_BEEN_EXPELLED_CANNOT_ENTER_INTO_AN_ALLIANCE_WITHIN_ONE_DAY_OF_WITHDRAWAL_OR_EXPULSION;
	
	@ClientString(id = 469, message = "You may not ally with a clan you are at battle with.")
	public static SystemMessageId YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_AT_BATTLE_WITH;
	
	@ClientString(id = 470, message = "Only the clan leader may apply for withdrawal from the alliance.")
	public static SystemMessageId ONLY_THE_CLAN_LEADER_MAY_APPLY_FOR_WITHDRAWAL_FROM_THE_ALLIANCE;
	
	@ClientString(id = 471, message = "Alliance leaders cannot withdraw.")
	public static SystemMessageId ALLIANCE_LEADERS_CANNOT_WITHDRAW;
	
	@ClientString(id = 472, message = "You cannot expel yourself from the clan.")
	public static SystemMessageId YOU_CANNOT_EXPEL_YOURSELF_FROM_THE_CLAN;
	
	@ClientString(id = 473, message = "Different alliance.")
	public static SystemMessageId DIFFERENT_ALLIANCE;
	
	@ClientString(id = 474, message = "The following clan does not exist.")
	public static SystemMessageId THE_FOLLOWING_CLAN_DOES_NOT_EXIST;
	
	@ClientString(id = 475, message = "Different alliance.")
	public static SystemMessageId DIFFERENT_ALLIANCE_2;
	
	@ClientString(id = 476, message = "Incorrect image size. Please adjust to 8x12.")
	public static SystemMessageId INCORRECT_IMAGE_SIZE_PLEASE_ADJUST_TO_8X12;
	
	@ClientString(id = 477, message = "No response. Invitation to join an alliance has been cancelled.")
	public static SystemMessageId NO_RESPONSE_INVITATION_TO_JOIN_AN_ALLIANCE_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 478, message = "No response. Your entrance to the alliance has been cancelled.")
	public static SystemMessageId NO_RESPONSE_YOUR_ENTRANCE_TO_THE_ALLIANCE_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 479, message = "$s1 has joined as a friend.")
	public static SystemMessageId S1_HAS_JOINED_AS_A_FRIEND;
	
	@ClientString(id = 480, message = "Please check your friends list.")
	public static SystemMessageId PLEASE_CHECK_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 481, message = "$s1 has been deleted from your friends list.")
	public static SystemMessageId S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 482, message = "You cannot add yourself to your own friend list.")
	public static SystemMessageId YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST_2;
	
	@ClientString(id = 483, message = "Friend list is not ready yet. Please try again later.")
	public static SystemMessageId FRIEND_LIST_IS_NOT_READY_YET_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 484, message = "Already registered on the friends list.")
	public static SystemMessageId ALREADY_REGISTERED_ON_THE_FRIENDS_LIST;
	
	@ClientString(id = 485, message = "No new friend invitations from other users.")
	public static SystemMessageId NO_NEW_FRIEND_INVITATIONS_FROM_OTHER_USERS;
	
	@ClientString(id = 486, message = "The following user is not in your friends list.")
	public static SystemMessageId THE_FOLLOWING_USER_IS_NOT_IN_YOUR_FRIENDS_LIST;
	
	@ClientString(id = 487, message = "======<Friends List>======")
	public static SystemMessageId FRIENDS_LIST;
	
	@ClientString(id = 488, message = "$s1 (Currently: Online)")
	public static SystemMessageId S1_CURRENTLY_ONLINE;
	
	@ClientString(id = 489, message = "$s1 (Currently: Offline)")
	public static SystemMessageId S1_CURRENTLY_OFFLINE;
	
	@ClientString(id = 490, message = "========================")
	public static SystemMessageId EMPTY_14;
	
	@ClientString(id = 491, message = "=======<Alliance Information>=======")
	public static SystemMessageId ALLIANCE_INFORMATION;
	
	@ClientString(id = 492, message = "Alliance Name: $s1")
	public static SystemMessageId ALLIANCE_NAME_S1;
	
	@ClientString(id = 493, message = "Connection: $s1 / Total $s2")
	public static SystemMessageId CONNECTION_S1_TOTAL_S2;
	
	@ClientString(id = 494, message = "Alliance Leader: $s2 of $s1")
	public static SystemMessageId ALLIANCE_LEADER_S2_OF_S1;
	
	@ClientString(id = 495, message = "Affiliated clans: Total $s1 clan(s)")
	public static SystemMessageId AFFILIATED_CLANS_TOTAL_S1_CLAN_S;
	
	@ClientString(id = 496, message = "=====<Clan Information>=====")
	public static SystemMessageId CLAN_INFORMATION;
	
	@ClientString(id = 497, message = "Clan Name: $s1")
	public static SystemMessageId CLAN_NAME_S1;
	
	@ClientString(id = 498, message = "Clan Leader: $s1")
	public static SystemMessageId CLAN_LEADER_S1;
	
	@ClientString(id = 499, message = "Clan Level: $s1")
	public static SystemMessageId CLAN_LEVEL_S1;
	
	@ClientString(id = 500, message = "------------------------")
	public static SystemMessageId EMPTY_15;
	
	@ClientString(id = 501, message = "========================")
	public static SystemMessageId EMPTY_16;
	
	@ClientString(id = 502, message = "You have already joined an alliance.")
	public static SystemMessageId YOU_HAVE_ALREADY_JOINED_AN_ALLIANCE;
	
	@ClientString(id = 503, message = "$s1 (Friend) has logged in.")
	public static SystemMessageId S1_FRIEND_HAS_LOGGED_IN;
	
	@ClientString(id = 504, message = "Only clan leaders may create alliances.")
	public static SystemMessageId ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES;
	
	@ClientString(id = 505, message = "You cannot create a new alliance within 10 days after dissolution.")
	public static SystemMessageId YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_10_DAYS_AFTER_DISSOLUTION;
	
	@ClientString(id = 506, message = "Incorrect alliance name.")
	public static SystemMessageId INCORRECT_ALLIANCE_NAME;
	
	@ClientString(id = 507, message = "Incorrect length for an alliance name.")
	public static SystemMessageId INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME;
	
	@ClientString(id = 508, message = "The following alliance already exists.")
	public static SystemMessageId THE_FOLLOWING_ALLIANCE_ALREADY_EXISTS;
	
	@ClientString(id = 509, message = "Cannot accept. clan ally is registered as an enemy during siege battle.")
	public static SystemMessageId CANNOT_ACCEPT_CLAN_ALLY_IS_REGISTERED_AS_AN_ENEMY_DURING_SIEGE_BATTLE;
	
	@ClientString(id = 510, message = "You have invited someone to your alliance.")
	public static SystemMessageId YOU_HAVE_INVITED_SOMEONE_TO_YOUR_ALLIANCE;
	
	@ClientString(id = 511, message = "Select user to invite.")
	public static SystemMessageId SELECT_USER_TO_INVITE;
	
	@ClientString(id = 512, message = "Do you really wish to withdraw from the alliance?")
	public static SystemMessageId DO_YOU_REALLY_WISH_TO_WITHDRAW_FROM_THE_ALLIANCE;
	
	@ClientString(id = 513, message = "Enter the name of the clan you wish to expel.")
	public static SystemMessageId ENTER_THE_NAME_OF_THE_CLAN_YOU_WISH_TO_EXPEL;
	
	@ClientString(id = 514, message = "Do you really wish to dissolve the alliance?")
	public static SystemMessageId DO_YOU_REALLY_WISH_TO_DISSOLVE_THE_ALLIANCE;
	
	@ClientString(id = 515, message = "Enter file name for the alliance crest.")
	public static SystemMessageId ENTER_FILE_NAME_FOR_THE_ALLIANCE_CREST;
	
	@ClientString(id = 516, message = "$s1 has invited you as a Friend.")
	public static SystemMessageId S1_HAS_INVITED_YOU_AS_A_FRIEND;
	
	@ClientString(id = 517, message = "You have accepted the alliance.")
	public static SystemMessageId YOU_HAVE_ACCEPTED_THE_ALLIANCE;
	
	@ClientString(id = 518, message = "You have failed to invite a clan into the alliance.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE;
	
	@ClientString(id = 519, message = "You have withdrawn from the alliance.")
	public static SystemMessageId YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE;
	
	@ClientString(id = 520, message = "You have failed to withdraw from the alliance.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_ALLIANCE;
	
	@ClientString(id = 521, message = "You have succeeded in expelling a clan.")
	public static SystemMessageId YOU_HAVE_SUCCEEDED_IN_EXPELLING_A_CLAN;
	
	@ClientString(id = 522, message = "You have failed to expel a clan.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_EXPEL_A_CLAN;
	
	@ClientString(id = 523, message = "The alliance has dissolved.")
	public static SystemMessageId THE_ALLIANCE_HAS_DISSOLVED;
	
	@ClientString(id = 524, message = "You have failed to dissolve the alliance.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE;
	
	@ClientString(id = 525, message = "You have succeeded in inviting a friend.")
	public static SystemMessageId YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND;
	
	@ClientString(id = 526, message = "You have failed to invite a friend.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_INVITE_A_FRIEND;
	
	@ClientString(id = 527, message = "$s1, the leader of $s2, has requested an alliance.")
	public static SystemMessageId S1_THE_LEADER_OF_S2_HAS_REQUESTED_AN_ALLIANCE;
	
	@ClientString(id = 528, message = "File not found.")
	public static SystemMessageId FILE_NOT_FOUND;
	
	@ClientString(id = 529, message = "You may only register 8x12 *.bmp files with 256 colors.")
	public static SystemMessageId YOU_MAY_ONLY_REGISTER_8X12_BMP_FILES_WITH_256_COLORS;
	
	@ClientString(id = 530, message = "Spiritshot does not match weapon grade.")
	public static SystemMessageId SPIRITSHOT_DOES_NOT_MATCH_WEAPON_GRADE;
	
	@ClientString(id = 531, message = "Not enough Spiritshots.")
	public static SystemMessageId NOT_ENOUGH_SPIRITSHOTS;
	
	@ClientString(id = 532, message = "Cannot use Spiritshots.")
	public static SystemMessageId CANNOT_USE_SPIRITSHOTS;
	
	@ClientString(id = 533, message = "Power of Mana enabled.")
	public static SystemMessageId POWER_OF_MANA_ENABLED;
	
	@ClientString(id = 534, message = "Power of Mana disabled.")
	public static SystemMessageId POWER_OF_MANA_DISABLED;
	
	@ClientString(id = 535, message = "Name pet.")
	public static SystemMessageId NAME_PET;
	
	@ClientString(id = 536, message = "How much adena do you wish to transfer to your Inventory?")
	public static SystemMessageId HOW_MUCH_ADENA_DO_YOU_WISH_TO_TRANSFER_TO_YOUR_INVENTORY;
	
	@ClientString(id = 537, message = "How much will you transfer?")
	public static SystemMessageId HOW_MUCH_WILL_YOU_TRANSFER;
	
	@ClientString(id = 538, message = "SP has decreased by $s1.")
	public static SystemMessageId SP_HAS_DECREASED_BY_S1;
	
	@ClientString(id = 539, message = "Experience has decreased by $s1.")
	public static SystemMessageId EXPERIENCE_HAS_DECREASED_BY_S1;
	
	@ClientString(id = 540, message = "Clan leaders cannot be deleted. Dissolve the clan and try again.")
	public static SystemMessageId CLAN_LEADERS_CANNOT_BE_DELETED_DISSOLVE_THE_CLAN_AND_TRY_AGAIN;
	
	@ClientString(id = 541, message = "You cannot delete a clan member. Withdraw from the clan and try again.")
	public static SystemMessageId YOU_CANNOT_DELETE_A_CLAN_MEMBER_WITHDRAW_FROM_THE_CLAN_AND_TRY_AGAIN;
	
	@ClientString(id = 542, message = "NPC server not operating. Pets cannot be summoned.")
	public static SystemMessageId NPC_SERVER_NOT_OPERATING_PETS_CANNOT_BE_SUMMONED;
	
	@ClientString(id = 543, message = "You already have a pet.")
	public static SystemMessageId YOU_ALREADY_HAVE_A_PET;
	
	@ClientString(id = 544, message = "Item not available for pets.")
	public static SystemMessageId ITEM_NOT_AVAILABLE_FOR_PETS;
	
	@ClientString(id = 545, message = "Due to the volume limit of the pet's inventory, no more items can be placed there.")
	public static SystemMessageId DUE_TO_THE_VOLUME_LIMIT_OF_THE_PET_S_INVENTORY_NO_MORE_ITEMS_CAN_BE_PLACED_THERE;
	
	@ClientString(id = 546, message = "Exceeded pet inventory's weight limit.")
	public static SystemMessageId EXCEEDED_PET_INVENTORY_S_WEIGHT_LIMIT;
	
	@ClientString(id = 547, message = "Summon a pet.")
	public static SystemMessageId SUMMON_A_PET;
	
	@ClientString(id = 548, message = "Your pet's name can be up to 8 characters.")
	public static SystemMessageId YOUR_PET_S_NAME_CAN_BE_UP_TO_8_CHARACTERS;
	
	@ClientString(id = 549, message = "To create an alliance, your clan must be Level 5 or higher.")
	public static SystemMessageId TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER;
	
	@ClientString(id = 550, message = "You cannot create an alliance during the term of dissolution postponement.")
	public static SystemMessageId YOU_CANNOT_CREATE_AN_ALLIANCE_DURING_THE_TERM_OF_DISSOLUTION_POSTPONEMENT;
	
	@ClientString(id = 551, message = "You cannot raise your clan level during the term of dispersion postponement.")
	public static SystemMessageId YOU_CANNOT_RAISE_YOUR_CLAN_LEVEL_DURING_THE_TERM_OF_DISPERSION_POSTPONEMENT;
	
	@ClientString(id = 552, message = "During the grace period for dissolving a clan, registration or deletion of a clan's crest is not allowed.")
	public static SystemMessageId DURING_THE_GRACE_PERIOD_FOR_DISSOLVING_A_CLAN_REGISTRATION_OR_DELETION_OF_A_CLAN_S_CREST_IS_NOT_ALLOWED;
	
	@ClientString(id = 553, message = "The opposing clan has applied for dispersion.")
	public static SystemMessageId THE_OPPOSING_CLAN_HAS_APPLIED_FOR_DISPERSION;
	
	@ClientString(id = 554, message = "You cannot disperse the clans in your alliance.")
	public static SystemMessageId YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE;
	
	@ClientString(id = 555, message = "You cannot move. Your item weight is too great.")
	public static SystemMessageId YOU_CANNOT_MOVE_YOUR_ITEM_WEIGHT_IS_TOO_GREAT;
	
	@ClientString(id = 556, message = "You cannot move in this state.")
	public static SystemMessageId YOU_CANNOT_MOVE_IN_THIS_STATE;
	
	@ClientString(id = 557, message = "The pet has been summoned and cannot be deleted.")
	public static SystemMessageId THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_DELETED;
	
	@ClientString(id = 558, message = "The pet has been summoned and cannot be let go.")
	public static SystemMessageId THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_LET_GO;
	
	@ClientString(id = 559, message = "Purchased $s2 from $s1.")
	public static SystemMessageId PURCHASED_S2_FROM_S1;
	
	@ClientString(id = 560, message = "Purchased +$s2 $s3 from $s1.")
	public static SystemMessageId PURCHASED_S2_S3_FROM_S1;
	
	@ClientString(id = 561, message = "Purchased $s3 $s2(s) from $s1 .")
	public static SystemMessageId PURCHASED_S3_S2_S_FROM_S1;
	
	@ClientString(id = 562, message = "Cannot crystallize. Crystallization skill level too low.")
	public static SystemMessageId CANNOT_CRYSTALLIZE_CRYSTALLIZATION_SKILL_LEVEL_TOO_LOW;
	
	@ClientString(id = 563, message = "Failed to disable attack target.")
	public static SystemMessageId FAILED_TO_DISABLE_ATTACK_TARGET;
	
	@ClientString(id = 564, message = "Failed to change attack target.")
	public static SystemMessageId FAILED_TO_CHANGE_ATTACK_TARGET;
	
	@ClientString(id = 565, message = "Not enough luck.")
	public static SystemMessageId NOT_ENOUGH_LUCK;
	
	@ClientString(id = 566, message = "Confusion failed.")
	public static SystemMessageId CONFUSION_FAILED;
	
	@ClientString(id = 567, message = "Fear failed.")
	public static SystemMessageId FEAR_FAILED;
	
	@ClientString(id = 568, message = "Cubic Summoning failed.")
	public static SystemMessageId CUBIC_SUMMONING_FAILED;
	
	@ClientString(id = 569, message = "Caution--the item price greatly differs from the shop's standard price. Do you wish to continue?")
	public static SystemMessageId CAUTION_THE_ITEM_PRICE_GREATLY_DIFFERS_FROM_THE_SHOP_S_STANDARD_PRICE_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 570, message = "How many $s1 (s) do you wish to purchase?")
	public static SystemMessageId HOW_MANY_S1_S_DO_YOU_WISH_TO_PURCHASE;
	
	@ClientString(id = 571, message = "How many $s1 (s) do you want to purchase?")
	public static SystemMessageId HOW_MANY_S1_S_DO_YOU_WANT_TO_PURCHASE;
	
	@ClientString(id = 572, message = "Do you wish to join $s1's party? (Item distribution: Finders Keepers)")
	public static SystemMessageId DO_YOU_WISH_TO_JOIN_S1_S_PARTY_ITEM_DISTRIBUTION_FINDERS_KEEPERS;
	
	@ClientString(id = 573, message = "Do you wish to join $s1's party? (Item distribution: Random)")
	public static SystemMessageId DO_YOU_WISH_TO_JOIN_S1_S_PARTY_ITEM_DISTRIBUTION_RANDOM;
	
	@ClientString(id = 574, message = "No servitors or pets available.")
	public static SystemMessageId NO_SERVITORS_OR_PETS_AVAILABLE;
	
	@ClientString(id = 575, message = "How much adena do you wish to transfer to your pet?")
	public static SystemMessageId HOW_MUCH_ADENA_DO_YOU_WISH_TO_TRANSFER_TO_YOUR_PET;
	
	@ClientString(id = 576, message = "How much do you wish to transfer?")
	public static SystemMessageId HOW_MUCH_DO_YOU_WISH_TO_TRANSFER;
	
	@ClientString(id = 577, message = "You cannot summon during a trade or while using the private shops.")
	public static SystemMessageId YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS;
	
	@ClientString(id = 578, message = "You cannot summon during combat.")
	public static SystemMessageId YOU_CANNOT_SUMMON_DURING_COMBAT;
	
	@ClientString(id = 579, message = "A pet cannot be sent back during battle.")
	public static SystemMessageId A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE;
	
	@ClientString(id = 580, message = "You may not use multiple pets or servitors at the same time.")
	public static SystemMessageId YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME;
	
	@ClientString(id = 581, message = "There is a space in the name.")
	public static SystemMessageId THERE_IS_A_SPACE_IN_THE_NAME;
	
	@ClientString(id = 582, message = "Inappropriate character name.")
	public static SystemMessageId INAPPROPRIATE_CHARACTER_NAME;
	
	@ClientString(id = 583, message = "Name includes forbidden words.")
	public static SystemMessageId NAME_INCLUDES_FORBIDDEN_WORDS;
	
	@ClientString(id = 584, message = "Already in use by another pet.")
	public static SystemMessageId ALREADY_IN_USE_BY_ANOTHER_PET;
	
	@ClientString(id = 585, message = "Please decide on the price.")
	public static SystemMessageId PLEASE_DECIDE_ON_THE_PRICE;
	
	@ClientString(id = 586, message = "Pet items cannot be registered as shortcuts.")
	public static SystemMessageId PET_ITEMS_CANNOT_BE_REGISTERED_AS_SHORTCUTS;
	
	@ClientString(id = 587, message = "Irregular system speed.")
	public static SystemMessageId IRREGULAR_SYSTEM_SPEED;
	
	@ClientString(id = 588, message = "Pet inventory is full.")
	public static SystemMessageId PET_INVENTORY_IS_FULL;
	
	@ClientString(id = 589, message = "A dead pet cannot be sent back.")
	public static SystemMessageId A_DEAD_PET_CANNOT_BE_SENT_BACK;
	
	@ClientString(id = 590, message = "Cannot give items to a dead pet.")
	public static SystemMessageId CANNOT_GIVE_ITEMS_TO_A_DEAD_PET;
	
	@ClientString(id = 591, message = "An invalid character is included in the pet's name.")
	public static SystemMessageId AN_INVALID_CHARACTER_IS_INCLUDED_IN_THE_PET_S_NAME;
	
	@ClientString(id = 592, message = "Do you wish to dismiss your pet? Dismissing your pet will cause the pet necklace to disappear.")
	public static SystemMessageId DO_YOU_WISH_TO_DISMISS_YOUR_PET_DISMISSING_YOUR_PET_WILL_CAUSE_THE_PET_NECKLACE_TO_DISAPPEAR;
	
	@ClientString(id = 593, message = "Your pet has left due to unbearable hunger.")
	public static SystemMessageId YOUR_PET_HAS_LEFT_DUE_TO_UNBEARABLE_HUNGER;
	
	@ClientString(id = 594, message = "You cannot restore hungry pets.")
	public static SystemMessageId YOU_CANNOT_RESTORE_HUNGRY_PETS;
	
	@ClientString(id = 595, message = "Your pet is very hungry.")
	public static SystemMessageId YOUR_PET_IS_VERY_HUNGRY;
	
	@ClientString(id = 596, message = "Your pet ate a little, but is still hungry.")
	public static SystemMessageId YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY;
	
	@ClientString(id = 597, message = "Your pet is very hungry. Please be careful.")
	public static SystemMessageId YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFUL;
	
	@ClientString(id = 598, message = "You cannot chat while you are invisible.")
	public static SystemMessageId YOU_CANNOT_CHAT_WHILE_YOU_ARE_INVISIBLE;
	
	@ClientString(id = 599, message = "The GM has an important notice. Chat is temporarily aborted.")
	public static SystemMessageId THE_GM_HAS_AN_IMPORTANT_NOTICE_CHAT_IS_TEMPORARILY_ABORTED;
	
	@ClientString(id = 600, message = "You cannot equip a pet item.")
	public static SystemMessageId YOU_CANNOT_EQUIP_A_PET_ITEM;
	
	@ClientString(id = 601, message = "There are $s1 petitions pending.")
	public static SystemMessageId THERE_ARE_S1_PETITIONS_PENDING;
	
	@ClientString(id = 602, message = "The petition system is currently unavailable. Please try again later.")
	public static SystemMessageId THE_PETITION_SYSTEM_IS_CURRENTLY_UNAVAILABLE_PLEASE_TRY_AGAIN_LATER;
	
	@ClientString(id = 603, message = "That item cannot be discarded or exchanged.")
	public static SystemMessageId THAT_ITEM_CANNOT_BE_DISCARDED_OR_EXCHANGED;
	
	@ClientString(id = 604, message = "You may not call forth a pet or summoned creature from this location.")
	public static SystemMessageId YOU_MAY_NOT_CALL_FORTH_A_PET_OR_SUMMONED_CREATURE_FROM_THIS_LOCATION;
	
	@ClientString(id = 605, message = "You may register up to 64 people on your list.")
	public static SystemMessageId YOU_MAY_REGISTER_UP_TO_64_PEOPLE_ON_YOUR_LIST;
	
	@ClientString(id = 606, message = "You cannot be registered because the other person has already registered 64 people on his/her list.")
	public static SystemMessageId YOU_CANNOT_BE_REGISTERED_BECAUSE_THE_OTHER_PERSON_HAS_ALREADY_REGISTERED_64_PEOPLE_ON_HIS_HER_LIST;
	
	@ClientString(id = 607, message = "You do not have any further skills to learn. Come back when you have reached Level $s1.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1;
	
	@ClientString(id = 608, message = "$s1 has obtained $s3 $s2 by using Sweeper.")
	public static SystemMessageId S1_HAS_OBTAINED_S3_S2_BY_USING_SWEEPER;
	
	@ClientString(id = 609, message = "$s1 has obtained $s2 by using Sweeper.")
	public static SystemMessageId S1_HAS_OBTAINED_S2_BY_USING_SWEEPER;
	
	@ClientString(id = 610, message = "Your skill has been canceled due to lack of HP.")
	public static SystemMessageId YOUR_SKILL_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_HP;
	
	@ClientString(id = 611, message = "You have succeeded in Confusing the enemy.")
	public static SystemMessageId YOU_HAVE_SUCCEEDED_IN_CONFUSING_THE_ENEMY;
	
	@ClientString(id = 612, message = "The Spoil condition has been activated.")
	public static SystemMessageId THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED;
	
	@ClientString(id = 613, message = "======<Ignore List>======")
	public static SystemMessageId IGNORE_LIST;
	
	@ClientString(id = 614, message = "$s1 $s2")
	public static SystemMessageId S1_S2;
	
	@ClientString(id = 615, message = "You have failed to register the user to your Ignore List.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST;
	
	@ClientString(id = 616, message = "You have failed to delete the character.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_2;
	
	@ClientString(id = 617, message = "$s1 has been added to your Ignore List.")
	public static SystemMessageId S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST;
	
	@ClientString(id = 618, message = "$s1 has been removed from your Ignore List.")
	public static SystemMessageId S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST;
	
	@ClientString(id = 619, message = "$s1 has placed you on his/her Ignore List.")
	public static SystemMessageId S1_HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST;
	
	@ClientString(id = 620, message = "$s1 has placed you on his/her Ignore List.")
	public static SystemMessageId S1_HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST_2;
	
	@ClientString(id = 621, message = "This server is reserved for players in Korea. To use Lineage II game services, please connect to the server in your region.")
	public static SystemMessageId THIS_SERVER_IS_RESERVED_FOR_PLAYERS_IN_KOREA_TO_USE_LINEAGE_II_GAME_SERVICES_PLEASE_CONNECT_TO_THE_SERVER_IN_YOUR_REGION;
	
	@ClientString(id = 622, message = "You may not make a declaration of war during an alliance battle.")
	public static SystemMessageId YOU_MAY_NOT_MAKE_A_DECLARATION_OF_WAR_DURING_AN_ALLIANCE_BATTLE;
	
	@ClientString(id = 623, message = "Your opponent has exceeded the number of simultaneous alliance battles allowed.")
	public static SystemMessageId YOUR_OPPONENT_HAS_EXCEEDED_THE_NUMBER_OF_SIMULTANEOUS_ALLIANCE_BATTLES_ALLOWED;
	
	@ClientString(id = 624, message = "$s1 Clan leader is not currently connected to the game server.")
	public static SystemMessageId S1_CLAN_LEADER_IS_NOT_CURRENTLY_CONNECTED_TO_THE_GAME_SERVER;
	
	@ClientString(id = 625, message = "Your request for Alliance Battle truce has been denied.")
	public static SystemMessageId YOUR_REQUEST_FOR_ALLIANCE_BATTLE_TRUCE_HAS_BEEN_DENIED;
	
	@ClientString(id = 626, message = "The $s1 clan did not respond: war proclamation has been refused.")
	public static SystemMessageId THE_S1_CLAN_DID_NOT_RESPOND_WAR_PROCLAMATION_HAS_BEEN_REFUSED_2;
	
	@ClientString(id = 627, message = "Clan battle has been refused because you did not respond to $s1 clan's war proclamation.")
	public static SystemMessageId CLAN_BATTLE_HAS_BEEN_REFUSED_BECAUSE_YOU_DID_NOT_RESPOND_TO_S1_CLAN_S_WAR_PROCLAMATION;
	
	@ClientString(id = 628, message = "You have already been at war with the $s1 clan: 5 days must pass before you can declare war again.")
	public static SystemMessageId YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_DECLARE_WAR_AGAIN;
	
	@ClientString(id = 629, message = "Your opponent has exceeded the number of simultaneous alliance battles allowed.")
	public static SystemMessageId YOUR_OPPONENT_HAS_EXCEEDED_THE_NUMBER_OF_SIMULTANEOUS_ALLIANCE_BATTLES_ALLOWED_2;
	
	@ClientString(id = 630, message = "War with the $s1 clan has begun.")
	public static SystemMessageId WAR_WITH_THE_S1_CLAN_HAS_BEGUN_2;
	
	@ClientString(id = 631, message = "War with the $s1 clan is over.")
	public static SystemMessageId WAR_WITH_THE_S1_CLAN_IS_OVER;
	
	@ClientString(id = 632, message = "You have won the war over the $s1 clan!")
	public static SystemMessageId YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN_2;
	
	@ClientString(id = 633, message = "You have surrendered to the $s1 clan.")
	public static SystemMessageId YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN_2;
	
	@ClientString(id = 634, message = "Your alliance leader has been slain. You have been defeated by the $s1 clan.")
	public static SystemMessageId YOUR_ALLIANCE_LEADER_HAS_BEEN_SLAIN_YOU_HAVE_BEEN_DEFEATED_BY_THE_S1_CLAN;
	
	@ClientString(id = 635, message = "The time limit for the clan war has been exceeded. War with the $s1 clan is over.")
	public static SystemMessageId THE_TIME_LIMIT_FOR_THE_CLAN_WAR_HAS_BEEN_EXCEEDED_WAR_WITH_THE_S1_CLAN_IS_OVER;
	
	@ClientString(id = 636, message = "Not involved in clan war.")
	public static SystemMessageId NOT_INVOLVED_IN_CLAN_WAR_2;
	
	@ClientString(id = 637, message = "A clan ally has registered itself to the opponent.")
	public static SystemMessageId A_CLAN_ALLY_HAS_REGISTERED_ITSELF_TO_THE_OPPONENT;
	
	@ClientString(id = 638, message = "You have already requested a Siege Battle.")
	public static SystemMessageId YOU_HAVE_ALREADY_REQUESTED_A_SIEGE_BATTLE;
	
	@ClientString(id = 639, message = "Your application has been denied because you have already submitted a request for another Siege Battle.")
	public static SystemMessageId YOUR_APPLICATION_HAS_BEEN_DENIED_BECAUSE_YOU_HAVE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE;
	
	@ClientString(id = 640, message = "You have failed to refuse castle defense aid.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_REFUSE_CASTLE_DEFENSE_AID;
	
	@ClientString(id = 641, message = "You have failed to approve castle defense aid.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_APPROVE_CASTLE_DEFENSE_AID;
	
	@ClientString(id = 642, message = "You are already registered to the attacker side and must cancel your registration before submitting your request.")
	public static SystemMessageId YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST;
	
	@ClientString(id = 643, message = "You have already registered to the defender side and must cancel your registration before submitting your request.")
	public static SystemMessageId YOU_HAVE_ALREADY_REGISTERED_TO_THE_DEFENDER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST;
	
	@ClientString(id = 644, message = "You are not yet registered for the castle siege.")
	public static SystemMessageId YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE;
	
	@ClientString(id = 645, message = "Only clans with Level 4 and higher may register for a castle siege.")
	public static SystemMessageId ONLY_CLANS_WITH_LEVEL_4_AND_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE;
	
	@ClientString(id = 646, message = "You do not have the authority to modify the castle defender list.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST;
	
	@ClientString(id = 647, message = "You do not have the authority to modify the siege time.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME;
	
	@ClientString(id = 648, message = "No more registrations may be accepted for the attacker side.")
	public static SystemMessageId NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE;
	
	@ClientString(id = 649, message = "No more registrations may be accepted for the defender side.")
	public static SystemMessageId NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE;
	
	@ClientString(id = 650, message = "You may not summon from your current location.")
	public static SystemMessageId YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION;
	
	@ClientString(id = 651, message = "Place $s1 in the current location and direction. Do you wish to continue?")
	public static SystemMessageId PLACE_S1_IN_THE_CURRENT_LOCATION_AND_DIRECTION_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 652, message = "The target of the summoned monster is wrong.")
	public static SystemMessageId THE_TARGET_OF_THE_SUMMONED_MONSTER_IS_WRONG;
	
	@ClientString(id = 653, message = "You do not have the authority to position mercenaries.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_POSITION_MERCENARIES;
	
	@ClientString(id = 654, message = "You do not have the authority to cancel mercenary positioning.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING;
	
	@ClientString(id = 655, message = "Mercenaries cannot be positioned here.")
	public static SystemMessageId MERCENARIES_CANNOT_BE_POSITIONED_HERE;
	
	@ClientString(id = 656, message = "This mercenary cannot be positioned anymore.")
	public static SystemMessageId THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE;
	
	@ClientString(id = 657, message = "Positioning cannot be done here because the distance between mercenaries is too short.")
	public static SystemMessageId POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT;
	
	@ClientString(id = 658, message = "This is not a mercenary of a castle that you own and so you cannot cancel its positioning.")
	public static SystemMessageId THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_YOU_CANNOT_CANCEL_ITS_POSITIONING;
	
	@ClientString(id = 659, message = "This is not the time for siege registration and so registrations cannot be accepted or rejected.")
	public static SystemMessageId THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED;
	
	@ClientString(id = 660, message = "This is not the time for siege registration and so registration and cancellation cannot be done.")
	public static SystemMessageId THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE;
	
	@ClientString(id = 661, message = "It is a character that cannot be spoiled.")
	public static SystemMessageId IT_IS_A_CHARACTER_THAT_CANNOT_BE_SPOILED;
	
	@ClientString(id = 662, message = "The other player is rejecting friend invitations.")
	public static SystemMessageId THE_OTHER_PLAYER_IS_REJECTING_FRIEND_INVITATIONS;
	
	@ClientString(id = 663, message = "The siege time has been declared for $s. It is not possible to change the time after a siege time has been declared. Do you want to continue?")
	public static SystemMessageId THE_SIEGE_TIME_HAS_BEEN_DECLARED_FOR_S_IT_IS_NOT_POSSIBLE_TO_CHANGE_THE_TIME_AFTER_A_SIEGE_TIME_HAS_BEEN_DECLARED_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 664, message = "Please choose a person to receive.")
	public static SystemMessageId PLEASE_CHOOSE_A_PERSON_TO_RECEIVE;
	
	@ClientString(id = 665, message = "$s2 of $s1 alliance is applying for alliance war. Do you want to accept the challenge?")
	public static SystemMessageId S2_OF_S1_ALLIANCE_IS_APPLYING_FOR_ALLIANCE_WAR_DO_YOU_WANT_TO_ACCEPT_THE_CHALLENGE;
	
	@ClientString(id = 666, message = "A request for ceasefire has been received from $s1 alliance. Do you agree?")
	public static SystemMessageId A_REQUEST_FOR_CEASEFIRE_HAS_BEEN_RECEIVED_FROM_S1_ALLIANCE_DO_YOU_AGREE;
	
	@ClientString(id = 667, message = "You are registering on the attacking side of the $s1 siege. Do you want to continue?")
	public static SystemMessageId YOU_ARE_REGISTERING_ON_THE_ATTACKING_SIDE_OF_THE_S1_SIEGE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 668, message = "You are registering on the defending side of the $s1 siege. Do you want to continue?")
	public static SystemMessageId YOU_ARE_REGISTERING_ON_THE_DEFENDING_SIDE_OF_THE_S1_SIEGE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 669, message = "You are canceling your application to participate in the $s1 siege battle. Do you want to continue?")
	public static SystemMessageId YOU_ARE_CANCELING_YOUR_APPLICATION_TO_PARTICIPATE_IN_THE_S1_SIEGE_BATTLE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 670, message = "You are refusing the registration of $s1 clan on the defending side. Do you want to continue?")
	public static SystemMessageId YOU_ARE_REFUSING_THE_REGISTRATION_OF_S1_CLAN_ON_THE_DEFENDING_SIDE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 671, message = "You are agreeing to the registration of $s1 clan on the defending side. Do you want to continue?")
	public static SystemMessageId YOU_ARE_AGREEING_TO_THE_REGISTRATION_OF_S1_CLAN_ON_THE_DEFENDING_SIDE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 672, message = "$s1 adena disappeared.")
	public static SystemMessageId S1_ADENA_DISAPPEARED;
	
	@ClientString(id = 673, message = "Only a clan leader whose clan is of level 2 or higher is allowed to participate in a clan hall auction.")
	public static SystemMessageId ONLY_A_CLAN_LEADER_WHOSE_CLAN_IS_OF_LEVEL_2_OR_HIGHER_IS_ALLOWED_TO_PARTICIPATE_IN_A_CLAN_HALL_AUCTION;
	
	@ClientString(id = 674, message = "It has not yet been seven days since canceling an auction.")
	public static SystemMessageId IT_HAS_NOT_YET_BEEN_SEVEN_DAYS_SINCE_CANCELING_AN_AUCTION;
	
	@ClientString(id = 675, message = "There are no clan halls up for auction.")
	public static SystemMessageId THERE_ARE_NO_CLAN_HALLS_UP_FOR_AUCTION;
	
	@ClientString(id = 676, message = "Since you have already submitted a bid, you are not allowed to participate in another auction at this time.")
	public static SystemMessageId SINCE_YOU_HAVE_ALREADY_SUBMITTED_A_BID_YOU_ARE_NOT_ALLOWED_TO_PARTICIPATE_IN_ANOTHER_AUCTION_AT_THIS_TIME;
	
	@ClientString(id = 677, message = "Your bid price must be higher than the minimum price that can be bid.")
	public static SystemMessageId YOUR_BID_PRICE_MUST_BE_HIGHER_THAN_THE_MINIMUM_PRICE_THAT_CAN_BE_BID;
	
	@ClientString(id = 678, message = "You have submitted a bid in the auction of $s1.")
	public static SystemMessageId YOU_HAVE_SUBMITTED_A_BID_IN_THE_AUCTION_OF_S1;
	
	@ClientString(id = 679, message = "You have canceled your bid.")
	public static SystemMessageId YOU_HAVE_CANCELED_YOUR_BID;
	
	@ClientString(id = 680, message = "You cannot participate in an auction.")
	public static SystemMessageId YOU_CANNOT_PARTICIPATE_IN_AN_AUCTION;
	
	@ClientString(id = 681, message = "The clan does not own a clan hall.")
	public static SystemMessageId THE_CLAN_DOES_NOT_OWN_A_CLAN_HALL;
	
	@ClientString(id = 682, message = "You are moving to another village. Do you want to continue?")
	public static SystemMessageId YOU_ARE_MOVING_TO_ANOTHER_VILLAGE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 683, message = "There are no priority rights on a sweeper.")
	public static SystemMessageId THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER;
	
	@ClientString(id = 684, message = "You cannot position mercenaries during a siege.")
	public static SystemMessageId YOU_CANNOT_POSITION_MERCENARIES_DURING_A_SIEGE;
	
	@ClientString(id = 685, message = "You cannot apply for clan war with a clan that belongs to the same alliance.")
	public static SystemMessageId YOU_CANNOT_APPLY_FOR_CLAN_WAR_WITH_A_CLAN_THAT_BELONGS_TO_THE_SAME_ALLIANCE;
	
	@ClientString(id = 686, message = "You have received $s1 damage from the fire of magic.")
	public static SystemMessageId YOU_HAVE_RECEIVED_S1_DAMAGE_FROM_THE_FIRE_OF_MAGIC;
	
	@ClientString(id = 687, message = "You cannot move in a frozen state. Please wait a moment.")
	public static SystemMessageId YOU_CANNOT_MOVE_IN_A_FROZEN_STATE_PLEASE_WAIT_A_MOMENT;
	
	@ClientString(id = 688, message = "The clan that owns the castle is automatically registered on the defending side.")
	public static SystemMessageId THE_CLAN_THAT_OWNS_THE_CASTLE_IS_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE;
	
	@ClientString(id = 689, message = "A clan that owns a castle cannot participate in another siege.")
	public static SystemMessageId A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE;
	
	@ClientString(id = 690, message = "You cannot register on the attacking side because you are part of an alliance with the clan that owns the castle.")
	public static SystemMessageId YOU_CANNOT_REGISTER_ON_THE_ATTACKING_SIDE_BECAUSE_YOU_ARE_PART_OF_AN_ALLIANCE_WITH_THE_CLAN_THAT_OWNS_THE_CASTLE;
	
	@ClientString(id = 691, message = "$s1 clan is already a member of $s2 alliance.")
	public static SystemMessageId S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE;
	
	@ClientString(id = 692, message = "The other party is frozen. Please wait a moment.")
	public static SystemMessageId THE_OTHER_PARTY_IS_FROZEN_PLEASE_WAIT_A_MOMENT;
	
	@ClientString(id = 693, message = "The package that arrived is in another warehouse.")
	public static SystemMessageId THE_PACKAGE_THAT_ARRIVED_IS_IN_ANOTHER_WAREHOUSE;
	
	@ClientString(id = 694, message = "No packages have arrived.")
	public static SystemMessageId NO_PACKAGES_HAVE_ARRIVED;
	
	@ClientString(id = 695, message = "You cannot set the name of the pet.")
	public static SystemMessageId YOU_CANNOT_SET_THE_NAME_OF_THE_PET;
	
	@ClientString(id = 696, message = "Your account is restricted for not paying your PC room usage fees.")
	public static SystemMessageId YOUR_ACCOUNT_IS_RESTRICTED_FOR_NOT_PAYING_YOUR_PC_ROOM_USAGE_FEES;
	
	@ClientString(id = 697, message = "The item enchant value is strange.")
	public static SystemMessageId THE_ITEM_ENCHANT_VALUE_IS_STRANGE;
	
	@ClientString(id = 698, message = "The price is different than the same item on the sales list.")
	public static SystemMessageId THE_PRICE_IS_DIFFERENT_THAN_THE_SAME_ITEM_ON_THE_SALES_LIST;
	
	@ClientString(id = 699, message = "Currently not purchasing.")
	public static SystemMessageId CURRENTLY_NOT_PURCHASING;
	
	@ClientString(id = 700, message = "The purchase is complete.")
	public static SystemMessageId THE_PURCHASE_IS_COMPLETE;
	
	@ClientString(id = 701, message = "You do not have enough required items.")
	public static SystemMessageId YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS;
	
	@ClientString(id = 702, message = "There are not any GMs that are providing customer service currently.")
	public static SystemMessageId THERE_ARE_NOT_ANY_GMS_THAT_ARE_PROVIDING_CUSTOMER_SERVICE_CURRENTLY;
	
	@ClientString(id = 703, message = "======<GM List>======")
	public static SystemMessageId GM_LIST;
	
	@ClientString(id = 704, message = "GM : $s1")
	public static SystemMessageId GM_S1;
	
	@ClientString(id = 705, message = "You cannot exclude yourself.")
	public static SystemMessageId YOU_CANNOT_EXCLUDE_YOURSELF;
	
	@ClientString(id = 706, message = "You can only register up to 64 names on your exclude list.")
	public static SystemMessageId YOU_CAN_ONLY_REGISTER_UP_TO_64_NAMES_ON_YOUR_EXCLUDE_LIST;
	
	@ClientString(id = 707, message = "You cannot teleport to a village that is in a siege.")
	public static SystemMessageId YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE;
	
	@ClientString(id = 708, message = "You do not have the right to use the castle warehouse.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CASTLE_WAREHOUSE;
	
	@ClientString(id = 709, message = "You do not have the right to use the clan warehouse.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE;
	
	@ClientString(id = 710, message = "Only clans of clan level 1 or higher can use a clan warehouse.")
	public static SystemMessageId ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE;
	
	@ClientString(id = 711, message = "The siege of $s1 has started.")
	public static SystemMessageId THE_SIEGE_OF_S1_HAS_STARTED;
	
	@ClientString(id = 712, message = "The siege of $s1 has finished.")
	public static SystemMessageId THE_SIEGE_OF_S1_HAS_FINISHED;
	
	@ClientString(id = 713, message = "$s1/$s2/$s3 $s4:$s5")
	public static SystemMessageId S1_S2_S3_S4_S5;
	
	@ClientString(id = 714, message = "A trap device has tripped.")
	public static SystemMessageId A_TRAP_DEVICE_HAS_TRIPPED;
	
	@ClientString(id = 715, message = "The trap device has stopped.")
	public static SystemMessageId THE_TRAP_DEVICE_HAS_STOPPED;
	
	@ClientString(id = 716, message = "The base camp has been destroyed and resurrection is not possible.")
	public static SystemMessageId THE_BASE_CAMP_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE;
	
	@ClientString(id = 717, message = "The guardian tower has been destroyed and resurrection is not possible.")
	public static SystemMessageId THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE;
	
	@ClientString(id = 718, message = "The castle gates cannot be opened and closed during a siege.")
	public static SystemMessageId THE_CASTLE_GATES_CANNOT_BE_OPENED_AND_CLOSED_DURING_A_SIEGE;
	
	@ClientString(id = 719, message = "You failed at item mixing.")
	public static SystemMessageId YOU_FAILED_AT_ITEM_MIXING;
	
	@ClientString(id = 720, message = "The purchase price is higher than the amount of money that you have and so you cannot open a personal store.")
	public static SystemMessageId THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE;
	
	@ClientString(id = 721, message = "You cannot create an alliance while participating in a siege.")
	public static SystemMessageId YOU_CANNOT_CREATE_AN_ALLIANCE_WHILE_PARTICIPATING_IN_A_SIEGE;
	
	@ClientString(id = 722, message = "You cannot dissolve an alliance while an affiliated clan is participating in a siege battle.")
	public static SystemMessageId YOU_CANNOT_DISSOLVE_AN_ALLIANCE_WHILE_AN_AFFILIATED_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE;
	
	@ClientString(id = 723, message = "The opposing clan is participating in a siege battle.")
	public static SystemMessageId THE_OPPOSING_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE;
	
	@ClientString(id = 724, message = "You cannot leave while participating in a siege battle.")
	public static SystemMessageId YOU_CANNOT_LEAVE_WHILE_PARTICIPATING_IN_A_SIEGE_BATTLE;
	
	@ClientString(id = 725, message = "You cannot banish a clan from an alliance while the clan is participating in a siege.")
	public static SystemMessageId YOU_CANNOT_BANISH_A_CLAN_FROM_AN_ALLIANCE_WHILE_THE_CLAN_IS_PARTICIPATING_IN_A_SIEGE;
	
	@ClientString(id = 726, message = "The frozen condition has started. Please wait a moment.")
	public static SystemMessageId THE_FROZEN_CONDITION_HAS_STARTED_PLEASE_WAIT_A_MOMENT;
	
	@ClientString(id = 727, message = "The frozen condition was removed.")
	public static SystemMessageId THE_FROZEN_CONDITION_WAS_REMOVED;
	
	@ClientString(id = 728, message = "You cannot apply for dissolution again within seven days after a previous application for dissolution.")
	public static SystemMessageId YOU_CANNOT_APPLY_FOR_DISSOLUTION_AGAIN_WITHIN_SEVEN_DAYS_AFTER_A_PREVIOUS_APPLICATION_FOR_DISSOLUTION;
	
	@ClientString(id = 729, message = "That item cannot be discarded.")
	public static SystemMessageId THAT_ITEM_CANNOT_BE_DISCARDED;
	
	@ClientString(id = 730, message = "You have submitted $s1 petitions. You may submit $s2 more petitions today.")
	public static SystemMessageId YOU_HAVE_SUBMITTED_S1_PETITIONS_YOU_MAY_SUBMIT_S2_MORE_PETITIONS_TODAY;
	
	@ClientString(id = 731, message = "A petition has been received by the GM on behalf of $s1. It is petition #$s2.")
	public static SystemMessageId A_PETITION_HAS_BEEN_RECEIVED_BY_THE_GM_ON_BEHALF_OF_S1_IT_IS_PETITION_S2;
	
	@ClientString(id = 732, message = "$s1 has received a request for a consultation with the GM.")
	public static SystemMessageId S1_HAS_RECEIVED_A_REQUEST_FOR_A_CONSULTATION_WITH_THE_GM;
	
	@ClientString(id = 733, message = "We have received $s1 petitions from you today and that is the maximum that you can submit in one day. You cannot submit any more petitions.")
	public static SystemMessageId WE_HAVE_RECEIVED_S1_PETITIONS_FROM_YOU_TODAY_AND_THAT_IS_THE_MAXIMUM_THAT_YOU_CAN_SUBMIT_IN_ONE_DAY_YOU_CANNOT_SUBMIT_ANY_MORE_PETITIONS;
	
	@ClientString(id = 734, message = "You failed at submitting a petition on behalf of someone else. $s1 already submitted a petition.")
	public static SystemMessageId YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_SOMEONE_ELSE_S1_ALREADY_SUBMITTED_A_PETITION;
	
	@ClientString(id = 735, message = "You failed at submitting a petition on behalf of $s1. The error is #$s2.")
	public static SystemMessageId YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_S1_THE_ERROR_IS_S2;
	
	@ClientString(id = 736, message = "The petition was canceled. You may submit $s1 more petitions today.")
	public static SystemMessageId THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITIONS_TODAY;
	
	@ClientString(id = 737, message = "You failed at submitting a petition on behalf of $s1.")
	public static SystemMessageId YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_S1;
	
	@ClientString(id = 738, message = "You have not submitted a petition.")
	public static SystemMessageId YOU_HAVE_NOT_SUBMITTED_A_PETITION;
	
	@ClientString(id = 739, message = "You failed at canceling a petition on behalf of $s1. The error code is $s2.")
	public static SystemMessageId YOU_FAILED_AT_CANCELING_A_PETITION_ON_BEHALF_OF_S1_THE_ERROR_CODE_IS_S2;
	
	@ClientString(id = 740, message = "$s1 participated in a petition chat at the request of the GM.")
	public static SystemMessageId S1_PARTICIPATED_IN_A_PETITION_CHAT_AT_THE_REQUEST_OF_THE_GM;
	
	@ClientString(id = 741, message = "You failed at adding $s1 to the petition chat. A petition has already been submitted.")
	public static SystemMessageId YOU_FAILED_AT_ADDING_S1_TO_THE_PETITION_CHAT_A_PETITION_HAS_ALREADY_BEEN_SUBMITTED;
	
	@ClientString(id = 742, message = "You failed at adding $s1 to the petition chat. The error code is $s2.")
	public static SystemMessageId YOU_FAILED_AT_ADDING_S1_TO_THE_PETITION_CHAT_THE_ERROR_CODE_IS_S2;
	
	@ClientString(id = 743, message = "$s1 left the petition chat.")
	public static SystemMessageId S1_LEFT_THE_PETITION_CHAT;
	
	@ClientString(id = 744, message = "You failed at removing $s1 from the petition chat. The error code is $s2.")
	public static SystemMessageId YOU_FAILED_AT_REMOVING_S1_FROM_THE_PETITION_CHAT_THE_ERROR_CODE_IS_S2;
	
	@ClientString(id = 745, message = "You are currently not in a petition chat.")
	public static SystemMessageId YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT;
	
	@ClientString(id = 746, message = "It is not currently a petition.")
	public static SystemMessageId IT_IS_NOT_CURRENTLY_A_PETITION;
	
	@ClientString(id = 747, message = "If you need help, please use 1:1 Inquiry on the official web site.")
	public static SystemMessageId IF_YOU_NEED_HELP_PLEASE_USE_1_1_INQUIRY_ON_THE_OFFICIAL_WEB_SITE;
	
	@ClientString(id = 748, message = "The distance is too far and so the casting has been stopped.")
	public static SystemMessageId THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED;
	
	@ClientString(id = 749, message = "The effect of $s1 has been removed.")
	public static SystemMessageId THE_EFFECT_OF_S1_HAS_BEEN_REMOVED;
	
	@ClientString(id = 750, message = "There are no other skills to learn.")
	public static SystemMessageId THERE_ARE_NO_OTHER_SKILLS_TO_LEARN;
	
	@ClientString(id = 751, message = "As there is a conflict in the siege relationship with a clan in the alliance, you cannot invite that clan to the alliance.")
	public static SystemMessageId AS_THERE_IS_A_CONFLICT_IN_THE_SIEGE_RELATIONSHIP_WITH_A_CLAN_IN_THE_ALLIANCE_YOU_CANNOT_INVITE_THAT_CLAN_TO_THE_ALLIANCE;
	
	@ClientString(id = 752, message = "That name cannot be used.")
	public static SystemMessageId THAT_NAME_CANNOT_BE_USED;
	
	@ClientString(id = 753, message = "You cannot position mercenaries here.")
	public static SystemMessageId YOU_CANNOT_POSITION_MERCENARIES_HERE;
	
	@ClientString(id = 754, message = "There are $s1 hours and $s2 minutes left in this week's usage time.")
	public static SystemMessageId THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THIS_WEEK_S_USAGE_TIME;
	
	@ClientString(id = 755, message = "There are $s1 minutes left in this week's usage time.")
	public static SystemMessageId THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEK_S_USAGE_TIME;
	
	@ClientString(id = 756, message = "This week's usage time has finished.")
	public static SystemMessageId THIS_WEEK_S_USAGE_TIME_HAS_FINISHED;
	
	@ClientString(id = 757, message = "There are $s1 hours and $s2 minutes left in the fixed use time.")
	public static SystemMessageId THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THE_FIXED_USE_TIME;
	
	@ClientString(id = 758, message = "There are $s1 minutes left in this week's play time.")
	public static SystemMessageId THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEK_S_PLAY_TIME;
	
	@ClientString(id = 759, message = "There are $s1 minutes left in this week's play time.")
	public static SystemMessageId THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEK_S_PLAY_TIME_2;
	
	@ClientString(id = 760, message = "$s1 cannot join the clan because five days have not yet passed since he/she left another clan.")
	public static SystemMessageId S1_CANNOT_JOIN_THE_CLAN_BECAUSE_FIVE_DAYS_HAVE_NOT_YET_PASSED_SINCE_HE_SHE_LEFT_ANOTHER_CLAN;
	
	@ClientString(id = 761, message = "$s1 clan cannot join the alliance because one day has not yet passed since it left another alliance.")
	public static SystemMessageId S1_CLAN_CANNOT_JOIN_THE_ALLIANCE_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_IT_LEFT_ANOTHER_ALLIANCE;
	
	@ClientString(id = 762, message = "$s1 rolled $s2 and $s3's eye came out.")
	public static SystemMessageId S1_ROLLED_S2_AND_S3_S_EYE_CAME_OUT;
	
	@ClientString(id = 763, message = "You failed at sending the package because you are too far from the warehouse.")
	public static SystemMessageId YOU_FAILED_AT_SENDING_THE_PACKAGE_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_WAREHOUSE;
	
	@ClientString(id = 764, message = "You have been playing for an extended period of time. Please consider taking a break.")
	public static SystemMessageId YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK;
	
	@ClientString(id = 765, message = "GameGuard is already running. Please try running it again after rebooting.")
	public static SystemMessageId GAMEGUARD_IS_ALREADY_RUNNING_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_REBOOTING;
	
	@ClientString(id = 766, message = "There is a GameGuard initialization error. Please try running it again after rebooting.")
	public static SystemMessageId THERE_IS_A_GAMEGUARD_INITIALIZATION_ERROR_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_REBOOTING;
	
	@ClientString(id = 767, message = "The GameGuard file is damaged . Please reinstall GameGuard.")
	public static SystemMessageId THE_GAMEGUARD_FILE_IS_DAMAGED_PLEASE_REINSTALL_GAMEGUARD;
	
	@ClientString(id = 768, message = "A Windows system file is damaged. Please reinstall Internet Explorer.")
	public static SystemMessageId A_WINDOWS_SYSTEM_FILE_IS_DAMAGED_PLEASE_REINSTALL_INTERNET_EXPLORER;
	
	@ClientString(id = 769, message = "A hacking tool has been discovered. Please try playing again after closing unnecessary programs.")
	public static SystemMessageId A_HACKING_TOOL_HAS_BEEN_DISCOVERED_PLEASE_TRY_PLAYING_AGAIN_AFTER_CLOSING_UNNECESSARY_PROGRAMS;
	
	@ClientString(id = 770, message = "The GameGuard update was canceled. Please check your network connection status or firewall.")
	public static SystemMessageId THE_GAMEGUARD_UPDATE_WAS_CANCELED_PLEASE_CHECK_YOUR_NETWORK_CONNECTION_STATUS_OR_FIREWALL;
	
	@ClientString(id = 771, message = "The GameGuard update was canceled. Please try running it again after doing a virus scan or changing the settings in your PC management program.")
	public static SystemMessageId THE_GAMEGUARD_UPDATE_WAS_CANCELED_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_DOING_A_VIRUS_SCAN_OR_CHANGING_THE_SETTINGS_IN_YOUR_PC_MANAGEMENT_PROGRAM;
	
	@ClientString(id = 772, message = "There was a problem when running GameGuard.")
	public static SystemMessageId THERE_WAS_A_PROBLEM_WHEN_RUNNING_GAMEGUARD;
	
	@ClientString(id = 773, message = "The game or GameGuard files are damaged.")
	public static SystemMessageId THE_GAME_OR_GAMEGUARD_FILES_ARE_DAMAGED;
	
	@ClientString(id = 774, message = "Since this is a peace zone, play time does not get expended here.")
	public static SystemMessageId SINCE_THIS_IS_A_PEACE_ZONE_PLAY_TIME_DOES_NOT_GET_EXPENDED_HERE;
	
	@ClientString(id = 775, message = "From here on, play time will be expended.")
	public static SystemMessageId FROM_HERE_ON_PLAY_TIME_WILL_BE_EXPENDED;
	
	@ClientString(id = 776, message = "The clan hall which was put up for auction has been awarded to $s1 clan.")
	public static SystemMessageId THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN;
	
	@ClientString(id = 777, message = "The clan hall which had been put up for auction was not sold and therefore has been re-listed.")
	public static SystemMessageId THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RE_LISTED;
	
	@ClientString(id = 778, message = "You may not log out from this location.")
	public static SystemMessageId YOU_MAY_NOT_LOG_OUT_FROM_THIS_LOCATION;
	
	@ClientString(id = 779, message = "You may not restart in this location.")
	public static SystemMessageId YOU_MAY_NOT_RESTART_IN_THIS_LOCATION;
	
	@ClientString(id = 780, message = "You are only qualified to observe during a siege.")
	public static SystemMessageId YOU_ARE_ONLY_QUALIFIED_TO_OBSERVE_DURING_A_SIEGE;
	
	@ClientString(id = 781, message = "Observers cannot participate.")
	public static SystemMessageId OBSERVERS_CANNOT_PARTICIPATE;
	
	@ClientString(id = 782, message = "You may not observe a summoned creature.")
	public static SystemMessageId YOU_MAY_NOT_OBSERVE_A_SUMMONED_CREATURE;
	
	@ClientString(id = 783, message = "Lottery ticket sales have been temporarily suspended.")
	public static SystemMessageId LOTTERY_TICKET_SALES_HAVE_BEEN_TEMPORARILY_SUSPENDED;
	
	@ClientString(id = 784, message = "Tickets for the current lottery are no longer available.")
	public static SystemMessageId TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE;
	
	@ClientString(id = 785, message = "The results of lottery number $s1 have not yet been published.")
	public static SystemMessageId THE_RESULTS_OF_LOTTERY_NUMBER_S1_HAVE_NOT_YET_BEEN_PUBLISHED;
	
	@ClientString(id = 786, message = "Incorrect syntax.")
	public static SystemMessageId INCORRECT_SYNTAX;
	
	@ClientString(id = 787, message = "The tryouts are finished.")
	public static SystemMessageId THE_TRYOUTS_ARE_FINISHED;
	
	@ClientString(id = 788, message = "The finals are finished.")
	public static SystemMessageId THE_FINALS_ARE_FINISHED;
	
	@ClientString(id = 789, message = "The tryouts have begun.")
	public static SystemMessageId THE_TRYOUTS_HAVE_BEGUN;
	
	@ClientString(id = 790, message = "The finals have begun.")
	public static SystemMessageId THE_FINALS_HAVE_BEGUN;
	
	@ClientString(id = 791, message = "The final match is about to begin. Line up!")
	public static SystemMessageId THE_FINAL_MATCH_IS_ABOUT_TO_BEGIN_LINE_UP;
	
	@ClientString(id = 792, message = "The siege of the clan hall is finished.")
	public static SystemMessageId THE_SIEGE_OF_THE_CLAN_HALL_IS_FINISHED;
	
	@ClientString(id = 793, message = "The siege of the clan hall has begun.")
	public static SystemMessageId THE_SIEGE_OF_THE_CLAN_HALL_HAS_BEGUN;
	
	@ClientString(id = 794, message = "You are not authorized to do that.")
	public static SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT;
	
	@ClientString(id = 795, message = "Only clan leaders are authorized to set rights.")
	public static SystemMessageId ONLY_CLAN_LEADERS_ARE_AUTHORIZED_TO_SET_RIGHTS;
	
	@ClientString(id = 796, message = "Your remaining observation time is $s1 minutes.")
	public static SystemMessageId YOUR_REMAINING_OBSERVATION_TIME_IS_S1_MINUTES;
	
	@ClientString(id = 797, message = "You may create up to 24 macros.")
	public static SystemMessageId YOU_MAY_CREATE_UP_TO_24_MACROS;
	
	@ClientString(id = 798, message = "Item registration is irreversible. Do you wish to continue?")
	public static SystemMessageId ITEM_REGISTRATION_IS_IRREVERSIBLE_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 799, message = "The observation time has expired.")
	public static SystemMessageId THE_OBSERVATION_TIME_HAS_EXPIRED;
	
	@ClientString(id = 800, message = "You are too late. The registration period is over.")
	public static SystemMessageId YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER;
	
	@ClientString(id = 801, message = "Registration for the clan hall siege is closed.")
	public static SystemMessageId REGISTRATION_FOR_THE_CLAN_HALL_SIEGE_IS_CLOSED;
	
	@ClientString(id = 802, message = "Petitions are not being accepted at this time. You may submit your petition after $s1 a.m./p.m.")
	public static SystemMessageId PETITIONS_ARE_NOT_BEING_ACCEPTED_AT_THIS_TIME_YOU_MAY_SUBMIT_YOUR_PETITION_AFTER_S1_A_M_P_M;
	
	@ClientString(id = 803, message = "Enter the specifics of your petition.")
	public static SystemMessageId ENTER_THE_SPECIFICS_OF_YOUR_PETITION;
	
	@ClientString(id = 804, message = "Select a type.")
	public static SystemMessageId SELECT_A_TYPE;
	
	@ClientString(id = 805, message = "Petitions are not being accepted at this time. You may submit your petition after $s1 a.m./p.m.")
	public static SystemMessageId PETITIONS_ARE_NOT_BEING_ACCEPTED_AT_THIS_TIME_YOU_MAY_SUBMIT_YOUR_PETITION_AFTER_S1_A_M_P_M_2;
	
	@ClientString(id = 806, message = "If you are trapped, try typing '/unstuck'.")
	public static SystemMessageId IF_YOU_ARE_TRAPPED_TRY_TYPING_UNSTUCK;
	
	@ClientString(id = 807, message = "This terrain is unnavigable. Prepare for transport to the nearest village.")
	public static SystemMessageId THIS_TERRAIN_IS_UNNAVIGABLE_PREPARE_FOR_TRANSPORT_TO_THE_NEAREST_VILLAGE;
	
	@ClientString(id = 808, message = "You are stuck. You may submit a petition by typing '/gm'.")
	public static SystemMessageId YOU_ARE_STUCK_YOU_MAY_SUBMIT_A_PETITION_BY_TYPING_GM;
	
	@ClientString(id = 809, message = "You are stuck. You will be transported to the nearest village in five minutes.")
	public static SystemMessageId YOU_ARE_STUCK_YOU_WILL_BE_TRANSPORTED_TO_THE_NEAREST_VILLAGE_IN_FIVE_MINUTES;
	
	@ClientString(id = 810, message = "Invalid macro. Refer to the Help file for instructions.")
	public static SystemMessageId INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS;
	
	@ClientString(id = 811, message = "You will be moved to ($s1). Do you wish to continue?")
	public static SystemMessageId YOU_WILL_BE_MOVED_TO_S1_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 812, message = "The secret trap has inflicted $s1 damage on you.")
	public static SystemMessageId THE_SECRET_TRAP_HAS_INFLICTED_S1_DAMAGE_ON_YOU;
	
	@ClientString(id = 813, message = "You have been poisoned by a Secret Trap.")
	public static SystemMessageId YOU_HAVE_BEEN_POISONED_BY_A_SECRET_TRAP;
	
	@ClientString(id = 814, message = "Your speed has been decreased by a Secret Trap.")
	public static SystemMessageId YOUR_SPEED_HAS_BEEN_DECREASED_BY_A_SECRET_TRAP;
	
	@ClientString(id = 815, message = "The tryouts are about to begin. Line up!")
	public static SystemMessageId THE_TRYOUTS_ARE_ABOUT_TO_BEGIN_LINE_UP;
	
	@ClientString(id = 816, message = "Tickets are now available for the $s1th Monster Race.")
	public static SystemMessageId TICKETS_ARE_NOW_AVAILABLE_FOR_THE_S1TH_MONSTER_RACE;
	
	@ClientString(id = 817, message = "We are now selling tickets for the $s1th Monster Race.")
	public static SystemMessageId WE_ARE_NOW_SELLING_TICKETS_FOR_THE_S1TH_MONSTER_RACE;
	
	@ClientString(id = 818, message = "Ticket sales for the Monster Race will cease in $s1 minute(s).")
	public static SystemMessageId TICKET_SALES_FOR_THE_MONSTER_RACE_WILL_CEASE_IN_S1_MINUTE_S;
	
	@ClientString(id = 819, message = "Tickets sales are closed for the $s1th Monster Race. Odds are posted.")
	public static SystemMessageId TICKETS_SALES_ARE_CLOSED_FOR_THE_S1TH_MONSTER_RACE_ODDS_ARE_POSTED;
	
	@ClientString(id = 820, message = "The $s2th Monster Race will begin in $s1 minutes.")
	public static SystemMessageId THE_S2TH_MONSTER_RACE_WILL_BEGIN_IN_S1_MINUTES;
	
	@ClientString(id = 821, message = "The $s1th Monster Race will begin in 30 seconds.")
	public static SystemMessageId THE_S1TH_MONSTER_RACE_WILL_BEGIN_IN_30_SECONDS;
	
	@ClientString(id = 822, message = "The $s1th Monster Race is about to begin. Countdown in five seconds.")
	public static SystemMessageId THE_S1TH_MONSTER_RACE_IS_ABOUT_TO_BEGIN_COUNTDOWN_IN_FIVE_SECONDS;
	
	@ClientString(id = 823, message = "The race will begin in $s1 seconds!")
	public static SystemMessageId THE_RACE_WILL_BEGIN_IN_S1_SECONDS;
	
	@ClientString(id = 824, message = "They're off!")
	public static SystemMessageId THEY_RE_OFF;
	
	@ClientString(id = 825, message = "The $s1th race is finished.")
	public static SystemMessageId THE_S1TH_RACE_IS_FINISHED;
	
	@ClientString(id = 826, message = "First prize goes to the player in lane $s1. Second prize goes to the player in lane $s2.")
	public static SystemMessageId FIRST_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S1_SECOND_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S2;
	
	@ClientString(id = 827, message = "You may not impose a block on a GM.")
	public static SystemMessageId YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM;
	
	@ClientString(id = 828, message = "Are you sure you wish to delete the $s1 macro?")
	public static SystemMessageId ARE_YOU_SURE_YOU_WISH_TO_DELETE_THE_S1_MACRO;
	
	@ClientString(id = 829, message = "You cannot recommend yourself.")
	public static SystemMessageId YOU_CANNOT_RECOMMEND_YOURSELF;
	
	@ClientString(id = 830, message = "You have recommended $s1. You are authorized to make $s2 more recommendations.")
	public static SystemMessageId YOU_HAVE_RECOMMENDED_S1_YOU_ARE_AUTHORIZED_TO_MAKE_S2_MORE_RECOMMENDATIONS;
	
	@ClientString(id = 831, message = "You have been recommended by $s1.")
	public static SystemMessageId YOU_HAVE_BEEN_RECOMMENDED_BY_S1;
	
	@ClientString(id = 832, message = "That character has already been recommended.")
	public static SystemMessageId THAT_CHARACTER_HAS_ALREADY_BEEN_RECOMMENDED;
	
	@ClientString(id = 833, message = "You are not authorized to make further recommendations at this time. You will receive more recommendation credits each day at 1 p.m.")
	public static SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_MAKE_FURTHER_RECOMMENDATIONS_AT_THIS_TIME_YOU_WILL_RECEIVE_MORE_RECOMMENDATION_CREDITS_EACH_DAY_AT_1_P_M;
	
	@ClientString(id = 834, message = "$s1 has rolled $s2.")
	public static SystemMessageId S1_HAS_ROLLED_S2;
	
	@ClientString(id = 835, message = "You may not throw the dice at this time.Try again later.")
	public static SystemMessageId YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER;
	
	@ClientString(id = 836, message = "The inventory is full, no further quest items may be deposited at this time.")
	public static SystemMessageId THE_INVENTORY_IS_FULL_NO_FURTHER_QUEST_ITEMS_MAY_BE_DEPOSITED_AT_THIS_TIME;
	
	@ClientString(id = 837, message = "Macro descriptions may contain up to 32 characters.")
	public static SystemMessageId MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS;
	
	@ClientString(id = 838, message = "Enter the name of the macro.")
	public static SystemMessageId ENTER_THE_NAME_OF_THE_MACRO;
	
	@ClientString(id = 839, message = "That name is already assigned to another macro.")
	public static SystemMessageId THAT_NAME_IS_ALREADY_ASSIGNED_TO_ANOTHER_MACRO;
	
	@ClientString(id = 840, message = "That recipe is already registered.")
	public static SystemMessageId THAT_RECIPE_IS_ALREADY_REGISTERED;
	
	@ClientString(id = 841, message = "No further recipes may be registered.")
	public static SystemMessageId NO_FURTHER_RECIPES_MAY_BE_REGISTERED;
	
	@ClientString(id = 842, message = "You are not authorized to register a recipe.")
	public static SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE;
	
	@ClientString(id = 843, message = "The siege of $s1 is finished.")
	public static SystemMessageId THE_SIEGE_OF_S1_IS_FINISHED;
	
	@ClientString(id = 844, message = "The siege to conquer $s1 has begun.")
	public static SystemMessageId THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN;
	
	@ClientString(id = 845, message = "The deadline to register for the siege of $s1 has passed.")
	public static SystemMessageId THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED;
	
	@ClientString(id = 846, message = "The siege of $s1 has been canceled due to lack of interest.")
	public static SystemMessageId THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST;
	
	@ClientString(id = 847, message = "A clan that owns a clan hall may not participate in a clan hall siege.")
	public static SystemMessageId A_CLAN_THAT_OWNS_A_CLAN_HALL_MAY_NOT_PARTICIPATE_IN_A_CLAN_HALL_SIEGE;
	
	@ClientString(id = 848, message = "$s1 has been deleted.")
	public static SystemMessageId S1_HAS_BEEN_DELETED;
	
	@ClientString(id = 849, message = "$s1 cannot be found.")
	public static SystemMessageId S1_CANNOT_BE_FOUND;
	
	@ClientString(id = 850, message = "$s1 already exists.")
	public static SystemMessageId S1_ALREADY_EXISTS_2;
	
	@ClientString(id = 851, message = "$s1 has been added.")
	public static SystemMessageId S1_HAS_BEEN_ADDED;
	
	@ClientString(id = 852, message = "The recipe is incorrect.")
	public static SystemMessageId THE_RECIPE_IS_INCORRECT;
	
	@ClientString(id = 853, message = "You may not alter your recipe book while engaged in manufacturing.")
	public static SystemMessageId YOU_MAY_NOT_ALTER_YOUR_RECIPE_BOOK_WHILE_ENGAGED_IN_MANUFACTURING;
	
	@ClientString(id = 854, message = "You lack $s2 of $s1.")
	public static SystemMessageId YOU_LACK_S2_OF_S1;
	
	@ClientString(id = 855, message = "$s1 clan has defeated $s2.")
	public static SystemMessageId S1_CLAN_HAS_DEFEATED_S2;
	
	@ClientString(id = 856, message = "The siege of $s1 has ended in a draw.")
	public static SystemMessageId THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW;
	
	@ClientString(id = 857, message = "$s1 clan has won in the preliminary match of $s2.")
	public static SystemMessageId S1_CLAN_HAS_WON_IN_THE_PRELIMINARY_MATCH_OF_S2;
	
	@ClientString(id = 858, message = "The preliminary match of $s1 has ended in a draw.")
	public static SystemMessageId THE_PRELIMINARY_MATCH_OF_S1_HAS_ENDED_IN_A_DRAW;
	
	@ClientString(id = 859, message = "Please register a recipe.")
	public static SystemMessageId PLEASE_REGISTER_A_RECIPE;
	
	@ClientString(id = 860, message = "You may not build your headquarters in close proximity to another headquarters.")
	public static SystemMessageId YOU_MAY_NOT_BUILD_YOUR_HEADQUARTERS_IN_CLOSE_PROXIMITY_TO_ANOTHER_HEADQUARTERS;
	
	@ClientString(id = 861, message = "You have exceeded the maximum number of memos.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_MAXIMUM_NUMBER_OF_MEMOS;
	
	@ClientString(id = 862, message = "Odds are not posted until ticket sales have closed.")
	public static SystemMessageId ODDS_ARE_NOT_POSTED_UNTIL_TICKET_SALES_HAVE_CLOSED;
	
	@ClientString(id = 863, message = "You feel the energy of fire.")
	public static SystemMessageId YOU_FEEL_THE_ENERGY_OF_FIRE;
	
	@ClientString(id = 864, message = "You feel the energy of water.")
	public static SystemMessageId YOU_FEEL_THE_ENERGY_OF_WATER;
	
	@ClientString(id = 865, message = "You feel the energy of wind.")
	public static SystemMessageId YOU_FEEL_THE_ENERGY_OF_WIND;
	
	@ClientString(id = 866, message = "You may no longer gather energy.")
	public static SystemMessageId YOU_MAY_NO_LONGER_GATHER_ENERGY;
	
	@ClientString(id = 867, message = "The energy is depleted.")
	public static SystemMessageId THE_ENERGY_IS_DEPLETED;
	
	@ClientString(id = 868, message = "The energy of fire has been delivered.")
	public static SystemMessageId THE_ENERGY_OF_FIRE_HAS_BEEN_DELIVERED;
	
	@ClientString(id = 869, message = "The energy of water has been delivered.")
	public static SystemMessageId THE_ENERGY_OF_WATER_HAS_BEEN_DELIVERED;
	
	@ClientString(id = 870, message = "The energy of wind has been delivered.")
	public static SystemMessageId THE_ENERGY_OF_WIND_HAS_BEEN_DELIVERED;
	
	@ClientString(id = 871, message = "The seed has been sown.")
	public static SystemMessageId THE_SEED_HAS_BEEN_SOWN;
	
	@ClientString(id = 872, message = "This seed may not be sown here.")
	public static SystemMessageId THIS_SEED_MAY_NOT_BE_SOWN_HERE;
	
	@ClientString(id = 873, message = "That character does not exist.")
	public static SystemMessageId THAT_CHARACTER_DOES_NOT_EXIST;
	
	@ClientString(id = 874, message = "The capacity of the warehouse has been exceeded.")
	public static SystemMessageId THE_CAPACITY_OF_THE_WAREHOUSE_HAS_BEEN_EXCEEDED;
	
	@ClientString(id = 875, message = "Transport of cargo has been canceled.")
	public static SystemMessageId TRANSPORT_OF_CARGO_HAS_BEEN_CANCELED;
	
	@ClientString(id = 876, message = "Cargo was not delivered.")
	public static SystemMessageId CARGO_WAS_NOT_DELIVERED;
	
	@ClientString(id = 877, message = "The symbol has been added.")
	public static SystemMessageId THE_SYMBOL_HAS_BEEN_ADDED;
	
	@ClientString(id = 878, message = "The symbol has been deleted.")
	public static SystemMessageId THE_SYMBOL_HAS_BEEN_DELETED;
	
	@ClientString(id = 879, message = "The manor system is currently under maintenance.")
	public static SystemMessageId THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE;
	
	@ClientString(id = 880, message = "The transaction is complete.")
	public static SystemMessageId THE_TRANSACTION_IS_COMPLETE;
	
	@ClientString(id = 881, message = "There is a discrepancy on the invoice.")
	public static SystemMessageId THERE_IS_A_DISCREPANCY_ON_THE_INVOICE;
	
	@ClientString(id = 882, message = "Seed quantity is incorrect.")
	public static SystemMessageId SEED_QUANTITY_IS_INCORRECT;
	
	@ClientString(id = 883, message = "Seed information is incorrect.")
	public static SystemMessageId SEED_INFORMATION_IS_INCORRECT;
	
	@ClientString(id = 884, message = "The manor information has been updated.")
	public static SystemMessageId THE_MANOR_INFORMATION_HAS_BEEN_UPDATED;
	
	@ClientString(id = 885, message = "The number of crops is incorrect.")
	public static SystemMessageId THE_NUMBER_OF_CROPS_IS_INCORRECT;
	
	@ClientString(id = 886, message = "The crops are priced incorrectly.")
	public static SystemMessageId THE_CROPS_ARE_PRICED_INCORRECTLY;
	
	@ClientString(id = 887, message = "The type is incorrect.")
	public static SystemMessageId THE_TYPE_IS_INCORRECT;
	
	@ClientString(id = 888, message = "No crops can be purchased at this time.")
	public static SystemMessageId NO_CROPS_CAN_BE_PURCHASED_AT_THIS_TIME;
	
	@ClientString(id = 889, message = "The seed was successfully sown.")
	public static SystemMessageId THE_SEED_WAS_SUCCESSFULLY_SOWN;
	
	@ClientString(id = 890, message = "The seed was not sown.")
	public static SystemMessageId THE_SEED_WAS_NOT_SOWN;
	
	@ClientString(id = 891, message = "You are not authorized to harvest.")
	public static SystemMessageId YOU_ARE_NOT_AUTHORIZED_TO_HARVEST;
	
	@ClientString(id = 892, message = "The harvest has failed.")
	public static SystemMessageId THE_HARVEST_HAS_FAILED;
	
	@ClientString(id = 893, message = "The harvest failed because the seed was not sown.")
	public static SystemMessageId THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN;
	
	@ClientString(id = 894, message = "Up to $s1 recipes can be registered.")
	public static SystemMessageId UP_TO_S1_RECIPES_CAN_BE_REGISTERED;
	
	@ClientString(id = 895, message = "No recipes have been registered.")
	public static SystemMessageId NO_RECIPES_HAVE_BEEN_REGISTERED;
	
	@ClientString(id = 896, message = "Quest recipes can not be registered.")
	public static SystemMessageId QUEST_RECIPES_CAN_NOT_BE_REGISTERED;
	
	@ClientString(id = 897, message = "The fee to create the item is incorrect.")
	public static SystemMessageId THE_FEE_TO_CREATE_THE_ITEM_IS_INCORRECT;
	
	@ClientString(id = 898, message = "Only characters of level 10 or above are authorized to make recommendations.")
	public static SystemMessageId ONLY_CHARACTERS_OF_LEVEL_10_OR_ABOVE_ARE_AUTHORIZED_TO_MAKE_RECOMMENDATIONS;
	
	@ClientString(id = 899, message = "The symbol cannot be drawn.")
	public static SystemMessageId THE_SYMBOL_CANNOT_BE_DRAWN;
	
	@ClientString(id = 900, message = "No slot exists to draw the symbol.")
	public static SystemMessageId NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL;
	
	@ClientString(id = 901, message = "The symbol information cannot be found.")
	public static SystemMessageId THE_SYMBOL_INFORMATION_CANNOT_BE_FOUND;
	
	@ClientString(id = 902, message = "The number of items is incorrect.")
	public static SystemMessageId THE_NUMBER_OF_ITEMS_IS_INCORRECT;
	
	@ClientString(id = 903, message = "You may not submit a petition while frozen. Be patient.")
	public static SystemMessageId YOU_MAY_NOT_SUBMIT_A_PETITION_WHILE_FROZEN_BE_PATIENT;
	
	@ClientString(id = 904, message = "Items cannot be discarded while in private store status.")
	public static SystemMessageId ITEMS_CANNOT_BE_DISCARDED_WHILE_IN_PRIVATE_STORE_STATUS;
	
	@ClientString(id = 905, message = "The current score for the Human race is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_HUMAN_RACE_IS_S1;
	
	@ClientString(id = 906, message = "The current score for the Elven race is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_ELVEN_RACE_IS_S1;
	
	@ClientString(id = 907, message = "The current score for the Dark Elven race is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_DARK_ELVEN_RACE_IS_S1;
	
	@ClientString(id = 908, message = "The current score for the Orc race is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_ORC_RACE_IS_S1;
	
	@ClientString(id = 909, message = "The current score for the Dwarven race is $s1.")
	public static SystemMessageId THE_CURRENT_SCORE_FOR_THE_DWARVEN_RACE_IS_S1;
	
	@ClientString(id = 910, message = "Current location : $s1, $s2, $s3 (Near Talking Island Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_TALKING_ISLAND_VILLAGE;
	
	@ClientString(id = 911, message = "Current location : $s1, $s2, $s3 (Near Gludin Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_GLUDIN_VILLAGE;
	
	@ClientString(id = 912, message = "Current location : $s1, $s2, $s3 (Near Gludio Castle Town)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_GLUDIO_CASTLE_TOWN;
	
	@ClientString(id = 913, message = "Current location : $s1, $s2, $s3 (Near the Neutral Zone)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_NEUTRAL_ZONE;
	
	@ClientString(id = 914, message = "Current location : $s1, $s2, $s3 (Near Elven Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_ELVEN_VILLAGE;
	
	@ClientString(id = 915, message = "Current location : $s1, $s2, $s3 (Near Dark Elven Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_DARK_ELVEN_VILLAGE;
	
	@ClientString(id = 916, message = "Current location : $s1, $s2, $s3 (Near Dion Castle Town)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_DION_CASTLE_TOWN;
	
	@ClientString(id = 917, message = "Current location : $s1, $s2, $s3 (Near Floran Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_FLORAN_VILLAGE;
	
	@ClientString(id = 918, message = "Current location : $s1, $s2, $s3 (Near Giran Castle Town)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_GIRAN_CASTLE_TOWN;
	
	@ClientString(id = 919, message = "Current location : $s1, $s2, $s3 (Near Giran Harbor)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_GIRAN_HARBOR;
	
	@ClientString(id = 920, message = "Current location : $s1, $s2, $s3 (Near Orc Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_ORC_VILLAGE;
	
	@ClientString(id = 921, message = "Current location : $s1, $s2, $s3 (Near Dwarven Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_DWARVEN_VILLAGE;
	
	@ClientString(id = 922, message = "Current location : $s1, $s2, $s3 (Near the town of Oren)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_OREN;
	
	@ClientString(id = 923, message = "Current location : $s1, $s2, $s3 (Near Hunters Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_HUNTERS_VILLAGE;
	
	@ClientString(id = 924, message = "Current location : $s1, $s2, $s3 (Near Aden Castle Town)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_ADEN_CASTLE_TOWN;
	
	@ClientString(id = 925, message = "Current location : $s1, $s2, $s3 (Near the Coliseum)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_THE_COLISEUM;
	
	@ClientString(id = 926, message = "Current location : $s1, $s2, $s3 (Near Heine)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_HEINE;
	
	@ClientString(id = 927, message = "The current time is $s1:$s2 in the day.")
	public static SystemMessageId THE_CURRENT_TIME_IS_S1_S2_IN_THE_DAY;
	
	@ClientString(id = 928, message = "The current time is $s1:$s2 in the night.")
	public static SystemMessageId THE_CURRENT_TIME_IS_S1_S2_IN_THE_NIGHT;
	
	@ClientString(id = 929, message = "No compensation was given for the farm products.")
	public static SystemMessageId NO_COMPENSATION_WAS_GIVEN_FOR_THE_FARM_PRODUCTS;
	
	@ClientString(id = 930, message = "Lottery tickets are not currently being sold.")
	public static SystemMessageId LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD;
	
	@ClientString(id = 931, message = "The winning lottery ticket number has not yet been announced.")
	public static SystemMessageId THE_WINNING_LOTTERY_TICKET_NUMBER_HAS_NOT_YET_BEEN_ANNOUNCED;
	
	@ClientString(id = 932, message = "You cannot chat locally while observing.")
	public static SystemMessageId YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING;
	
	@ClientString(id = 933, message = "The seed pricing greatly differs from standard seed prices.")
	public static SystemMessageId THE_SEED_PRICING_GREATLY_DIFFERS_FROM_STANDARD_SEED_PRICES;
	
	@ClientString(id = 934, message = "It is a deleted recipe.")
	public static SystemMessageId IT_IS_A_DELETED_RECIPE;
	
	@ClientString(id = 935, message = "The amount is not sufficient and so the manor is not in operation.")
	public static SystemMessageId THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION;
	
	@ClientString(id = 936, message = "Use $s1.")
	public static SystemMessageId USE_S1_2;
	
	@ClientString(id = 937, message = "Currently preparing for private workshop.")
	public static SystemMessageId CURRENTLY_PREPARING_FOR_PRIVATE_WORKSHOP;
	
	@ClientString(id = 938, message = "The community server is currently offline.")
	public static SystemMessageId THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE;
	
	@ClientString(id = 939, message = "You cannot exchange while blocking everything.")
	public static SystemMessageId YOU_CANNOT_EXCHANGE_WHILE_BLOCKING_EVERYTHING;
	
	@ClientString(id = 940, message = "$s1 is blocking everything.")
	public static SystemMessageId S1_IS_BLOCKING_EVERYTHING;
	
	@ClientString(id = 941, message = "Restart at Talking Island Village.")
	public static SystemMessageId RESTART_AT_TALKING_ISLAND_VILLAGE;
	
	@ClientString(id = 942, message = "Restart at Gludin Village.")
	public static SystemMessageId RESTART_AT_GLUDIN_VILLAGE;
	
	@ClientString(id = 943, message = "Restart at Gludin Castle Town.")
	public static SystemMessageId RESTART_AT_GLUDIN_CASTLE_TOWN;
	
	@ClientString(id = 944, message = "Restart at the Neutral Zone.")
	public static SystemMessageId RESTART_AT_THE_NEUTRAL_ZONE;
	
	@ClientString(id = 945, message = "Restart at Elven Village.")
	public static SystemMessageId RESTART_AT_ELVEN_VILLAGE;
	
	@ClientString(id = 946, message = "Restart at Dark Elven Village.")
	public static SystemMessageId RESTART_AT_DARK_ELVEN_VILLAGE;
	
	@ClientString(id = 947, message = "Restart at Dion Castle Town.")
	public static SystemMessageId RESTART_AT_DION_CASTLE_TOWN;
	
	@ClientString(id = 948, message = "Restart at Floran Village.")
	public static SystemMessageId RESTART_AT_FLORAN_VILLAGE;
	
	@ClientString(id = 949, message = "Restart at Giran Castle Town.")
	public static SystemMessageId RESTART_AT_GIRAN_CASTLE_TOWN;
	
	@ClientString(id = 950, message = "Restart at Giran Harbor.")
	public static SystemMessageId RESTART_AT_GIRAN_HARBOR;
	
	@ClientString(id = 951, message = "Restart at Orc Village.")
	public static SystemMessageId RESTART_AT_ORC_VILLAGE;
	
	@ClientString(id = 952, message = "Restart at Dwarven Village.")
	public static SystemMessageId RESTART_AT_DWARVEN_VILLAGE;
	
	@ClientString(id = 953, message = "Restart at the town of Oren.")
	public static SystemMessageId RESTART_AT_THE_TOWN_OF_OREN;
	
	@ClientString(id = 954, message = "Restart at Hunters Village.")
	public static SystemMessageId RESTART_AT_HUNTERS_VILLAGE;
	
	@ClientString(id = 955, message = "Restart at Aden Castle Town.")
	public static SystemMessageId RESTART_AT_ADEN_CASTLE_TOWN;
	
	@ClientString(id = 956, message = "Restart at the Coliseum.")
	public static SystemMessageId RESTART_AT_THE_COLISEUM;
	
	@ClientString(id = 957, message = "Restart at Heine.")
	public static SystemMessageId RESTART_AT_HEINE;
	
	@ClientString(id = 958, message = "Items cannot be discarded or destroyed while operating a private store or workshop.")
	public static SystemMessageId ITEMS_CANNOT_BE_DISCARDED_OR_DESTROYED_WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP;
	
	@ClientString(id = 959, message = "$s1 (*$s2) manufacturing success.")
	public static SystemMessageId S1_S2_MANUFACTURING_SUCCESS;
	
	@ClientString(id = 960, message = "$s1 manufacturing failure.")
	public static SystemMessageId S1_MANUFACTURING_FAILURE;
	
	@ClientString(id = 961, message = "You are now blocking everything.")
	public static SystemMessageId YOU_ARE_NOW_BLOCKING_EVERYTHING;
	
	@ClientString(id = 962, message = "You are no longer blocking everything.")
	public static SystemMessageId YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING;
	
	@ClientString(id = 963, message = "Please determine the manufacturing price.")
	public static SystemMessageId PLEASE_DETERMINE_THE_MANUFACTURING_PRICE;
	
	@ClientString(id = 964, message = "Chatting is prohibited for about one minute.")
	public static SystemMessageId CHATTING_IS_PROHIBITED_FOR_ABOUT_ONE_MINUTE;
	
	@ClientString(id = 965, message = "The chatting prohibition has been removed.")
	public static SystemMessageId THE_CHATTING_PROHIBITION_HAS_BEEN_REMOVED;
	
	@ClientString(id = 966, message = "Chatting is currently prohibited. If you try to chat before the prohibition is removed, the prohibition time will become even longer.")
	public static SystemMessageId CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_BECOME_EVEN_LONGER;
	
	@ClientString(id = 967, message = "Do you accept the party invitation from $s1? (Item distribution: Random including spoil)")
	public static SystemMessageId DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_RANDOM_INCLUDING_SPOIL;
	
	@ClientString(id = 968, message = "Do you accept the party invitation from $s1? (Item distribution: By turn)")
	public static SystemMessageId DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_BY_TURN;
	
	@ClientString(id = 969, message = "Do you accept the party invitation from $s1? (Item distribution: By turn including spoil)")
	public static SystemMessageId DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_BY_TURN_INCLUDING_SPOIL;
	
	@ClientString(id = 970, message = "$s2's MP has been drained by $s1.")
	public static SystemMessageId S2_S_MP_HAS_BEEN_DRAINED_BY_S1;
	
	@ClientString(id = 971, message = "Petitions cannot exceed 255 characters.")
	public static SystemMessageId PETITIONS_CANNOT_EXCEED_255_CHARACTERS;
	
	@ClientString(id = 972, message = "Pets cannot use this item.")
	public static SystemMessageId PETS_CANNOT_USE_THIS_ITEM;
	
	@ClientString(id = 973, message = "Please input no more than the number you have.")
	public static SystemMessageId PLEASE_INPUT_NO_MORE_THAN_THE_NUMBER_YOU_HAVE;
	
	@ClientString(id = 974, message = "The soul crystal succeeded in absorbing a soul.")
	public static SystemMessageId THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL;
	
	@ClientString(id = 975, message = "The soul crystal was not able to absorb a soul.")
	public static SystemMessageId THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_A_SOUL;
	
	@ClientString(id = 976, message = "The soul crystal broke because it was not able to endure the soul energy.")
	public static SystemMessageId THE_SOUL_CRYSTAL_BROKE_BECAUSE_IT_WAS_NOT_ABLE_TO_ENDURE_THE_SOUL_ENERGY;
	
	@ClientString(id = 977, message = "The soul crystals caused resonation and failed at absorbing a soul.")
	public static SystemMessageId THE_SOUL_CRYSTALS_CAUSED_RESONATION_AND_FAILED_AT_ABSORBING_A_SOUL;
	
	@ClientString(id = 978, message = "The soul crystal is refusing to absorb a soul.")
	public static SystemMessageId THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_A_SOUL;
	
	@ClientString(id = 979, message = "Arrived at Talking Island Harbor.")
	public static SystemMessageId ARRIVED_AT_TALKING_ISLAND_HARBOR;
	
	@ClientString(id = 980, message = "Will leave for Gludin Harbor after anchoring for ten minutes.")
	public static SystemMessageId WILL_LEAVE_FOR_GLUDIN_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES;
	
	@ClientString(id = 981, message = "Will leave for Gludin Harbor in five minutes.")
	public static SystemMessageId WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_FIVE_MINUTES;
	
	@ClientString(id = 982, message = "Will leave for Gludin Harbor in one minute.")
	public static SystemMessageId WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_ONE_MINUTE;
	
	@ClientString(id = 983, message = "Those wishing to ride should make haste to get on.")
	public static SystemMessageId THOSE_WISHING_TO_RIDE_SHOULD_MAKE_HASTE_TO_GET_ON;
	
	@ClientString(id = 984, message = "Leaving soon for Gludin Harbor.")
	public static SystemMessageId LEAVING_SOON_FOR_GLUDIN_HARBOR;
	
	@ClientString(id = 985, message = "Leaving for Gludin Harbor.")
	public static SystemMessageId LEAVING_FOR_GLUDIN_HARBOR;
	
	@ClientString(id = 986, message = "Arrived at Gludin Harbor.")
	public static SystemMessageId ARRIVED_AT_GLUDIN_HARBOR;
	
	@ClientString(id = 987, message = "Will leave for Talking Island Harbor after anchoring for ten minutes.")
	public static SystemMessageId WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES;
	
	@ClientString(id = 988, message = "Will leave for Talking Island Harbor in five minutes.")
	public static SystemMessageId WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_IN_FIVE_MINUTES;
	
	@ClientString(id = 989, message = "Will leave for Talking Island Harbor in one minute.")
	public static SystemMessageId WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_IN_ONE_MINUTE;
	
	@ClientString(id = 990, message = "Leaving soon for Talking Island Harbor.")
	public static SystemMessageId LEAVING_SOON_FOR_TALKING_ISLAND_HARBOR;
	
	@ClientString(id = 991, message = "Leaving for Talking Island Harbor.")
	public static SystemMessageId LEAVING_FOR_TALKING_ISLAND_HARBOR;
	
	@ClientString(id = 992, message = "Arrived at Giran Harbor.")
	public static SystemMessageId ARRIVED_AT_GIRAN_HARBOR;
	
	@ClientString(id = 993, message = "Will leave for Giran Harbor after anchoring for ten minutes.")
	public static SystemMessageId WILL_LEAVE_FOR_GIRAN_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES;
	
	@ClientString(id = 994, message = "Will leave for Giran Harbor in five minutes.")
	public static SystemMessageId WILL_LEAVE_FOR_GIRAN_HARBOR_IN_FIVE_MINUTES;
	
	@ClientString(id = 995, message = "Will leave for Giran Harbor in one minute.")
	public static SystemMessageId WILL_LEAVE_FOR_GIRAN_HARBOR_IN_ONE_MINUTE;
	
	@ClientString(id = 996, message = "Leaving soon for Giran Harbor.")
	public static SystemMessageId LEAVING_SOON_FOR_GIRAN_HARBOR;
	
	@ClientString(id = 997, message = "Leaving for Giran Harbor.")
	public static SystemMessageId LEAVING_FOR_GIRAN_HARBOR;
	
	@ClientString(id = 998, message = "The Innadril pleasure boat has arrived. It will anchor for ten minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_HAS_ARRIVED_IT_WILL_ANCHOR_FOR_TEN_MINUTES;
	
	@ClientString(id = 999, message = "The Innadril pleasure boat will leave in five minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_LEAVE_IN_FIVE_MINUTES;
	
	@ClientString(id = 1000, message = "The Innadril pleasure boat will leave in one minute.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_LEAVE_IN_ONE_MINUTE;
	
	@ClientString(id = 1001, message = "Innadril pleasure boat is leaving soon.")
	public static SystemMessageId INNADRIL_PLEASURE_BOAT_IS_LEAVING_SOON;
	
	@ClientString(id = 1002, message = "Innadril pleasure boat is leaving.")
	public static SystemMessageId INNADRIL_PLEASURE_BOAT_IS_LEAVING;
	
	@ClientString(id = 1003, message = "Cannot process a monster race ticket.")
	public static SystemMessageId CANNOT_PROCESS_A_MONSTER_RACE_TICKET;
	
	@ClientString(id = 1004, message = "You have registered for a clan hall auction.")
	public static SystemMessageId YOU_HAVE_REGISTERED_FOR_A_CLAN_HALL_AUCTION;
	
	@ClientString(id = 1005, message = "There is not enough adena in the clan hall warehouse.")
	public static SystemMessageId THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE;
	
	@ClientString(id = 1006, message = "You have bid in a clan hall auction.")
	public static SystemMessageId YOU_HAVE_BID_IN_A_CLAN_HALL_AUCTION;
	
	@ClientString(id = 1007, message = "The preliminary match registration of $s1 has finished.")
	public static SystemMessageId THE_PRELIMINARY_MATCH_REGISTRATION_OF_S1_HAS_FINISHED;
	
	@ClientString(id = 1008, message = "A hungry strider cannot be mounted or dismounted.")
	public static SystemMessageId A_HUNGRY_STRIDER_CANNOT_BE_MOUNTED_OR_DISMOUNTED;
	
	@ClientString(id = 1009, message = "A strider cannot be ridden when dead.")
	public static SystemMessageId A_STRIDER_CANNOT_BE_RIDDEN_WHEN_DEAD;
	
	@ClientString(id = 1010, message = "A dead strider cannot be ridden.")
	public static SystemMessageId A_DEAD_STRIDER_CANNOT_BE_RIDDEN;
	
	@ClientString(id = 1011, message = "A strider in battle cannot be ridden.")
	public static SystemMessageId A_STRIDER_IN_BATTLE_CANNOT_BE_RIDDEN;
	
	@ClientString(id = 1012, message = "A strider cannot be ridden while in battle.")
	public static SystemMessageId A_STRIDER_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE;
	
	@ClientString(id = 1013, message = "A strider can be ridden only when standing.")
	public static SystemMessageId A_STRIDER_CAN_BE_RIDDEN_ONLY_WHEN_STANDING;
	
	@ClientString(id = 1014, message = "The pet acquired experience points of $s1.")
	public static SystemMessageId THE_PET_ACQUIRED_EXPERIENCE_POINTS_OF_S1;
	
	@ClientString(id = 1015, message = "The pet gave damage of $s1.")
	public static SystemMessageId THE_PET_GAVE_DAMAGE_OF_S1;
	
	@ClientString(id = 1016, message = "The pet received damage of $s2 caused by $s1.")
	public static SystemMessageId THE_PET_RECEIVED_DAMAGE_OF_S2_CAUSED_BY_S1;
	
	@ClientString(id = 1017, message = "Pet's critical hit!")
	public static SystemMessageId PET_S_CRITICAL_HIT;
	
	@ClientString(id = 1018, message = "The pet uses $s1.")
	public static SystemMessageId THE_PET_USES_S1;
	
	@ClientString(id = 1019, message = "The pet uses $s1.")
	public static SystemMessageId THE_PET_USES_S1_2;
	
	@ClientString(id = 1020, message = "The pet gave $s1.")
	public static SystemMessageId THE_PET_GAVE_S1;
	
	@ClientString(id = 1021, message = "The pet gave $s2 $s1(s).")
	public static SystemMessageId THE_PET_GAVE_S2_S1_S;
	
	@ClientString(id = 1022, message = "The pet gave +$s1 $s2.")
	public static SystemMessageId THE_PET_GAVE_S1_S2;
	
	@ClientString(id = 1023, message = "The pet gave $s1 adena.")
	public static SystemMessageId THE_PET_GAVE_S1_ADENA;
	
	@ClientString(id = 1024, message = "The pet put on $s1.")
	public static SystemMessageId THE_PET_PUT_ON_S1;
	
	@ClientString(id = 1025, message = "The pet took off $s1.")
	public static SystemMessageId THE_PET_TOOK_OFF_S1;
	
	@ClientString(id = 1026, message = "The summoned monster gave damage of $s1.")
	public static SystemMessageId THE_SUMMONED_MONSTER_GAVE_DAMAGE_OF_S1;
	
	@ClientString(id = 1027, message = "The summoned monster received damage of $s2 caused by $s1.")
	public static SystemMessageId THE_SUMMONED_MONSTER_RECEIVED_DAMAGE_OF_S2_CAUSED_BY_S1;
	
	@ClientString(id = 1028, message = "Summoned monster's critical hit!")
	public static SystemMessageId SUMMONED_MONSTER_S_CRITICAL_HIT;
	
	@ClientString(id = 1029, message = "A summoned monster uses $s1.")
	public static SystemMessageId A_SUMMONED_MONSTER_USES_S1;
	
	@ClientString(id = 1030, message = "<Party Information>")
	public static SystemMessageId PARTY_INFORMATION;
	
	@ClientString(id = 1031, message = "Looting method: Finders keepers")
	public static SystemMessageId LOOTING_METHOD_FINDERS_KEEPERS;
	
	@ClientString(id = 1032, message = "Looting method: Random")
	public static SystemMessageId LOOTING_METHOD_RANDOM;
	
	@ClientString(id = 1033, message = "Looting method: Random including spoil")
	public static SystemMessageId LOOTING_METHOD_RANDOM_INCLUDING_SPOIL;
	
	@ClientString(id = 1034, message = "Looting method: By turn")
	public static SystemMessageId LOOTING_METHOD_BY_TURN;
	
	@ClientString(id = 1035, message = "Looting method: By turn including spoil")
	public static SystemMessageId LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL;
	
	@ClientString(id = 1036, message = "You have exceeded the quantity that can be inputted.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED;
	
	@ClientString(id = 1037, message = "$s1 manufactured $s2.")
	public static SystemMessageId S1_MANUFACTURED_S2;
	
	@ClientString(id = 1038, message = "$s1 manufactured $s3 $s2(s).")
	public static SystemMessageId S1_MANUFACTURED_S3_S2_S;
	
	@ClientString(id = 1039, message = "Items left at the clan hall warehouse can only be retrieved by the clan leader. Do you want to continue?")
	public static SystemMessageId ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 1040, message = "Packages sent can only be retrieved at this warehouse. Do you want to continue?")
	public static SystemMessageId PACKAGES_SENT_CAN_ONLY_BE_RETRIEVED_AT_THIS_WAREHOUSE_DO_YOU_WANT_TO_CONTINUE;
	
	@ClientString(id = 1041, message = "The next seed purchase price is $s1 adena.")
	public static SystemMessageId THE_NEXT_SEED_PURCHASE_PRICE_IS_S1_ADENA;
	
	@ClientString(id = 1042, message = "The next farm goods purchase price is $s1 adena.")
	public static SystemMessageId THE_NEXT_FARM_GOODS_PURCHASE_PRICE_IS_S1_ADENA;
	
	@ClientString(id = 1043, message = "At the current time, the '/unstuck' command cannot be used. Please send in a petition.")
	public static SystemMessageId AT_THE_CURRENT_TIME_THE_UNSTUCK_COMMAND_CANNOT_BE_USED_PLEASE_SEND_IN_A_PETITION;
	
	@ClientString(id = 1044, message = "Monster race payout information is not available while tickets are being sold.")
	public static SystemMessageId MONSTER_RACE_PAYOUT_INFORMATION_IS_NOT_AVAILABLE_WHILE_TICKETS_ARE_BEING_SOLD;
	
	@ClientString(id = 1045, message = "Not currently preparing for a monster race.")
	public static SystemMessageId NOT_CURRENTLY_PREPARING_FOR_A_MONSTER_RACE;
	
	@ClientString(id = 1046, message = "Monster race tickets are no longer available.")
	public static SystemMessageId MONSTER_RACE_TICKETS_ARE_NO_LONGER_AVAILABLE;
	
	@ClientString(id = 1047, message = "We did not succeed in producing $s1 item.")
	public static SystemMessageId WE_DID_NOT_SUCCEED_IN_PRODUCING_S1_ITEM;
	
	@ClientString(id = 1048, message = "Whispering is not possible in state of overall blocking.")
	public static SystemMessageId WHISPERING_IS_NOT_POSSIBLE_IN_STATE_OF_OVERALL_BLOCKING;
	
	@ClientString(id = 1049, message = "It is not possible to make invitations for organizing parties in state of overall blocking.")
	public static SystemMessageId IT_IS_NOT_POSSIBLE_TO_MAKE_INVITATIONS_FOR_ORGANIZING_PARTIES_IN_STATE_OF_OVERALL_BLOCKING;
	
	@ClientString(id = 1050, message = "There are no communities in my clan. Clan communities are allowed for clans with skill levels of 2 and higher.")
	public static SystemMessageId THERE_ARE_NO_COMMUNITIES_IN_MY_CLAN_CLAN_COMMUNITIES_ARE_ALLOWED_FOR_CLANS_WITH_SKILL_LEVELS_OF_2_AND_HIGHER;
	
	@ClientString(id = 1051, message = "Payment for your clan hall has not been made. Please make payment to your clan warehouse by $s1 tomorrow.")
	public static SystemMessageId PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW;
	
	@ClientString(id = 1052, message = "The clan hall fee is one week overdue; therefore the clan hall ownership has been revoked.")
	public static SystemMessageId THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED;
	
	@ClientString(id = 1053, message = "It is impossible to be ressurected in battlefields where siege wars are in process.")
	public static SystemMessageId IT_IS_IMPOSSIBLE_TO_BE_RESSURECTED_IN_BATTLEFIELDS_WHERE_SIEGE_WARS_ARE_IN_PROCESS;
	
	@ClientString(id = 1054, message = "You have entered a land with mysterious powers.")
	public static SystemMessageId YOU_HAVE_ENTERED_A_LAND_WITH_MYSTERIOUS_POWERS;
	
	@ClientString(id = 1055, message = "You have left the land which has mysterious powers.")
	public static SystemMessageId YOU_HAVE_LEFT_THE_LAND_WHICH_HAS_MYSTERIOUS_POWERS;
	
	@ClientString(id = 1056, message = "You have exceeded the castle's storage limit of adena.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_CASTLE_S_STORAGE_LIMIT_OF_ADENA;
	
	@ClientString(id = 1057, message = "This command can only be used in the relax server.")
	public static SystemMessageId THIS_COMMAND_CAN_ONLY_BE_USED_IN_THE_RELAX_SERVER;
	
	@ClientString(id = 1058, message = "The sales amount of seeds is $s1 adena.")
	public static SystemMessageId THE_SALES_AMOUNT_OF_SEEDS_IS_S1_ADENA;
	
	@ClientString(id = 1059, message = "The remaining purchasing amount is $s1 adena.")
	public static SystemMessageId THE_REMAINING_PURCHASING_AMOUNT_IS_S1_ADENA;
	
	@ClientString(id = 1060, message = "The remainder after selling the seeds is $s1.")
	public static SystemMessageId THE_REMAINDER_AFTER_SELLING_THE_SEEDS_IS_S1;
	
	@ClientString(id = 1061, message = "The recipe cannot be registered. You do not have the ability to create items.")
	public static SystemMessageId THE_RECIPE_CANNOT_BE_REGISTERED_YOU_DO_NOT_HAVE_THE_ABILITY_TO_CREATE_ITEMS;
	
	@ClientString(id = 1062, message = "Writing something new is possible after level 10.")
	public static SystemMessageId WRITING_SOMETHING_NEW_IS_POSSIBLE_AFTER_LEVEL_10;
	
	@ClientString(id = 1063, message = "Petition service is not availabel for $s1 to $s2, in case of being trapped in territory where you are unable to move, please use the '/unstuck' command")
	public static SystemMessageId PETITION_SERVICE_IS_NOT_AVAILABEL_FOR_S1_TO_S2_IN_CASE_OF_BEING_TRAPPED_IN_TERRITORY_WHERE_YOU_ARE_UNABLE_TO_MOVE_PLEASE_USE_THE_UNSTUCK_COMMAND;
	
	@ClientString(id = 1064, message = "Equipment of +$s1 $s2 has been removed.")
	public static SystemMessageId EQUIPMENT_OF_S1_S2_HAS_BEEN_REMOVED;
	
	@ClientString(id = 1065, message = "'While operating a private store or workshop, you cannot discard, destroy or trade an item.")
	public static SystemMessageId WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM;
	
	@ClientString(id = 1066, message = "$s1 HPs have been restored.")
	public static SystemMessageId S1_HPS_HAVE_BEEN_RESTORED;
	
	@ClientString(id = 1067, message = "'$s2's HP has been restored by $s1.")
	public static SystemMessageId S2_S_HP_HAS_BEEN_RESTORED_BY_S1;
	
	@ClientString(id = 1068, message = "$s1 MPs have been restored.")
	public static SystemMessageId S1_MPS_HAVE_BEEN_RESTORED;
	
	@ClientString(id = 1069, message = "'$s2's MP has been restored by $s1.")
	public static SystemMessageId S2_S_MP_HAS_BEEN_RESTORED_BY_S1;
	
	@ClientString(id = 1070, message = "'You do not have 'read' permission.")
	public static SystemMessageId YOU_DO_NOT_HAVE_READ_PERMISSION;
	
	@ClientString(id = 1071, message = "'You do not have 'write' permission.")
	public static SystemMessageId YOU_DO_NOT_HAVE_WRITE_PERMISSION;
	
	@ClientString(id = 1072, message = "'You have obtained a ticket for the Monster Race #$s1 - Single.")
	public static SystemMessageId YOU_HAVE_OBTAINED_A_TICKET_FOR_THE_MONSTER_RACE_S1_SINGLE;
	
	@ClientString(id = 1073, message = "'You have obtained a ticket for the Monster Race #$s1 - Double.")
	public static SystemMessageId YOU_HAVE_OBTAINED_A_TICKET_FOR_THE_MONSTER_RACE_S1_DOUBLE;
	
	@ClientString(id = 1074, message = "You do not meet the age requirement to purchase a Monster Race Ticket.")
	public static SystemMessageId YOU_DO_NOT_MEET_THE_AGE_REQUIREMENT_TO_PURCHASE_A_MONSTER_RACE_TICKET;
	
	@ClientString(id = 1075, message = "'The second bid amount must be higher than the original.")
	public static SystemMessageId THE_SECOND_BID_AMOUNT_MUST_BE_HIGHER_THAN_THE_ORIGINAL;
	
	@ClientString(id = 1076, message = "'The game cannot be terminated.")
	public static SystemMessageId THE_GAME_CANNOT_BE_TERMINATED;
	
	@ClientString(id = 1077, message = "'A GameGuard Execution error has occurred. Please send the *.erl file(s) located in the GameGuard folder to game@inca.co.kr.")
	public static SystemMessageId A_GAMEGUARD_EXECUTION_ERROR_HAS_OCCURRED_PLEASE_SEND_THE_ERL_FILE_S_LOCATED_IN_THE_GAMEGUARD_FOLDER_TO_GAME_INCA_CO_KR;
	
	@ClientString(id = 1078, message = "When a user's keyboard input exceeds a certain cumulative score a chat ban will be applied. This is done to discourage spamming. Please avoid posting the same message multiple times during a short period.")
	public static SystemMessageId WHEN_A_USER_S_KEYBOARD_INPUT_EXCEEDS_A_CERTAIN_CUMULATIVE_SCORE_A_CHAT_BAN_WILL_BE_APPLIED_THIS_IS_DONE_TO_DISCOURAGE_SPAMMING_PLEASE_AVOID_POSTING_THE_SAME_MESSAGE_MULTIPLE_TIMES_DURING_A_SHORT_PERIOD;
	
	@ClientString(id = 1079, message = "'The target is currently banned from chatting.")
	public static SystemMessageId THE_TARGET_IS_CURRENTLY_BANNED_FROM_CHATTING;
	
	@ClientString(id = 1080, message = "Do you wish to use the facelifting potion ' Type A? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_FACELIFTING_POTION_TYPE_A_IT_IS_PERMANENT;
	
	@ClientString(id = 1081, message = "Do you wish to use the dye potion ' Type A? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_DYE_POTION_TYPE_A_IT_IS_PERMANENT;
	
	@ClientString(id = 1082, message = "Do you wish to use the hair style change potion ' Type A? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_A_IT_IS_PERMANENT;
	
	@ClientString(id = 1083, message = "The facelifting potion - Type A is being used.")
	public static SystemMessageId THE_FACELIFTING_POTION_TYPE_A_IS_BEING_USED;
	
	@ClientString(id = 1084, message = "The dye potion - Type A is being used.")
	public static SystemMessageId THE_DYE_POTION_TYPE_A_IS_BEING_USED;
	
	@ClientString(id = 1085, message = "The hair style change potion - Type A is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_A_IS_BEING_USED;
	
	@ClientString(id = 1086, message = "Your facial appearance has been changed.")
	public static SystemMessageId YOUR_FACIAL_APPEARANCE_HAS_BEEN_CHANGED;
	
	@ClientString(id = 1087, message = "Your hair color has been changed.")
	public static SystemMessageId YOUR_HAIR_COLOR_HAS_BEEN_CHANGED;
	
	@ClientString(id = 1088, message = "Your hair style has been changed.")
	public static SystemMessageId YOUR_HAIR_STYLE_HAS_BEEN_CHANGED;
	
	@ClientString(id = 1089, message = "$s1 has obtained a first anniversary commemorative item.")
	public static SystemMessageId S1_HAS_OBTAINED_A_FIRST_ANNIVERSARY_COMMEMORATIVE_ITEM;
	
	@ClientString(id = 1090, message = "Do you wish to use the facelifting potion ' Type B? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_FACELIFTING_POTION_TYPE_B_IT_IS_PERMANENT;
	
	@ClientString(id = 1091, message = "'Do you wish to use the facelifting potion ' Type C? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_FACELIFTING_POTION_TYPE_C_IT_IS_PERMANENT;
	
	@ClientString(id = 1092, message = "'Do you wish to use the dye potion ' Type B? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_DYE_POTION_TYPE_B_IT_IS_PERMANENT;
	
	@ClientString(id = 1093, message = "'Do you wish to use the dye potion ' Type C? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_DYE_POTION_TYPE_C_IT_IS_PERMANENT;
	
	@ClientString(id = 1094, message = "'Do you wish to use the dye potion ' Type D? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_DYE_POTION_TYPE_D_IT_IS_PERMANENT;
	
	@ClientString(id = 1095, message = "'Do you wish to use the hair style change potion ' Type B? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_B_IT_IS_PERMANENT;
	
	@ClientString(id = 1096, message = "'Do you wish to use the hair style change potion ' Type C? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_C_IT_IS_PERMANENT;
	
	@ClientString(id = 1097, message = "'Do you wish to use the hair style change potion ' Type D? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_D_IT_IS_PERMANENT;
	
	@ClientString(id = 1098, message = "'Do you wish to use the hair style change potion ' Type E? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_E_IT_IS_PERMANENT;
	
	@ClientString(id = 1099, message = "'Do you wish to use the hair style change potion ' Type F? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_F_IT_IS_PERMANENT;
	
	@ClientString(id = 1100, message = "'Do you wish to use the hair style change potion ' Type G? It is permanent.")
	public static SystemMessageId DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION_TYPE_G_IT_IS_PERMANENT;
	
	@ClientString(id = 1101, message = "'The facelifting potion - Type B is being used.")
	public static SystemMessageId THE_FACELIFTING_POTION_TYPE_B_IS_BEING_USED;
	
	@ClientString(id = 1102, message = "'The facelifting potion - Type C is being used.")
	public static SystemMessageId THE_FACELIFTING_POTION_TYPE_C_IS_BEING_USED;
	
	@ClientString(id = 1103, message = "'The dye potion - Type B is being used.")
	public static SystemMessageId THE_DYE_POTION_TYPE_B_IS_BEING_USED;
	
	@ClientString(id = 1104, message = "'The dye potion - Type C is being used.")
	public static SystemMessageId THE_DYE_POTION_TYPE_C_IS_BEING_USED;
	
	@ClientString(id = 1105, message = "'The dye potion - Type D is being used.")
	public static SystemMessageId THE_DYE_POTION_TYPE_D_IS_BEING_USED;
	
	@ClientString(id = 1106, message = "The hair style change potion - Type B is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_B_IS_BEING_USED;
	
	@ClientString(id = 1107, message = "'The hair style change potion - Type C is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_C_IS_BEING_USED;
	
	@ClientString(id = 1108, message = "'The hair style change potion - Type D is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_D_IS_BEING_USED;
	
	@ClientString(id = 1109, message = "'The hair style change potion - Type E is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_E_IS_BEING_USED;
	
	@ClientString(id = 1110, message = "'The hair style change potion - Type F is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_F_IS_BEING_USED;
	
	@ClientString(id = 1111, message = "'The hair style change potion - Type G is being used.")
	public static SystemMessageId THE_HAIR_STYLE_CHANGE_POTION_TYPE_G_IS_BEING_USED;
	
	@ClientString(id = 1112, message = "'The prize amount for the winner of Lottery #$s1 is $s2 adena. We have $s3 first prize winners.")
	public static SystemMessageId THE_PRIZE_AMOUNT_FOR_THE_WINNER_OF_LOTTERY_S1_IS_S2_ADENA_WE_HAVE_S3_FIRST_PRIZE_WINNERS;
	
	@ClientString(id = 1113, message = "The prize amount for Lucky Lottery #$s1 is $s2 adena. There was no first prize winner in this drawing, therefore the jackpot will be added to the next drawing.")
	public static SystemMessageId THE_PRIZE_AMOUNT_FOR_LUCKY_LOTTERY_S1_IS_S2_ADENA_THERE_WAS_NO_FIRST_PRIZE_WINNER_IN_THIS_DRAWING_THEREFORE_THE_JACKPOT_WILL_BE_ADDED_TO_THE_NEXT_DRAWING;
	
	@ClientString(id = 1114, message = "Your clan may not register to participate in a siege while under a grace period of the clan's dissolution.")
	public static SystemMessageId YOUR_CLAN_MAY_NOT_REGISTER_TO_PARTICIPATE_IN_A_SIEGE_WHILE_UNDER_A_GRACE_PERIOD_OF_THE_CLAN_S_DISSOLUTION;
	
	@ClientString(id = 1115, message = "Individuals may not surrender during combat.")
	public static SystemMessageId INDIVIDUALS_MAY_NOT_SURRENDER_DURING_COMBAT;
	
	@ClientString(id = 1116, message = "One cannot leave one's clan during combat.")
	public static SystemMessageId ONE_CANNOT_LEAVE_ONE_S_CLAN_DURING_COMBAT;
	
	@ClientString(id = 1117, message = "'A clan member may not be dismissed during combat.")
	public static SystemMessageId A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT;
	
	@ClientString(id = 1118, message = "Progress in a quest is possible only when your inventory's weight and volume are less than 80 percent of capacity.")
	public static SystemMessageId PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORY_S_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY;
	
	@ClientString(id = 1119, message = "Quest was automatically canceled when you attempted to settle the accounts of your quest while your inventory exceeded 80 percent of capacity.")
	public static SystemMessageId QUEST_WAS_AUTOMATICALLY_CANCELED_WHEN_YOU_ATTEMPTED_TO_SETTLE_THE_ACCOUNTS_OF_YOUR_QUEST_WHILE_YOUR_INVENTORY_EXCEEDED_80_PERCENT_OF_CAPACITY;
	
	@ClientString(id = 1120, message = "You are still in the clan.")
	public static SystemMessageId YOU_ARE_STILL_IN_THE_CLAN;
	
	@ClientString(id = 1121, message = "'You do not have the right to vote.")
	public static SystemMessageId YOU_DO_NOT_HAVE_THE_RIGHT_TO_VOTE;
	
	@ClientString(id = 1122, message = "There is no candidate.")
	public static SystemMessageId THERE_IS_NO_CANDIDATE;
	
	@ClientString(id = 1123, message = "Weight and volume limit has been exceeded. That skill is currently unavailable.")
	public static SystemMessageId WEIGHT_AND_VOLUME_LIMIT_HAS_BEEN_EXCEEDED_THAT_SKILL_IS_CURRENTLY_UNAVAILABLE;
	
	@ClientString(id = 1124, message = "A recipe book may not be used while using a skill.")
	public static SystemMessageId A_RECIPE_BOOK_MAY_NOT_BE_USED_WHILE_USING_A_SKILL;
	
	@ClientString(id = 1125, message = "An item may not be created while engaged in trading.")
	public static SystemMessageId AN_ITEM_MAY_NOT_BE_CREATED_WHILE_ENGAGED_IN_TRADING;
	
	@ClientString(id = 1126, message = "'You may not enter a negative number.")
	public static SystemMessageId YOU_MAY_NOT_ENTER_A_NEGATIVE_NUMBER;
	
	@ClientString(id = 1127, message = "The reward must be less than 10 times the standard price.")
	public static SystemMessageId THE_REWARD_MUST_BE_LESS_THAN_10_TIMES_THE_STANDARD_PRICE;
	
	@ClientString(id = 1128, message = "A private store may not be opened while using a skill.")
	public static SystemMessageId A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL;
	
	@ClientString(id = 1129, message = "'This is not allowed while using a ferry.")
	public static SystemMessageId THIS_IS_NOT_ALLOWED_WHILE_USING_A_FERRY;
	
	@ClientString(id = 1130, message = "'You have given $s1 damage to your target and $s2 damage to the servitor.")
	public static SystemMessageId YOU_HAVE_GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_THE_SERVITOR;
	
	@ClientString(id = 1131, message = "It is now midnight and the effect of $s1 can be felt.")
	public static SystemMessageId IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT;
	
	@ClientString(id = 1132, message = "It is dawn and the effect of $s1 will now disappear.")
	public static SystemMessageId IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR;
	
	@ClientString(id = 1133, message = "Since HP has decreased, the effect of $s1 can be felt.")
	public static SystemMessageId SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT;
	
	@ClientString(id = 1134, message = "Since HP has increased, the effect of $s1 will disappear.")
	public static SystemMessageId SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR;
	
	@ClientString(id = 1135, message = "While you are engaged in combat, you cannot operate a private store or private workshop.")
	public static SystemMessageId WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP;
	
	@ClientString(id = 1136, message = "Since there was an account that used this IP and attempted to log in illegally, this account is not allowed to connect to the game server for $s1 minutes. Please use another game server.")
	public static SystemMessageId SINCE_THERE_WAS_AN_ACCOUNT_THAT_USED_THIS_IP_AND_ATTEMPTED_TO_LOG_IN_ILLEGALLY_THIS_ACCOUNT_IS_NOT_ALLOWED_TO_CONNECT_TO_THE_GAME_SERVER_FOR_S1_MINUTES_PLEASE_USE_ANOTHER_GAME_SERVER;
	
	@ClientString(id = 1137, message = "$s1 harvested $s3 $s2(s).")
	public static SystemMessageId S1_HARVESTED_S3_S2_S;
	
	@ClientString(id = 1138, message = "$s1 harvested $s2(s).")
	public static SystemMessageId S1_HARVESTED_S2_S;
	
	@ClientString(id = 1139, message = "The weight and volume limit of inventory must not be exceeded.")
	public static SystemMessageId THE_WEIGHT_AND_VOLUME_LIMIT_OF_INVENTORY_MUST_NOT_BE_EXCEEDED;
	
	@ClientString(id = 1140, message = "Would you like to open the gate?")
	public static SystemMessageId WOULD_YOU_LIKE_TO_OPEN_THE_GATE;
	
	@ClientString(id = 1141, message = "Would you like to close the gate?")
	public static SystemMessageId WOULD_YOU_LIKE_TO_CLOSE_THE_GATE;
	
	@ClientString(id = 1142, message = "Since $s1 already exists nearby, you cannot summon it again.")
	public static SystemMessageId SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN;
	
	@ClientString(id = 1143, message = "Since you do not have enough items to maintain the servitor's stay, the servitor will disappear.")
	public static SystemMessageId SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITOR_S_STAY_THE_SERVITOR_WILL_DISAPPEAR;
	
	@ClientString(id = 1144, message = "Currently, you don't have anybody to chat with in the game.")
	public static SystemMessageId CURRENTLY_YOU_DON_T_HAVE_ANYBODY_TO_CHAT_WITH_IN_THE_GAME;
	
	@ClientString(id = 1145, message = "$s2 has been created for $s1 after the payment of $s3 adena is received.")
	public static SystemMessageId S2_HAS_BEEN_CREATED_FOR_S1_AFTER_THE_PAYMENT_OF_S3_ADENA_IS_RECEIVED;
	
	@ClientString(id = 1146, message = "$s1 created $s2 after receiving $s3 adena.")
	public static SystemMessageId S1_CREATED_S2_AFTER_RECEIVING_S3_ADENA;
	
	@ClientString(id = 1147, message = "$s2 $s3 have been created for $s1 at the price of $s4 adena.")
	public static SystemMessageId S2_S3_HAVE_BEEN_CREATED_FOR_S1_AT_THE_PRICE_OF_S4_ADENA;
	
	@ClientString(id = 1148, message = "$s1 created $s2 $s3 at the price of $s4 adena.")
	public static SystemMessageId S1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA;
	
	@ClientString(id = 1149, message = "The attempt to create $s2 for $s1 at the price of $s3 adena has failed.")
	public static SystemMessageId THE_ATTEMPT_TO_CREATE_S2_FOR_S1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED;
	
	@ClientString(id = 1150, message = "$s1 has failed to create $s2 at the price of $s3 adena.")
	public static SystemMessageId S1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA;
	
	@ClientString(id = 1151, message = "$s2 is sold to $s1 at the price of $s3 adena.")
	public static SystemMessageId S2_IS_SOLD_TO_S1_AT_THE_PRICE_OF_S3_ADENA;
	
	@ClientString(id = 1152, message = "$s2 $s3 have been sold to $s1 for $s4 adena.")
	public static SystemMessageId S2_S3_HAVE_BEEN_SOLD_TO_S1_FOR_S4_ADENA;
	
	@ClientString(id = 1153, message = "$s2 has been purchased from $s1 at the price of $s3 adena.")
	public static SystemMessageId S2_HAS_BEEN_PURCHASED_FROM_S1_AT_THE_PRICE_OF_S3_ADENA;
	
	@ClientString(id = 1154, message = "$s3 $s2 has been purchased from $s1 for $s4 adena.")
	public static SystemMessageId S3_S2_HAS_BEEN_PURCHASED_FROM_S1_FOR_S4_ADENA;
	
	@ClientString(id = 1155, message = "+$s2$s3 has been sold to $s1 at the price of $s4 adena.")
	public static SystemMessageId S2_S3_HAS_BEEN_SOLD_TO_S1_AT_THE_PRICE_OF_S4_ADENA;
	
	@ClientString(id = 1156, message = "+$s2$s3 has been purchased from $s1 at the price of $s4 adena.")
	public static SystemMessageId S2_S3_HAS_BEEN_PURCHASED_FROM_S1_AT_THE_PRICE_OF_S4_ADENA;
	
	@ClientString(id = 1157, message = "Trying on state lasts for only 5 seconds. When a character's state changes, it can be cancelled.")
	public static SystemMessageId TRYING_ON_STATE_LASTS_FOR_ONLY_5_SECONDS_WHEN_A_CHARACTER_S_STATE_CHANGES_IT_CAN_BE_CANCELLED;
	
	@ClientString(id = 1158, message = "You cannot get down from a place that is too high.")
	public static SystemMessageId YOU_CANNOT_GET_DOWN_FROM_A_PLACE_THAT_IS_TOO_HIGH;
	
	@ClientString(id = 1159, message = "The ferry from Talking Island will arrive at Gludin Harbor in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_ARRIVE_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1160, message = "The ferry from Talking Island will be arriving at Gludin Harbor in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1161, message = "The ferry from Talking Island will be arriving at Gludin Harbor in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1162, message = "The ferry from Giran Harbor will be arriving at Talking Island in approximately 15 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1163, message = "The ferry from Giran Harbor will be arriving at Talking Island in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1164, message = "The ferry from Giran Harbor will be arriving at Talking Island in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1165, message = "The ferry from Giran Harbor will be arriving at Talking Island in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1166, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_20_MINUTES;
	
	@ClientString(id = 1167, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 15 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1168, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1169, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1170, message = "The ferry from Talking Island will be arriving at Giran Harbor in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1171, message = "The Innadril pleasure boat will arrive in approximately 20 minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_20_MINUTES;
	
	@ClientString(id = 1172, message = "The Innadril pleasure boat will arrive in approximately 15 minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1173, message = "The Innadril pleasure boat will arrive in approximately 10 minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1174, message = "The Innadril pleasure boat will arrive in approximately 5 minutes.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1175, message = "The Innadril pleasure boat will arrive in approximately 1 minute.")
	public static SystemMessageId THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1176, message = "This is a quest event period.")
	public static SystemMessageId THIS_IS_A_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1177, message = "This is the seal validation period.")
	public static SystemMessageId THIS_IS_THE_SEAL_VALIDATION_PERIOD;
	
	@ClientString(id = 1178, message = "This seal permits the group that holds it to exclusively enter the dungeon opened by the Seal of Avarice during the seal validation period. It also permits trading with the Merchant of Mammon who appears in special dungeons and permits meetings with Anakim or Lilith in the Disciples Necropolis.")
	public static SystemMessageId THIS_SEAL_PERMITS_THE_GROUP_THAT_HOLDS_IT_TO_EXCLUSIVELY_ENTER_THE_DUNGEON_OPENED_BY_THE_SEAL_OF_AVARICE_DURING_THE_SEAL_VALIDATION_PERIOD_IT_ALSO_PERMITS_TRADING_WITH_THE_MERCHANT_OF_MAMMON_WHO_APPEARS_IN_SPECIAL_DUNGEONS_AND_PERMITS_MEETINGS_WITH_ANAKIM_OR_LILITH_IN_THE_DISCIPLES_NECROPOLIS;
	
	@ClientString(id = 1179, message = "This seal permits the group that holds it to enter the dungeon opened by the Seal of Gnosis, use the teleportation service offered by the priest in the village, and do business with the Merchant of Mammon. The Orator of Revelations appears and casts good magic on the winners, and the Preacher of Doom appears and casts bad magic on the losers.")
	public static SystemMessageId THIS_SEAL_PERMITS_THE_GROUP_THAT_HOLDS_IT_TO_ENTER_THE_DUNGEON_OPENED_BY_THE_SEAL_OF_GNOSIS_USE_THE_TELEPORTATION_SERVICE_OFFERED_BY_THE_PRIEST_IN_THE_VILLAGE_AND_DO_BUSINESS_WITH_THE_MERCHANT_OF_MAMMON_THE_ORATOR_OF_REVELATIONS_APPEARS_AND_CASTS_GOOD_MAGIC_ON_THE_WINNERS_AND_THE_PREACHER_OF_DOOM_APPEARS_AND_CASTS_BAD_MAGIC_ON_THE_LOSERS;
	
	@ClientString(id = 1180, message = "During the Seal Validation period, the costs of castle defense mercenaries and renovations, basic P. Def. of castle gates and castle walls and maximum tax rates will all change to favor the group of fighters that possesses this seal.")
	public static SystemMessageId DURING_THE_SEAL_VALIDATION_PERIOD_THE_COSTS_OF_CASTLE_DEFENSE_MERCENARIES_AND_RENOVATIONS_BASIC_P_DEF_OF_CASTLE_GATES_AND_CASTLE_WALLS_AND_MAXIMUM_TAX_RATES_WILL_ALL_CHANGE_TO_FAVOR_THE_GROUP_OF_FIGHTERS_THAT_POSSESSES_THIS_SEAL;
	
	@ClientString(id = 1181, message = "Do you really wish to change the title?")
	public static SystemMessageId DO_YOU_REALLY_WISH_TO_CHANGE_THE_TITLE;
	
	@ClientString(id = 1182, message = "Do you really wish to delete the clan crest?")
	public static SystemMessageId DO_YOU_REALLY_WISH_TO_DELETE_THE_CLAN_CREST;
	
	@ClientString(id = 1183, message = "This is the initial period.")
	public static SystemMessageId THIS_IS_THE_INITIAL_PERIOD;
	
	@ClientString(id = 1184, message = "This is a period of calculatiing statistics in the server.")
	public static SystemMessageId THIS_IS_A_PERIOD_OF_CALCULATIING_STATISTICS_IN_THE_SERVER;
	
	@ClientString(id = 1185, message = "days left until deletion.")
	public static SystemMessageId DAYS_LEFT_UNTIL_DELETION;
	
	@ClientString(id = 1186, message = "In order to open a new account, visit the official Lineage II website (http://www.lineage2.com) and sign up as a member under the 'My Account,' tab.")
	public static SystemMessageId IN_ORDER_TO_OPEN_A_NEW_ACCOUNT_VISIT_THE_OFFICIAL_LINEAGE_II_WEBSITE_HTTP_WWW_LINEAGE2_COM_AND_SIGN_UP_AS_A_MEMBER_UNDER_THE_MY_ACCOUNT_TAB;
	
	@ClientString(id = 1187, message = "If you have lost your account information, please visit the official Lineage II support website at http://support.plaync.com")
	public static SystemMessageId IF_YOU_HAVE_LOST_YOUR_ACCOUNT_INFORMATION_PLEASE_VISIT_THE_OFFICIAL_LINEAGE_II_SUPPORT_WEBSITE_AT_HTTP_SUPPORT_PLAYNC_COM;
	
	@ClientString(id = 1188, message = "Your selected target can no longer receive a recommendation.")
	public static SystemMessageId YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION;
	
	@ClientString(id = 1189, message = "The temporary alliance of the Castle Attacker team is in effect. It will be dissolved when the Castle Lord is replaced.")
	public static SystemMessageId THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT_IT_WILL_BE_DISSOLVED_WHEN_THE_CASTLE_LORD_IS_REPLACED;
	
	@ClientString(id = 1190, message = "The temporary alliance of the Castle Attacker team has been dissolved.")
	public static SystemMessageId THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED;
	
	@ClientString(id = 1191, message = "The ferry from Gludin Harbor will be arriving at Talking Island in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1192, message = "The ferry from Gludin Harbor will be arriving at Talking Island in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1193, message = "The ferry from Gludin Harbor will be arriving at Talking Island in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1194, message = "A mercenary can be assigned to a position from the beginning of the Seal Validation period until the time when a siege starts.")
	public static SystemMessageId A_MERCENARY_CAN_BE_ASSIGNED_TO_A_POSITION_FROM_THE_BEGINNING_OF_THE_SEAL_VALIDATION_PERIOD_UNTIL_THE_TIME_WHEN_A_SIEGE_STARTS;
	
	@ClientString(id = 1195, message = "This mercenary cannot be assigned to a position by using the Seal of Strife.")
	public static SystemMessageId THIS_MERCENARY_CANNOT_BE_ASSIGNED_TO_A_POSITION_BY_USING_THE_SEAL_OF_STRIFE;
	
	@ClientString(id = 1196, message = "Your force has reached maximum capacity.")
	public static SystemMessageId YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY;
	
	@ClientString(id = 1197, message = "Summoning a servitor costs $s2 $s1.")
	public static SystemMessageId SUMMONING_A_SERVITOR_COSTS_S2_S1;
	
	@ClientString(id = 1198, message = "The item has been successfully crystallized.")
	public static SystemMessageId THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED;
	
	@ClientString(id = 1199, message = "=======<Clan War Target>=======")
	public static SystemMessageId CLAN_WAR_TARGET;
	
	@ClientString(id = 1200, message = "=$s1 ($s2 alliance)")
	public static SystemMessageId S1_S2_ALLIANCE;
	
	@ClientString(id = 1201, message = "Please select the quest you wish to quit.")
	public static SystemMessageId PLEASE_SELECT_THE_QUEST_YOU_WISH_TO_QUIT;
	
	@ClientString(id = 1202, message = "=$s1 (No alliance exists.)")
	public static SystemMessageId S1_NO_ALLIANCE_EXISTS;
	
	@ClientString(id = 1203, message = "There is no clan war in progress.")
	public static SystemMessageId THERE_IS_NO_CLAN_WAR_IN_PROGRESS;
	
	@ClientString(id = 1204, message = "The screenshot has been saved. ($s1 $s2x$s3)")
	public static SystemMessageId THE_SCREENSHOT_HAS_BEEN_SAVED_S1_S2X_S3;
	
	@ClientString(id = 1205, message = "Mailbox is full.100 message maximum.")
	public static SystemMessageId MAILBOX_IS_FULL_100_MESSAGE_MAXIMUM;
	
	@ClientString(id = 1206, message = "Memo box is full. 100 memo maximum.")
	public static SystemMessageId MEMO_BOX_IS_FULL_100_MEMO_MAXIMUM;
	
	@ClientString(id = 1207, message = "Please make an entry in the field.")
	public static SystemMessageId PLEASE_MAKE_AN_ENTRY_IN_THE_FIELD;
	
	@ClientString(id = 1208, message = "$s1 died and dropped $s3 $s2.")
	public static SystemMessageId S1_DIED_AND_DROPPED_S3_S2;
	
	@ClientString(id = 1209, message = "Congratulations. Your raid was successful.")
	public static SystemMessageId CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL;
	
	@ClientString(id = 1210, message = "Seven Signs: The quest event period has begun. Visit a Priest of Dawn or Dusk to participate in the event.")
	public static SystemMessageId SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_VISIT_A_PRIEST_OF_DAWN_OR_DUSK_TO_PARTICIPATE_IN_THE_EVENT;
	
	@ClientString(id = 1211, message = "Seven Signs: The quest event period has ended. The next quest event will start in one week.")
	public static SystemMessageId SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_ENDED_THE_NEXT_QUEST_EVENT_WILL_START_IN_ONE_WEEK;
	
	@ClientString(id = 1212, message = "Seven Signs: The Lords of Dawn have obtained the Seal of Avarice.")
	public static SystemMessageId SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_AVARICE;
	
	@ClientString(id = 1213, message = "Seven Signs: The Lords of Dawn have obtained the Seal of Gnosis.")
	public static SystemMessageId SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS;
	
	@ClientString(id = 1214, message = "Seven Signs: The Lords of Dawn have obtained the Seal of Strife.")
	public static SystemMessageId SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_STRIFE;
	
	@ClientString(id = 1215, message = "Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Avarice.")
	public static SystemMessageId SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_AVARICE;
	
	@ClientString(id = 1216, message = "Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Gnosis.")
	public static SystemMessageId SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS;
	
	@ClientString(id = 1217, message = "Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Strife.")
	public static SystemMessageId SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_STRIFE;
	
	@ClientString(id = 1218, message = "Seven Signs: The Seal Validation period has begun.")
	public static SystemMessageId SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_BEGUN;
	
	@ClientString(id = 1219, message = "Seven Signs: The Seal Validation period has ended.")
	public static SystemMessageId SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_ENDED;
	
	@ClientString(id = 1220, message = "Are you sure you wish to summon it?")
	public static SystemMessageId ARE_YOU_SURE_YOU_WISH_TO_SUMMON_IT;
	
	@ClientString(id = 1221, message = "Do you really wish to return it?")
	public static SystemMessageId DO_YOU_REALLY_WISH_TO_RETURN_IT;
	
	@ClientString(id = 1222, message = "Current Location: $s1, $s2, $s3 (GM Consultation Service)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_GM_CONSULTATION_SERVICE;
	
	@ClientString(id = 1223, message = "We depart for Talking Island in five minutes.")
	public static SystemMessageId WE_DEPART_FOR_TALKING_ISLAND_IN_FIVE_MINUTES;
	
	@ClientString(id = 1224, message = "We depart for Talking Island in one minute.")
	public static SystemMessageId WE_DEPART_FOR_TALKING_ISLAND_IN_ONE_MINUTE;
	
	@ClientString(id = 1225, message = "All aboard for Talking Island!")
	public static SystemMessageId ALL_ABOARD_FOR_TALKING_ISLAND;
	
	@ClientString(id = 1226, message = "We are now leaving for Talking Island.")
	public static SystemMessageId WE_ARE_NOW_LEAVING_FOR_TALKING_ISLAND;
	
	@ClientString(id = 1227, message = "You have $s1 unread messages.")
	public static SystemMessageId YOU_HAVE_S1_UNREAD_MESSAGES;
	
	@ClientString(id = 1228, message = "$s1 has blocked you. You cannot send mail to $s1 .")
	public static SystemMessageId S1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_S1;
	
	@ClientString(id = 1229, message = "No more messages may be sent at this time. Each account is allowed 10 messages per day.")
	public static SystemMessageId NO_MORE_MESSAGES_MAY_BE_SENT_AT_THIS_TIME_EACH_ACCOUNT_IS_ALLOWED_10_MESSAGES_PER_DAY;
	
	@ClientString(id = 1230, message = "You are limited to five recipients at a time.")
	public static SystemMessageId YOU_ARE_LIMITED_TO_FIVE_RECIPIENTS_AT_A_TIME;
	
	@ClientString(id = 1231, message = "You've sent mail.")
	public static SystemMessageId YOU_VE_SENT_MAIL;
	
	@ClientString(id = 1232, message = "The message was not sent.")
	public static SystemMessageId THE_MESSAGE_WAS_NOT_SENT;
	
	@ClientString(id = 1233, message = "You've got mail.")
	public static SystemMessageId YOU_VE_GOT_MAIL;
	
	@ClientString(id = 1234, message = "The mail has been stored in your temporary mailbox.")
	public static SystemMessageId THE_MAIL_HAS_BEEN_STORED_IN_YOUR_TEMPORARY_MAILBOX;
	
	@ClientString(id = 1235, message = "Do you wish to delete all your friends?")
	public static SystemMessageId DO_YOU_WISH_TO_DELETE_ALL_YOUR_FRIENDS;
	
	@ClientString(id = 1236, message = "Please enter security card number.")
	public static SystemMessageId PLEASE_ENTER_SECURITY_CARD_NUMBER;
	
	@ClientString(id = 1237, message = "Please enter the card number for number $s1.")
	public static SystemMessageId PLEASE_ENTER_THE_CARD_NUMBER_FOR_NUMBER_S1;
	
	@ClientString(id = 1238, message = "Your temporary mailbox is full. No more mail can be stored. 10 message limit.")
	public static SystemMessageId YOUR_TEMPORARY_MAILBOX_IS_FULL_NO_MORE_MAIL_CAN_BE_STORED_10_MESSAGE_LIMIT;
	
	@ClientString(id = 1239, message = "Loading of the keyboard security module has failed. Please exit the game and reload.")
	public static SystemMessageId LOADING_OF_THE_KEYBOARD_SECURITY_MODULE_HAS_FAILED_PLEASE_EXIT_THE_GAME_AND_RELOAD;
	
	@ClientString(id = 1240, message = "Seven Signs: The Revolutionaries of Dusk have won.")
	public static SystemMessageId SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_WON;
	
	@ClientString(id = 1241, message = "Seven Signs: The Lords of Dawn have won.")
	public static SystemMessageId SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_WON;
	
	@ClientString(id = 1242, message = "Users who have not verified their age cannot log in between 10:00 p.m. and 6:00 a.m.")
	public static SystemMessageId USERS_WHO_HAVE_NOT_VERIFIED_THEIR_AGE_CANNOT_LOG_IN_BETWEEN_10_00_P_M_AND_6_00_A_M;
	
	@ClientString(id = 1243, message = "The security card number is invalid.")
	public static SystemMessageId THE_SECURITY_CARD_NUMBER_IS_INVALID;
	
	@ClientString(id = 1244, message = "Users who have not verified their age cannot log in between 10:00 p.m. and 6:00 a.m. Logging off.")
	public static SystemMessageId USERS_WHO_HAVE_NOT_VERIFIED_THEIR_AGE_CANNOT_LOG_IN_BETWEEN_10_00_P_M_AND_6_00_A_M_LOGGING_OFF;
	
	@ClientString(id = 1245, message = "You will be logged out in $s1 minutes.")
	public static SystemMessageId YOU_WILL_BE_LOGGED_OUT_IN_S1_MINUTES;
	
	@ClientString(id = 1246, message = "$s1 died and has dropped $s2 adena.")
	public static SystemMessageId S1_DIED_AND_HAS_DROPPED_S2_ADENA;
	
	@ClientString(id = 1247, message = "The corpse is too old. The skill cannot be used.")
	public static SystemMessageId THE_CORPSE_IS_TOO_OLD_THE_SKILL_CANNOT_BE_USED;
	
	@ClientString(id = 1248, message = "You are out of feed. Mount status canceled.")
	public static SystemMessageId YOU_ARE_OUT_OF_FEED_MOUNT_STATUS_CANCELED;
	
	@ClientString(id = 1249, message = "You may only ride a wyvern while you're riding a strider.")
	public static SystemMessageId YOU_MAY_ONLY_RIDE_A_WYVERN_WHILE_YOU_RE_RIDING_A_STRIDER;
	
	@ClientString(id = 1250, message = "Do you really want to surrender? If you surrender during an alliance war, your Exp will drop as much as when your character dies once.")
	public static SystemMessageId DO_YOU_REALLY_WANT_TO_SURRENDER_IF_YOU_SURRENDER_DURING_AN_ALLIANCE_WAR_YOUR_EXP_WILL_DROP_AS_MUCH_AS_WHEN_YOUR_CHARACTER_DIES_ONCE;
	
	@ClientString(id = 1251, message = "Are you sure you want to dismiss the alliance? If you use the /allydismiss command, you will not be able to accept another clan to your alliance for one day.")
	public static SystemMessageId ARE_YOU_SURE_YOU_WANT_TO_DISMISS_THE_ALLIANCE_IF_YOU_USE_THE_ALLYDISMISS_COMMAND_YOU_WILL_NOT_BE_ABLE_TO_ACCEPT_ANOTHER_CLAN_TO_YOUR_ALLIANCE_FOR_ONE_DAY;
	
	@ClientString(id = 1252, message = "Are you sure you want to surrender? Exp penalty will be the same as death.")
	public static SystemMessageId ARE_YOU_SURE_YOU_WANT_TO_SURRENDER_EXP_PENALTY_WILL_BE_THE_SAME_AS_DEATH;
	
	@ClientString(id = 1253, message = "Are you sure you want to surrender? Exp penalty will be the same as death and you will not be allowed to participate in clan war.")
	public static SystemMessageId ARE_YOU_SURE_YOU_WANT_TO_SURRENDER_EXP_PENALTY_WILL_BE_THE_SAME_AS_DEATH_AND_YOU_WILL_NOT_BE_ALLOWED_TO_PARTICIPATE_IN_CLAN_WAR;
	
	@ClientString(id = 1254, message = "Thank you for submitting feedback.")
	public static SystemMessageId THANK_YOU_FOR_SUBMITTING_FEEDBACK;
	
	@ClientString(id = 1255, message = "GM consultation has begun.")
	public static SystemMessageId GM_CONSULTATION_HAS_BEGUN;
	
	@ClientString(id = 1256, message = "Please write the name after the command.")
	public static SystemMessageId PLEASE_WRITE_THE_NAME_AFTER_THE_COMMAND;
	
	@ClientString(id = 1257, message = "The special skill of a servitor or pet cannot be registered as a macro.")
	public static SystemMessageId THE_SPECIAL_SKILL_OF_A_SERVITOR_OR_PET_CANNOT_BE_REGISTERED_AS_A_MACRO;
	
	@ClientString(id = 1258, message = "$s1 has been crystallized.")
	public static SystemMessageId S1_HAS_BEEN_CRYSTALLIZED;
	
	@ClientString(id = 1259, message = "=======<Alliance Target>=======")
	public static SystemMessageId ALLIANCE_TARGET;
	
	@ClientString(id = 1260, message = "Seven Signs: Preparations have begun for the next quest event.")
	public static SystemMessageId SEVEN_SIGNS_PREPARATIONS_HAVE_BEGUN_FOR_THE_NEXT_QUEST_EVENT;
	
	@ClientString(id = 1261, message = "Seven Signs: The quest event period has begun. Speak with a Priest of Dawn or Dusk Priestess if you wish to participate in the event.")
	public static SystemMessageId SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_SPEAK_WITH_A_PRIEST_OF_DAWN_OR_DUSK_PRIESTESS_IF_YOU_WISH_TO_PARTICIPATE_IN_THE_EVENT;
	
	@ClientString(id = 1262, message = "Seven Signs: Quest event has ended. Results are being tallied.")
	public static SystemMessageId SEVEN_SIGNS_QUEST_EVENT_HAS_ENDED_RESULTS_ARE_BEING_TALLIED;
	
	@ClientString(id = 1263, message = "Seven Signs: This is the seal validation period. A new quest event period begins next Monday.")
	public static SystemMessageId SEVEN_SIGNS_THIS_IS_THE_SEAL_VALIDATION_PERIOD_A_NEW_QUEST_EVENT_PERIOD_BEGINS_NEXT_MONDAY;
	
	@ClientString(id = 1264, message = "This soul stone cannot currently absorb souls. Absorption has failed.")
	public static SystemMessageId THIS_SOUL_STONE_CANNOT_CURRENTLY_ABSORB_SOULS_ABSORPTION_HAS_FAILED;
	
	@ClientString(id = 1265, message = "You can't absorb souls without a soul stone.")
	public static SystemMessageId YOU_CAN_T_ABSORB_SOULS_WITHOUT_A_SOUL_STONE;
	
	@ClientString(id = 1266, message = "The exchange has ended.")
	public static SystemMessageId THE_EXCHANGE_HAS_ENDED;
	
	@ClientString(id = 1267, message = "Your contribution score is increased by $s1.")
	public static SystemMessageId YOUR_CONTRIBUTION_SCORE_IS_INCREASED_BY_S1;
	
	@ClientString(id = 1268, message = "Do you wish to add $s1 class as your sub class?")
	public static SystemMessageId DO_YOU_WISH_TO_ADD_S1_CLASS_AS_YOUR_SUB_CLASS;
	
	@ClientString(id = 1269, message = "The new sub class has been added.")
	public static SystemMessageId THE_NEW_SUB_CLASS_HAS_BEEN_ADDED;
	
	@ClientString(id = 1270, message = "The transfer of sub class has been completed.")
	public static SystemMessageId THE_TRANSFER_OF_SUB_CLASS_HAS_BEEN_COMPLETED;
	
	@ClientString(id = 1271, message = "Do you wish to participate? Until the next seal validation period, you are a member of the Lords of Dawn.")
	public static SystemMessageId DO_YOU_WISH_TO_PARTICIPATE_UNTIL_THE_NEXT_SEAL_VALIDATION_PERIOD_YOU_ARE_A_MEMBER_OF_THE_LORDS_OF_DAWN;
	
	@ClientString(id = 1272, message = "Do you wish to participate? Until the next seal validation period, you are a member of the Revolutionaries of Dusk.")
	public static SystemMessageId DO_YOU_WISH_TO_PARTICIPATE_UNTIL_THE_NEXT_SEAL_VALIDATION_PERIOD_YOU_ARE_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK;
	
	@ClientString(id = 1273, message = "You will participate in the Seven Signs as a member of the Lords of Dawn.")
	public static SystemMessageId YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_LORDS_OF_DAWN;
	
	@ClientString(id = 1274, message = "You will participate in the Seven Signs as a member of the Revolutionaries of Dusk.")
	public static SystemMessageId YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK;
	
	@ClientString(id = 1275, message = "You've chosen to fight for the Seal of Avarice during this quest event period.")
	public static SystemMessageId YOU_VE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_AVARICE_DURING_THIS_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1276, message = "You've chosen to fight for the Seal of Gnosis during this quest event period.")
	public static SystemMessageId YOU_VE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_GNOSIS_DURING_THIS_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1277, message = "You've chosen to fight for the Seal of Strife during this quest event period.")
	public static SystemMessageId YOU_VE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_STRIFE_DURING_THIS_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1278, message = "The NPC server is not operating.")
	public static SystemMessageId THE_NPC_SERVER_IS_NOT_OPERATING;
	
	@ClientString(id = 1279, message = "Contribution level has exceeded the limit. You may not continue.")
	public static SystemMessageId CONTRIBUTION_LEVEL_HAS_EXCEEDED_THE_LIMIT_YOU_MAY_NOT_CONTINUE;
	
	@ClientString(id = 1280, message = "Magic Critical Hit!")
	public static SystemMessageId MAGIC_CRITICAL_HIT;
	
	@ClientString(id = 1281, message = "Your excellent shield defense was a success!")
	public static SystemMessageId YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS;
	
	@ClientString(id = 1282, message = "Your Karma has been changed to $s1.")
	public static SystemMessageId YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1;
	
	@ClientString(id = 1283, message = "The minimum frame option has been activated.")
	public static SystemMessageId THE_MINIMUM_FRAME_OPTION_HAS_BEEN_ACTIVATED;
	
	@ClientString(id = 1284, message = "The minimum frame option has been deactivated.")
	public static SystemMessageId THE_MINIMUM_FRAME_OPTION_HAS_BEEN_DEACTIVATED;
	
	@ClientString(id = 1285, message = "No inventory exists. You cannot purchase an item.")
	public static SystemMessageId NO_INVENTORY_EXISTS_YOU_CANNOT_PURCHASE_AN_ITEM;
	
	@ClientString(id = 1286, message = "(Until next Monday at 6:00 p.m.)")
	public static SystemMessageId UNTIL_NEXT_MONDAY_AT_6_00_P_M;
	
	@ClientString(id = 1287, message = "(Until today at 6:00 p.m.)")
	public static SystemMessageId UNTIL_TODAY_AT_6_00_P_M;
	
	@ClientString(id = 1288, message = "If trends continue, $s1 will win and the seal will belong to:")
	public static SystemMessageId IF_TRENDS_CONTINUE_S1_WILL_WIN_AND_THE_SEAL_WILL_BELONG_TO;
	
	@ClientString(id = 1289, message = "Since the seal was owned during the previous period and 10 percent or more people have voted.")
	public static SystemMessageId SINCE_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_10_PERCENT_OR_MORE_PEOPLE_HAVE_VOTED;
	
	@ClientString(id = 1290, message = "Although the seal was not owned, since 35 percent or more people have voted.")
	public static SystemMessageId ALTHOUGH_THE_SEAL_WAS_NOT_OWNED_SINCE_35_PERCENT_OR_MORE_PEOPLE_HAVE_VOTED;
	
	@ClientString(id = 1291, message = "Although the seal was owned during the previous period, because less than 10 percent of people have voted.")
	public static SystemMessageId ALTHOUGH_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_BECAUSE_LESS_THAN_10_PERCENT_OF_PEOPLE_HAVE_VOTED;
	
	@ClientString(id = 1292, message = "Since the seal was not owned during the previous period, and since less than 35 percent of people have voted.")
	public static SystemMessageId SINCE_THE_SEAL_WAS_NOT_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_SINCE_LESS_THAN_35_PERCENT_OF_PEOPLE_HAVE_VOTED;
	
	@ClientString(id = 1293, message = "If current trends continue, it will end in a tie.")
	public static SystemMessageId IF_CURRENT_TRENDS_CONTINUE_IT_WILL_END_IN_A_TIE;
	
	@ClientString(id = 1294, message = "Since the competition has ended in a tie, the seal will not be awarded.")
	public static SystemMessageId SINCE_THE_COMPETITION_HAS_ENDED_IN_A_TIE_THE_SEAL_WILL_NOT_BE_AWARDED;
	
	@ClientString(id = 1295, message = "Sub classes may not be created or changed while a skill is in use.")
	public static SystemMessageId SUB_CLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE;
	
	@ClientString(id = 1296, message = "A private store may not be opened in this area.")
	public static SystemMessageId A_PRIVATE_STORE_MAY_NOT_BE_OPENED_IN_THIS_AREA;
	
	@ClientString(id = 1297, message = "A private workshop may not be opened in this area.")
	public static SystemMessageId A_PRIVATE_WORKSHOP_MAY_NOT_BE_OPENED_IN_THIS_AREA;
	
	@ClientString(id = 1298, message = "Exiting the monster race track.")
	public static SystemMessageId EXITING_THE_MONSTER_RACE_TRACK;
	
	@ClientString(id = 1299, message = "$s1's casting has been interrupted.")
	public static SystemMessageId S1_S_CASTING_HAS_BEEN_INTERRUPTED;
	
	@ClientString(id = 1300, message = "Trying-on mode canceled.")
	public static SystemMessageId TRYING_ON_MODE_CANCELED;
	
	@ClientString(id = 1301, message = "Can be used only by the Lords of Dawn.")
	public static SystemMessageId CAN_BE_USED_ONLY_BY_THE_LORDS_OF_DAWN;
	
	@ClientString(id = 1302, message = "Can be used only by the Revolutionaries of Dusk.")
	public static SystemMessageId CAN_BE_USED_ONLY_BY_THE_REVOLUTIONARIES_OF_DUSK;
	
	@ClientString(id = 1303, message = "Used only during a quest event period.")
	public static SystemMessageId USED_ONLY_DURING_A_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1304, message = "Due to the influence of the Seal of Strife, all defensive registration has been canceled except by alliances of castle-owning clans.")
	public static SystemMessageId DUE_TO_THE_INFLUENCE_OF_THE_SEAL_OF_STRIFE_ALL_DEFENSIVE_REGISTRATION_HAS_BEEN_CANCELED_EXCEPT_BY_ALLIANCES_OF_CASTLE_OWNING_CLANS;
	
	@ClientString(id = 1305, message = "You may give someone else a seal stone for safekeeping only during a quest event period.")
	public static SystemMessageId YOU_MAY_GIVE_SOMEONE_ELSE_A_SEAL_STONE_FOR_SAFEKEEPING_ONLY_DURING_A_QUEST_EVENT_PERIOD;
	
	@ClientString(id = 1306, message = "Trying-on mode has ended.")
	public static SystemMessageId TRYING_ON_MODE_HAS_ENDED;
	
	@ClientString(id = 1307, message = "Accounts may only be settled during the seal validation period.")
	public static SystemMessageId ACCOUNTS_MAY_ONLY_BE_SETTLED_DURING_THE_SEAL_VALIDATION_PERIOD;
	
	@ClientString(id = 1308, message = "Congratulations!! You have transferred to a new class.")
	public static SystemMessageId CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS;
	
	@ClientString(id = 1309, message = "This option requires that the latest version of MSN Messenger client be installed on your computer.")
	public static SystemMessageId THIS_OPTION_REQUIRES_THAT_THE_LATEST_VERSION_OF_MSN_MESSENGER_CLIENT_BE_INSTALLED_ON_YOUR_COMPUTER;
	
	@ClientString(id = 1310, message = "For full functionality, the latest version of MSN Messenger client must be installed on the user's computer.")
	public static SystemMessageId FOR_FULL_FUNCTIONALITY_THE_LATEST_VERSION_OF_MSN_MESSENGER_CLIENT_MUST_BE_INSTALLED_ON_THE_USER_S_COMPUTER;
	
	@ClientString(id = 1311, message = "Previous versions of MSN Messenger only provide the basic features to chat in the game. Add/Delete Contacts and other options aren't available.")
	public static SystemMessageId PREVIOUS_VERSIONS_OF_MSN_MESSENGER_ONLY_PROVIDE_THE_BASIC_FEATURES_TO_CHAT_IN_THE_GAME_ADD_DELETE_CONTACTS_AND_OTHER_OPTIONS_AREN_T_AVAILABLE;
	
	@ClientString(id = 1312, message = "The latest version of MSN Messenger may be obtained from the MSN web site (http://messenger.msn.com).")
	public static SystemMessageId THE_LATEST_VERSION_OF_MSN_MESSENGER_MAY_BE_OBTAINED_FROM_THE_MSN_WEB_SITE_HTTP_MESSENGER_MSN_COM;
	
	@ClientString(id = 1313, message = "$s1. To better serve our customers, all chat histories are stored and maintained by NCsoft. If you do not agree to have your chat records stored, close the chat window now. For more information regarding this issue, please visit our home page at www.ncsoft.com.")
	public static SystemMessageId S1_TO_BETTER_SERVE_OUR_CUSTOMERS_ALL_CHAT_HISTORIES_ARE_STORED_AND_MAINTAINED_BY_NCSOFT_IF_YOU_DO_NOT_AGREE_TO_HAVE_YOUR_CHAT_RECORDS_STORED_CLOSE_THE_CHAT_WINDOW_NOW_FOR_MORE_INFORMATION_REGARDING_THIS_ISSUE_PLEASE_VISIT_OUR_HOME_PAGE_AT_WWW_NCSOFT_COM;
	
	@ClientString(id = 1314, message = "Please enter the passport ID of the person you wish to add to your contact list.")
	public static SystemMessageId PLEASE_ENTER_THE_PASSPORT_ID_OF_THE_PERSON_YOU_WISH_TO_ADD_TO_YOUR_CONTACT_LIST;
	
	@ClientString(id = 1315, message = "Deleting a contact will remove that contact from MSN Messenger as well. The contact can still check your online status and will not be blocked from sending you a message.")
	public static SystemMessageId DELETING_A_CONTACT_WILL_REMOVE_THAT_CONTACT_FROM_MSN_MESSENGER_AS_WELL_THE_CONTACT_CAN_STILL_CHECK_YOUR_ONLINE_STATUS_AND_WILL_NOT_BE_BLOCKED_FROM_SENDING_YOU_A_MESSAGE;
	
	@ClientString(id = 1316, message = "The contact will be deleted and blocked from your contact list.")
	public static SystemMessageId THE_CONTACT_WILL_BE_DELETED_AND_BLOCKED_FROM_YOUR_CONTACT_LIST;
	
	@ClientString(id = 1317, message = "Would you like to delete this contact?")
	public static SystemMessageId WOULD_YOU_LIKE_TO_DELETE_THIS_CONTACT;
	
	@ClientString(id = 1318, message = "Please select the contact you want to block or unblock.")
	public static SystemMessageId PLEASE_SELECT_THE_CONTACT_YOU_WANT_TO_BLOCK_OR_UNBLOCK;
	
	@ClientString(id = 1319, message = "Please select the name of the contact you wish to change to another group.")
	public static SystemMessageId PLEASE_SELECT_THE_NAME_OF_THE_CONTACT_YOU_WISH_TO_CHANGE_TO_ANOTHER_GROUP;
	
	@ClientString(id = 1320, message = "After selecting the group you wish to move your contact to, press the OK button.")
	public static SystemMessageId AFTER_SELECTING_THE_GROUP_YOU_WISH_TO_MOVE_YOUR_CONTACT_TO_PRESS_THE_OK_BUTTON;
	
	@ClientString(id = 1321, message = "Enter the name of the group you wish to add.")
	public static SystemMessageId ENTER_THE_NAME_OF_THE_GROUP_YOU_WISH_TO_ADD;
	
	@ClientString(id = 1322, message = "Select the group and enter the new name.")
	public static SystemMessageId SELECT_THE_GROUP_AND_ENTER_THE_NEW_NAME;
	
	@ClientString(id = 1323, message = "Select the group you wish to delete and click the OK button.")
	public static SystemMessageId SELECT_THE_GROUP_YOU_WISH_TO_DELETE_AND_CLICK_THE_OK_BUTTON;
	
	@ClientString(id = 1324, message = "Signing in...")
	public static SystemMessageId SIGNING_IN;
	
	@ClientString(id = 1325, message = "You've logged into another computer and been logged out of the NET Messenger Service on this computer.")
	public static SystemMessageId YOU_VE_LOGGED_INTO_ANOTHER_COMPUTER_AND_BEEN_LOGGED_OUT_OF_THE_NET_MESSENGER_SERVICE_ON_THIS_COMPUTER;
	
	@ClientString(id = 1326, message = "$s1:")
	public static SystemMessageId S1;
	
	@ClientString(id = 1327, message = "The following message could not be delivered:")
	public static SystemMessageId THE_FOLLOWING_MESSAGE_COULD_NOT_BE_DELIVERED;
	
	@ClientString(id = 1328, message = "Members of the Revolutionaries of Dusk will not be resurrected.")
	public static SystemMessageId MEMBERS_OF_THE_REVOLUTIONARIES_OF_DUSK_WILL_NOT_BE_RESURRECTED;
	
	@ClientString(id = 1329, message = "You are currently banned from activities related to the Private Store and Private Workshop.")
	public static SystemMessageId YOU_ARE_CURRENTLY_BANNED_FROM_ACTIVITIES_RELATED_TO_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP;
	
	@ClientString(id = 1330, message = "No Private Store or Private Workshop may be opened for $s1 minutes.")
	public static SystemMessageId NO_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_MAY_BE_OPENED_FOR_S1_MINUTES;
	
	@ClientString(id = 1331, message = "Activities related to the Private Store and Private Workshop are now permitted.")
	public static SystemMessageId ACTIVITIES_RELATED_TO_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP_ARE_NOW_PERMITTED;
	
	@ClientString(id = 1332, message = "Items may not be used after your character or pet dies.")
	public static SystemMessageId ITEMS_MAY_NOT_BE_USED_AFTER_YOUR_CHARACTER_OR_PET_DIES;
	
	@ClientString(id = 1333, message = "Replay file isn't accessible. Verify that Replay.ini file exists.")
	public static SystemMessageId REPLAY_FILE_ISN_T_ACCESSIBLE_VERIFY_THAT_REPLAY_INI_FILE_EXISTS;
	
	@ClientString(id = 1334, message = "The new camera data has been stored.")
	public static SystemMessageId THE_NEW_CAMERA_DATA_HAS_BEEN_STORED;
	
	@ClientString(id = 1335, message = "The attempt to store the new camera data has failed.")
	public static SystemMessageId THE_ATTEMPT_TO_STORE_THE_NEW_CAMERA_DATA_HAS_FAILED;
	
	@ClientString(id = 1336, message = "The Replay file has been corrupted. Please check the $s1.$s2 file.")
	public static SystemMessageId THE_REPLAY_FILE_HAS_BEEN_CORRUPTED_PLEASE_CHECK_THE_S1_S2_FILE;
	
	@ClientString(id = 1337, message = "Replay mode will be terminated. Do you wish to continue?")
	public static SystemMessageId REPLAY_MODE_WILL_BE_TERMINATED_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 1338, message = "You have exceeded the quantity that can be transferred at one time.")
	public static SystemMessageId YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_TRANSFERRED_AT_ONE_TIME;
	
	@ClientString(id = 1339, message = "Once a macro is assigned to a shortcut, it cannot be run as a macro again.")
	public static SystemMessageId ONCE_A_MACRO_IS_ASSIGNED_TO_A_SHORTCUT_IT_CANNOT_BE_RUN_AS_A_MACRO_AGAIN;
	
	@ClientString(id = 1340, message = "This server cannot be accessed by the coupon you are using.")
	public static SystemMessageId THIS_SERVER_CANNOT_BE_ACCESSED_BY_THE_COUPON_YOU_ARE_USING;
	
	@ClientString(id = 1341, message = "The name or e-mail address you entered is incorrect.")
	public static SystemMessageId THE_NAME_OR_E_MAIL_ADDRESS_YOU_ENTERED_IS_INCORRECT;
	
	@ClientString(id = 1342, message = "You are already logged in.")
	public static SystemMessageId YOU_ARE_ALREADY_LOGGED_IN;
	
	@ClientString(id = 1343, message = "The password or e-mail address you entered is incorrect. Your attempt to log into .NET Messenger Service has failed.")
	public static SystemMessageId THE_PASSWORD_OR_E_MAIL_ADDRESS_YOU_ENTERED_IS_INCORRECT_YOUR_ATTEMPT_TO_LOG_INTO_NET_MESSENGER_SERVICE_HAS_FAILED;
	
	@ClientString(id = 1344, message = "The service you requested could not be located and therefore your attempt to log into the .NET Messenger Service has failed. Please verify that you are currently connected to the Internet.")
	public static SystemMessageId THE_SERVICE_YOU_REQUESTED_COULD_NOT_BE_LOCATED_AND_THEREFORE_YOUR_ATTEMPT_TO_LOG_INTO_THE_NET_MESSENGER_SERVICE_HAS_FAILED_PLEASE_VERIFY_THAT_YOU_ARE_CURRENTLY_CONNECTED_TO_THE_INTERNET;
	
	@ClientString(id = 1345, message = "After selecting a contact name, click on the OK button.")
	public static SystemMessageId AFTER_SELECTING_A_CONTACT_NAME_CLICK_ON_THE_OK_BUTTON;
	
	@ClientString(id = 1346, message = "You are currently entering a chat message.")
	public static SystemMessageId YOU_ARE_CURRENTLY_ENTERING_A_CHAT_MESSAGE;
	
	@ClientString(id = 1347, message = "The Lineage II messenger could not carry out the task you requested.")
	public static SystemMessageId THE_LINEAGE_II_MESSENGER_COULD_NOT_CARRY_OUT_THE_TASK_YOU_REQUESTED;
	
	@ClientString(id = 1348, message = "$s1 has entered the chat room.")
	public static SystemMessageId S1_HAS_ENTERED_THE_CHAT_ROOM;
	
	@ClientString(id = 1349, message = "$s1 has left the chat room.")
	public static SystemMessageId S1_HAS_LEFT_THE_CHAT_ROOM;
	
	@ClientString(id = 1350, message = "The status will be changed to indicate 'off-line.' All the chat windows currently opened will be closed.")
	public static SystemMessageId THE_STATUS_WILL_BE_CHANGED_TO_INDICATE_OFF_LINE_ALL_THE_CHAT_WINDOWS_CURRENTLY_OPENED_WILL_BE_CLOSED;
	
	@ClientString(id = 1351, message = "After selecting the contact you want to delete, click the Delete button.")
	public static SystemMessageId AFTER_SELECTING_THE_CONTACT_YOU_WANT_TO_DELETE_CLICK_THE_DELETE_BUTTON;
	
	@ClientString(id = 1352, message = "You have been added to the contact list of $s1 ($s2).")
	public static SystemMessageId YOU_HAVE_BEEN_ADDED_TO_THE_CONTACT_LIST_OF_S1_S2;
	
	@ClientString(id = 1353, message = "You can set the option to show your status as always being off-line to all of your contacts.")
	public static SystemMessageId YOU_CAN_SET_THE_OPTION_TO_SHOW_YOUR_STATUS_AS_ALWAYS_BEING_OFF_LINE_TO_ALL_OF_YOUR_CONTACTS;
	
	@ClientString(id = 1354, message = "You are not allowed to chat with your contact while you are blocked from chatting.")
	public static SystemMessageId YOU_ARE_NOT_ALLOWED_TO_CHAT_WITH_YOUR_CONTACT_WHILE_YOU_ARE_BLOCKED_FROM_CHATTING;
	
	@ClientString(id = 1355, message = "The contact you chose to chat with is currently blocked from chatting.")
	public static SystemMessageId THE_CONTACT_YOU_CHOSE_TO_CHAT_WITH_IS_CURRENTLY_BLOCKED_FROM_CHATTING;
	
	@ClientString(id = 1356, message = "The contact you chose to chat with is not currently logged in.")
	public static SystemMessageId THE_CONTACT_YOU_CHOSE_TO_CHAT_WITH_IS_NOT_CURRENTLY_LOGGED_IN;
	
	@ClientString(id = 1357, message = "You have been blocked from the contact you selected.")
	public static SystemMessageId YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED;
	
	@ClientString(id = 1358, message = "You are being logged out...")
	public static SystemMessageId YOU_ARE_BEING_LOGGED_OUT;
	
	@ClientString(id = 1359, message = "$s1 has logged in.")
	public static SystemMessageId S1_HAS_LOGGED_IN_2;
	
	@ClientString(id = 1360, message = "You have received a message from $s1.")
	public static SystemMessageId YOU_HAVE_RECEIVED_A_MESSAGE_FROM_S1;
	
	@ClientString(id = 1361, message = "Due to a system error, you have been logged out of the .NET Messenger Service.")
	public static SystemMessageId DUE_TO_A_SYSTEM_ERROR_YOU_HAVE_BEEN_LOGGED_OUT_OF_THE_NET_MESSENGER_SERVICE;
	
	@ClientString(id = 1362, message = "Please select the contact you wish to delete. If you would like to delete a group, click the button next to My Status, and then use the Options menu.")
	public static SystemMessageId PLEASE_SELECT_THE_CONTACT_YOU_WISH_TO_DELETE_IF_YOU_WOULD_LIKE_TO_DELETE_A_GROUP_CLICK_THE_BUTTON_NEXT_TO_MY_STATUS_AND_THEN_USE_THE_OPTIONS_MENU;
	
	@ClientString(id = 1363, message = "Your request to participate in the alliance war has been denied.")
	public static SystemMessageId YOUR_REQUEST_TO_PARTICIPATE_IN_THE_ALLIANCE_WAR_HAS_BEEN_DENIED;
	
	@ClientString(id = 1364, message = "The request for an alliance war has been rejected.")
	public static SystemMessageId THE_REQUEST_FOR_AN_ALLIANCE_WAR_HAS_BEEN_REJECTED;
	
	@ClientString(id = 1365, message = "$s2 of $s1 clan has surrendered as an individual.")
	public static SystemMessageId S2_OF_S1_CLAN_HAS_SURRENDERED_AS_AN_INDIVIDUAL;
	
	@ClientString(id = 1366, message = "You can delete a group only when you do not have any contact in that group. In order to delete a group, first transfer your contact(s) in that group to another group.")
	public static SystemMessageId YOU_CAN_DELETE_A_GROUP_ONLY_WHEN_YOU_DO_NOT_HAVE_ANY_CONTACT_IN_THAT_GROUP_IN_ORDER_TO_DELETE_A_GROUP_FIRST_TRANSFER_YOUR_CONTACT_S_IN_THAT_GROUP_TO_ANOTHER_GROUP;
	
	@ClientString(id = 1367, message = "Only members of the group are allowed to add records.")
	public static SystemMessageId ONLY_MEMBERS_OF_THE_GROUP_ARE_ALLOWED_TO_ADD_RECORDS;
	
	@ClientString(id = 1368, message = "Those items may not be tried on simultaneously.")
	public static SystemMessageId THOSE_ITEMS_MAY_NOT_BE_TRIED_ON_SIMULTANEOUSLY;
	
	@ClientString(id = 1369, message = "You've exceeded the maximum.")
	public static SystemMessageId YOU_VE_EXCEEDED_THE_MAXIMUM;
	
	@ClientString(id = 1370, message = "You cannot send mail to a GM such as $s1.")
	public static SystemMessageId YOU_CANNOT_SEND_MAIL_TO_A_GM_SUCH_AS_S1;
	
	@ClientString(id = 1371, message = "It has been determined that you're not engaged in normal gameplay and a restriction has been imposed upon you. You may not move for $s1 minutes.")
	public static SystemMessageId IT_HAS_BEEN_DETERMINED_THAT_YOU_RE_NOT_ENGAGED_IN_NORMAL_GAMEPLAY_AND_A_RESTRICTION_HAS_BEEN_IMPOSED_UPON_YOU_YOU_MAY_NOT_MOVE_FOR_S1_MINUTES;
	
	@ClientString(id = 1372, message = "Your punishment will continue for $s1 minutes.")
	public static SystemMessageId YOUR_PUNISHMENT_WILL_CONTINUE_FOR_S1_MINUTES;
	
	@ClientString(id = 1373, message = "$s1 has picked up $s2 that was dropped by a Raid Boss.")
	public static SystemMessageId S1_HAS_PICKED_UP_S2_THAT_WAS_DROPPED_BY_A_RAID_BOSS;
	
	@ClientString(id = 1374, message = "$s1 has picked up $s3 $s2(s) that was dropped by a Raid Boss.")
	public static SystemMessageId S1_HAS_PICKED_UP_S3_S2_S_THAT_WAS_DROPPED_BY_A_RAID_BOSS;
	
	@ClientString(id = 1375, message = "$s1 has picked up $s2 adena that was dropped by a Raid Boss.")
	public static SystemMessageId S1_HAS_PICKED_UP_S2_ADENA_THAT_WAS_DROPPED_BY_A_RAID_BOSS;
	
	@ClientString(id = 1376, message = "$s1 has picked up $s2 that was dropped by another character.")
	public static SystemMessageId S1_HAS_PICKED_UP_S2_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER;
	
	@ClientString(id = 1377, message = "$s1 has picked up $s3 $s2(s) that was dropped by another character.")
	public static SystemMessageId S1_HAS_PICKED_UP_S3_S2_S_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER;
	
	@ClientString(id = 1378, message = "$s1 has picked up +$s3$s2 that was dropped by another character.")
	public static SystemMessageId S1_HAS_PICKED_UP_S3_S2_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER;
	
	@ClientString(id = 1379, message = "$s1 has obtained $s2 adena.")
	public static SystemMessageId S1_HAS_OBTAINED_S2_ADENA;
	
	@ClientString(id = 1380, message = "You can't summon a $s1 while on the battleground.")
	public static SystemMessageId YOU_CAN_T_SUMMON_A_S1_WHILE_ON_THE_BATTLEGROUND;
	
	@ClientString(id = 1381, message = "The party leader has obtained $s2 of $s1.")
	public static SystemMessageId THE_PARTY_LEADER_HAS_OBTAINED_S2_OF_S1;
	
	@ClientString(id = 1382, message = "Are you sure you want to choose this weapon? To fulfill the quest, you must bring the chosen weapon.")
	public static SystemMessageId ARE_YOU_SURE_YOU_WANT_TO_CHOOSE_THIS_WEAPON_TO_FULFILL_THE_QUEST_YOU_MUST_BRING_THE_CHOSEN_WEAPON;
	
	@ClientString(id = 1383, message = "Are you sure you want to exchange?")
	public static SystemMessageId ARE_YOU_SURE_YOU_WANT_TO_EXCHANGE;
	
	@ClientString(id = 1384, message = "$s1 has become a party leader.")
	public static SystemMessageId S1_HAS_BECOME_A_PARTY_LEADER;
	
	@ClientString(id = 1385, message = "You're not allowed to dismount here.")
	public static SystemMessageId YOU_RE_NOT_ALLOWED_TO_DISMOUNT_HERE;
	
	@ClientString(id = 1386, message = "Hold state has been lifted.")
	public static SystemMessageId HOLD_STATE_HAS_BEEN_LIFTED;
	
	@ClientString(id = 1387, message = "Please select the item you would like to try on.")
	public static SystemMessageId PLEASE_SELECT_THE_ITEM_YOU_WOULD_LIKE_TO_TRY_ON;
	
	@ClientString(id = 1388, message = "A party room has been created.")
	public static SystemMessageId A_PARTY_ROOM_HAS_BEEN_CREATED;
	
	@ClientString(id = 1389, message = "The party room's information has been revised.")
	public static SystemMessageId THE_PARTY_ROOM_S_INFORMATION_HAS_BEEN_REVISED;
	
	@ClientString(id = 1390, message = "You are not allowed to enter the party room.")
	public static SystemMessageId YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM;
	
	@ClientString(id = 1391, message = "You have exited from the party room.")
	public static SystemMessageId YOU_HAVE_EXITED_FROM_THE_PARTY_ROOM;
	
	@ClientString(id = 1392, message = "$s1 has left the party room.")
	public static SystemMessageId S1_HAS_LEFT_THE_PARTY_ROOM;
	
	@ClientString(id = 1393, message = "You have been ousted from the party room.")
	public static SystemMessageId YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM;
	
	@ClientString(id = 1394, message = "$s1 has been ousted from the party room.")
	public static SystemMessageId S1_HAS_BEEN_OUSTED_FROM_THE_PARTY_ROOM;
	
	@ClientString(id = 1395, message = "The party room has been disbanded.")
	public static SystemMessageId THE_PARTY_ROOM_HAS_BEEN_DISBANDED;
	
	@ClientString(id = 1396, message = "The list of party rooms can be viewed by a person who has not joined a party or who is a party leader.")
	public static SystemMessageId THE_LIST_OF_PARTY_ROOMS_CAN_BE_VIEWED_BY_A_PERSON_WHO_HAS_NOT_JOINED_A_PARTY_OR_WHO_IS_A_PARTY_LEADER;
	
	@ClientString(id = 1397, message = "The leader of the party room has changed.")
	public static SystemMessageId THE_LEADER_OF_THE_PARTY_ROOM_HAS_CHANGED;
	
	@ClientString(id = 1398, message = "We are recruiting party members.")
	public static SystemMessageId WE_ARE_RECRUITING_PARTY_MEMBERS;
	
	@ClientString(id = 1399, message = "Only a party leader can transfer one's rights to another player.")
	public static SystemMessageId ONLY_A_PARTY_LEADER_CAN_TRANSFER_ONE_S_RIGHTS_TO_ANOTHER_PLAYER;
	
	@ClientString(id = 1400, message = "Please select the person to whom you would like to transfer the rights of a party leader.")
	public static SystemMessageId PLEASE_SELECT_THE_PERSON_TO_WHOM_YOU_WOULD_LIKE_TO_TRANSFER_THE_RIGHTS_OF_A_PARTY_LEADER;
	
	@ClientString(id = 1401, message = "You cannot transfer rights to yourself.")
	public static SystemMessageId YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF;
	
	@ClientString(id = 1402, message = "You can transfer rights only to another party member.")
	public static SystemMessageId YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER;
	
	@ClientString(id = 1403, message = "You have failed to transfer the party leader rights.")
	public static SystemMessageId YOU_HAVE_FAILED_TO_TRANSFER_THE_PARTY_LEADER_RIGHTS;
	
	@ClientString(id = 1404, message = "The owner of the private manufacturing store has changed the price for creating this item. Please check the new price before trying again.")
	public static SystemMessageId THE_OWNER_OF_THE_PRIVATE_MANUFACTURING_STORE_HAS_CHANGED_THE_PRICE_FOR_CREATING_THIS_ITEM_PLEASE_CHECK_THE_NEW_PRICE_BEFORE_TRYING_AGAIN;
	
	@ClientString(id = 1405, message = "$s1 CP's will be restored.")
	public static SystemMessageId S1_CP_S_WILL_BE_RESTORED;
	
	@ClientString(id = 1406, message = "$s1 will restore $s2's CP.")
	public static SystemMessageId S1_WILL_RESTORE_S2_S_CP;
	
	@ClientString(id = 1407, message = "You are using a computer that does not allow you to log in with two accounts at the same time.")
	public static SystemMessageId YOU_ARE_USING_A_COMPUTER_THAT_DOES_NOT_ALLOW_YOU_TO_LOG_IN_WITH_TWO_ACCOUNTS_AT_THE_SAME_TIME;
	
	@ClientString(id = 1408, message = "Your prepaid remaining usage time is $s1 hours and $s2 minutes. You have $s3 paid reservations left.")
	public static SystemMessageId YOUR_PREPAID_REMAINING_USAGE_TIME_IS_S1_HOURS_AND_S2_MINUTES_YOU_HAVE_S3_PAID_RESERVATIONS_LEFT;
	
	@ClientString(id = 1409, message = "Your prepaid usage time has expired. Your new prepaid reservation will be used. The remaining usage time is $s1 hours and $s2 minutes.")
	public static SystemMessageId YOUR_PREPAID_USAGE_TIME_HAS_EXPIRED_YOUR_NEW_PREPAID_RESERVATION_WILL_BE_USED_THE_REMAINING_USAGE_TIME_IS_S1_HOURS_AND_S2_MINUTES;
	
	@ClientString(id = 1410, message = "Your prepaid usage time has expired. You do not have any more prepaid reservations left.")
	public static SystemMessageId YOUR_PREPAID_USAGE_TIME_HAS_EXPIRED_YOU_DO_NOT_HAVE_ANY_MORE_PREPAID_RESERVATIONS_LEFT;
	
	@ClientString(id = 1411, message = "The number of your prepaid reservations has changed.")
	public static SystemMessageId THE_NUMBER_OF_YOUR_PREPAID_RESERVATIONS_HAS_CHANGED;
	
	@ClientString(id = 1412, message = "Your prepaid usage time has $s1 minutes left.")
	public static SystemMessageId YOUR_PREPAID_USAGE_TIME_HAS_S1_MINUTES_LEFT;
	
	@ClientString(id = 1413, message = "Since you do not meet the requirements, you are not allowed to enter the party room.")
	public static SystemMessageId SINCE_YOU_DO_NOT_MEET_THE_REQUIREMENTS_YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM;
	
	@ClientString(id = 1414, message = "The width and length should be 100 or more grids and less than 5000 grids respectively.")
	public static SystemMessageId THE_WIDTH_AND_LENGTH_SHOULD_BE_100_OR_MORE_GRIDS_AND_LESS_THAN_5000_GRIDS_RESPECTIVELY;
	
	@ClientString(id = 1415, message = "The command file is not set.")
	public static SystemMessageId THE_COMMAND_FILE_IS_NOT_SET;
	
	@ClientString(id = 1416, message = "The party representative of Team 1 has not been selected.")
	public static SystemMessageId THE_PARTY_REPRESENTATIVE_OF_TEAM_1_HAS_NOT_BEEN_SELECTED;
	
	@ClientString(id = 1417, message = "The party representative of Team 2 has not been selected.")
	public static SystemMessageId THE_PARTY_REPRESENTATIVE_OF_TEAM_2_HAS_NOT_BEEN_SELECTED;
	
	@ClientString(id = 1418, message = "The name of Team 1 has not yet been chosen.")
	public static SystemMessageId THE_NAME_OF_TEAM_1_HAS_NOT_YET_BEEN_CHOSEN;
	
	@ClientString(id = 1419, message = "The name of Team 2 has not yet been chosen.")
	public static SystemMessageId THE_NAME_OF_TEAM_2_HAS_NOT_YET_BEEN_CHOSEN;
	
	@ClientString(id = 1420, message = "The name of Team 1 and the name of Team 2 are identical.")
	public static SystemMessageId THE_NAME_OF_TEAM_1_AND_THE_NAME_OF_TEAM_2_ARE_IDENTICAL;
	
	@ClientString(id = 1421, message = "The race setup file has not been designated.")
	public static SystemMessageId THE_RACE_SETUP_FILE_HAS_NOT_BEEN_DESIGNATED;
	
	@ClientString(id = 1422, message = "Race setup file error - BuffCnt is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_BUFFCNT_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1423, message = "Race setup file error - BuffID$s1 is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_BUFFID_S1_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1424, message = "Race setup file error - BuffLv$s1 is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_BUFFLV_S1_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1425, message = "Race setup file error - DefaultAllow is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_DEFAULTALLOW_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1426, message = "Race setup file error - ExpSkillCnt is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_EXPSKILLCNT_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1427, message = "Race setup file error - ExpSkillID$s1 is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_EXPSKILLID_S1_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1428, message = "Race setup file error - ExpItemCnt is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_EXPITEMCNT_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1429, message = "Race setup file error - ExpItemID$s1 is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_EXPITEMID_S1_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1430, message = "Race setup file error - TeleportDelay is not specified.")
	public static SystemMessageId RACE_SETUP_FILE_ERROR_TELEPORTDELAY_IS_NOT_SPECIFIED;
	
	@ClientString(id = 1431, message = "The race will be stopped temporarily.")
	public static SystemMessageId THE_RACE_WILL_BE_STOPPED_TEMPORARILY;
	
	@ClientString(id = 1432, message = "Your opponent is currently in a petrified state.")
	public static SystemMessageId YOUR_OPPONENT_IS_CURRENTLY_IN_A_PETRIFIED_STATE;
	
	@ClientString(id = 1433, message = "The use of $s1 will now be automated.")
	public static SystemMessageId THE_USE_OF_S1_WILL_NOW_BE_AUTOMATED;
	
	@ClientString(id = 1434, message = "The automatic use of $s1 will now be cancelled.")
	public static SystemMessageId THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED;
	
	@ClientString(id = 1435, message = "Due to insufficient $s1, the automatic use function has been cancelled.")
	public static SystemMessageId DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 1436, message = "Due to insufficient $s1, the automatic use function cannot be activated.")
	public static SystemMessageId DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_CANNOT_BE_ACTIVATED;
	
	@ClientString(id = 1437, message = "Players are no longer allowed to play dice. Dice can no longer be purchased from a village store. However, you can still sell them to any village store.")
	public static SystemMessageId PLAYERS_ARE_NO_LONGER_ALLOWED_TO_PLAY_DICE_DICE_CAN_NO_LONGER_BE_PURCHASED_FROM_A_VILLAGE_STORE_HOWEVER_YOU_CAN_STILL_SELL_THEM_TO_ANY_VILLAGE_STORE;
	
	@ClientString(id = 1438, message = "There is no skill that enables enchant.")
	public static SystemMessageId THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
	
	@ClientString(id = 1439, message = "Items required for skill enchant are insufficient.")
	public static SystemMessageId ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT;
	
	@ClientString(id = 1440, message = "Succeeded in enchanting skill $s1.")
	public static SystemMessageId SUCCEEDED_IN_ENCHANTING_SKILL_S1;
	
	@ClientString(id = 1441, message = "Failed in enchanting skill $s1.")
	public static SystemMessageId FAILED_IN_ENCHANTING_SKILL_S1;
	
	@ClientString(id = 1442, message = "Remaining Time: $s1 second")
	public static SystemMessageId REMAINING_TIME_S1_SECOND;
	
	@ClientString(id = 1443, message = "SP required for skill enchant is insufficient.")
	public static SystemMessageId SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT;
	
	@ClientString(id = 1444, message = "Exp. required for skill enchant is insufficient.")
	public static SystemMessageId EXP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT;
	
	@ClientString(id = 1445, message = "Your previous sub-class will be deleted and your new sub-class will start at level 40. Do you wish to proceed?")
	public static SystemMessageId YOUR_PREVIOUS_SUB_CLASS_WILL_BE_DELETED_AND_YOUR_NEW_SUB_CLASS_WILL_START_AT_LEVEL_40_DO_YOU_WISH_TO_PROCEED;
	
	@ClientString(id = 1446, message = "The ferry from $s1 to $s2 has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_S1_TO_S2_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1447, message = "Other skills are not available while fishing.")
	public static SystemMessageId OTHER_SKILLS_ARE_NOT_AVAILABLE_WHILE_FISHING;
	
	@ClientString(id = 1448, message = "Only fishing skills are available.")
	public static SystemMessageId ONLY_FISHING_SKILLS_ARE_AVAILABLE;
	
	@ClientString(id = 1449, message = "Succeeded in getting a bite.")
	public static SystemMessageId SUCCEEDED_IN_GETTING_A_BITE;
	
	@ClientString(id = 1450, message = "Time is up, so that fish got away.")
	public static SystemMessageId TIME_IS_UP_SO_THAT_FISH_GOT_AWAY;
	
	@ClientString(id = 1451, message = "The fish got away.")
	public static SystemMessageId THE_FISH_GOT_AWAY;
	
	@ClientString(id = 1452, message = "Baits have been lost because the fish got away.")
	public static SystemMessageId BAITS_HAVE_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY;
	
	@ClientString(id = 1453, message = "Fishing poles are not installed.")
	public static SystemMessageId FISHING_POLES_ARE_NOT_INSTALLED;
	
	@ClientString(id = 1454, message = "Baits are not put on a hook.")
	public static SystemMessageId BAITS_ARE_NOT_PUT_ON_A_HOOK;
	
	@ClientString(id = 1455, message = "You can't fish in water.")
	public static SystemMessageId YOU_CAN_T_FISH_IN_WATER;
	
	@ClientString(id = 1456, message = "You can't fish while you are on board.")
	public static SystemMessageId YOU_CAN_T_FISH_WHILE_YOU_ARE_ON_BOARD;
	
	@ClientString(id = 1457, message = "You can't fish here.")
	public static SystemMessageId YOU_CAN_T_FISH_HERE;
	
	@ClientString(id = 1458, message = "Cancels fishing.")
	public static SystemMessageId CANCELS_FISHING;
	
	@ClientString(id = 1459, message = "Not enough bait.")
	public static SystemMessageId NOT_ENOUGH_BAIT;
	
	@ClientString(id = 1460, message = "Ends fishing.")
	public static SystemMessageId ENDS_FISHING;
	
	@ClientString(id = 1461, message = "Starts fishing.")
	public static SystemMessageId STARTS_FISHING;
	
	@ClientString(id = 1462, message = "Pumping skill is available only while fishing.")
	public static SystemMessageId PUMPING_SKILL_IS_AVAILABLE_ONLY_WHILE_FISHING;
	
	@ClientString(id = 1463, message = "Reeling skill is available only while fishing.")
	public static SystemMessageId REELING_SKILL_IS_AVAILABLE_ONLY_WHILE_FISHING;
	
	@ClientString(id = 1464, message = "Fish has resisted.")
	public static SystemMessageId FISH_HAS_RESISTED;
	
	@ClientString(id = 1465, message = "Pumping is successful. Damage: $s1")
	public static SystemMessageId PUMPING_IS_SUCCESSFUL_DAMAGE_S1;
	
	@ClientString(id = 1466, message = "Pumping failed, Damage: $s1")
	public static SystemMessageId PUMPING_FAILED_DAMAGE_S1;
	
	@ClientString(id = 1467, message = "Reeling is successful, Damage: $s1")
	public static SystemMessageId REELING_IS_SUCCESSFUL_DAMAGE_S1;
	
	@ClientString(id = 1468, message = "Reeling failed, Damage: $s1")
	public static SystemMessageId REELING_FAILED_DAMAGE_S1;
	
	@ClientString(id = 1469, message = "Succeeded in fishing.")
	public static SystemMessageId SUCCEEDED_IN_FISHING;
	
	@ClientString(id = 1470, message = "You can't mount, dismount, break and drop items while fishing.")
	public static SystemMessageId YOU_CAN_T_MOUNT_DISMOUNT_BREAK_AND_DROP_ITEMS_WHILE_FISHING;
	
	@ClientString(id = 1471, message = "You cannot do anything else while fishing.")
	public static SystemMessageId YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING;
	
	@ClientString(id = 1472, message = "You can't make an attack with a fishing pole.")
	public static SystemMessageId YOU_CAN_T_MAKE_AN_ATTACK_WITH_A_FISHING_POLE;
	
	@ClientString(id = 1473, message = "$s1 is not sufficient.")
	public static SystemMessageId S1_IS_NOT_SUFFICIENT;
	
	@ClientString(id = 1474, message = "$s1 is not available.")
	public static SystemMessageId S1_IS_NOT_AVAILABLE;
	
	@ClientString(id = 1475, message = "Pet has dropped $s1.")
	public static SystemMessageId PET_HAS_DROPPED_S1;
	
	@ClientString(id = 1476, message = "Pet has dropped +$s1$s2.")
	public static SystemMessageId PET_HAS_DROPPED_S1_S2;
	
	@ClientString(id = 1477, message = "Pet has dropped $s2 of $s1.")
	public static SystemMessageId PET_HAS_DROPPED_S2_OF_S1;
	
	@ClientString(id = 1478, message = "You can register only 256 color bmp files with a size of 64*64.")
	public static SystemMessageId YOU_CAN_REGISTER_ONLY_256_COLOR_BMP_FILES_WITH_A_SIZE_OF_64_64;
	
	@ClientString(id = 1479, message = "This fishing shot is not fit for the fishing pole crystal.")
	public static SystemMessageId THIS_FISHING_SHOT_IS_NOT_FIT_FOR_THE_FISHING_POLE_CRYSTAL;
	
	@ClientString(id = 1480, message = "Do you want to cancel your application for joining the Grand Olympiad?")
	public static SystemMessageId DO_YOU_WANT_TO_CANCEL_YOUR_APPLICATION_FOR_JOINING_THE_GRAND_OLYMPIAD;
	
	@ClientString(id = 1481, message = "You have been selected for No Class Game. Do you want to join?")
	public static SystemMessageId YOU_HAVE_BEEN_SELECTED_FOR_NO_CLASS_GAME_DO_YOU_WANT_TO_JOIN;
	
	@ClientString(id = 1482, message = "You have been selected for Classified Game. Do you want to join?")
	public static SystemMessageId YOU_HAVE_BEEN_SELECTED_FOR_CLASSIFIED_GAME_DO_YOU_WANT_TO_JOIN;
	
	@ClientString(id = 1483, message = "Do you want to become a Hero now?")
	public static SystemMessageId DO_YOU_WANT_TO_BECOME_A_HERO_NOW;
	
	@ClientString(id = 1484, message = "Do you want to use the Heroes weapon that you chose?")
	public static SystemMessageId DO_YOU_WANT_TO_USE_THE_HEROES_WEAPON_THAT_YOU_CHOSE;
	
	@ClientString(id = 1485, message = "The ferry from Talking Island to Gludin Harbor has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_TO_GLUDIN_HARBOR_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1486, message = "The ferry from Gludin Harbor to Talking Island has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_TO_TALKING_ISLAND_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1487, message = "The ferry from Giran Harbor to Talking Island has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_GIRAN_HARBOR_TO_TALKING_ISLAND_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1488, message = "The ferry from Talking Island to Giran Harbor has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_TALKING_ISLAND_TO_GIRAN_HARBOR_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1489, message = "Innadril cruise service has been delayed.")
	public static SystemMessageId INNADRIL_CRUISE_SERVICE_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1490, message = "Traded $s2 of crop $s1.")
	public static SystemMessageId TRADED_S2_OF_CROP_S1;
	
	@ClientString(id = 1491, message = "Failed in trading $s2 of crop $s1.")
	public static SystemMessageId FAILED_IN_TRADING_S2_OF_CROP_S1;
	
	@ClientString(id = 1492, message = "You will enter the Olympiad Stadium in $s1 second(s).")
	public static SystemMessageId YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S;
	
	@ClientString(id = 1493, message = "The battle has been cancelled because your opponent has left the game.")
	public static SystemMessageId THE_BATTLE_HAS_BEEN_CANCELLED_BECAUSE_YOUR_OPPONENT_HAS_LEFT_THE_GAME;
	
	@ClientString(id = 1494, message = "The game has been cancelled because the other party does not meet the requirements for joining the game.")
	public static SystemMessageId THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME;
	
	@ClientString(id = 1495, message = "The game will start in $s1 second(s).")
	public static SystemMessageId THE_GAME_WILL_START_IN_S1_SECOND_S;
	
	@ClientString(id = 1496, message = "The battle has started!")
	public static SystemMessageId THE_BATTLE_HAS_STARTED;
	
	@ClientString(id = 1497, message = "$s1 has won the game!")
	public static SystemMessageId S1_HAS_WON_THE_GAME;
	
	@ClientString(id = 1498, message = "The game ended in a tie.")
	public static SystemMessageId THE_GAME_ENDED_IN_A_TIE;
	
	@ClientString(id = 1499, message = "You will go back to the Village in $s1 second(s).")
	public static SystemMessageId YOU_WILL_GO_BACK_TO_THE_VILLAGE_IN_S1_SECOND_S;
	
	@ClientString(id = 1500, message = "You can't join the Olympiad with a sub-job character.")
	public static SystemMessageId YOU_CAN_T_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER;
	
	@ClientString(id = 1501, message = "Only Nobless can participate in the Olympiad.")
	public static SystemMessageId ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD;
	
	@ClientString(id = 1502, message = "You have already been registered in a waiting list of an event.")
	public static SystemMessageId YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT;
	
	@ClientString(id = 1503, message = "You have been registered in a waiting list of Classified Games.")
	public static SystemMessageId YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES;
	
	@ClientString(id = 1504, message = "You have been registered in a waiting list of No Class Games.")
	public static SystemMessageId YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES;
	
	@ClientString(id = 1505, message = "You have been removed from the Grand Olympiad Games waiting list.")
	public static SystemMessageId YOU_HAVE_BEEN_REMOVED_FROM_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST;
	
	@ClientString(id = 1506, message = "You are not registered on the Grand Olympiad Games waiting list.")
	public static SystemMessageId YOU_ARE_NOT_REGISTERED_ON_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST;
	
	@ClientString(id = 1507, message = "This item can't be equipped for the Olympiad event.")
	public static SystemMessageId THIS_ITEM_CAN_T_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT;
	
	@ClientString(id = 1508, message = "This item is not available for the Olympiad event.")
	public static SystemMessageId THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT;
	
	@ClientString(id = 1509, message = "This skill is not available for the Olympiad event.")
	public static SystemMessageId THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT;
	
	@ClientString(id = 1510, message = "$s1 is making an attempt at resurrection. Do you want to continue with this resurrection?")
	public static SystemMessageId S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION_DO_YOU_WANT_TO_CONTINUE_WITH_THIS_RESURRECTION;
	
	@ClientString(id = 1511, message = "While a pet is attempting to resurrect, it cannot help in resurrecting its master.")
	public static SystemMessageId WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER;
	
	@ClientString(id = 1512, message = "While a pet's master is attempting to resurrect, the pet cannot be resurrected at the same time.")
	public static SystemMessageId WHILE_A_PET_S_MASTER_IS_ATTEMPTING_TO_RESURRECT_THE_PET_CANNOT_BE_RESURRECTED_AT_THE_SAME_TIME;
	
	@ClientString(id = 1513, message = "Better resurrection has been already proposed.")
	public static SystemMessageId BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED;
	
	@ClientString(id = 1514, message = "Since the pet was in the process of being resurrected, the attempt to resurrect its master has been cancelled.")
	public static SystemMessageId SINCE_THE_PET_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_ITS_MASTER_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 1515, message = "Since the master was in the process of being resurrected, the attempt to resurrect the pet has been cancelled.")
	public static SystemMessageId SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED;
	
	@ClientString(id = 1516, message = "The target is unavailable for seeding.")
	public static SystemMessageId THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING;
	
	@ClientString(id = 1517, message = "Failed in Blessed Enchant. The enchant value of the item became 0.")
	public static SystemMessageId FAILED_IN_BLESSED_ENCHANT_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0;
	
	@ClientString(id = 1518, message = "The item can't be installed because it is not fit for the condition for item installation.")
	public static SystemMessageId THE_ITEM_CAN_T_BE_INSTALLED_BECAUSE_IT_IS_NOT_FIT_FOR_THE_CONDITION_FOR_ITEM_INSTALLATION;
	
	@ClientString(id = 1519, message = "Pet has died. If he is not resurrected in 20 minutes, the Pet's corpse will disappear and his items will disappear as well.")
	public static SystemMessageId PET_HAS_DIED_IF_HE_IS_NOT_RESURRECTED_IN_20_MINUTES_THE_PET_S_CORPSE_WILL_DISAPPEAR_AND_HIS_ITEMS_WILL_DISAPPEAR_AS_WELL;
	
	@ClientString(id = 1520, message = "Servitor passed away.")
	public static SystemMessageId SERVITOR_PASSED_AWAY;
	
	@ClientString(id = 1521, message = "Your servitor has disappeared because the summoning time is over.")
	public static SystemMessageId YOUR_SERVITOR_HAS_DISAPPEARED_BECAUSE_THE_SUMMONING_TIME_IS_OVER;
	
	@ClientString(id = 1522, message = "The corpse disappeared because much time passed after Pet died.")
	public static SystemMessageId THE_CORPSE_DISAPPEARED_BECAUSE_MUCH_TIME_PASSED_AFTER_PET_DIED;
	
	@ClientString(id = 1523, message = "Because Pet or Servitor may be drowned while the boat moves, please release the summon before departure.")
	public static SystemMessageId BECAUSE_PET_OR_SERVITOR_MAY_BE_DROWNED_WHILE_THE_BOAT_MOVES_PLEASE_RELEASE_THE_SUMMON_BEFORE_DEPARTURE;
	
	@ClientString(id = 1524, message = "Pet of $s1 gained $s2.")
	public static SystemMessageId PET_OF_S1_GAINED_S2;
	
	@ClientString(id = 1525, message = "Pet of $s1 gained $s3 of $s2.")
	public static SystemMessageId PET_OF_S1_GAINED_S3_OF_S2;
	
	@ClientString(id = 1526, message = "Pet of $s1 gained +$s2$s3.")
	public static SystemMessageId PET_OF_S1_GAINED_S2_S3;
	
	@ClientString(id = 1527, message = "Pet took $s1 because he was hungry.")
	public static SystemMessageId PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY;
	
	@ClientString(id = 1528, message = "A forcible petition from GM has been received.")
	public static SystemMessageId A_FORCIBLE_PETITION_FROM_GM_HAS_BEEN_RECEIVED;
	
	@ClientString(id = 1529, message = "$s1 has invited you to the command channel. Do you want to join?")
	public static SystemMessageId S1_HAS_INVITED_YOU_TO_THE_COMMAND_CHANNEL_DO_YOU_WANT_TO_JOIN;
	
	@ClientString(id = 1530, message = "Select a target or enter the name.")
	public static SystemMessageId SELECT_A_TARGET_OR_ENTER_THE_NAME;
	
	@ClientString(id = 1531, message = "Enter the name of Clan against which you want to make an attack.")
	public static SystemMessageId ENTER_THE_NAME_OF_CLAN_AGAINST_WHICH_YOU_WANT_TO_MAKE_AN_ATTACK;
	
	@ClientString(id = 1532, message = "Enter the name of Clan against which you want to stop the war.")
	public static SystemMessageId ENTER_THE_NAME_OF_CLAN_AGAINST_WHICH_YOU_WANT_TO_STOP_THE_WAR;
	
	@ClientString(id = 1533, message = "Attention: $s1 picked up $s2.")
	public static SystemMessageId ATTENTION_S1_PICKED_UP_S2;
	
	@ClientString(id = 1534, message = "Attention: $s1 picked up +$s2 $s3.")
	public static SystemMessageId ATTENTION_S1_PICKED_UP_S2_S3;
	
	@ClientString(id = 1535, message = "Attention: $s1 Pet picked up $s2.")
	public static SystemMessageId ATTENTION_S1_PET_PICKED_UP_S2;
	
	@ClientString(id = 1536, message = "Attention: $s1 Pet picked up +$s2 $s3.")
	public static SystemMessageId ATTENTION_S1_PET_PICKED_UP_S2_S3;
	
	@ClientString(id = 1537, message = "Current Location : $s1, $s2, $s3 (near Rune Village)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_RUNE_VILLAGE;
	
	@ClientString(id = 1538, message = "Current Location : $s1, $s2, $s3 (near Goddard Castle Town)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_NEAR_GODDARD_CASTLE_TOWN;
	
	@ClientString(id = 1539, message = "Cargo has arrived at Talking Island Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_TALKING_ISLAND_VILLAGE;
	
	@ClientString(id = 1540, message = "Cargo has arrived at Dark Elven Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_DARK_ELVEN_VILLAGE;
	
	@ClientString(id = 1541, message = "Cargo has arrived at Elven Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_ELVEN_VILLAGE;
	
	@ClientString(id = 1542, message = "Cargo has arrived at Orc Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_ORC_VILLAGE;
	
	@ClientString(id = 1543, message = "Cargo has arrived at Dwarven Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_DWARVEN_VILLAGE;
	
	@ClientString(id = 1544, message = "Cargo has arrived at Aden Castle Town.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_ADEN_CASTLE_TOWN;
	
	@ClientString(id = 1545, message = "Cargo has arrived at Oren Castle Town.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_OREN_CASTLE_TOWN;
	
	@ClientString(id = 1546, message = "Cargo has arrived at Hunters Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_HUNTERS_VILLAGE;
	
	@ClientString(id = 1547, message = "Cargo has arrived at Dion Castle Town.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_DION_CASTLE_TOWN;
	
	@ClientString(id = 1548, message = "Cargo has arrived at Floran Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_FLORAN_VILLAGE;
	
	@ClientString(id = 1549, message = "Cargo has arrived at Gludin Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_GLUDIN_VILLAGE;
	
	@ClientString(id = 1550, message = "Cargo has arrived at Gludio Castle Town.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_GLUDIO_CASTLE_TOWN;
	
	@ClientString(id = 1551, message = "Cargo has arrived at Giran Castle Town.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_GIRAN_CASTLE_TOWN;
	
	@ClientString(id = 1552, message = "Cargo has arrived at Heine.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_HEINE;
	
	@ClientString(id = 1553, message = "Cargo has arrived at Rune Village.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_RUNE_VILLAGE;
	
	@ClientString(id = 1554, message = "Cargo has arrived at Goddard Castle Town.")
	public static SystemMessageId CARGO_HAS_ARRIVED_AT_GODDARD_CASTLE_TOWN;
	
	@ClientString(id = 1555, message = "Do you want to cancel character deletion?")
	public static SystemMessageId DO_YOU_WANT_TO_CANCEL_CHARACTER_DELETION;
	
	@ClientString(id = 1556, message = "Notice has been saved.")
	public static SystemMessageId NOTICE_HAS_BEEN_SAVED;
	
	@ClientString(id = 1557, message = "Seed price should be more than $s1 and less than $s2.")
	public static SystemMessageId SEED_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	@ClientString(id = 1558, message = "The quantity of seed should be more than $s1 and less than $s2.")
	public static SystemMessageId THE_QUANTITY_OF_SEED_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	@ClientString(id = 1559, message = "Crop price should be more than $s1 and less than $s2.")
	public static SystemMessageId CROP_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	@ClientString(id = 1560, message = "The quantity of crop should be more than $s1 and less than $s2 .")
	public static SystemMessageId THE_QUANTITY_OF_CROP_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2;
	
	@ClientString(id = 1561, message = "$s1 Clan has declared Clan War.")
	public static SystemMessageId S1_CLAN_HAS_DECLARED_CLAN_WAR;
	
	@ClientString(id = 1562, message = "Clan War has been declared against $s1 Clan. If you are killed during the Clan War by members of the opposing clan, the experience penalty will be reduced to 1/4 of normal..")
	public static SystemMessageId CLAN_WAR_HAS_BEEN_DECLARED_AGAINST_S1_CLAN_IF_YOU_ARE_KILLED_DURING_THE_CLAN_WAR_BY_MEMBERS_OF_THE_OPPOSING_CLAN_THE_EXPERIENCE_PENALTY_WILL_BE_REDUCED_TO_1_4_OF_NORMAL;
	
	@ClientString(id = 1563, message = "$s1 Clan can't make a declaration of Clan War since it hasn't reached the clan level or doesn't have enough clan members.")
	public static SystemMessageId S1_CLAN_CAN_T_MAKE_A_DECLARATION_OF_CLAN_WAR_SINCE_IT_HASN_T_REACHED_THE_CLAN_LEVEL_OR_DOESN_T_HAVE_ENOUGH_CLAN_MEMBERS;
	
	@ClientString(id = 1564, message = "A Clan War can be declared only if the clan is level three or above, and the number of clan members is fifteen or greater.")
	public static SystemMessageId A_CLAN_WAR_CAN_BE_DECLARED_ONLY_IF_THE_CLAN_IS_LEVEL_THREE_OR_ABOVE_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_FIFTEEN_OR_GREATER;
	
	@ClientString(id = 1565, message = "A Clan War cannot be declared because that clan does not exist!")
	public static SystemMessageId A_CLAN_WAR_CANNOT_BE_DECLARED_BECAUSE_THAT_CLAN_DOES_NOT_EXIST;
	
	@ClientString(id = 1566, message = "$s1 Clan has stopped the war.")
	public static SystemMessageId S1_CLAN_HAS_STOPPED_THE_WAR;
	
	@ClientString(id = 1567, message = "The war against $s1 Clan has been stopped.")
	public static SystemMessageId THE_WAR_AGAINST_S1_CLAN_HAS_BEEN_STOPPED;
	
	@ClientString(id = 1568, message = "The target for declaration is wrong.")
	public static SystemMessageId THE_TARGET_FOR_DECLARATION_IS_WRONG;
	
	@ClientString(id = 1569, message = "A declaration of Clan War against an allied clan can't be made.")
	public static SystemMessageId A_DECLARATION_OF_CLAN_WAR_AGAINST_AN_ALLIED_CLAN_CAN_T_BE_MADE;
	
	@ClientString(id = 1570, message = "A declaration of war against more than 30 Clans can't be made at the same time.")
	public static SystemMessageId A_DECLARATION_OF_WAR_AGAINST_MORE_THAN_30_CLANS_CAN_T_BE_MADE_AT_THE_SAME_TIME;
	
	@ClientString(id = 1571, message = "=======<Attack List>=======")
	public static SystemMessageId ATTACK_LIST;
	
	@ClientString(id = 1572, message = "======<Under Attack List>======")
	public static SystemMessageId UNDER_ATTACK_LIST;
	
	@ClientString(id = 1573, message = "There is no Attack Clan.")
	public static SystemMessageId THERE_IS_NO_ATTACK_CLAN;
	
	@ClientString(id = 1574, message = "There is no Under Attack Clan.")
	public static SystemMessageId THERE_IS_NO_UNDER_ATTACK_CLAN;
	
	@ClientString(id = 1575, message = "Only Party leader, a Clan Leader with a clan level more than 5, can open a command channel.")
	public static SystemMessageId ONLY_PARTY_LEADER_A_CLAN_LEADER_WITH_A_CLAN_LEVEL_MORE_THAN_5_CAN_OPEN_A_COMMAND_CHANNEL;
	
	@ClientString(id = 1576, message = "Pet uses the power of spirit.")
	public static SystemMessageId PET_USES_THE_POWER_OF_SPIRIT;
	
	@ClientString(id = 1577, message = "Servitor uses the power of spirit.")
	public static SystemMessageId SERVITOR_USES_THE_POWER_OF_SPIRIT;
	
	@ClientString(id = 1578, message = "Items are not available for a private store or private manufacture.")
	public static SystemMessageId ITEMS_ARE_NOT_AVAILABLE_FOR_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURE;
	
	@ClientString(id = 1579, message = "$s1 Pet gained $s2 Adena.")
	public static SystemMessageId S1_PET_GAINED_S2_ADENA;
	
	@ClientString(id = 1580, message = "A command channel has been opened.")
	public static SystemMessageId A_COMMAND_CHANNEL_HAS_BEEN_OPENED;
	
	@ClientString(id = 1581, message = "A command channel has been closed.")
	public static SystemMessageId A_COMMAND_CHANNEL_HAS_BEEN_CLOSED;
	
	@ClientString(id = 1582, message = "You have participated in the command channel.")
	public static SystemMessageId YOU_HAVE_PARTICIPATED_IN_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1583, message = "You have been dismissed from the command channel.")
	public static SystemMessageId YOU_HAVE_BEEN_DISMISSED_FROM_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1584, message = "$s1 Party has been dismissed from the command channel.")
	public static SystemMessageId S1_PARTY_HAS_BEEN_DISMISSED_FROM_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1585, message = "The command channel has been deactivated.")
	public static SystemMessageId THE_COMMAND_CHANNEL_HAS_BEEN_DEACTIVATED;
	
	@ClientString(id = 1586, message = "You have left the command channel.")
	public static SystemMessageId YOU_HAVE_LEFT_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1587, message = "$s1 Party has left the command channel.")
	public static SystemMessageId S1_PARTY_HAS_LEFT_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1588, message = "The command channel is activated only if at least five parties participate in.")
	public static SystemMessageId THE_COMMAND_CHANNEL_IS_ACTIVATED_ONLY_IF_AT_LEAST_FIVE_PARTIES_PARTICIPATE_IN;
	
	@ClientString(id = 1589, message = "The authority for command channel has been transferred to $s1.")
	public static SystemMessageId THE_AUTHORITY_FOR_COMMAND_CHANNEL_HAS_BEEN_TRANSFERRED_TO_S1;
	
	@ClientString(id = 1590, message = "===<Command Channel Info(Total Parties: $s1)>===")
	public static SystemMessageId COMMAND_CHANNEL_INFO_TOTAL_PARTIES_S1;
	
	@ClientString(id = 1591, message = "No user was invited to the command channel.")
	public static SystemMessageId NO_USER_WAS_INVITED_TO_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1592, message = "You can't open command channels any more.")
	public static SystemMessageId YOU_CAN_T_OPEN_COMMAND_CHANNELS_ANY_MORE;
	
	@ClientString(id = 1593, message = "You have no right of inviting to the command channel.")
	public static SystemMessageId YOU_HAVE_NO_RIGHT_OF_INVITING_TO_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1594, message = "$s1 Party has been already joined the command channel.")
	public static SystemMessageId S1_PARTY_HAS_BEEN_ALREADY_JOINED_THE_COMMAND_CHANNEL;
	
	@ClientString(id = 1595, message = "$s1 has succeeded.")
	public static SystemMessageId S1_HAS_SUCCEEDED;
	
	@ClientString(id = 1596, message = "Hit by $s1.")
	public static SystemMessageId HIT_BY_S1;
	
	@ClientString(id = 1597, message = "$s1 has failed.")
	public static SystemMessageId S1_HAS_FAILED;
	
	@ClientString(id = 1598, message = "When Pet or Servitor is dead, soulshots or spiritshots for Pet or Servitor are not available.")
	public static SystemMessageId WHEN_PET_OR_SERVITOR_IS_DEAD_SOULSHOTS_OR_SPIRITSHOTS_FOR_PET_OR_SERVITOR_ARE_NOT_AVAILABLE;
	
	@ClientString(id = 1599, message = "Watching is impossible during combat.")
	public static SystemMessageId WATCHING_IS_IMPOSSIBLE_DURING_COMBAT;
	
	@ClientString(id = 1600, message = "Tomorrow's items will ALL be set to 0. Do you wish to continue?")
	public static SystemMessageId TOMORROW_S_ITEMS_WILL_ALL_BE_SET_TO_0_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 1601, message = "Tomorrow's items will all be set to the same value as today's items. Do you wish to continue?")
	public static SystemMessageId TOMORROW_S_ITEMS_WILL_ALL_BE_SET_TO_THE_SAME_VALUE_AS_TODAY_S_ITEMS_DO_YOU_WISH_TO_CONTINUE;
	
	@ClientString(id = 1602, message = "Commander dialogue is available only to party leader.")
	public static SystemMessageId COMMANDER_DIALOGUE_IS_AVAILABLE_ONLY_TO_PARTY_LEADER;
	
	@ClientString(id = 1603, message = "Only channel opener can give All Command.")
	public static SystemMessageId ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND;
	
	@ClientString(id = 1604, message = "While dressed in formal wear, you can't use items that require all skills and casting operations.")
	public static SystemMessageId WHILE_DRESSED_IN_FORMAL_WEAR_YOU_CAN_T_USE_ITEMS_THAT_REQUIRE_ALL_SKILLS_AND_CASTING_OPERATIONS;
	
	@ClientString(id = 1605, message = "* Here, you can buy only seeds of $s1 Manor.")
	public static SystemMessageId HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR;
	
	@ClientString(id = 1606, message = "You have completed the quest for 3rd occupation change and moved to another class. Congratulations!")
	public static SystemMessageId YOU_HAVE_COMPLETED_THE_QUEST_FOR_3RD_OCCUPATION_CHANGE_AND_MOVED_TO_ANOTHER_CLASS_CONGRATULATIONS;
	
	@ClientString(id = 1607, message = "$s1 Adena has been paid for purchasing fees.")
	public static SystemMessageId S1_ADENA_HAS_BEEN_PAID_FOR_PURCHASING_FEES;
	
	@ClientString(id = 1608, message = "You can't buy another castle since Adena is not sufficient.")
	public static SystemMessageId YOU_CAN_T_BUY_ANOTHER_CASTLE_SINCE_ADENA_IS_NOT_SUFFICIENT;
	
	@ClientString(id = 1609, message = "The declaration of war has been already made to the clan.")
	public static SystemMessageId THE_DECLARATION_OF_WAR_HAS_BEEN_ALREADY_MADE_TO_THE_CLAN;
	
	@ClientString(id = 1610, message = "You can't make a declaration of war to your own clan.")
	public static SystemMessageId YOU_CAN_T_MAKE_A_DECLARATION_OF_WAR_TO_YOUR_OWN_CLAN;
	
	@ClientString(id = 1611, message = "Party Leader: $s1")
	public static SystemMessageId PARTY_LEADER_S1;
	
	@ClientString(id = 1612, message = "=====<War List>=====")
	public static SystemMessageId WAR_LIST;
	
	@ClientString(id = 1613, message = "There is no clan listed on War List.")
	public static SystemMessageId THERE_IS_NO_CLAN_LISTED_ON_WAR_LIST;
	
	@ClientString(id = 1614, message = "You are participating in the channel which has been already opened.")
	public static SystemMessageId YOU_ARE_PARTICIPATING_IN_THE_CHANNEL_WHICH_HAS_BEEN_ALREADY_OPENED;
	
	@ClientString(id = 1615, message = "The number of remaining parties is $s1 until a channel is activated.")
	public static SystemMessageId THE_NUMBER_OF_REMAINING_PARTIES_IS_S1_UNTIL_A_CHANNEL_IS_ACTIVATED;
	
	@ClientString(id = 1616, message = "The command channel has been activated.")
	public static SystemMessageId THE_COMMAND_CHANNEL_HAS_BEEN_ACTIVATED;
	
	@ClientString(id = 1617, message = "You have no right to use a command channel.")
	public static SystemMessageId YOU_HAVE_NO_RIGHT_TO_USE_A_COMMAND_CHANNEL;
	
	@ClientString(id = 1618, message = "The ferry from Rune Harbor to Gludin Harbor has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_TO_GLUDIN_HARBOR_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1619, message = "The ferry from Gludin Harbor to Rune Harbor has been delayed.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_TO_RUNE_HARBOR_HAS_BEEN_DELAYED;
	
	@ClientString(id = 1620, message = "Arrived at Rune Harbor.")
	public static SystemMessageId ARRIVED_AT_RUNE_HARBOR;
	
	@ClientString(id = 1621, message = "Will leave for Gludin Harbor in five minutes.")
	public static SystemMessageId WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_FIVE_MINUTES_2;
	
	@ClientString(id = 1622, message = "Will leave for Gludin Harbor in one minute.")
	public static SystemMessageId WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_ONE_MINUTE_2;
	
	@ClientString(id = 1623, message = "Leaving soon for Gludin Harbor.")
	public static SystemMessageId LEAVING_SOON_FOR_GLUDIN_HARBOR_2;
	
	@ClientString(id = 1624, message = "Leaving for Gludin Harbor.")
	public static SystemMessageId LEAVING_FOR_GLUDIN_HARBOR_2;
	
	@ClientString(id = 1625, message = "Will leave for Rune Harbor after anchoring for ten minutes.")
	public static SystemMessageId WILL_LEAVE_FOR_RUNE_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES;
	
	@ClientString(id = 1626, message = "Will leave for Rune Harbor in five minutes.")
	public static SystemMessageId WILL_LEAVE_FOR_RUNE_HARBOR_IN_FIVE_MINUTES;
	
	@ClientString(id = 1627, message = "Will leave for Rune Harbor in one minute.")
	public static SystemMessageId WILL_LEAVE_FOR_RUNE_HARBOR_IN_ONE_MINUTE;
	
	@ClientString(id = 1628, message = "Leaving soon for Rune Harbor.")
	public static SystemMessageId LEAVING_SOON_FOR_RUNE_HARBOR;
	
	@ClientString(id = 1629, message = "Leaving for Rune Harbor.")
	public static SystemMessageId LEAVING_FOR_RUNE_HARBOR;
	
	@ClientString(id = 1630, message = "The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 15 minutes.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1631, message = "The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1632, message = "The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1633, message = "The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1634, message = "The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 15 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_15_MINUTES;
	
	@ClientString(id = 1635, message = "The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 10 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_10_MINUTES;
	
	@ClientString(id = 1636, message = "The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 5 minutes.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_5_MINUTES;
	
	@ClientString(id = 1637, message = "The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 1 minute.")
	public static SystemMessageId THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_1_MINUTE;
	
	@ClientString(id = 1638, message = "You cannot fish while using a recipe book, private manufacture or private store.")
	public static SystemMessageId YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE;
	
	@ClientString(id = 1639, message = "Olympiad period $s1 has started.")
	public static SystemMessageId OLYMPIAD_PERIOD_S1_HAS_STARTED;
	
	@ClientString(id = 1640, message = "Olympiad period $s1 has ended.")
	public static SystemMessageId OLYMPIAD_PERIOD_S1_HAS_ENDED;
	
	@ClientString(id = 1641, message = "The Olympiad game has started.")
	public static SystemMessageId THE_OLYMPIAD_GAME_HAS_STARTED;
	
	@ClientString(id = 1642, message = "The Olympiad game has ended.")
	public static SystemMessageId THE_OLYMPIAD_GAME_HAS_ENDED;
	
	@ClientString(id = 1643, message = "Current Location: $s1, $s2, $s3 (Dimension Gap)")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_DIMENSION_GAP;
	
	@ClientString(id = 1644, message = "none")
	public static SystemMessageId NONE;
	
	@ClientString(id = 1645, message = "none")
	public static SystemMessageId NONE_2;
	
	@ClientString(id = 1646, message = "none")
	public static SystemMessageId NONE_3;
	
	@ClientString(id = 1647, message = "none")
	public static SystemMessageId NONE_4;
	
	@ClientString(id = 1648, message = "none")
	public static SystemMessageId NONE_5;
	
	@ClientString(id = 1649, message = "none")
	public static SystemMessageId NONE_6;
	
	@ClientString(id = 1650, message = "Due to a large number of users currently accessing our server, your login attempt has failed. Please wait a little while and attempt to log in again.")
	public static SystemMessageId DUE_TO_A_LARGE_NUMBER_OF_USERS_CURRENTLY_ACCESSING_OUR_SERVER_YOUR_LOGIN_ATTEMPT_HAS_FAILED_PLEASE_WAIT_A_LITTLE_WHILE_AND_ATTEMPT_TO_LOG_IN_AGAIN;
	
	@ClientString(id = 1651, message = "The Olympiad game is not currently in progress.")
	public static SystemMessageId THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS;
	
	@ClientString(id = 1652, message = "The video recording of the replay will now begin.")
	public static SystemMessageId THE_VIDEO_RECORDING_OF_THE_REPLAY_WILL_NOW_BEGIN;
	
	@ClientString(id = 1653, message = "The replay file has been stored successfully. ($s1)")
	public static SystemMessageId THE_REPLAY_FILE_HAS_BEEN_STORED_SUCCESSFULLY_S1;
	
	@ClientString(id = 1654, message = "The attempt to record the replay file has failed.")
	public static SystemMessageId THE_ATTEMPT_TO_RECORD_THE_REPLAY_FILE_HAS_FAILED;
	
	@ClientString(id = 1655, message = "You have caught a monster!")
	public static SystemMessageId YOU_HAVE_CAUGHT_A_MONSTER;
	
	@ClientString(id = 1656, message = "You have successfully traded the item with the NPC.")
	public static SystemMessageId YOU_HAVE_SUCCESSFULLY_TRADED_THE_ITEM_WITH_THE_NPC;
	
	@ClientString(id = 1657, message = "$s1 has gained $s2 Olympiad points.")
	public static SystemMessageId S1_HAS_GAINED_S2_OLYMPIAD_POINTS;
	
	@ClientString(id = 1658, message = "$s1 has lost $s2 Olympiad points.")
	public static SystemMessageId S1_HAS_LOST_S2_OLYMPIAD_POINTS;
	
	@ClientString(id = 1659, message = "Current Location: $s1, $s2, $s3 (Cemetery of the Empire).")
	public static SystemMessageId CURRENT_LOCATION_S1_S2_S3_CEMETERY_OF_THE_EMPIRE;
	
	@ClientString(id = 1660, message = "The channel was opened by: $s1")
	public static SystemMessageId THE_CHANNEL_WAS_OPENED_BY_S1;
	
	@ClientString(id = 1661, message = "$s1 has obtained $s3 $s2s.")
	public static SystemMessageId S1_HAS_OBTAINED_S3_S2S;
	
	@ClientString(id = 1662, message = "If you fish in one spot for a long time, the success rate of a fish taking the bait becomes lower. Please move to another place and continue your fishing there.")
	public static SystemMessageId IF_YOU_FISH_IN_ONE_SPOT_FOR_A_LONG_TIME_THE_SUCCESS_RATE_OF_A_FISH_TAKING_THE_BAIT_BECOMES_LOWER_PLEASE_MOVE_TO_ANOTHER_PLACE_AND_CONTINUE_YOUR_FISHING_THERE;
	
	@ClientString(id = 1663, message = "The clan's emblem was successfully registered. Only a clan that owns a clan hall or a castle can get their emblem displayed on clan related items.")
	public static SystemMessageId THE_CLAN_S_EMBLEM_WAS_SUCCESSFULLY_REGISTERED_ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS;
	
	@ClientString(id = 1664, message = "Because the fish is resisting, the float is bobbing up and down a lot.")
	public static SystemMessageId BECAUSE_THE_FISH_IS_RESISTING_THE_FLOAT_IS_BOBBING_UP_AND_DOWN_A_LOT;
	
	@ClientString(id = 1665, message = "Since the fish is exhausted, the float is moving only slightly.")
	public static SystemMessageId SINCE_THE_FISH_IS_EXHAUSTED_THE_FLOAT_IS_MOVING_ONLY_SLIGHTLY;
	
	@ClientString(id = 1666, message = "You have obtained +$s1$s2.")
	public static SystemMessageId YOU_HAVE_OBTAINED_S1_S2_2;
	
	@ClientString(id = 1667, message = "Lethal Strike!")
	public static SystemMessageId LETHAL_STRIKE;
	
	@ClientString(id = 1668, message = "Your lethal strike was successful!")
	public static SystemMessageId YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL;
	
	@ClientString(id = 1669, message = "The attempt for item conversion has failed.")
	public static SystemMessageId THE_ATTEMPT_FOR_ITEM_CONVERSION_HAS_FAILED;
	
	@ClientString(id = 1670, message = "Since the skill level of reeling (pumping) is higher than the level of your fishing mastery, a penalty of $s1 will be applied.")
	public static SystemMessageId SINCE_THE_SKILL_LEVEL_OF_REELING_PUMPING_IS_HIGHER_THAN_THE_LEVEL_OF_YOUR_FISHING_MASTERY_A_PENALTY_OF_S1_WILL_BE_APPLIED;
	
	@ClientString(id = 1671, message = "Your reeling was successful! (Mastery Penalty:$s1 )")
	public static SystemMessageId YOUR_REELING_WAS_SUCCESSFUL_MASTERY_PENALTY_S1;
	
	@ClientString(id = 1672, message = "Your pumping was successful! (Mastery Penalty:$s1 )")
	public static SystemMessageId YOUR_PUMPING_WAS_SUCCESSFUL_MASTERY_PENALTY_S1;
	
	@ClientString(id = 1673, message = "The present record during the current Olympiad session is $s1 wins $s2 defeats. You have earned $s3 Olympiad points.")
	public static SystemMessageId THE_PRESENT_RECORD_DURING_THE_CURRENT_OLYMPIAD_SESSION_IS_S1_WINS_S2_DEFEATS_YOU_HAVE_EARNED_S3_OLYMPIAD_POINTS;
	
	@ClientString(id = 1674, message = "This command can only be used by a noblesse.")
	public static SystemMessageId THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE;
	
	@ClientString(id = 1675, message = "A manor cannot be set up between 6 a.m. and 8 p.m.")
	public static SystemMessageId A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_A_M_AND_8_P_M;
	
	@ClientString(id = 1676, message = "Since a servitor or a pet does not exist, automatic use is not applicable.")
	public static SystemMessageId SINCE_A_SERVITOR_OR_A_PET_DOES_NOT_EXIST_AUTOMATIC_USE_IS_NOT_APPLICABLE;
	
	@ClientString(id = 1677, message = "Since there are clan members who are engaged in combat, the war declaration cannot be cancelled.")
	public static SystemMessageId SINCE_THERE_ARE_CLAN_MEMBERS_WHO_ARE_ENGAGED_IN_COMBAT_THE_WAR_DECLARATION_CANNOT_BE_CANCELLED;
	
	@ClientString(id = 1678, message = "You have not declared a clan war to $s1 clan.")
	public static SystemMessageId YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_TO_S1_CLAN;
	
	@ClientString(id = 1679, message = "Only the creator of a channel can issue a global command.")
	public static SystemMessageId ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_ISSUE_A_GLOBAL_COMMAND;
	
	@ClientString(id = 1680, message = "$s1 has declined the channel invitation.")
	public static SystemMessageId S1_HAS_DECLINED_THE_CHANNEL_INVITATION;
	
	@ClientString(id = 1681, message = "Since $s1 did not respond, your channel invitation has failed.")
	public static SystemMessageId SINCE_S1_DID_NOT_RESPOND_YOUR_CHANNEL_INVITATION_HAS_FAILED;
	
	@ClientString(id = 1682, message = "Only the creator of a channel can use the channel dismiss command.")
	public static SystemMessageId ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_USE_THE_CHANNEL_DISMISS_COMMAND;
	
	@ClientString(id = 1683, message = "Only a party leader can choose the option to leave a channel.")
	public static SystemMessageId ONLY_A_PARTY_LEADER_CAN_CHOOSE_THE_OPTION_TO_LEAVE_A_CHANNEL;
	
	@ClientString(id = 1684, message = "While a clan is being dissolved, it is impossible to declare a clan war against it.")
	public static SystemMessageId WHILE_A_CLAN_IS_BEING_DISSOLVED_IT_IS_IMPOSSIBLE_TO_DECLARE_A_CLAN_WAR_AGAINST_IT;
	
	@ClientString(id = 1685, message = "If your PK count is 1 or more, you are not allowed to wear this item.")
	public static SystemMessageId IF_YOUR_PK_COUNT_IS_1_OR_MORE_YOU_ARE_NOT_ALLOWED_TO_WEAR_THIS_ITEM;
	
	@ClientString(id = 1686, message = "The castle wall has sustained damage.")
	public static SystemMessageId THE_CASTLE_WALL_HAS_SUSTAINED_DAMAGE;
	
	@ClientString(id = 1687, message = "You cannot enter this area while riding on a wyvern. If you continue to remain in this area while mounted on a wyvern, your riding status will be cancelled by force.")
	public static SystemMessageId YOU_CANNOT_ENTER_THIS_AREA_WHILE_RIDING_ON_A_WYVERN_IF_YOU_CONTINUE_TO_REMAIN_IN_THIS_AREA_WHILE_MOUNTED_ON_A_WYVERN_YOUR_RIDING_STATUS_WILL_BE_CANCELLED_BY_FORCE;
	
	@ClientString(id = 1688, message = "You cannot practice enchanting while operating a private store or private manufacturing workshop.")
	public static SystemMessageId YOU_CANNOT_PRACTICE_ENCHANTING_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURING_WORKSHOP;
	
	@ClientString(id = 1689, message = "You are already on the waiting list to participate in the game for your class.")
	public static SystemMessageId YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS;
	
	@ClientString(id = 1690, message = "You are already on the waiting list for all classes waiting to participate in the game.")
	public static SystemMessageId YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME;
	
	@ClientString(id = 1691, message = "Since 80 percent or more of your inventory slots are full, you cannot participate in the Olympiad.")
	public static SystemMessageId SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD;
	
	@ClientString(id = 1692, message = "Since you have changed your class into a sub-job, you cannot participate in the Olympiad.")
	public static SystemMessageId SINCE_YOU_HAVE_CHANGED_YOUR_CLASS_INTO_A_SUB_JOB_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD;
	
	@ClientString(id = 1693, message = "While you are on the waiting list, you are not allowed to watch the game.")
	public static SystemMessageId WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME;
	
	@ClientString(id = 1694, message = "Only a clan leader that is a Noblesse can view the Siege War Status window during a siege war.")
	public static SystemMessageId ONLY_A_CLAN_LEADER_THAT_IS_A_NOBLESSE_CAN_VIEW_THE_SIEGE_WAR_STATUS_WINDOW_DURING_A_SIEGE_WAR;
	
	@ClientString(id = 1695, message = "It can be used only while a siege war is taking place.")
	public static SystemMessageId IT_CAN_BE_USED_ONLY_WHILE_A_SIEGE_WAR_IS_TAKING_PLACE;
	
	@ClientString(id = 1696, message = "If the accumulated online access time is $s1 or more, a penalty will be imposed. Please terminate your session and take a break.")
	public static SystemMessageId IF_THE_ACCUMULATED_ONLINE_ACCESS_TIME_IS_S1_OR_MORE_A_PENALTY_WILL_BE_IMPOSED_PLEASE_TERMINATE_YOUR_SESSION_AND_TAKE_A_BREAK;
	
	@ClientString(id = 1697, message = "Since your cumulative access time has exceeded $s1, your Exp and item drop rate were reduced by half. Please terminate your session and take a break.")
	public static SystemMessageId SINCE_YOUR_CUMULATIVE_ACCESS_TIME_HAS_EXCEEDED_S1_YOUR_EXP_AND_ITEM_DROP_RATE_WERE_REDUCED_BY_HALF_PLEASE_TERMINATE_YOUR_SESSION_AND_TAKE_A_BREAK;
	
	@ClientString(id = 1698, message = "Since your cumulative access time has exceeded $s1, you no longer have Exp or item drop privilege. Please terminate your session and take a break.")
	public static SystemMessageId SINCE_YOUR_CUMULATIVE_ACCESS_TIME_HAS_EXCEEDED_S1_YOU_NO_LONGER_HAVE_EXP_OR_ITEM_DROP_PRIVILEGE_PLEASE_TERMINATE_YOUR_SESSION_AND_TAKE_A_BREAK;
	
	@ClientString(id = 1699, message = "You cannot dismiss a party member by force.")
	public static SystemMessageId YOU_CANNOT_DISMISS_A_PARTY_MEMBER_BY_FORCE;
	
	@ClientString(id = 1700, message = "You don't have enough spiritshots needed for a pet/servitor.")
	public static SystemMessageId YOU_DON_T_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PET_SERVITOR;
	
	@ClientString(id = 1701, message = "You don't have enough soulshots needed for a pet/servitor.")
	public static SystemMessageId YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR;
	
	@ClientString(id = 1702, message = "The user who conducted a search a moment ago has been confirmed to be a BOT user.")
	public static SystemMessageId THE_USER_WHO_CONDUCTED_A_SEARCH_A_MOMENT_AGO_HAS_BEEN_CONFIRMED_TO_BE_A_BOT_USER;
	
	@ClientString(id = 1703, message = "The user who conducted a search a moment ago has been confirmed to be a non-BOT user.")
	public static SystemMessageId THE_USER_WHO_CONDUCTED_A_SEARCH_A_MOMENT_AGO_HAS_BEEN_CONFIRMED_TO_BE_A_NON_BOT_USER;
	
	@ClientString(id = 1704, message = "Please close the setup window for a private manufacturing store or the setup window for a private store and try again.")
	public static SystemMessageId PLEASE_CLOSE_THE_SETUP_WINDOW_FOR_A_PRIVATE_MANUFACTURING_STORE_OR_THE_SETUP_WINDOW_FOR_A_PRIVATE_STORE_AND_TRY_AGAIN;
	
	@ClientString(id = 1705, message = "PC Bang Points acquisition period. Points acquisition period left $s1 hour.")
	public static SystemMessageId PC_BANG_POINTS_ACQUISITION_PERIOD_POINTS_ACQUISITION_PERIOD_LEFT_S1_HOUR;
	
	@ClientString(id = 1706, message = "PC Bang Points use period. Points use period left $s1 hour.")
	public static SystemMessageId PC_BANG_POINTS_USE_PERIOD_POINTS_USE_PERIOD_LEFT_S1_HOUR;
	
	@ClientString(id = 1707, message = "You acquired $s1 PC Bang Point.")
	public static SystemMessageId YOU_ACQUIRED_S1_PC_BANG_POINT;
	
	@ClientString(id = 1708, message = "Double points! You acquired $s1 PC Bang Point.")
	public static SystemMessageId DOUBLE_POINTS_YOU_ACQUIRED_S1_PC_BANG_POINT;
	
	@ClientString(id = 1709, message = "You are using $s1 point.")
	public static SystemMessageId YOU_ARE_USING_S1_POINT;
	
	@ClientString(id = 1710, message = "You are short of accumulated points.")
	public static SystemMessageId YOU_ARE_SHORT_OF_ACCUMULATED_POINTS;
	
	@ClientString(id = 1711, message = "PC Bang Points use period has expired.")
	public static SystemMessageId PC_BANG_POINTS_USE_PERIOD_HAS_EXPIRED;
	
	@ClientString(id = 1712, message = "PC Bang Points accumulation period has expired.")
	public static SystemMessageId PC_BANG_POINTS_ACCUMULATION_PERIOD_HAS_EXPIRED;
	
	static
	{
		buildFastLookupTable();
	}
	
	private static void buildFastLookupTable()
	{
		for (Field field : SystemMessageId.class.getDeclaredFields())
		{
			final int mod = field.getModifiers();
			if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && field.getType().equals(SystemMessageId.class) && field.isAnnotationPresent(ClientString.class))
			{
				try
				{
					final ClientString annotation = field.getAnnotationsByType(ClientString.class)[0];
					final SystemMessageId smId = new SystemMessageId(annotation.id());
					smId.setName(annotation.message());
					smId.setParamCount(parseMessageParameters(field.getName()));
					field.set(null, smId);
					VALUES.put(smId.getId(), smId);
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING, "SystemMessageId: Failed field access for '" + field.getName() + "'", e);
				}
			}
		}
	}
	
	private static int parseMessageParameters(String name)
	{
		int paramCount = 0;
		char c1;
		char c2;
		for (int i = 0; i < (name.length() - 1); i++)
		{
			c1 = name.charAt(i);
			if ((c1 == 'C') || (c1 == 'S'))
			{
				c2 = name.charAt(i + 1);
				if (Character.isDigit(c2))
				{
					paramCount = Math.max(paramCount, Character.getNumericValue(c2));
					i++;
				}
			}
		}
		
		return paramCount;
	}
	
	public static SystemMessageId getSystemMessageId(int id)
	{
		final SystemMessageId smi = getSystemMessageIdInternal(id);
		return smi == null ? new SystemMessageId(id) : smi;
	}
	
	private static SystemMessageId getSystemMessageIdInternal(int id)
	{
		return VALUES.get(id);
	}
	
	public static SystemMessageId getSystemMessageId(String name)
	{
		try
		{
			return (SystemMessageId) SystemMessageId.class.getField(name).get(null);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public static void loadLocalisations()
	{
		for (SystemMessageId smId : VALUES.values())
		{
			if (smId != null)
			{
				smId.removeAllLocalisations();
			}
		}
		
		if (!MultilingualSupportConfig.MULTILANG_ENABLE)
		{
			LOGGER.log(Level.INFO, "SystemMessageId: MultiLanguage disabled.");
			return;
		}
		
		final List<String> languages = MultilingualSupportConfig.MULTILANG_ALLOWED;
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		File file;
		Node node;
		Document document;
		NamedNodeMap nnmb;
		SystemMessageId smId;
		String text;
		for (String lang : languages)
		{
			file = new File("data/lang/" + lang + "/SystemMessageLocalisation.xml");
			if (!file.isFile())
			{
				continue;
			}
			
			try
			{
				document = factory.newDocumentBuilder().parse(file);
				for (Node na = document.getFirstChild(); na != null; na = na.getNextSibling())
				{
					if ("list".equals(na.getNodeName()))
					{
						for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
						{
							if ("localisation".equals(nb.getNodeName()))
							{
								nnmb = nb.getAttributes();
								node = nnmb.getNamedItem("id");
								if (node != null)
								{
									smId = getSystemMessageId(Integer.parseInt(node.getNodeValue()));
									if (smId == null)
									{
										LOGGER.log(Level.WARNING, "SystemMessageId: Unknown SMID '" + node.getNodeValue() + "', lang '" + lang + "'.");
										continue;
									}
									
									node = nnmb.getNamedItem("translation");
									if (node == null)
									{
										LOGGER.log(Level.WARNING, "SystemMessageId: No text defined for SMID '" + smId + "', lang '" + lang + "'.");
										continue;
									}
									
									text = node.getNodeValue();
									if (text.isEmpty() || (text.length() > 255))
									{
										LOGGER.log(Level.WARNING, "SystemMessageId: Invalid text defined for SMID '" + smId + "' (to long or empty), lang '" + lang + "'.");
										continue;
									}
									
									smId.attachLocalizedText(lang, text);
								}
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "SystemMessageId: Failed loading '" + file + "'", e);
			}
			
			LOGGER.log(Level.INFO, "SystemMessageId: Loaded localisations for [" + lang + "].");
		}
	}
	
	private final int _id;
	private String _name;
	private byte _params;
	private SMLocalisation[] _localisations;
	private SystemMessage _staticSystemMessage;
	
	private SystemMessageId(int id)
	{
		_id = id;
		_localisations = EMPTY_SML_ARRAY;
	}
	
	public int getId()
	{
		return _id;
	}
	
	private void setName(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getParamCount()
	{
		return _params;
	}
	
	public void setParamCount(int params)
	{
		if (params < 0)
		{
			throw new IllegalArgumentException("Invalid negative param count: " + params);
		}
		
		if (params > 10)
		{
			throw new IllegalArgumentException("Maximum param count exceeded: " + params);
		}
		
		if (params != 0)
		{
			_staticSystemMessage = null;
		}
		
		_params = (byte) params;
	}
	
	public SMLocalisation getLocalisation(String lang)
	{
		if (_localisations == null)
		{
			return null;
		}
		
		SMLocalisation sml;
		for (int i = _localisations.length; i-- > 0;)
		{
			sml = _localisations[i];
			if (sml.getLanguage().hashCode() == lang.hashCode())
			{
				return sml;
			}
		}
		
		return null;
	}
	
	public void attachLocalizedText(String lang, String text)
	{
		final int length = _localisations.length;
		final SMLocalisation[] localisations = Arrays.copyOf(_localisations, length + 1);
		localisations[length] = new SMLocalisation(lang, text);
		_localisations = localisations;
	}
	
	public void removeAllLocalisations()
	{
		_localisations = EMPTY_SML_ARRAY;
	}
	
	public SystemMessage getStaticSystemMessage()
	{
		return _staticSystemMessage;
	}
	
	public void setStaticSystemMessage(SystemMessage sm)
	{
		_staticSystemMessage = sm;
	}
	
	@Override
	public String toString()
	{
		return "SM[" + getId() + ": " + getName() + "]";
	}
	
	public static class SMLocalisation
	{
		private final String _lang;
		private final Builder _builder;
		
		public SMLocalisation(String lang, String text)
		{
			_lang = lang;
			_builder = Builder.newBuilder(text);
		}
		
		public String getLanguage()
		{
			return _lang;
		}
		
		public String getLocalisation(Object... params)
		{
			return _builder.toString(params);
		}
	}
}
