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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.WritableBuffer;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

public class EquipUpdate extends ServerPacket {
	private final Item _item;
	private final int _change;

	public EquipUpdate(Item item, int change) {
		_item = item;
		_change = change;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer) {
		ServerPackets.EQUIP_UPDATE.writeId(this, buffer);
		buffer.writeInt(_change);
		buffer.writeInt(_item.getObjectId());

		switch (_item.getTemplate().getBodyPart()) {
			case L_EAR: {
				buffer.writeInt(0x01);
				break;
			}
			case R_EAR: {
				buffer.writeInt(0x02);
				break;
			}
			case NECK: {
				buffer.writeInt(0x03);
				break;
			}
			case R_FINGER: {
				buffer.writeInt(0x04);
				break;
			}
			case L_FINGER: {
				buffer.writeInt(0x05);
				break;
			}
			case HEAD: {
				buffer.writeInt(0x06);
				break;
			}
			case R_HAND: {
				buffer.writeInt(0x07);
				break;
			}
			case L_HAND: {
				buffer.writeInt(0x08);
				break;
			}
			case GLOVES: {
				buffer.writeInt(0x09);
				break;
			}
			case CHEST: {
				buffer.writeInt(0x0a);
				break;
			}
			case LEGS: {
				buffer.writeInt(0x0b);
				break;
			}
			case FEET: {
				buffer.writeInt(0x0c);
				break;
			}
			case BACK: {
				buffer.writeInt(0x0d);
				break;
			}
			case LR_HAND: {
				buffer.writeInt(0x0e);
				break;
			}
			case HAIR: {
				buffer.writeInt(0x0f);
				break;
			}
			default: {
				buffer.writeInt(0);
				break;
			}
		}
	}
}
