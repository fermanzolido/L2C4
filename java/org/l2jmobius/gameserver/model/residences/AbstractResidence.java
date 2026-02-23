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
package org.l2jmobius.gameserver.model.residences;

import org.l2jmobius.gameserver.model.events.ListenersContainer;
import org.l2jmobius.gameserver.model.zone.type.ResidenceZone;

/**
 * @author xban1x
 */
public abstract class AbstractResidence extends ListenersContainer
{
	private final int _residenceId;
	private String _name;
	
	private ResidenceZone _zone = null;
	
	public AbstractResidence(int residenceId)
	{
		_residenceId = residenceId;
	}
	
	protected abstract void load();
	
	protected abstract void initResidenceZone();
	
	public int getResidenceId()
	{
		return _residenceId;
	}
	
	public String getName()
	{
		return _name;
	}
	
	// Separate castle/fort table loading (unified table not implemented).
	public void setName(String name)
	{
		_name = name;
	}
	
	public ResidenceZone getResidenceZone()
	{
		return _zone;
	}
	
	protected void setResidenceZone(ResidenceZone zone)
	{
		_zone = zone;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof AbstractResidence) && (((AbstractResidence) obj).getResidenceId() == getResidenceId());
	}
	
	@Override
	public String toString()
	{
		return _name + "(" + _residenceId + ")";
	}
}
