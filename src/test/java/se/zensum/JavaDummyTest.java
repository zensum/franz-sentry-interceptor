package se.zensum;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class JavaDummyTest
{
	@Test
	public void junit5testCase()
	{
		assertEquals("Hello, world!", "Hello, world!");
	}

	@Disabled
	@Test
	public void junit5failingTestCase()
	{
		// This is placed here to make sure that we are actually running the test cases and
		// not "passing" because not test cases are run.
		fail("I will fail: comment me out or use @Disabled to pass test cases");
	}
}
