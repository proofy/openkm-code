/**
 *  OpenKM, Open Document Management System (http://www.openkm.com)
 *  Copyright (c) 2006-2015  Paco Avila & Josep Llort
 *
 *  No bytes were intentionally harmed during the development of this application.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.openkm.jcr;

import java.io.File;
import java.io.IOException;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTest extends TestCase {
	private static Logger log = LoggerFactory.getLogger(SimpleTest.class);

	public SimpleTest(String name) {
		super(name);
	}

	public static void main(String[] args) throws Exception {
		SimpleTest test = new SimpleTest("main");
		test.setUp();
		test.testBasic();
		test.testSimple();
		test.tearDown();
	}

	@Override
	protected void setUp() {
		log.info("setUp()");
		log.info("Delete repository: {}", TestConfig.REPOSITORY_HOME);
		FileUtils.deleteQuietly(new File(TestConfig.REPOSITORY_HOME));
	}

	@Override
	protected void tearDown() {
		log.info("tearDown()");
		log.info("Delete repository: {}", TestConfig.REPOSITORY_HOME);
		FileUtils.deleteQuietly(new File(TestConfig.REPOSITORY_HOME));
	}

	public void testBasic() throws IOException, LoginException, RepositoryException {
		log.info("testBasic()");
		Repository repository = new TransientRepository(TestConfig.REPOSITORY_CONFIG, TestConfig.REPOSITORY_HOME);
		Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
		Node rootNode = session.getRootNode();
		Node newNode = rootNode.addNode("new node");
		log.info("Restricted node: " + newNode.getPath());
		assertEquals(newNode.getPath(), "/new node");
		rootNode.save();
		session.logout();
	}
	
	public void testSimple() throws IOException, LoginException, RepositoryException {
		log.info("testSimple()");
		RepositoryConfig config = RepositoryConfig.create(TestConfig.REPOSITORY_CONFIG, TestConfig.REPOSITORY_HOME);
		Repository repository = RepositoryImpl.create(config);
		Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray())); 
		Node rootNode = session.getRootNode();
		Node newNode = rootNode.addNode("new node");
		log.info("Restricted node: " + newNode.getPath());
		assertEquals(newNode.getPath(), "/new node");
		rootNode.save();
		session.logout();
		((RepositoryImpl)repository).shutdown();
	}	
}
