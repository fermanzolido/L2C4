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
package org.l2jmobius.gameserver.geoengine.pathfinding;

import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.l2jmobius.gameserver.config.GeoEngineConfig;

/**
 * @author Mobius
 */
public class NodeBuffer
{
	private static final int MAX_ITERATIONS = 7000;
	
	private final ReentrantLock _lock = new ReentrantLock();
	private final int _mapSize;
	private final GeoNode[][] _buffer;
	
	// A* specific data structures.
	// Use a wrapper to preserve Heap integrity when costs change.
	private final PriorityQueue<NodeRecord> _openList;
	
	private int _baseX = 0;
	private int _baseY = 0;
	
	private int _targetX = 0;
	private int _targetY = 0;
	private int _targetZ = 0;
	
	private GeoNode _current = null;
	private int _currentSearchId = 0;

	private static class NodeRecord implements Comparable<NodeRecord>
	{
		final GeoNode node;
		final double fCost;

		public NodeRecord(GeoNode node, double fCost)
		{
			this.node = node;
			this.fCost = fCost;
		}

		@Override
		public int compareTo(NodeRecord o)
		{
			return Double.compare(this.fCost, o.fCost);
		}
	}
	
	public NodeBuffer(int size)
	{
		_mapSize = size;
		_buffer = new GeoNode[_mapSize][_mapSize];
		_openList = new PriorityQueue<>();
	}
	
	public final boolean lock()
	{
		return _lock.tryLock();
	}
	
	/**
	 * Enhanced A* pathfinding algorithm.
	 * @param x starting X coordinate
	 * @param y starting Y coordinate
	 * @param z starting Z coordinate
	 * @param tx target X coordinate
	 * @param ty target Y coordinate
	 * @param tz target Z coordinate
	 * @return the final node if path found, null otherwise
	 */
	public GeoNode findPath(int x, int y, int z, int tx, int ty, int tz)
	{
		_currentSearchId++;
		_baseX = x + ((tx - x - _mapSize) / 2); // Middle of the line (x,y) - (tx,ty).
		_baseY = y + ((ty - y - _mapSize) / 2); // Will be in the center of the buffer.
		_targetX = tx;
		_targetY = ty;
		_targetZ = tz;
		
		_openList.clear();

		_current = getNode(x, y, z);
		if (_current == null)
		{
			return null;
		}
		
		// Initialize start node.
		_current.setGCost(0);
		_current.setHCost(getCost(x, y, z));
		_current.calculateFCost();
		_current.setOpen(true);
		
		_openList.add(new NodeRecord(_current, _current.getFCost()));
		
		for (int count = 0; count < MAX_ITERATIONS; count++)
		{
			if (_openList.isEmpty())
			{
				return null; // No path found.
			}
			
			final NodeRecord record = _openList.poll();
			_current = record.node;

			// Lazy Deletion: Skip if already closed (visited via a better path previously).
			if (_current.isClosed())
			{
				continue;
			}

			// Optional: Check if this record is obsolete (we found a better path later).
			// If record.fCost > _current.getFCost(), it means we improved the cost after inserting this record.
			// The newer, better record is still in the queue (or already processed).
			// Since we process min cost first, we would have processed the better record already if valid.
			// However, if we simply skip closed nodes, that covers most cases.
			// There is an edge case: we popped the WORSE path first? Impossible with Min-Heap.
			// We always pop the BEST path available.
			// If there are duplicate records for the same node, the one with lower fCost comes out first.
			// We mark it closed.
			// The one with higher fCost comes out later. It is closed. We skip.
			
			// Check if we reached the target.
			if ((_current.getLocation().getNodeX() == _targetX) && (_current.getLocation().getNodeY() == _targetY) && (Math.abs(_current.getLocation().getZ() - _targetZ) < 64))
			{
				return _current; // Found target.
			}
			
			_current.setClosed(true);
			
			// Get and process neighbors.
			getNeighbors();
		}
		
		return null; // Path not found within iteration limit.
	}
	
	public void free()
	{
		_current = null;
		_openList.clear();
		_lock.unlock();
	}
	
	/**
	 * Enhanced neighbor discovery using A* principles.
	 */
	private void getNeighbors()
	{
		if (_current.getLocation().canGoNone())
		{
			return;
		}
		
		final int x = _current.getLocation().getNodeX();
		final int y = _current.getLocation().getNodeY();
		final int z = _current.getLocation().getZ();
		
		GeoNode nodeE = null;
		GeoNode nodeS = null;
		GeoNode nodeW = null;
		GeoNode nodeN = null;
		
		// East.
		if (_current.getLocation().canGoEast())
		{
			nodeE = addNode(x + 1, y, z, false);
		}
		
		// South.
		if (_current.getLocation().canGoSouth())
		{
			nodeS = addNode(x, y + 1, z, false);
		}
		
		// West.
		if (_current.getLocation().canGoWest())
		{
			nodeW = addNode(x - 1, y, z, false);
		}
		
		// North.
		if (_current.getLocation().canGoNorth())
		{
			nodeN = addNode(x, y - 1, z, false);
		}
		
		// Diagonal movements (if enabled).
		if (!GeoEngineConfig.ADVANCED_DIAGONAL_STRATEGY)
		{
			return;
		}
		
		// SouthEast
		if ((nodeE != null) && (nodeS != null) && nodeE.getLocation().canGoSouth() && nodeS.getLocation().canGoEast())
		{
			addNode(x + 1, y + 1, z, true);
		}
		
		// SouthWest
		if ((nodeS != null) && (nodeW != null) && nodeW.getLocation().canGoSouth() && nodeS.getLocation().canGoWest())
		{
			addNode(x - 1, y + 1, z, true);
		}
		
		// NorthEast
		if ((nodeN != null) && (nodeE != null) && nodeE.getLocation().canGoNorth() && nodeN.getLocation().canGoEast())
		{
			addNode(x + 1, y - 1, z, true);
		}
		
		// NorthWest
		if ((nodeN != null) && (nodeW != null) && nodeW.getLocation().canGoNorth() && nodeN.getLocation().canGoWest())
		{
			addNode(x - 1, y - 1, z, true);
		}
	}
	
	private GeoNode getNode(int x, int y, int z)
	{
		final int aX = x - _baseX;
		if ((aX < 0) || (aX >= _mapSize))
		{
			return null;
		}
		
		final int aY = y - _baseY;
		if ((aY < 0) || (aY >= _mapSize))
		{
			return null;
		}
		
		GeoNode result = _buffer[aX][aY];
		if (result == null)
		{
			result = new GeoNode(new GeoLocation(x, y, z));
			result.setSearchId(_currentSearchId);
			_buffer[aX][aY] = result;
		}
		else if (result.getSearchId() != _currentSearchId)
		{
			// Reset node for this search
			result.reset(_currentSearchId);
			if (result.getLocation() != null)
			{
				result.getLocation().set(x, y, z);
			}
			else
			{
				result.setLoc(new GeoLocation(x, y, z));
			}
			result.setInUse();
		}
		
		return result;
	}
	
	/**
	 * Enhanced node addition using A* algorithm.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param z the Z coordinate
	 * @param diagonal whether this is a diagonal move
	 * @return the added node or null if not valid
	 */
	private GeoNode addNode(int x, int y, int z, boolean diagonal)
	{
		final GeoNode newNode = getNode(x, y, z);
		if (newNode == null)
		{
			return null;
		}
		
		// Skip if already closed.
		if (newNode.isClosed())
		{
			return newNode;
		}
		
		final int geoZ = newNode.getLocation().getZ();
		final int stepZ = Math.abs(geoZ - _current.getLocation().getZ());
		
		// Calculate movement cost based on terrain and movement type.
		float weight = diagonal ? GeoEngineConfig.DIAGONAL_WEIGHT : GeoEngineConfig.LOW_WEIGHT;
		if (!newNode.getLocation().canGoAll() || (stepZ > 16))
		{
			weight = GeoEngineConfig.HIGH_WEIGHT;
		}
		else if (isHighWeight(x + 1, y, geoZ) || isHighWeight(x - 1, y, geoZ) || isHighWeight(x, y + 1, geoZ) || isHighWeight(x, y - 1, geoZ))
		{
			weight = GeoEngineConfig.MEDIUM_WEIGHT;
		}
		
		// Calculate new G cost (actual cost from start).
		final double newGCost = _current.getGCost() + weight;
		
		// Check if this node is already open.
		final boolean inOpenList = newNode.isOpen(); // O(1) check
		
		// If not in open list or we found a better path.
		if (!inOpenList || (newGCost < newNode.getGCost()))
		{
			// Set parent and costs.
			newNode.setParent(_current);
			newNode.setGCost(newGCost);
			newNode.setHCost(getCost(x, y, geoZ));
			newNode.calculateFCost();
			newNode.setOpen(true);
			
			// Add new record to PQ. Old records remain but will be ignored if popped later (since this one pops first).
			_openList.add(new NodeRecord(newNode, newNode.getFCost()));
		}
		
		return newNode;
	}
	
	private boolean isHighWeight(int x, int y, int z)
	{
		final GeoNode result = getNode(x, y, z);
		return (result == null) || !result.getLocation().canGoAll() || (Math.abs(result.getLocation().getZ() - z) > 16);
	}
	
	/**
	 * Calculate heuristic cost using 3D distance with proper scaling.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param z the Z coordinate
	 * @return the heuristic cost
	 */
	private double getCost(int x, int y, int z)
	{
		final int dX = x - _targetX;
		final int dY = y - _targetY;
		final int dZ = z - _targetZ;
		
		// Use 3D Euclidean distance for more accurate heuristic.
		return Math.sqrt((dX * dX) + (dY * dY) + ((dZ * dZ) / 256.0));
	}
}
