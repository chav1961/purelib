package chav1961.purelib.basic;

import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class ListRangeMatcherTest {
	@Test
	public void intPredicateTest() throws SyntaxException {
		IntPredicate 	pred = ListRangeMatcher.parseIntRange(("10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertFalse(pred.test(20));

		pred = ListRangeMatcher.parseIntRange(("+10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertFalse(pred.test(20));

		pred = ListRangeMatcher.parseIntRange(("-10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(-10));
		Assert.assertFalse(pred.test(-20));
		
		pred = ListRangeMatcher.parseIntRange(("10..20"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertTrue(pred.test(20));
		Assert.assertFalse(pred.test(30));

		pred = ListRangeMatcher.parseIntRange(("10..20,30"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertTrue(pred.test(20));
		Assert.assertTrue(pred.test(30));
		Assert.assertFalse(pred.test(40));
		
		try {
			ListRangeMatcher.parseIntRange(("unknown"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown lex)");
		} catch (SyntaxException exc) {
		}
		try {
			ListRangeMatcher.parseIntRange(("10,"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (missimg operand)");
		} catch (SyntaxException exc) {
		}
		try {
			ListRangeMatcher.parseIntRange(("10 unknown"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (unparsed tail)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void longPredicateTest() throws SyntaxException {
		LongPredicate 	pred = ListRangeMatcher.parseLongRange(("10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertFalse(pred.test(20));

		pred = ListRangeMatcher.parseLongRange(("+10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertFalse(pred.test(20));

		pred = ListRangeMatcher.parseLongRange(("-10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(-10));
		Assert.assertFalse(pred.test(-20));
		
		pred = ListRangeMatcher.parseLongRange(("10..20"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertTrue(pred.test(20));
		Assert.assertFalse(pred.test(30));

		pred = ListRangeMatcher.parseLongRange(("10..20,30"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertTrue(pred.test(20));
		Assert.assertTrue(pred.test(30));
		Assert.assertFalse(pred.test(40));
		
		try {
			ListRangeMatcher.parseLongRange(("unknown"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown lex)");
		} catch (SyntaxException exc) {
		}
		try {
			ListRangeMatcher.parseLongRange(("10, "+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (missing operand)");
		} catch (SyntaxException exc) {
		}
		try {
			ListRangeMatcher.parseLongRange(("10 unknown"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (unparsed tail)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void doublePredicateTest() throws SyntaxException {
		DoublePredicate 	pred = ListRangeMatcher.parseDoubleRange(("10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertFalse(pred.test(20));

		pred = ListRangeMatcher.parseDoubleRange(("+10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertFalse(pred.test(20));

		pred = ListRangeMatcher.parseDoubleRange(("-10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(-10));
		Assert.assertFalse(pred.test(-20));
		
		pred = ListRangeMatcher.parseDoubleRange(("10 ..20"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertTrue(pred.test(20));
		Assert.assertFalse(pred.test(30));

		pred = ListRangeMatcher.parseDoubleRange(("10 ..20,30"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test(10));
		Assert.assertTrue(pred.test(20));
		Assert.assertTrue(pred.test(30));
		Assert.assertFalse(pred.test(40));
		
		try {
			ListRangeMatcher.parseDoubleRange(("unknown"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown lex)");
		} catch (SyntaxException exc) {
		}
		try {
			ListRangeMatcher.parseDoubleRange(("10,"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (missing operand)");
		} catch (SyntaxException exc) {
		}
		try {
			ListRangeMatcher.parseDoubleRange(("10 unknown"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (unparsed tail)");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void objectPredicateTest() throws SyntaxException {
		Predicate<String> 	pred = ListRangeMatcher.<String>parseRange(("10"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test("10"));
		Assert.assertFalse(pred.test("20"));

		pred = ListRangeMatcher.<String>parseRange(("10 ..20"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test("10"));
		Assert.assertTrue(pred.test("20"));
		Assert.assertFalse(pred.test("30"));

		pred = ListRangeMatcher.<String>parseRange(("10 ..20,30"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test("10"));
		Assert.assertTrue(pred.test("20"));
		Assert.assertTrue(pred.test("30"));
		Assert.assertFalse(pred.test("40"));

		pred = ListRangeMatcher.<String>parseRange(("'10'..\"20\",30"+ListRangeMatcher.EOF).toCharArray());
		
		Assert.assertTrue(pred.test("10"));
		Assert.assertTrue(pred.test("20"));
		Assert.assertTrue(pred.test("30"));
		Assert.assertFalse(pred.test("40"));
		
		try {
			ListRangeMatcher.parseRange(("?"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown lex)");
		} catch (SyntaxException exc) {
		}
		try {
			ListRangeMatcher.parseRange(("10,"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (missing operand)");
		} catch (SyntaxException exc) {
		}
		try {
			ListRangeMatcher.parseRange(("10 unknown"+ListRangeMatcher.EOF).toCharArray());
			Assert.fail("Mandatory exception was not detected (unparsed tail)");
		} catch (SyntaxException exc) {
		}
	}
}
