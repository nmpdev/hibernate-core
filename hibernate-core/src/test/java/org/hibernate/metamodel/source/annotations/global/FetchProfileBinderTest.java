package org.hibernate.metamodel.source.annotations.global;

import java.util.Collections;
import java.util.Iterator;

import org.jboss.jandex.Index;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.MappingException;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.FetchProfiles;
import org.hibernate.metamodel.source.MetadataSources;
import org.hibernate.metamodel.source.annotations.util.JandexHelper;
import org.hibernate.metamodel.source.internal.MetadataImpl;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.internal.BasicServiceRegistryImpl;
import org.hibernate.testing.junit4.BaseUnitTestCase;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class FetchProfileBinderTest extends BaseUnitTestCase {

	private BasicServiceRegistryImpl serviceRegistry;
	private ClassLoaderService service;
	private MetadataImpl meta;

	@Before
	public void setUp() {
		serviceRegistry = new BasicServiceRegistryImpl( Collections.emptyMap() );
		service = serviceRegistry.getService( ClassLoaderService.class );
		meta = new MetadataImpl( new MetadataSources( serviceRegistry ) );
	}

	@After
	public void tearDown() {
		serviceRegistry.destroy();
	}

	@Test
	public void testSingleFetchProfile() {
		@FetchProfile(name = "foo", fetchOverrides = {
				@FetchProfile.FetchOverride(entity = Foo.class, association = "bar", mode = FetchMode.JOIN)
		})
		class Foo {
		}
		Index index = JandexHelper.indexForClass( service, Foo.class );

		FetchProfileBinder.bindFetchProfiles( meta, index );

		Iterator<org.hibernate.mapping.FetchProfile> mappedFetchProfiles = meta.getFetchProfiles().iterator();
		assertTrue( mappedFetchProfiles.hasNext() );
		org.hibernate.mapping.FetchProfile profile = mappedFetchProfiles.next();
		assertEquals( "Wrong fetch profile name", "foo", profile.getName() );
		org.hibernate.mapping.FetchProfile.Fetch fetch = profile.getFetches().iterator().next();
		assertEquals( "Wrong association name", "bar", fetch.getAssociation() );
		assertEquals( "Wrong association type", Foo.class.getName(), fetch.getEntity() );
	}

	@Test
	public void testFetchProfiles() {
		Index index = JandexHelper.indexForClass( service, FooBar.class );
		FetchProfileBinder.bindFetchProfiles( meta, index );

		Iterator<org.hibernate.mapping.FetchProfile> mappedFetchProfiles = meta.getFetchProfiles().iterator();
		assertTrue( mappedFetchProfiles.hasNext() );
		org.hibernate.mapping.FetchProfile profile = mappedFetchProfiles.next();
		assertProfiles( profile );

		assertTrue( mappedFetchProfiles.hasNext() );
		profile = mappedFetchProfiles.next();
		assertProfiles( profile );
	}

	private void assertProfiles(org.hibernate.mapping.FetchProfile profile) {
		if ( profile.getName().equals( "foobar" ) ) {
			org.hibernate.mapping.FetchProfile.Fetch fetch = profile.getFetches().iterator().next();
			assertEquals( "Wrong association name", "foobar", fetch.getAssociation() );
			assertEquals( "Wrong association type", FooBar.class.getName(), fetch.getEntity() );
		}
		else if ( profile.getName().equals( "fubar" ) ) {
			org.hibernate.mapping.FetchProfile.Fetch fetch = profile.getFetches().iterator().next();
			assertEquals( "Wrong association name", "fubar", fetch.getAssociation() );
			assertEquals( "Wrong association type", FooBar.class.getName(), fetch.getEntity() );
		}
		else {
			fail( "Wrong fetch name:" + profile.getName() );
		}
	}

	@Test(expected = MappingException.class)
	public void testNonJoinFetchThrowsException() {
		@FetchProfile(name = "foo", fetchOverrides = {
				@FetchProfile.FetchOverride(entity = Foo.class, association = "bar", mode = FetchMode.SELECT)
		})
		class Foo {
		}
		Index index = JandexHelper.indexForClass( service, Foo.class );

		FetchProfileBinder.bindFetchProfiles( meta, index );
	}

	@FetchProfiles( {
			@FetchProfile(name = "foobar", fetchOverrides = {
					@FetchProfile.FetchOverride(entity = FooBar.class, association = "foobar", mode = FetchMode.JOIN)
			}),
			@FetchProfile(name = "fubar", fetchOverrides = {
					@FetchProfile.FetchOverride(entity = FooBar.class, association = "fubar", mode = FetchMode.JOIN)
			})
	})
	class FooBar {
	}
}


