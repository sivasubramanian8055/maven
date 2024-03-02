/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.internal.xml;

import javax.xml.stream.XMLStreamReader;

import java.io.IOException;
import java.io.StringReader;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.maven.api.xml.XmlNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlNodeBuilderTest {

    @Test
    void testReadMultiDoc() throws Exception {
        String doc = "<?xml version='1.0'?><doc><child>foo</child></doc>";
        StringReader r = new StringReader(doc + doc) {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return super.read(cbuf, off, 1);
            }
        };
        XmlNode node1 = XmlNodeBuilder.build(r);
        XmlNode node2 = XmlNodeBuilder.build(r);
        assertEquals(node1, node2);
    }

    @Test
    void testWithNamespace() throws Exception {
        String doc = "<?xml version='1.0'?><doc xmlns='foo:bar'/>";
        StringReader r = new StringReader(doc);
        XMLStreamReader xsr = WstxInputFactory.newFactory().createXMLStreamReader(r);
        XmlNode node = XmlNodeBuilder.build(xsr);
        assertEquals("doc", node.getName());
        assertEquals(1, node.getAttributes().size());
        assertEquals("foo:bar", node.getAttribute("xmlns"));
    }

    @Test
    void testElementWithMultipleAttributes() throws Exception {
        String doc = "<?xml version='1.0'?><doc attr1='value1' attr2='value2'></doc>";
        StringReader r = new StringReader(doc);
        XmlNode node = XmlNodeBuilder.build(r);
        assertEquals("doc", node.getName());
        assertEquals("value1", node.getAttribute("attr1"));
        assertEquals("value2", node.getAttribute("attr2"));
    }

    @Test
    void testNestedElements() throws Exception {
        String doc = "<?xml version='1.0'?><root><parent><child>value</child></parent></root>";
        StringReader r = new StringReader(doc);
        XmlNode node = XmlNodeBuilder.build(r);
        assertEquals("root", node.getName());
        XmlNode parent = node.getChildren().get(0);
        assertEquals("parent", parent.getName());
        XmlNode child = parent.getChildren().get(0);
        assertEquals("child", child.getName());
        assertEquals("value", child.getValue());
    }

    @Test
    void testXmlCommentsHandling() throws Exception {
        String doc = "<?xml version='1.0'?><doc><!-- This is a comment --><child>text</child></doc>";
        StringReader r = new StringReader(doc);
        XmlNode node = XmlNodeBuilder.build(r);
        assertEquals("doc", node.getName());
        assertEquals(1, node.getChildren().size());
        assertEquals("child", node.getChildren().get(0).getName());
    }
}
