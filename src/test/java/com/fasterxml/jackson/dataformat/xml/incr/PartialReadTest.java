package com.fasterxml.jackson.dataformat.xml.incr;

import java.io.*;
import javax.xml.stream.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class PartialReadTest extends XmlTestBase
{
    private final XmlMapper MAPPER = xmlMapper(true);

    public void testSimpleRead() throws Exception
    {
        final String XML = "<?xml version='1.0'?><root>"
                +"<NameBean age=\"13\"><first>Grizabella</first><last>Glamour</last></NameBean>"
                +"<NameBean age=\"17\"><first>Growl</first><last>Tiger</last></NameBean></root>";
        XMLStreamReader sr = MAPPER.getFactory().getXMLInputFactory().createXMLStreamReader(
                new StringReader(XML));
        assertEquals(sr.next(), XMLStreamConstants.START_ELEMENT);
        assertEquals("root", sr.getLocalName());

        /* 30-May-2014, tatu: This is bit tricky... need to ensure that currently
         *    pointed to START_ELEMENT is sort of re-read.
         */
        assertEquals(sr.next(), XMLStreamConstants.START_ELEMENT);
        assertEquals("NameBean", sr.getLocalName());
        
        NameBean bean1 = MAPPER.readValue(sr, NameBean.class);
        assertNotNull(bean1);
        assertEquals(sr.getEventType(), XMLStreamConstants.END_ELEMENT);
        assertEquals("NameBean", sr.getLocalName());

        assertEquals(sr.next(), XMLStreamConstants.START_ELEMENT);
        assertEquals("NameBean", sr.getLocalName());
        NameBean bean2 = MAPPER.readValue(sr, NameBean.class);
        assertNotNull(bean2);
        assertEquals(sr.getEventType(), XMLStreamConstants.END_ELEMENT);
        assertEquals("NameBean", sr.getLocalName());

        assertEquals(sr.next(), XMLStreamConstants.END_ELEMENT);
        assertEquals("root", sr.getLocalName());
        
        sr.close();
    }

    // @since 2.17
    public void testReadUsingXMLStreamReader() throws Exception
    {
        final String DOC = "<Point><x>1</x><y>2</y></Point>";

        XMLInputFactory staxF = MAPPER.getFactory().getXMLInputFactory();

        // First read using XmlMapper convenience method
        XMLStreamReader sr = staxF.createXMLStreamReader(new StringReader(DOC));
        Point p = MAPPER.readValue(sr, Point.class);
        assertEquals(1, p.x);
        assertEquals(2, p.y);
        sr.close();

        // Then read using XmlFactory parser factory method
        sr = staxF.createXMLStreamReader(new StringReader(DOC));
        try (JsonParser jp = MAPPER.createParser(sr)) {
            p = MAPPER.readValue(jp, Point.class);
            assertEquals(1, p.x);
            assertEquals(2, p.y);
        }
    }
}
