/*
 *  Copyright (c) 2014 Kevin Hunter
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License. 
 */

package com.silverbaytech.blog.imageIoMetadata;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;

public class DumpMetadataFormat
{
	private static Set<String> ALREADY_DUMPED;
	
	private static void indent(int level)
	{
		for (int i = 0; i < level; i++)
		{
			System.out.print("    ");
		}
	}
	
	private static void dumpEnumeration(IIOMetadataFormat format, String elementName, String attributeName)
	{
		String[] values = format.getAttributeEnumerations(elementName, attributeName);
		System.out.print(":");
		System.out.print(values[0]);
		for (int i = 1; i < values.length; i++)
		{
			System.out.print("|");
			System.out.print(values[i]);
		}
	}
	
	private static void dumpRange(IIOMetadataFormat format, String elementName, String attributeName, int valueType)
	{
		String minValue = format.getAttributeMinValue(elementName, attributeName);
		String maxValue = format.getAttributeMaxValue(elementName, attributeName);
		
		System.out.print(":");
		System.out.print(minValue);
		
		if ((valueType & IIOMetadataFormat.VALUE_RANGE_MIN_INCLUSIVE_MASK) != 0)
		{
			System.out.print("<=");
		}
		else
		{
			System.out.print("<");
		}
		
		System.out.print("x");
		
		if ((valueType & IIOMetadataFormat.VALUE_RANGE_MAX_INCLUSIVE_MASK) != 0)
		{
			System.out.print("<=");
		}
		else
		{
			System.out.print("<");
		}
		System.out.print(maxValue);
	}
	
	private static void dumpList(IIOMetadataFormat format, String elementName, String attributeName)
	{
		int minLength = format.getAttributeListMinLength(elementName, attributeName);
		int maxLength = format.getAttributeListMaxLength(elementName, attributeName);
		System.out.print("[");
		System.out.print(minLength);
		System.out.print(",");
		System.out.print(maxLength);
		System.out.print("]");
	}
	
	private static void dumpAttributes(IIOMetadataFormat format, String elementName)
	{
		String[] attributeNames = format.getAttributeNames(elementName);
		if (attributeNames != null && attributeNames.length > 0)
		{
			for (String attributeName : attributeNames)
			{
				System.out.print(" ");
				System.out.print(attributeName);
				System.out.print("='");
				
				int dataType = format.getAttributeDataType(elementName, attributeName);
				switch(dataType)
				{
				case IIOMetadataFormat.DATATYPE_BOOLEAN:
					System.out.print("(BOOLEAN)");
					break;
				case IIOMetadataFormat.DATATYPE_DOUBLE:
					System.out.print("(DOUBLE)");
					break;
				case IIOMetadataFormat.DATATYPE_FLOAT:
					System.out.print("(FLOAT)");
					break;
				case IIOMetadataFormat.DATATYPE_INTEGER:
					System.out.print("(INTEGER)");
					break;
				case IIOMetadataFormat.DATATYPE_STRING:
					System.out.print("(STRING)");
					break;
				}
				
				int valueType = format.getAttributeValueType(elementName, attributeName);
				switch(valueType)
				{
				case IIOMetadataFormat.VALUE_ARBITRARY:
				case IIOMetadataFormat.VALUE_NONE:
					break;
				case IIOMetadataFormat.VALUE_ENUMERATION:
					dumpEnumeration(format, elementName, attributeName);
					break;
				case IIOMetadataFormat.VALUE_LIST:
					dumpList(format, elementName, attributeName);
					break;
				case IIOMetadataFormat.VALUE_RANGE:
				case IIOMetadataFormat.VALUE_RANGE_MAX_INCLUSIVE:
				case IIOMetadataFormat.VALUE_RANGE_MIN_INCLUSIVE:
				case IIOMetadataFormat.VALUE_RANGE_MIN_MAX_INCLUSIVE:
					dumpRange(format, elementName, attributeName, valueType);
					break;
				}
				
				String defaultValue = format.getAttributeDefaultValue(elementName, attributeName);
				if (defaultValue != null)
				{
					System.out.print("=");
					System.out.print(defaultValue);
				}
				
				System.out.print("'");
				if (format.isAttributeRequired(elementName, attributeName))
				{
					System.out.print("*");
				}
			}
		}
	}
	
	private static void dumpElement(IIOMetadataFormat format, String elementName, int indent)
	{
		indent(indent);
		System.out.print("<");
		System.out.print(elementName);
		
		/*
		 * Handle possible recursion in element children.  (TIFF does this)
		 */
		if (ALREADY_DUMPED.contains(elementName))
		{
			System.out.println("> (see above)");
			return;
		}
		
		ALREADY_DUMPED.add(elementName);
		
		dumpAttributes(format, elementName);
		
		String[] children = format.getChildNames(elementName);
		if (children == null || children.length == 0)
		{
			System.out.println("/>");
			return;
		}
		
		System.out.print("> ");
		
		int childPolicy = format.getChildPolicy(elementName);
		switch(childPolicy)
		{
		case IIOMetadataFormat.CHILD_POLICY_ALL:
			System.out.println("(single instance of all children required)");
			break;
		case IIOMetadataFormat.CHILD_POLICY_CHOICE:
			System.out.println("(0 or 1 instance of legal child elements)");
			break;
		case IIOMetadataFormat.CHILD_POLICY_EMPTY:
			System.out.println("");
			break;
		case IIOMetadataFormat.CHILD_POLICY_REPEAT:
			System.out.println("(zero or more instances of child element)");
			break;
		case IIOMetadataFormat.CHILD_POLICY_SEQUENCE:
			System.out.println("(sequence of instances of any of its legal child elements)");
			break;
		case IIOMetadataFormat.CHILD_POLICY_SOME:
			System.out.println("(zero or one instance of each of its legal child elements, in order)");
			break;
		}
		
		for (String child : children)
		{
			dumpElement(format, child, indent + 1);
		}
		
		indent(indent);
		System.out.print("</");
		System.out.print(elementName);
		System.out.println(">");
		
		ALREADY_DUMPED.remove(elementName);
	}
	
	private static void dumpFormat(IIOMetadataFormat format)
	{
		ALREADY_DUMPED = new HashSet<String>();
		String rootNodeName = format.getRootName();
		dumpElement(format, rootNodeName, 3);
	}
	
	private static void dumpProvider(ImageReaderSpi provider)
	{
		indent(1);
		System.out.println(provider.getPluginClassName());
		
		if (provider.isStandardImageMetadataFormatSupported())
		{
			indent(2);
			System.out.print("Image format: ");
			System.out.println(IIOMetadataFormatImpl.standardMetadataFormatName);
			IIOMetadataFormat format = provider.getImageMetadataFormat(IIOMetadataFormatImpl.standardMetadataFormatName);
			dumpFormat(format);
		}
		
		String formatName = provider.getNativeImageMetadataFormatName();
		if (formatName != null)
		{
			indent(2);
			System.out.print("Image format: ");
			System.out.println(formatName);
			IIOMetadataFormat format = provider.getImageMetadataFormat(formatName);
			dumpFormat(format);
		}
		
		if (provider.isStandardStreamMetadataFormatSupported())
		{
			indent(2);
			System.out.print("Stream format: ");
			System.out.println(IIOMetadataFormatImpl.standardMetadataFormatName);
			IIOMetadataFormat format = provider.getStreamMetadataFormat(IIOMetadataFormatImpl.standardMetadataFormatName);
			dumpFormat(format);
		}
		
		formatName = provider.getNativeStreamMetadataFormatName();
		if (formatName != null)
		{
			indent(2);
			System.out.print("Stream format: ");
			System.out.println(formatName);
			IIOMetadataFormat format = provider.getStreamMetadataFormat(formatName);
			dumpFormat(format);
		}
	}
	
	private static boolean supportsFormat(ImageReaderSpi provider, String format)
	{
		String[] formats = provider.getFormatNames();
		for (String supportedFormat : formats)
		{
			if (supportedFormat.equalsIgnoreCase(format))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static void dumpFormat(String format)
	{
		System.out.print("Format: ");
		System.out.println(format);
		
		IIORegistry registry = IIORegistry.getDefaultInstance();
		Iterator<ImageReaderSpi> providers = registry.getServiceProviders(ImageReaderSpi.class, true);
		while(providers.hasNext())
		{
			ImageReaderSpi provider = providers.next();
			if (supportsFormat(provider, format))
			{
				dumpProvider(provider);
			}
		}
	}
	
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("Usage: DumpMetadataFormat graphicsFormat [...graphicsFormat]");
			return;
		}
		
		for (int i = 0; i < args.length; i++)
		{
			dumpFormat(args[i]);
		}
		System.out.println("Done");
	}
}

