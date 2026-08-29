package org.l2jmobius.gameserver.model.itemcontainer;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.enums.ItemLocation;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;
import org.l2jmobius.gameserver.model.item.type.ItemType;

/**
 * Guards the invariant behind the lock that {@code addItem} takes on a stack it is merging
 * into.
 * <p>
 * {@link Item#changeCount} reads the count, decides it against the cap and writes it back.
 * {@code destroyItem} and {@code transferItem} have always held the item's monitor across
 * that; the two {@code addItem} paths did not, so two additions to one stack could lose one
 * of them. Two clan members depositing the same stackable into a shared warehouse is enough,
 * and that is two packet threads on one container.
 * <p>
 * A test cannot prove the absence of a race, so this does not try to: it drives enough
 * concurrent additions that a lost update is overwhelmingly likely to show, and asserts the
 * total is exact. Run against the unlocked version it fails; run against this one it passes.
 * @author Claude
 */
public class ItemContainerConcurrencyTest
{
	// Deliberately not adena: changeCount caps adena at Inventory.MAX_ADENA, which is read
	// from PlayerConfig, and a test runs with no configuration loaded -- so that cap would
	// be zero here and every addition would clamp the stack away. Any other stackable takes
	// the Integer.MAX_VALUE branch and exercises the same read, decide and write back.
	private static final int STACKABLE_ID = 1060;
	private static final int THREADS = 8;
	private static final int ADDS_PER_THREAD = 2000;

	@Test
	public void concurrentAdditionsToOneStackKeepTheWholeTotal() throws Exception
	{
		final TestContainer container = new TestContainer();
		final Item stack = newStackable(STACKABLE_ID, 1000);
		container.addForTest(stack);

		final int before = stack.getCount();
		final CountDownLatch ready = new CountDownLatch(THREADS);
		final CountDownLatch go = new CountDownLatch(1);
		final CountDownLatch done = new CountDownLatch(THREADS);
		final AtomicInteger failures = new AtomicInteger();

		for (int i = 0; i < THREADS; i++)
		{
			final Thread thread = new Thread(() ->
			{
				ready.countDown();
				try
				{
					go.await();
					for (int add = 0; add < ADDS_PER_THREAD; add++)
					{
						container.addItem(ItemProcessType.NONE, STACKABLE_ID, 1, null, null);
					}
				}
				catch (Exception e)
				{
					failures.incrementAndGet();
				}
				finally
				{
					done.countDown();
				}
			});
			thread.setDaemon(true);
			thread.start();
		}

		ready.await(10, TimeUnit.SECONDS);
		go.countDown();
		done.await(60, TimeUnit.SECONDS);

		assertEquals("No thread should have thrown", 0, failures.get());
		assertEquals("Every addition must be in the stack", before + (THREADS * ADDS_PER_THREAD), stack.getCount());
	}

	@Test
	public void additionsAndRemovalsCancelOutExactly() throws Exception
	{
		final TestContainer container = new TestContainer();
		final Item stack = newStackable(STACKABLE_ID, 1_000_000);
		container.addForTest(stack);

		final int before = stack.getCount();
		final CountDownLatch go = new CountDownLatch(1);
		final CountDownLatch done = new CountDownLatch(THREADS);

		// Half the threads add and half take the same amount away, so the count has to come
		// back to exactly where it started.
		for (int i = 0; i < THREADS; i++)
		{
			final boolean adding = (i % 2) == 0;
			final Thread thread = new Thread(() ->
			{
				try
				{
					go.await();
					for (int step = 0; step < ADDS_PER_THREAD; step++)
					{
						if (adding)
						{
							container.addItem(ItemProcessType.NONE, STACKABLE_ID, 1, null, null);
						}
						else
						{
							synchronized (stack)
							{
								stack.changeCount(ItemProcessType.NONE, -1, null, null);
							}
						}
					}
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				finally
				{
					done.countDown();
				}
			});
			thread.setDaemon(true);
			thread.start();
		}

		go.countDown();
		done.await(60, TimeUnit.SECONDS);

		assertEquals("Equal additions and removals must leave the count untouched", before, stack.getCount());
	}

	private static Item newStackable(int itemId, int count)
	{
		final StatSet set = new StatSet();
		set.set("item_id", itemId);
		set.set("name", "Test Stackable");
		set.set("is_stackable", true);

		final Item item = new Item(itemId + 1000, new TestTemplate(set));
		item.setCount(count);
		return item;
	}

	private static class TestTemplate extends ItemTemplate
	{
		TestTemplate(StatSet set)
		{
			super(set);
		}

		@Override
		public ItemType getItemType()
		{
			return EtcItemType.NONE;
		}

		@Override
		public int getItemMask()
		{
			return 0;
		}
	}

	private static class TestContainer extends ItemContainer
	{
		void addForTest(Item item)
		{
			_items.add(item);
		}

		@Override
		protected Creature getOwner()
		{
			return null;
		}

		@Override
		protected ItemLocation getBaseLocation()
		{
			return ItemLocation.INVENTORY;
		}
	}
}
