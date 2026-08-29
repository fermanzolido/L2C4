package org.l2jmobius.gameserver.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.conditions.ConditionLogicNot;

/**
 * {@code parseCondition} answers null by three routes -- a node this parser does not know, an
 * empty {@code <not>}, and a {@code <player>} with no attribute it recognises. The two sibling
 * combinators know it and drop such a child: {@code ConditionLogicAnd.add} and
 * {@code ConditionLogicOr.add} both open by returning on null. {@code parseLogicNot} wrapped
 * it instead, so the null was kept in the field and dereferenced on every test of that skill,
 * for as long as the server ran.
 * @author Claude
 */
public class DocumentBaseNotConditionTest
{
	@Test
	public void anUnknownChildIsRefusedRatherThanWrapped() throws Exception
	{
		final Condition condition = parseNot("<not><thisIsNotAConditionName /></not>");
		assertNull("A <not> around something the parser cannot read must not become a condition", condition);
	}

	@Test
	public void aPlayerChildWithNoReadableAttributeIsRefused() throws Exception
	{
		final Condition condition = parseNot("<not><player nonsenseAttribute=\"1\" /></not>");
		assertNull("A <player> the parser reads nothing out of is the same null", condition);
	}

	@Test
	public void anEmptyNotIsRefused() throws Exception
	{
		assertNull("An empty <not> was already refused and still is", parseNot("<not></not>"));
	}

	@Test
	public void aReadableChildStillBecomesANegatedCondition() throws Exception
	{
		final Condition condition = parseNot("<not><player level=\"10\" /></not>");
		assertNotNull("The normal path must be untouched", condition);
		assertTrue("A <not> around a readable child is still a negation", condition instanceof ConditionLogicNot);
	}

	private static Condition parseNot(String xml) throws Exception
	{
		final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
			.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		final Node notNode = document.getDocumentElement();
		return new TestDocument().parseNot(notNode);
	}

	/**
	 * {@link DocumentBase}'s constructor only stores the file, so nothing here reaches disk.
	 */
	private static class TestDocument extends DocumentBase
	{
		TestDocument()
		{
			super(new File("no-such-file.xml"));
		}

		Condition parseNot(Node node)
		{
			return parseLogicNot(node, null);
		}

		@Override
		protected void parseDocument(Document document)
		{
		}

		@Override
		protected StatSet getStatSet()
		{
			return new StatSet();
		}

		@Override
		protected String getTableValue(String name)
		{
			return null;
		}

		@Override
		protected String getTableValue(String name, int idx)
		{
			return null;
		}
	}
}
