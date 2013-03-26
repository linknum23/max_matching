package graph.tests;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ GraphLoadingTest.class , BlossomTest.class, MatchingTest.class, SimpleAlgTest.class})
public class AllTests {
	
}
