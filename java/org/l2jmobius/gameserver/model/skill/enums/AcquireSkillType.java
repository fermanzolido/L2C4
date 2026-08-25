/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.model.skill.enums;

/**
 * Learning skill types.
 * @author Zoey76
 */
public enum AcquireSkillType
{
	CLASS,
	FISHING,
	PLEDGE;
	
	/**
	 * @param id the type id, which reaches this straight off the wire in
	 *            RequestAcquireSkill and RequestAcquireSkillInfo
	 * @return the matching type, or {@link #CLASS} when the id is out of range
	 */
	public static AcquireSkillType getAcquireSkillType(int id)
	{
		// An unchecked lookup would throw out of readImpl() instead of reaching the two
		// callers, which already reject a type they do not expect. RequestMakeMacro and
		// RequestShortcutReg bound their own values()[] lookups the same way.
		final AcquireSkillType[] values = values();
		return ((id < 0) || (id >= values.length)) ? CLASS : values[id];
	}
}
